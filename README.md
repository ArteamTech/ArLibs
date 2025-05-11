# ArLibs

A Minecraft library plugin designed as a foundation for extending other projects.

## Features

- Base plugin structure for Minecraft 1.20.1+
- Written in Kotlin 2.1.20 with Java 21 compatibility
- Uses Shadow for dependency management

## Development

### Requirements

- Java 21 JDK
- Gradle (wrapper included)

### Building

To build the plugin:

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/ArLibs.jar`

### Installation

Place the JAR file in your server's `plugins` folder and restart the server.

## Usage

This plugin serves as a library for other plugins. To use it in your project, add it as a dependency in your plugin.yml:

```yaml
depend: [ArLibs]
```

## License

This project is licensed under the [LGPL-3.0 License](LICENSE.txt).