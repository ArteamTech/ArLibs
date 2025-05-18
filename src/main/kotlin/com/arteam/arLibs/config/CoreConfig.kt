/**
 * Core configuration for the ArLibs plugin.
 * Contains critical settings for the plugin's operation.
 *
 * ArLibs 插件的核心配置。
 * 包含插件运行的关键设置。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config

import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigField
import com.arteam.arLibs.config.annotations.ConfigValue

/**
 * Core configuration for the ArLibs plugin.
 * ArLibs 插件的核心配置。
 */
@Suppress("unused")
@Config(
    fileName = "core",
    comments = [
        "ArLibs Core Configuration",
        "Contains core settings for the plugin's operation",
        "",
        "ArLibs 核心配置",
        "包含插件运行的核心设置"
    ]
)
class CoreConfig {
    
    /**
     * Debug settings section.
     * 调试设置部分。
     */
    @ConfigField(
        path = "debug",
        comments = [
            "Debug settings",
            "Enable or disable various debug features",
            "",
            "调试设置",
            "启用或禁用各种调试功能"
        ]
    )
    val debug = DebugSettings()
    
    /**
     * Performance settings section.
     * 性能设置部分。
     */
    @ConfigField(
        path = "performance",
        comments = [
            "Performance settings",
            "Adjust these to optimize performance",
            "",
            "性能设置",
            "调整这些以优化性能"
        ]
    )
    val performance = PerformanceSettings()
    
    /**
     * Storage settings section.
     * 存储设置部分。
     */
    @ConfigField(
        path = "storage",
        comments = [
            "Storage settings",
            "Configure data storage options",
            "",
            "存储设置",
            "配置数据存储选项"
        ]
    )
    val storage = StorageSettings()
    
    /**
     * Debug settings class.
     * 调试设置类。
     */
    inner class DebugSettings {
        /**
         * Whether debug mode is enabled.
         * 是否启用调试模式。
         */
        @ConfigValue(
            path = "enabled",
            defaultValue = "false",
            comments = [
                "Whether debug mode is enabled",
                "Set to true to enable additional logging and debug features",
                "",
                "是否启用调试模式",
                "设置为 true 以启用额外的日志记录和调试功能"
            ]
        )
        var enabled: Boolean = false
        
        /**
         * Whether to enable example features.
         * 是否启用示例功能。
         */
        @ConfigValue(
            path = "enable-examples",
            defaultValue = "false",
            comments = [
                "Whether to enable example features",
                "Set to true to enable example commands and configurations",
                "",
                "是否启用示例功能",
                "设置为 true 以启用示例命令和配置"
            ]
        )
        var enableExamples: Boolean = false
        
        /**
         * Debug log level.
         * 调试日志级别。
         */
        @ConfigValue(
            path = "log-level",
            defaultValue = "1",
            type = "int",
            validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(0,3)"],
            comments = [
                "Debug log level (0-3)",
                "0: None, 1: Errors only, 2: Warnings and errors, 3: All debug info",
                "",
                "调试日志级别 (0-3)",
                "0: 无, 1: 仅错误, 2: 警告和错误, 3: 所有调试信息"
            ]
        )
        var logLevel: Int = 1
    }
    
    /**
     * Performance settings class.
     * 性能设置类。
     */
    inner class PerformanceSettings {
        /**
         * Maximum cache size.
         * 最大缓存大小。
         */
        @ConfigValue(
            path = "max-cache-size",
            defaultValue = "1000",
            type = "int",
            validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(100,10000)"],
            comments = [
                "Maximum cache size (100-10000)",
                "Higher values use more memory but may improve performance",
                "",
                "最大缓存大小 (100-10000)",
                "较高的值会使用更多内存，但可能会提高性能"
            ]
        )
        var maxCacheSize: Int = 1000
        
        /**
         * Whether to use async processing when possible.
         * 是否尽可能使用异步处理。
         */
        @ConfigValue(
            path = "use-async",
            defaultValue = "true",
            comments = [
                "Whether to use async processing when possible",
                "Set to false only if you experience issues with async processing",
                "",
                "是否在可能的情况下使用异步处理",
                "仅当异步处理出现问题时才设置为 false"
            ]
        )
        var useAsync: Boolean = true
    }
    
    /**
     * Storage settings class.
     * 存储设置类。
     */
    inner class StorageSettings {
        /**
         * Storage type to use.
         * 要使用的存储类型。
         */
        @ConfigValue(
            path = "type",
            defaultValue = "yaml",
            comments = [
                "Storage type to use",
                "Options: yaml, sqlite, mysql",
                "",
                "要使用的存储类型",
                "选项：yaml, sqlite, mysql"
            ]
        )
        var type: String = "yaml"
        
        /**
         * Auto-save interval in seconds.
         * 自动保存间隔（秒）。
         */
        @ConfigValue(
            path = "auto-save-interval",
            defaultValue = "300",
            type = "int",
            validators = ["com.arteam.arLibs.config.validation.validators.RangeValidator\$Companion#forInt(60,3600)"],
            comments = [
                "Auto-save interval in seconds (60-3600)",
                "How often to automatically save data",
                "",
                "自动保存间隔（秒）(60-3600)",
                "多久自动保存一次数据"
            ]
        )
        var autoSaveInterval: Int = 300
    }
} 