/**
 * Defines a specific configuration value in a configuration file or section.
 * Fields annotated with @ConfigValue will be mapped to individual key-value pairs.
 *
 * 定义配置文件或部分中的特定配置值。使用 @ConfigValue 注解的字段将映射到单个键值对。
 *
 * Parameters:
 * - path: The path to this configuration value (e.g., "host", "port")
 *         配置值的路径（例如，"host"，"port"）
 * - defaultValue: Optional default value as a string, used if the value is not present in the configuration
 *                 可选的默认值（字符串形式），当配置中不存在该值时使用
 * - type: Optional type specification for this value (String, Int, Boolean, etc.)
 *         When specifying list types (e.g., "List<String>", "List<Int>"), this primarily applies to lists of simple types. 
 *         Lists of complex objects may require custom handling and are not automatically converted from string defaults.
 *         可选的值类型规范（String、Int、Boolean 等）
 *         当指定列表类型（例如，"List<String>"，"List<Int>"）时，这主要适用于简单类型的列表。
 *         复杂对象的列表可能需要自定义处理，并且不会从字符串默认值自动转换。
 * - validators: Optional array of validator class names to apply to this value
 *               可选的验证器类名数组，用于验证此值
 * - comments: Optional comments to include before this value in the configuration file
 *             可选的注释，将包含在配置文件中该值之前
 * - required: Boolean indicating whether this value is required
 *             布尔值，指示此值是否必需
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigValue(
    val path: String,
    val defaultValue: String = "",
    val type: String = "",
    val validators: Array<String> = [],
    val comments: Array<String> = [],
    val required: Boolean = false
) 