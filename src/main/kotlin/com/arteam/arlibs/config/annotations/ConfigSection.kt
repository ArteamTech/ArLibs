package com.arteam.arlibs.config.annotations

/**
 * Annotation to mark a nested class or property as a configuration section
 * 用于标记嵌套类或属性作为配置节的注解
 *
 * @property path The path to the section in the configuration file
 *                配置文件中该节的路径
 * @property comment The comment for this section
 *                   该配置节的注释
 * @property required Whether this section is required. If false, it might not be written to the config unless 'createIfOptionalOnFirstLoad' is true and it's the first load.
 *                    该配置节是否是必需的。如果为false，则可能不会写入配置，除非 'createIfOptionalOnFirstLoad' 为true且为首次加载。
 * @property createIfOptionalOnFirstLoad If the section is not required, this controls whether it (and its default values) should be created in the YAML file when the config file is generated for the first time. Defaults to true.
 *                                       如果该节不是必需的，此属性控制当首次生成配置文件时，是否应在YAML文件中创建它（及其默认值）。默认为true。
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigSection(
    val path: String = "",
    val comment: Array<String> = [],
    val required: Boolean = true,
    val createIfOptionalOnFirstLoad: Boolean = true
) 