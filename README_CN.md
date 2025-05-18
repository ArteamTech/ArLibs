# ArLibs

[English](README.md) 

---

一个强大的 Minecraft 插件开发库。

## 文档

详细文档请访问 [docs.arteam.dev/ArLibs](https://docs.arteam.dev/ArLibs)。

## 特性

*   **配置模块 (Configuration Module)**：易于使用且灵活的配置管理。
*   **语言模块 (Language Module)**：为您的插件提供简化的国际化 (i18n) 和本地化 (l10n) 支持。
*   **数据库模块 (Database Module)**：抽象化的数据库操作，支持多种数据库类型。
*   **命令模块 (Command Module)**：强大且直观的命令创建与处理。

## 安装

要在您的 Gradle 项目中使用 ArLibs，请将以下内容添加到您的 `build.gradle.kts` 文件中：

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

dependencies {
    implementation("com.arteamtech:arlibs:1.0.0")
}
```

## 贡献

欢迎贡献！请阅读我们的 [CONTRIBUTING.md](CONTRIBUTING.md) 以了解如何贡献。

## 许可证

本项目采用 [LGPL-3.0 许可证](LICENSE)授权。
