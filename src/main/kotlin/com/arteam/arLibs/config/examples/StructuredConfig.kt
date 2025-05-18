/**
 * Structured configuration example.
 * This file demonstrates a structured approach to configuration organization.
 *
 * 结构化配置示例。
 * 此文件演示了配置组织的结构化方法。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.examples

import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigField
import com.arteam.arLibs.config.annotations.ConfigValue

/**
 * Structured configuration example.
 * 结构化配置示例。
 */
@Suppress("unused")
@Config(
    fileName = "structured-example",
    filePath = "examples",
    comments = [
        "Structured Configuration Example",
        "This is a demonstration of how to organize configuration in a structured way",
        "",
        "结构化配置示例",
        "这是如何以结构化方式组织配置的演示"
    ]
)
class StructuredConfig {
    
    /**
     * General settings section.
     * 常规设置部分。
     */
    @ConfigField(
        path = "general",
        comments = [
            "General Settings",
            "",
            "常规设置"
        ]
    )
    val general = GeneralSection()
    
    /**
     * Network settings section.
     * 网络设置部分。
     */
    @ConfigField(
        path = "network",
        comments = [
            "Network Settings",
            "",
            "网络设置"
        ]
    )
    val network = NetworkSection()
    
    /**
     * Storage settings section.
     * 存储设置部分。
     */
    @ConfigField(
        path = "storage",
        comments = [
            "Storage Settings",
            "",
            "存储设置"
        ]
    )
    val storage = StorageSection()
    
    /**
     * General settings section.
     * 常规设置部分。
     */
    inner class GeneralSection {
        /**
         * The application name.
         * 应用程序名称。
         */
        @ConfigValue(
            path = "name",
            defaultValue = "MyApp",
            validators = ["com.arteam.arLibs.config.validation.validators.NotEmptyValidators#string()"],
            comments = ["Application name", "应用程序名称"]
        )
        var name: String = "MyApp"
        
        /**
         * The application version.
         * 应用程序版本。
         */
        @ConfigValue(
            path = "version",
            defaultValue = "1.0.0",
            comments = ["Application version", "应用程序版本"]
        )
        var version: String = "1.0.0"
        
        /**
         * The application language.
         * 应用程序语言。
         */
        @ConfigValue(
            path = "language",
            defaultValue = "en",
            comments = ["Application language (en, zh, etc.)", "应用程序语言 (en, zh 等)"]
        )
        var language: String = "en"
        
        /**
         * User interface settings.
         * 用户界面设置。
         */
        @ConfigField(
            path = "ui",
            comments = ["User Interface Settings", "用户界面设置"]
        )
        val ui = UISettings()
        
        /**
         * User interface settings class.
         * 用户界面设置类。
         */
        inner class UISettings {
            /**
             * The UI theme.
             * 用户界面主题。
             */
            @ConfigValue(
                path = "theme",
                defaultValue = "light",
                comments = ["UI theme (light, dark, system)", "用户界面主题 (light, dark, system)"]
            )
            var theme: String = "light"
            
            /**
             * The UI font size.
             * 用户界面字体大小。
             */
            @ConfigValue(
                path = "font-size",
                defaultValue = "14",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(8,24)"],
                comments = ["UI font size (8-24)", "用户界面字体大小 (8-24)"]
            )
            var fontSize: Int = 14
            
            /**
             * Whether to show tooltips.
             * 是否显示工具提示。
             */
            @ConfigValue(
                path = "show-tooltips",
                defaultValue = "true",
                comments = ["Whether to show tooltips", "是否显示工具提示"]
            )
            var showTooltips: Boolean = true
        }
    }
    
    /**
     * Network settings section.
     * 网络设置部分。
     */
    inner class NetworkSection {
        /**
         * The server host.
         * 服务器主机。
         */
        @ConfigValue(
            path = "host",
            defaultValue = "localhost",
            comments = ["Server host", "服务器主机"]
        )
        var host: String = "localhost"
        
        /**
         * The server port.
         * 服务器端口。
         */
        @ConfigValue(
            path = "port",
            defaultValue = "8080",
            type = "int",
            validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(1024,65535)"],
            comments = ["Server port (1024-65535)", "服务器端口 (1024-65535)"]
        )
        var port: Int = 8080
        
        /**
         * Connection settings.
         * 连接设置。
         */
        @ConfigField(
            path = "connection",
            comments = ["Connection Settings", "连接设置"]
        )
        val connection = ConnectionSettings()
        
        /**
         * API settings.
         * API 设置。
         */
        @ConfigField(
            path = "api",
            comments = ["API Settings", "API 设置"]
        )
        val api = APISettings()
        
        /**
         * Connection settings class.
         * 连接设置类。
         */
        inner class ConnectionSettings {
            /**
             * Connection timeout in seconds.
             * 连接超时（秒）。
             */
            @ConfigValue(
                path = "timeout",
                defaultValue = "30",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(5,300)"],
                comments = ["Connection timeout in seconds (5-300)", "连接超时（秒）(5-300)"]
            )
            var timeout: Int = 30
            
            /**
             * Maximum number of retries.
             * 最大重试次数。
             */
            @ConfigValue(
                path = "max-retries",
                defaultValue = "3",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(0,10)"],
                comments = ["Maximum number of retries (0-10)", "最大重试次数 (0-10)"]
            )
            var maxRetries: Int = 3
            
            /**
             * Whether to use SSL.
             * 是否使用 SSL。
             */
            @ConfigValue(
                path = "use-ssl",
                defaultValue = "true",
                comments = ["Whether to use SSL", "是否使用 SSL"]
            )
            var useSsl: Boolean = true
        }
        
        /**
         * API settings class.
         * API 设置类。
         */
        inner class APISettings {
            /**
             * API key.
             * API 密钥。
             */
            @ConfigValue(
                path = "key",
                defaultValue = "",
                validators = ["com.arteam.arLibs.config.validation.validators.NotEmptyValidators#string()"],
                comments = [
                    "API key (required)",
                    "You must set this to a valid API key",
                    "",
                    "API 密钥（必需）",
                    "您必须将其设置为有效的 API 密钥"
                ]
            )
            var key: String = ""
            
            /**
             * API version.
             * API 版本。
             */
            @ConfigValue(
                path = "version",
                defaultValue = "v1",
                comments = ["API version", "API 版本"]
            )
            var version: String = "v1"
            
            /**
             * API rate limit per minute.
             * 每分钟 API 速率限制。
             */
            @ConfigValue(
                path = "rate-limit",
                defaultValue = "60",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(1,1000)"],
                comments = ["API rate limit per minute (1-1000)", "每分钟 API 速率限制 (1-1000)"]
            )
            var rateLimit: Int = 60
        }
    }
    
    /**
     * Storage settings section.
     * 存储设置部分。
     */
    inner class StorageSection {
        /**
         * Storage type.
         * 
         * 存储类型。
         */
        @ConfigValue(
            path = "type",
            defaultValue = "sqlite",
            comments = [
                "Storage type (sqlite, mysql, postgresql)",
                "存储类型 (sqlite, mysql, postgresql)"
            ]
        )
        var type: String = "sqlite"
        
        /**
         * File storage settings.
         * 
         * 文件存储设置。
         */
        @ConfigField(
            path = "file",
            comments = ["File Storage Settings", "文件存储设置"]
        )
        val file = FileSettings()
        
        /**
         * Database settings.
         * 
         * 数据库设置。
         */
        @ConfigField(
            path = "database",
            comments = ["Database Settings", "数据库设置"]
        )
        val database = DatabaseSettings()
        
        /**
         * Cache settings class.
         * 缓存设置类。
         */
        @ConfigField(
            path = "cache",
            comments = ["Cache Settings", "缓存设置"]
        )
        val cache = CacheSettings()
        
        /**
         * Logging settings class.
         * 日志记录设置类。
         */
        @ConfigField(
            path = "logging",
            comments = ["Logging Settings", "日志记录设置"]
        )
        val logging = LoggingSettings()
        
        /**
         * File storage settings class.
         * 
         * 文件存储设置类。
         */
        inner class FileSettings {
            /**
             * File path.
             * 
             * 文件路径。
             */
            @ConfigValue(
                path = "path",
                defaultValue = "data/storage.db",
                comments = ["File path", "文件路径"]
            )
            var path: String = "data/storage.db"
            
            /**
             * Whether to compress files.
             * 
             * 是否压缩文件。
             */
            @ConfigValue(
                path = "compress",
                defaultValue = "true",
                comments = ["Whether to compress files", "是否压缩文件"]
            )
            var compress: Boolean = true
            
            /**
             * Auto-backup frequency in hours.
             * 
             * 自动备份频率（小时）。
             */
            @ConfigValue(
                path = "backup-frequency",
                defaultValue = "24",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(0,168)"],
                comments = [
                    "Auto-backup frequency in hours (0-168, 0 to disable)",
                    "自动备份频率（小时）(0-168，0 表示禁用)"
                ]
            )
            var backupFrequency: Int = 24
        }
        
        /**
         * Database settings class.
         * 
         * 数据库设置类。
         */
        inner class DatabaseSettings {
            /**
             * Database host.
             * 
             * 数据库主机。
             */
            @ConfigValue(
                path = "host",
                defaultValue = "localhost",
                comments = ["Database host", "数据库主机"]
            )
            var host: String = "localhost"
            
            /**
             * Database port.
             * 
             * 数据库端口。
             */
            @ConfigValue(
                path = "port",
                defaultValue = "3306",
                type = "int",
                comments = ["Database port", "数据库端口"]
            )
            var port: Int = 3306
            
            /**
             * Database name.
             * 
             * 数据库名称。
             */
            @ConfigValue(
                path = "name",
                defaultValue = "myapp",
                validators = ["com.arteam.arLibs.config.validation.validators.NotEmptyValidators#string()"],
                comments = ["Database name", "数据库名称"]
            )
            var name: String = "myapp"
            
            /**
             * Database username.
             * 
             * 数据库用户名。
             */
            @ConfigValue(
                path = "username",
                defaultValue = "root",
                comments = ["Database username", "数据库用户名"]
            )
            var username: String = "root"
            
            /**
             * Database password.
             * 
             * 数据库密码。
             */
            @ConfigValue(
                path = "password",
                defaultValue = "",
                comments = ["Database password", "数据库密码"]
            )
            var password: String = ""
            
            /**
             * Connection pool settings.
             * 
             * 连接池设置。
             */
            @ConfigField(
                path = "pool",
                comments = ["Connection Pool Settings", "连接池设置"]
            )
            val pool = PoolSettings()
            
            /**
             * Connection pool settings class.
             * 
             * 连接池设置类。
             */
            inner class PoolSettings {
                /**
                 * Minimum pool size.
                 * 
                 * 最小池大小。
                 */
                @ConfigValue(
                    path = "min-size",
                    defaultValue = "5",
                    type = "int",
                    validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(1,100)"],
                    comments = ["Minimum pool size (1-100)", "最小池大小 (1-100)"]
                )
                var minSize: Int = 5
                
                /**
                 * Maximum pool size.
                 * 
                 * 最大池大小。
                 */
                @ConfigValue(
                    path = "max-size",
                    defaultValue = "20",
                    type = "int",
                    validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(5,200)"],
                    comments = ["Maximum pool size (5-200)", "最大池大小 (5-200)"]
                )
                var maxSize: Int = 20
                
                /**
                 * Connection idle timeout in seconds.
                 * 
                 * 连接空闲超时（秒）。
                 */
                @ConfigValue(
                    path = "idle-timeout",
                    defaultValue = "300",
                    type = "int",
                    validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(60,3600)"],
                    comments = [
                        "Connection idle timeout in seconds (60-3600)",
                        "连接空闲超时（秒）(60-3600)"
                    ]
                )
                var idleTimeout: Int = 300
            }
        }
        
        /**
         * Cache settings class.
         * 缓存设置类。
         */
        inner class CacheSettings {
            /**
             * Whether caching is enabled.
             * 是否启用缓存。
             */
            @ConfigValue(
                path = "enabled",
                defaultValue = "true",
                comments = ["Whether caching is enabled", "是否启用缓存"]
            )
            var enabled: Boolean = true
            
            /**
             * Cache expiration time in minutes.
             * 缓存过期时间（分钟）。
             */
            @ConfigValue(
                path = "expiration-minutes",
                defaultValue = "60",
                type = "int",
                validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(1,1440)"],
                comments = ["Cache expiration time in minutes (1-1440)", "缓存过期时间（分钟）(1-1440)"]
            )
            var expirationMinutes: Int = 60
        }
        
        /**
         * Logging settings class.
         * 日志记录设置类。
         */
        inner class LoggingSettings {
            /**
             * The log file path.
             * 日志文件路径。
             */
            @ConfigValue(
                path = "file-path",
                defaultValue = "logs/app.log",
                comments = ["Log file path", "日志文件路径"]
            )
            var filePath: String = "logs/app.log"
            
            /**
             * The log level.
             * 日志级别。
             */
            @ConfigValue(
                path = "level",
                defaultValue = "INFO",
                comments = ["Log level (DEBUG, INFO, WARN, ERROR)", "日志级别 (DEBUG, INFO, WARN, ERROR)"]
            )
            var level: String = "INFO"
        }
    }
} 