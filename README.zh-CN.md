# 网络助手

[English README](./README.md)

这是一个基于 LSPosed/Xposed 的 Android 模块，用于向指定应用伪装 WiFi 相关信息。

## 功能特性

- 自定义 `SSID`
- 自定义 `BSSID`
- 自定义设备 `MAC`
- 伪造 `WifiManager.getScanResults()`
- 独立设置页面
- 通过 LSPosed 作用域对指定应用生效

## 实现方式

项目会 hook Android 应用常见的 WiFi 接口：

- `WifiInfo.getSSID()`
- `WifiInfo.getBSSID()`
- `WifiInfo.getMacAddress()`
- `WifiManager.getScanResults()`

模块应用本身负责保存配置，目标应用在被 hook 后通过共享 provider 读取同一份配置。

## 编译

```powershell
gradlew.bat assembleDebug
```

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/V1.0_VersionCode-1-debug.apk
```

## 使用方法

1. 安装 APK。
2. 打开 `网络助手`，填写要伪装的 WiFi 信息。
3. 在 LSPosed 中启用模块。
4. 在作用域中勾选目标应用。
5. 重启目标应用。

## 注意事项

- 伪装 WiFi 字段并不等于完全改变真实网络类型。
- 如果应用还会检查 `ConnectivityManager`、IP 路由、DNS 或更底层的网络状态，仍然可能识别出真实连接状态。
- 这个模块更适合主要依赖 WiFi 信息接口的目标应用。
