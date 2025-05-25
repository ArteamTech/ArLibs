/**
 * Marks a class as a configuration file. Classes annotated with @Config will be processed
 * by the configuration system to load and save configuration values.
 *
 * 标记一个类作为配置文件。被 @Config 注解的类将由配置系统处理，用于加载和保存配置值。
 *
 * Parameters:
 * - fileName: The name of the configuration file (without extension)
 *             配置文件的名称（不包括扩展名）
 * - filePath: Optional relative path within the plugin's data folder (e.g., "modules/settings")
 *             可选的相对路径，位于插件数据文件夹内（例如，"modules/settings"）
 * - comments: Optional comments to include at the top of the configuration file
 *             可选的注释，将包含在配置文件的顶部
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(
    val fileName: String,
    val filePath: String = "",
    val comments: Array<String> = []
) 