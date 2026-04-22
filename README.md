# Network Helper

[中文说明](./README.zh-CN.md)

An LSPosed/Xposed module for spoofing WiFi-related information returned to selected apps.

## Features

- Custom `SSID`
- Custom `BSSID`
- Custom device `MAC`
- Fake `WifiManager.getScanResults()`
- Independent settings screen
- Per-app enablement through LSPosed scope

## How It Works

This project hooks common WiFi APIs used by Android apps:

- `WifiInfo.getSSID()`
- `WifiInfo.getBSSID()`
- `WifiInfo.getMacAddress()`
- `WifiManager.getScanResults()`

The module app stores the configuration locally, and hooked target apps read the same configuration through a shared provider.

## Build

```powershell
gradlew.bat assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/V1.0_VersionCode-1-debug.apk
```

## Usage

1. Install the APK.
2. Open `Network Helper` and fill in the WiFi values you want to spoof.
3. Enable the module in LSPosed.
4. Select the target apps in LSPosed scope.
5. Restart the target apps.

## Notes

- Spoofing WiFi fields does not fully change the real network transport type.
- Apps that also check `ConnectivityManager`, IP routing, DNS, or lower-level network state may still detect the real connection.
- This module is most effective for apps that mainly rely on WiFi info APIs.
