package com.arteam.arlibs.language.annotations

/**
 * Annotation to mark a class as a language definition.
 * This class should contain properties annotated with @LanguageKey to define the actual translations.
 * 用于标记一个类作为语言定义的注解。
 * 此类应包含使用 @LanguageKey 注解的属性来定义实际的翻译条目。
 *
 * @property name The name of the language (e.g., "zh_CN", "en_US"). This is used as the filename (e.g., "zh_CN.yml").
 *                语言名称（例如："zh_CN"、"en_US"）。这将用作文件名（例如："zh_CN.yml"）。
 * @property displayName The display name of the language (e.g., "简体中文", "English").
 *                       语言的显示名称（例如："简体中文"、"English"）。
 * @property path The sub-path within the plugin's 'languages' folder where this language file will be stored. Defaults to root of 'languages'.
 *                语言文件在插件 'languages' 文件夹中的子路径。默认为 'languages' 文件夹的根目录。
 * @property version The structure/schema version of the language keys defined in this class. Used to manage compatibility.
 *                   此类中定义的语言键的结构/模式版本。用于管理兼容性。
 * @property isDefault Whether this is the default language for the plugin.
 *                     是否为插件的默认语言。
 * @property cloudUrl The URL to download updated versions of this language file. The downloaded file should contain 'structure-version' and 'content-version' keys.
 *                    从此URL下载此语言文件的更新版本。下载的文件应包含 'structure-version' 和 'content-version' 键。
 * @property autoUpdate Whether to automatically check for and download updates from the cloudUrl.
 *                      是否自动从cloudUrl检查并下载更新。
 * @property updateInterval The interval (in minutes) to check for updates if autoUpdate is true.
 *                          如果autoUpdate为true，检查更新的间隔（分钟）。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Language(
    val name: String,
    val displayName: String,
    val path: String = "", // Relative to plugin's "languages" folder
    val version: Int = 1, // Structure version
    val isDefault: Boolean = false,
    val cloudUrl: String = "",
    val autoUpdate: Boolean = false,
    val updateInterval: Long = 60 // In minutes
) 