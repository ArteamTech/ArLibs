# ArLibs

[简体中文](README_CN.md) 

---

A powerful library for Minecraft plugin development.

## Documentation

For detailed documentation, please visit [docs.arteam.dev/ArLibs](https://docs.arteam.dev/ArLibs).

## Features

*   **Configuration Module**: Easy-to-use and flexible configuration management.
*   **Language Module**: Simplified internationalization (i18n) and localization (l10n) for your plugins.
*   **Database Module**: Abstracted database operations with support for various database types.
*   **Command Module**: Powerful and intuitive command creation and handling.

## Installation

To use ArLibs in your Gradle project, add the following to your `build.gradle.kts` file:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

dependencies {
    implementation("com.arteamtech:arlibs:1.0.0")
}
```

## Contributing

Contributions are welcome! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) (to be created) for details on how to contribute.

## License

This project is licensed under the [LGPL-3.0 License](LICENSE).
