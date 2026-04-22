# Xfly

[中文说明](./README.zh-CN.md)

`Xfly` is an LSPosed/Xposed module for spoofing WiFi-related information returned to selected Android apps.

## Download

- [Xfly_1.2-debug.apk](./release-assets/Xfly_1.2-debug.apk)

## Upstream

This project is based on:

- [JeffChen001218/Fake-Wifi](https://github.com/JeffChen001218/Fake-Wifi)

This repository contains local modifications and extended functionality on top of that upstream project.

## Features

- Custom `SSID`
- Custom `BSSID`
- Custom device `MAC`
- Fake `WifiManager.getScanResults()`
- Independent settings screen
- Per-app enablement through LSPosed scope
- Extra WiFi connectivity-state spoofing for apps that also verify whether the device is really on WiFi

## Hook Coverage

Current versions spoof or patch these common WiFi / connectivity checks:

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

## How It Works

The module app stores the spoofed configuration locally.

When a target app is loaded by LSPosed, the hooked process reads the same configuration through a shared provider and returns the spoofed values to the target app.

## Build

```powershell
gradlew.bat assembleDebug
```

Debug APK output follows the `Xfly_<version>-debug.apk` naming format.

Current example:

```text
app/build/outputs/apk/debug/Xfly_1.2-debug.apk
```

## Requirements

- Android 7.0+ (`minSdk 24`)
- LSPosed / compatible Xposed environment
- Xposed API level `82` or above
- The module must be enabled in LSPosed
- Target apps must be selected in LSPosed scope
- Restart target apps after updating spoofed values

## Usage

1. Install the APK.
2. Open `Xfly` and fill in the WiFi values you want to spoof.
3. Enable the module in LSPosed.
4. Select the target apps in LSPosed scope.
5. Restart the target apps.

## Release Notes

- [v1.2 release summary](./RELEASE_v1.2.md)
- [GitHub Release copy text](./RELEASE_v1.2_GITHUB.md)

## Changelog

- [CHANGELOG.md](./CHANGELOG.md)

## License Status

Please read [LICENSE_STATUS.md](./LICENSE_STATUS.md) before reusing or redistributing this repository.

At the time of writing, the upstream repository does not appear to publish a clear open-source license file. Because of that, this fork does **not** currently declare a new permissive license on top of the upstream code.

## Notes

- Spoofing WiFi fields is not the same as fully changing the real transport type used by Android.
- This project now also spoofs several connectivity-state APIs, which helps with apps that perform a second-layer WiFi check.
- Some apps may still use additional signals such as cellular state, routing details, VPN state, or proprietary risk controls.
