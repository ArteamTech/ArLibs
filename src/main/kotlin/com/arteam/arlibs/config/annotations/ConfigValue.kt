package com.arteam.arlibs.config.annotations

import com.arteam.arlibs.config.validator.ConfigValidator
import com.arteam.arlibs.config.validator.NoValidator
import kotlin.reflect.KClass

/**
 * Annotation to mark a property as a configuration value
 * 用于标记属性作为配置值的注解
 *
 * @property path The path to the value in the configuration file
 *                配置文件中该值的路径
 * @property comment The comment for this value (for multiple lines). If 'commentLine' is also set, 'comment' takes precedence.
 *                   该配置值的注释（用于多行）。如果 'commentLine' 也被设置，则 'comment' 优先。
 * @property commentLine A single line comment for this value. Simpler alternative to 'comment' for single lines.
 *                       该配置值的单行注释。对于单行注释，是 'comment' 的简单替代。
 * @property required Whether this value is required (cannot be null or missing from config after initial generation).
 *                    该配置值是否是必需的（在初始生成后，不能为空或从配置中缺失）。
 * @property validator The validator class to use for this value
 *                     用于验证该配置值的验证器类
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigValue(
    val path: String = "",
    val comment: Array<String> = [],
    val commentLine: String = "",
    val required: Boolean = true,
    val validator: KClass<out ConfigValidator<*>> = NoValidator::class
)