# Xfly v1.2

## Highlights

`v1.2` focuses on improving WiFi-connected state spoofing for apps that do more than read plain `SSID` / `BSSID` fields.

This version adds a stronger second layer of network-state spoofing so that target apps are more likely to treat the current connection as real WiFi instead of briefly showing spoofed information and then falling back to "no WiFi info".

## What's New

- Added stronger WiFi connectivity-state spoofing
- Expanded `ConnectivityManager` / `NetworkCapabilities` related handling
- Expanded `NetworkInfo` related handling
- Added `LinkProperties` interface name and DNS spoofing
- Kept the independent settings screen
- Standardized APK naming to `Xfly_<version>-debug.apk`

## Hook Coverage

- `WifiInfo.getSSID()`
- `WifiInfo.getBSSID()`
- `WifiInfo.getMacAddress()`
- `WifiInfo.getSupplicantState()`
- `WifiInfo.getNetworkId()`
- `WifiManager.isWifiEnabled()`
- `WifiManager.getWifiState()`
- `WifiManager.getScanResults()`
- `NetworkCapabilities.hasTransport(TRANSPORT_WIFI)`
- `NetworkCapabilities.hasCapability(INTERNET / VALIDATED)`
- `ConnectivityManager.getActiveNetworkInfo()`
- `ConnectivityManager.getNetworkInfo(TYPE_WIFI)`
- `ConnectivityManager.isActiveNetworkMetered()`
- `NetworkInfo.getType()`
- `NetworkInfo.getTypeName()`
- `NetworkInfo.isConnected()`
- `NetworkInfo.isAvailable()`
- `NetworkInfo.isConnectedOrConnecting()`
- `LinkProperties.getInterfaceName()`
- `LinkProperties.getDnsServers()`

## Build Artifact

Included APK:

```text
release-assets/Xfly_1.2-debug.apk
```

Original build output:

```text
app/build/outputs/apk/debug/Xfly_1.2-debug.apk
```

## Requirements

- Android 7.0+ (`minSdk 24`)
- LSPosed / compatible Xposed environment
- Xposed API level `82+`
- Enable the module in LSPosed
- Select target apps in LSPosed scope
- Restart target apps after changing spoofed values

## Quick Start

1. Install the APK.
2. Open `Xfly`.
3. Fill in the WiFi values you want to spoof.
4. Enable the module in LSPosed.
5. Select the target apps in LSPosed scope.
6. Restart the target apps.

## Notes

- This project spoofs WiFi-related fields and several connectivity-state signals, but some apps may still use extra detection signals.
- You may still need additional spoofing for specific apps with stricter networking checks.
