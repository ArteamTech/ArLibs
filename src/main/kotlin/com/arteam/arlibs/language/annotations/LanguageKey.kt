package com.arteam.arlibs.language.annotations

/**
 * Marks a property within a @Language-annotated class as a translatable string.
 * The property name itself is not directly used for the key; the 'path' field in this annotation is.
 * Its main purpose is to hold the default translation and comments for that key.
 * 用于标记 @Language 注解类中的属性作为可翻译字符串。
 * 属性名称本身不直接用作键，而是使用此注解中的 'path' 字段。
 * 其主要目的是保存该键的默认翻译和注释。
 *
 * @property path The unique key for this language string (e.g., "plugin.greeting", "command.error.no_permission").
 *                此语言字符串的唯一键（例如："plugin.greeting", "command.error.no_permission"）。
 * @property default The default translation for this language string. This will be used if no translation is found in the language file,
 *                   or when generating the language file for the first time.
 *                   此语言字符串的默认翻译。如果在语言文件中找不到翻译，或者首次生成语言文件时，将使用此翻译。
 * @property comment Optional comments to be placed above this key in the generated language YAML file.
 *                   可选注释，将放置在生成的语言 YAML 文件中此键的上方。
 */
@Target(AnnotationTarget.PROPERTY) // To be used on properties within the @Language class
@Retention(AnnotationRetention.RUNTIME) // To read annotation data via reflection at runtime
annotation class LanguageKey(
    val path: String,
    val default: String,
    val comment: Array<String> = []
) 