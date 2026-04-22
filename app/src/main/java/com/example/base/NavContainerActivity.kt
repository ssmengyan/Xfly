package com.hook.fakewifi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hook.fakewifi.ui.theme.AppTheme
import hook.tool.FakeWifiConfig
import hook.tool.loadConfig
import hook.tool.saveConfig

class NavContainerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    val savedConfig = loadConfig(context)

    var ssid by rememberSaveable { mutableStateOf(savedConfig.ssid) }
    var bssid by rememberSaveable { mutableStateOf(savedConfig.bssid) }
    var mac by rememberSaveable { mutableStateOf(savedConfig.mac) }

    val ssidError = validateSsid(ssid)
    val bssidError = validateMacLike(bssid, "BSSID")
    val macError = validateMacLike(mac, "MAC")
    val canSave = ssidError == null && bssidError == null && macError == null

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("网络配置", style = MaterialTheme.typography.headlineMedium)
            Text(
                "在这里统一配置 SSID、BSSID、MAC。启用模块并在 LSPosed 中勾选目标应用后，目标应用中的 WifiInfo 和 ScanResult 会读取这里的配置。",
                style = MaterialTheme.typography.bodyMedium,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("WiFi 名称 (SSID)") },
                        placeholder = { Text("例如：Office-WiFi-5G") },
                        singleLine = true,
                        isError = ssidError != null,
                        supportingText = { Text(ssidError ?: "可留空；填写后会作为 getSSID() 返回值。") },
                    )
                    OutlinedTextField(
                        value = bssid,
                        onValueChange = { bssid = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("路由器地址 (BSSID)") },
                        placeholder = { Text("例如：aa:bb:cc:dd:ee:ff") },
                        singleLine = true,
                        isError = bssidError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        supportingText = { Text(bssidError ?: "可留空；填写后必须是 6 组十六进制地址。") },
                    )
                    OutlinedTextField(
                        value = mac,
                        onValueChange = { mac = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("本机 MAC 地址") },
                        placeholder = { Text("例如：11:22:33:44:55:66") },
                        singleLine = true,
                        isError = macError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        supportingText = { Text(macError ?: "可留空；填写后会作为 getMacAddress() 返回值。") },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {
                                saveConfig(FakeWifiConfig(ssid = ssid, bssid = bssid, mac = mac), context)
                                Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = canSave,
                        ) {
                            Text("保存")
                        }

                        TextButton(
                            onClick = {
                                ssid = ""
                                bssid = ""
                                mac = ""
                                saveConfig(FakeWifiConfig(), context)
                                Toast.makeText(context, "配置已重置", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("重置")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("使用方法", style = MaterialTheme.typography.titleMedium)
                    Text("1. 安装模块后，在 LSPosed 中启用。")
                    Text("2. 勾选你想生效的目标应用。")
                    Text("3. 回到这里保存配置，然后重启目标应用。")
                    Text("4. 目标应用读取 WifiInfo 或 getScanResults() 时会返回这里的内容。")
                }
            }
        }
    }
}

private fun validateSsid(value: String): String? {
    return if (value.isNotEmpty() && value.isBlank()) {
        "SSID 不能只输入空格。"
    } else {
        null
    }
}

private fun validateMacLike(value: String, fieldName: String): String? {
    if (value.isBlank()) return null
    val normalized = value.trim()
    val macRegex = Regex("^[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}$")
    return if (macRegex.matches(normalized)) {
        null
    } else {
        "$fieldName 格式不正确，应为 aa:bb:cc:dd:ee:ff。"
    }
}
