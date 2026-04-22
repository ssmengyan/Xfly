package hook

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.ProxyInfo
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
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
import java.net.InetAddress
import java.util.Collections

class XposedStart : IXposedHookLoadPackage {
    companion object {
        private const val DEFAULT_RSSI = -42
        private const val DEFAULT_FREQUENCY = 5745
        private const val DEFAULT_CAPABILITIES = "[WPA2-PSK-CCMP][ESS]"
        private const val DEFAULT_INTERFACE_NAME = "wlan0"
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
        hookWifiState(loadPackageParam)
        hookConnectivityState(loadPackageParam)
        hookLinkProperties(loadPackageParam)
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

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.WifiInfo",
            loadPackageParam.classLoader,
            "getSupplicantState",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = SupplicantState.COMPLETED
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            "android.net.wifi.WifiInfo",
            loadPackageParam.classLoader,
            "getNetworkId",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = 1
                    }
                }
            }
        )
    }

    private fun hookWifiState(loadPackageParam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            WifiManager::class.java.name,
            loadPackageParam.classLoader,
            "isWifiEnabled",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = true
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            WifiManager::class.java.name,
            loadPackageParam.classLoader,
            "getWifiState",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = WifiManager.WIFI_STATE_ENABLED
                    }
                }
            }
        )
    }

    private fun hookConnectivityState(loadPackageParam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            ConnectivityManager::class.java.name,
            loadPackageParam.classLoader,
            "isActiveNetworkMetered",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = false
                    }
                }
            }
        )

        for (methodName in listOf("getActiveNetworkInfo", "getNetworkInfo")) {
            when (methodName) {
                "getActiveNetworkInfo" -> XposedHelpers.findAndHookMethod(
                    ConnectivityManager::class.java.name,
                    loadPackageParam.classLoader,
                    methodName,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam?) {
                            val config = readConfig() ?: return
                            if (!config.shouldSpoofWifiState()) return
                            val result = param?.result as? NetworkInfo ?: return
                            patchNetworkInfoInPlace(result)
                        }
                    }
                )

                "getNetworkInfo" -> XposedHelpers.findAndHookMethod(
                    ConnectivityManager::class.java.name,
                    loadPackageParam.classLoader,
                    methodName,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam?) {
                            val config = readConfig() ?: return
                            if (!config.shouldSpoofWifiState()) return
                            val requestedType = param?.args?.getOrNull(0) as? Int ?: return
                            if (requestedType != ConnectivityManager.TYPE_WIFI) return
                            val result = param.result as? NetworkInfo ?: return
                            patchNetworkInfoInPlace(result)
                        }
                    }
                )
            }
        }

        XposedHelpers.findAndHookMethod(
            NetworkCapabilities::class.java.name,
            loadPackageParam.classLoader,
            "hasTransport",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    val transportType = param?.args?.getOrNull(0) as? Int ?: return
                    if (config.shouldSpoofWifiState() && transportType == NetworkCapabilities.TRANSPORT_WIFI) {
                        param.result = true
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            NetworkCapabilities::class.java.name,
            loadPackageParam.classLoader,
            "hasCapability",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    val capability = param?.args?.getOrNull(0) as? Int ?: return
                    if (!config.shouldSpoofWifiState()) return
                    if (capability == NetworkCapabilities.NET_CAPABILITY_INTERNET ||
                        capability == NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    ) {
                        param.result = true
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            NetworkInfo::class.java.name,
            loadPackageParam.classLoader,
            "getType",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = ConnectivityManager.TYPE_WIFI
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            NetworkInfo::class.java.name,
            loadPackageParam.classLoader,
            "getTypeName",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = "WIFI"
                    }
                }
            }
        )

        for (methodName in listOf("isConnected", "isAvailable", "isConnectedOrConnecting")) {
            XposedHelpers.findAndHookMethod(
                NetworkInfo::class.java.name,
                loadPackageParam.classLoader,
                methodName,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val config = readConfig() ?: return
                        if (config.shouldSpoofWifiState()) {
                            param?.result = true
                        }
                    }
                }
            )
        }
    }

    private fun hookLinkProperties(loadPackageParam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            LinkProperties::class.java.name,
            loadPackageParam.classLoader,
            "getInterfaceName",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = DEFAULT_INTERFACE_NAME
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            LinkProperties::class.java.name,
            loadPackageParam.classLoader,
            "getDnsServers",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = listOf(InetAddress.getByName("8.8.8.8"), InetAddress.getByName("1.1.1.1"))
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            LinkProperties::class.java.name,
            loadPackageParam.classLoader,
            "getHttpProxy",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val config = readConfig() ?: return
                    if (config.shouldSpoofWifiState()) {
                        param?.result = null as ProxyInfo?
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
                        val originalResults = (param?.result as? List<*>)?.filterIsInstance<ScanResult>().orEmpty()
                        param?.result = mergeFakeScanResult(originalResults, config)
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

    private fun patchNetworkInfoInPlace(networkInfo: NetworkInfo) {
        runCatching {
            XposedHelpers.callMethod(networkInfo, "setDetailedState", NetworkInfo.DetailedState.CONNECTED, null, null)
        }
        runCatching {
            XposedHelpers.setIntField(networkInfo, "mNetworkType", ConnectivityManager.TYPE_WIFI)
        }
        runCatching {
            XposedHelpers.setBooleanField(networkInfo, "mIsAvailable", true)
        }
    }

    private fun mergeFakeScanResult(
        originalResults: List<ScanResult>,
        config: FakeWifiConfig,
    ): List<ScanResult> {
        val targetBssid = config.normalizedBssid()
        val targetSsid = config.scanResultSsid()
        val mutable = originalResults.toMutableList()
        val targetIndex = mutable.indexOfFirst { scanResult ->
            scanResult.BSSID == targetBssid || scanResult.SSID == targetSsid
        }

        return if (targetIndex >= 0) {
            mutable[targetIndex] = buildFakeScanResultFromBase(mutable[targetIndex], config)
            mutable
        } else {
            listOf(createFakeScanResult(config)) + mutable
        }
    }

    private fun buildFakeScanResultFromBase(base: ScanResult, config: FakeWifiConfig): ScanResult {
        val result = ScanResult::class.java.getDeclaredConstructor().newInstance()
        result.SSID = if (config.hasCustomSsid()) config.scanResultSsid() else base.SSID
        result.BSSID = if (config.hasCustomBssid()) config.normalizedBssid() else base.BSSID
        result.level = base.level
        result.frequency = base.frequency
        result.capabilities = base.capabilities
        result.timestamp = base.timestamp
        return result
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
