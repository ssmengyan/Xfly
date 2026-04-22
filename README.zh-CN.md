# Xfly

[English README](./README.md)

`Xfly` 是一个基于 LSPosed/Xposed 的 Android 模块，用于向指定应用伪装 WiFi 相关信息。

## 上游来源

本项目基于以下上游仓库修改而来：

- [JeffChen001218/Fake-Wifi](https://github.com/JeffChen001218/Fake-Wifi)

当前仓库是在上游基础上继续扩展和修改的版本。

## 功能特性

- 自定义 `SSID`
- 自定义 `BSSID`
- 自定义设备 `MAC`
- 伪造 `WifiManager.getScanResults()`
- 独立设置页面
- 通过 LSPosed 作用域对指定应用生效
- 额外补充了一层“WiFi 已连接状态”伪装，适合会继续检测联网状态的应用

## Hook 覆盖范围

当前版本会伪造或修补这些常见接口：

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

## 实现方式

模块应用本身负责保存配置。

目标应用在被 LSPosed 注入后，会通过共享 provider 读取同一份配置，并在对应接口返回值上进行伪装。

## 编译

```powershell
gradlew.bat assembleDebug
```

Debug APK 输出采用 `Xfly_<版本号>-debug.apk` 命名格式。

当前示例：

```text
app/build/outputs/apk/debug/Xfly_1.2-debug.apk
```

## 使用方法

1. 安装 APK。
2. 打开 `Xfly`，填写要伪装的 WiFi 信息。
3. 在 LSPosed 中启用模块。
4. 在作用域中勾选目标应用。
5. 重启目标应用。

## 许可状态

请先阅读 [LICENSE_STATUS.md](./LICENSE_STATUS.md)。

截至当前检查时，上游仓库似乎**没有明确提供开源许可证文件**。因此，这个衍生仓库目前**没有**擅自声明新的宽松开源协议，以避免在上游授权未明确时造成许可问题。

## 注意事项

- 伪装 WiFi 字段并不等于完全改变 Android 的真实联网方式。
- 当前版本已经补充了多组“WiFi 已连接状态”相关接口，能更好适配会进行二次校验的应用。
- 某些应用仍可能继续检查蜂窝网络状态、路由细节、VPN 状态或自定义风控逻辑。
