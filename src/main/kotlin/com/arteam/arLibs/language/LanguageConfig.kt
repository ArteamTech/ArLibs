/**
 * Language configuration for the ArLibs plugin.
 * Contains settings for internationalization and localization.
 *
 * ArLibs 插件的语言配置。
 * 包含国际化和本地化的设置。
 *
 * @author ArteamTech
 * @since 2025-07-12
 * @version 1.0.0
 */
package com.arteam.arLibs.language

import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigValue

/**
 * Language configuration for the ArLibs plugin.
 * ArLibs 插件的语言配置。
 */
@Config(
    fileName = "language",
    comments = [
        "ArLibs Language Configuration",
        "Contains settings for internationalization and localization"
    ]
)
class LanguageConfig {

    @ConfigValue(
        path = "default_language",
        comments = [
            "Default language for the server",
            "This language will be used when a player's language is not set or not available"
        ]
    )
    var defaultLanguage: String = "en"

    @ConfigValue(
        path = "fallback_language",
        comments = [
            "Fallback language when a message is not found in the current language",
            "This ensures that messages are always displayed even if translation is incomplete"
        ]
    )
    var fallbackLanguage: String = "en"

    @ConfigValue(
        path = "auto_reload",
        comments = [
            "Automatically reload language files when they are modified",
            "Enable this for development, disable for production for better performance"
        ]
    )
    var autoReload: Boolean = false

    @ConfigValue(
        path = "debug_missing_keys",
        comments = [
            "Show debug messages when language keys are missing",
            "Helpful for identifying missing translations during development"
        ]
    )
    var debugMissingKeys: Boolean = false

    @ConfigValue(
        path = "supported_languages",
        comments = [
            "List of supported languages",
            "Only languages listed here will be loaded"
        ]
    )
    var supportedLanguages: List<String> = listOf("en", "zh_cn", "zh_tw")
} 