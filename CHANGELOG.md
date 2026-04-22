# Changelog

## v1.2

- Strengthened WiFi connectivity-state spoofing
- Added broader handling for apps that verify whether WiFi is really connected
- Expanded `ConnectivityManager`, `NetworkCapabilities`, `NetworkInfo`, and `LinkProperties` spoofing
- Standardized APK naming to `Xfly_<version>-debug.apk`
- Added release artifact packaging under `release-assets/`

## v1.1

- Added independent settings screen
- Added unified config storage
- Added `ContentProvider`-based config access for hooked processes
- Added `WifiManager.getScanResults()` spoofing
- Renamed the app to `Xfly`

## v1.0

- Initial Fake-Wifi based functionality
- Spoofed `SSID`, `BSSID`, and `MAC`
