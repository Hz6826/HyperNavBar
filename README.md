# HyperNavBar

小米 HyperOS 小白条沉浸规则管理工具 — 基于 Root 的导航栏行为自定义应用。

## 功能

- **多云订阅支持** — 添加云端 JSON 订阅源或本地 JSON，自动拉取并解析 `NBIRules`
- **优先级合并** — 多订阅源按优先级智能合并，按包名去重
- **智能格式转换** — 自动检测 HyperOS 版本（OS22 / OS30 / OS33），输出对应格式（XML / JSON）
- **一键应用 & 热重载** — Root 写文件 + `cmd miui_navigation_bar_immersive update` 即时生效
- **还原官方规则** — 支持备份 / 恢复系统默认规则
- **开机自动应用** — 通过 `BootReceiver` 开机后自动应用规则
- **3 种导航栏样式** — 标准 / 悬浮 / 液态玻璃（iOS 风格液态玻璃效果）
- **6 种主题模式** — 系统跟随 / 浅色 / 深色 / Monet 跟随 / Monet 浅色 / Monet 深色
- **中英文双语** — 支持简体中文和英文，跟随系统语言
- **设置导入导出** — 一键备份 / 恢复全部配置

## 系统要求

- 小米设备，已刷 **HyperOS**（2.2 / 3.0 / 3.3+）
- **Root 权限**（Magisk / KernelSU / APatch）
- Android 15+（minSdk 35）

## 架构

```
MainActivity (Jetpack Compose + Miuix)
├── HorizontalPager 4 页
│   ├── HomePage        — Root 状态、设备信息、订阅数 / 应用数
│   ├── RulesPage       — 订阅管理、合并、应用、恢复
│   ├── SettingsPage    — 主题、导航栏样式、语言、自启、导入导出
│   └── AboutPage       — 版本、链接、许可证
├── BottomNavigationBar — 标准 / 悬浮 / 液态玻璃
└── BootReceiver        — 开机自动应用规则
```

### 规则处理管线

```
RuleConfigSource (订阅数据模型)
  → RuleFetcher (HTTP 拉取 / 本地解析)
  → RuleCombiner (多源按优先级合并)
  → RuleConverter (OS 版本检测 → 格式转换)
  → RootApplier (su 写文件 + cmd 热重载)
```

### 关键技术栈

| 层 | 技术 |
|---|---|
| UI | Jetpack Compose + Miuix |
| 状态管理 | `SharedPreferences` + `remember` + `mutableStateOf` |
| 网络 | `HttpURLConnection` |
| Root | `su` 执行 shell 命令 |
| 动画 | 自定义 Spring 物理 + OpenGL Fragment Shader |
| 路由 | `HorizontalPager` + 无 ViewModel / 无 DI |

## 构建

```bash
# 克隆项目
git clone https://github.com/ianzb/HyperNavBar.git
cd HyperNavBar

# 设置 JDK 17+ 和 Android SDK
# 编辑 local.properties 指向你的 SDK 路径

# 构建 Debug APK
./gradlew assembleDebug

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 下载

前往 [Releases](https://github.com/ianzb/HyperNavBar/releases) 下载最新 APK。

## 第三方库

- [Miuix](https://github.com/YuKongA/Miuix) — HyperOS 风格 Compose UI 组件库
- [AndroidX Compose](https://developer.android.com/jetpack/compose) — 声明式 UI 框架
- [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) — 液态玻璃导航栏效果参考
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) — 异步支持

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。

## 致谢

- `cmd miui_navigation_bar_immersive` 逆向工程参考
- 小米 HyperOS 社区
