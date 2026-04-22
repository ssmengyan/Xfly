# Xfly v1.2

## Highlights

This release improves WiFi-connected state spoofing for apps that do more than read plain `SSID` / `BSSID` values.

It is intended for apps that briefly show spoofed WiFi information and then switch back to "no WiFi info" after a second-layer connectivity check.

## What's New

- Added stronger WiFi connectivity-state spoofing
- Expanded `ConnectivityManager` / `NetworkCapabilities` related handling
- Expanded `NetworkInfo` related handling
- Added `LinkProperties` interface name and DNS spoofing
- Kept the independent settings screen
- Standardized APK naming to `Xfly_<version>-debug.apk`

## Included APK

- `Xfly_1.2-debug.apk`

## Requirements

- Android 7.0+ (`minSdk 24`)
- LSPosed / compatible Xposed environment
- Xposed API level `82+`
- Enable the module in LSPosed
- Select target apps in LSPosed scope
- Restart target apps after changing spoofed values

## Notes

- This version improves WiFi detection compatibility, but some apps may still use additional proprietary checks.
- If needed, future versions can continue expanding cellular-state and transport-level spoofing.
