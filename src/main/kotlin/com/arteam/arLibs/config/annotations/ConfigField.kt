/**
 * Defines a section in a configuration file. Fields annotated with @ConfigField
 * represent structured sections that can contain multiple ConfigValue entries.
 * ConfigField annotations can be nested to create deeper configuration hierarchies.
 * 定义配置文件中的一个部分。使用 @ConfigField 注解的字段表示可以包含多个 ConfigValue 条目的结构化部分。
 * ConfigField 注解可以嵌套，以创建更深层次的配置层次结构。
 *
 * Parameters:
 * - path: The path in the configuration file (e.g., "database", "settings.advanced")
 *         配置文件中的路径（例如，"database"，"settings.advanced"）
 * - comments: Optional comments to include before this section in the configuration file
 *             可选的注释，将包含在配置文件中该部分之前
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigField(
    val path: String,
    val comments: Array<String> = []
) 