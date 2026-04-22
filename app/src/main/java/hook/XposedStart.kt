package hook

import android.content.Context
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.SystemClock
import com.hook.fakewifi.BuildConfig
import com.hook.fakewifi.ConfigProvider
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import hook.tool.FakeWifiConfig
import java.util.Collections

class XposedStart : IXposedHookLoadPackage {
    companion object {
        private const val DEFAULT_RSSI = -42
        private const val DEFAULT_FREQUENCY = 5745
        private const val DEFAULT_CAPABILITIES = "[WPA2-PSK-CCMP][ESS]"
        private val hookedProcesses = Collections.synchronizedSet(mutableSetOf<String>())
        private val providerUri: Uri = Uri.parse("content://${BuildConfig.APPLICATION_ID}.config")
        @Volatile
        private var cachedContext: Context? = null
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val processKey = "${loadPackageParam.packageName}:${loadPackageParam.processName}"
        if (!hookedProcesses.add(processKey)) {
            return
        }

        XposedBridge.log("Fake-Wifi: installing hooks for $processKey")

        XposedHelpers.findAndHookMethod(
            "android.content.ContextWrapper",
            loadPackageParam.classLoader,
            "attachBaseContext",
            Context::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    cachedContext = param.args[0] as? Context ?: cachedContext
                }
            }
        )

        hookWifiInfo(loadPackageParam)
        hookScanResults(loadPackageParam)
    }

    private fun hookWifiInfo(loadPackageParam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "android.net.wifi.WifiInfo",
            loadPackageParam.classLoader,
            "getSSID",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.hasCustomSsid()) {
                        param?.result = config.wifiInfoSsid()
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.WifiInfo",
            loadPackageParam.classLoader,
            "getBSSID",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.hasCustomBssid()) {
                        param?.result = config.normalizedBssid()
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.WifiInfo",
            loadPackageParam.classLoader,
            "getMacAddress",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.hasCustomMac()) {
                        param?.result = config.normalizedMac()
                    }
                }
            }
        )
    }

    private fun hookScanResults(loadPackageParam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            WifiManager::class.java.name,
            loadPackageParam.classLoader,
            "getScanResults",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.hasCustomScanResult()) {
                        param?.result = listOf(createFakeScanResult(config))
                    }
                }
            }
        )
    }

    private fun readConfig(): FakeWifiConfig? {
        val context = cachedContext ?: return null
        return runCatching {
            val bundle = context.contentResolver.call(providerUri, ConfigProvider.METHOD_GET_CONFIG, null, null)
            FakeWifiConfig.fromBundle(bundle)
        }.onFailure {
            XposedBridge.log("Fake-Wifi: failed to read config: ${it.message}")
        }.getOrNull()
    }

    private fun createFakeScanResult(config: FakeWifiConfig): ScanResult {
        val result = ScanResult::class.java.getDeclaredConstructor().newInstance()
        result.SSID = config.scanResultSsid()
        result.BSSID = config.normalizedBssid()
        result.level = DEFAULT_RSSI
        result.frequency = DEFAULT_FREQUENCY
        result.capabilities = DEFAULT_CAPABILITIES
        result.timestamp = SystemClock.elapsedRealtimeNanos()
        return result
    }
}
