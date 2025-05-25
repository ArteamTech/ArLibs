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
import com.arteam.arLibs.config.annotations.ConfigValue

/**
 * Core configuration for the ArLibs plugin.
 * ArLibs 插件的核心配置。
 */
@Config(
    fileName = "config",
    comments = [
        "ArLibs Core Configuration",
        "Contains core settings for the plugin's operation"
    ]
)
class CoreConfig {

    @ConfigValue(
        path = "debug",
        comments = [
            "Debug settings",
            "Enable or disable various debug features"
        ]
    )
    var debug: Boolean = false
}