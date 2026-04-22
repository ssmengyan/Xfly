package hook.tool

import android.content.Context
import android.os.Bundle

private const val PREF_NAME = "fake_wifi_config"
private const val KEY_SSID = "ssid"
private const val KEY_BSSID = "bssid"
private const val KEY_MAC = "mac"

data class FakeWifiConfig(
    val ssid: String = "",
    val bssid: String = "",
    val mac: String = "",
) {
    fun shouldSpoofWifiState(): Boolean = hasCustomSsid() || hasCustomBssid() || hasCustomMac()
    fun hasCustomSsid(): Boolean = ssid.trim().isNotEmpty()
    fun hasCustomBssid(): Boolean = bssid.trim().isNotEmpty()
    fun hasCustomMac(): Boolean = mac.trim().isNotEmpty()
    fun hasCustomScanResult(): Boolean = hasCustomSsid() || hasCustomBssid()

    fun normalizedBssid(): String = bssid.trim().ifBlank { "02:00:00:00:00:00" }
    fun normalizedMac(): String = mac.trim().ifBlank { "02:00:00:00:00:00" }

    fun wifiInfoSsid(): String {
        val raw = ssid.trim()
        if (raw.isEmpty()) return "\"FakeWifi\""
        return if (raw.startsWith("\"") && raw.endsWith("\"")) raw else "\"$raw\""
    }

    fun scanResultSsid(): String = wifiInfoSsid().removeSurrounding("\"")

    fun toBundle(): Bundle = Bundle().apply {
        putString(KEY_SSID, ssid)
        putString(KEY_BSSID, bssid)
        putString(KEY_MAC, mac)
    }

    companion object {
        fun fromBundle(bundle: Bundle?): FakeWifiConfig {
            if (bundle == null) return FakeWifiConfig()
            return FakeWifiConfig(
                ssid = bundle.getString(KEY_SSID).orEmpty(),
                bssid = bundle.getString(KEY_BSSID).orEmpty(),
                mac = bundle.getString(KEY_MAC).orEmpty(),
            )
        }
    }
}

fun saveConfig(config: FakeWifiConfig, context: Context) {
    prefs(context).edit()
        .putString(KEY_SSID, config.ssid.trim())
        .putString(KEY_BSSID, config.bssid.trim())
        .putString(KEY_MAC, config.mac.trim())
        .apply()
}

fun loadConfig(context: Context): FakeWifiConfig {
    val prefs = prefs(context)
    return FakeWifiConfig(
        ssid = prefs.getString(KEY_SSID, "").orEmpty(),
        bssid = prefs.getString(KEY_BSSID, "").orEmpty(),
        mac = prefs.getString(KEY_MAC, "").orEmpty(),
    )
}

private fun prefs(context: Context) = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
