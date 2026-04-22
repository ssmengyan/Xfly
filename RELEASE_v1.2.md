# Xfly v1.2

## Release Summary

This release focuses on improving WiFi-connected state spoofing for apps that do more than read plain `SSID` / `BSSID` fields.

## Artifact

Current build artifact:

```text
app/build/outputs/apk/debug/Xfly_1.2-debug.apk
```

Local build path used in this repository:

```text
D:\xiaomi\Fake-Wifi\app\build\outputs\apk\debug\Xfly_1.2-debug.apk
```

## Configuration Requirements

- Android 7.0+ (`minSdk 24`)
- LSPosed / compatible Xposed environment
- Xposed API level `82+`
- Enable the module in LSPosed
- Select target apps in LSPosed scope
- Restart target apps after changing spoofed values

## Main Changes

- Added WiFi connectivity-state spoofing
- Added stronger handling for apps that verify whether WiFi is really connected
- Kept independent settings UI
- Standardized APK naming to `Xfly_<version>-debug.apk`

## Quick Setup

1. Install the APK.
2. Open `Xfly`.
3. Fill in SSID, BSSID, and MAC if needed.
4. Enable the module in LSPosed.
5. Select the target apps.
6. Restart the target apps.
