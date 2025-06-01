/**
 * Validates that a string value matches a specified regex pattern.
 * 验证字符串值是否匹配指定的正则表达式模式。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.validation.validators

import com.arteam.arLibs.config.validation.ConfigValidator
import com.arteam.arLibs.config.validation.ValidationResult

/**
 * Validates that a string value matches a specified regex pattern.
 * 验证字符串值是否匹配指定的正则表达式模式。
 */
@Suppress("unused")
class RegexValidator(
    private val pattern: Regex,
    private val errorMessageFormat: String = "Value '{value}' at '{path}' does not match the required pattern"
) : ConfigValidator<String> {
    
    /**
     * Constructs a RegexValidator with a pattern string.
     * 使用模式字符串构造 RegexValidator。
     */
    constructor(patternString: String, errorMessageFormat: String = "Value '{value}' at '{path}' does not match the required pattern") : 
        this(patternString.toRegex(), errorMessageFormat)
    
    /**
     * Validates the given value.
     * 验证给定的值。
     */
    override fun validate(value: Any?, path: String): ValidationResult {
        value ?: return ValidationResult.failure("Value at '$path' cannot be null for RegexValidator")
        
        if (value !is String) {
            return ValidationResult.failure("Value at '$path' must be a String, but was ${value::class.simpleName}")
        }
        
        return if (pattern.matches(value)) ValidationResult.success()
               else ValidationResult.failure(errorMessageFormat.replace("{value}", value).replace("{path}", path))
    }

    /**
     * Companion object for creating predefined regex validators.
     * 用于创建预定义的正则表达式验证器的伴生对象。
     */
    companion object {
        /**
         * Email validator pattern.
         * 电子邮件验证器模式。
         */
        val EMAIL = RegexValidator("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", "Value '{value}' at '{path}' is not a valid email address")
        
        /**
         * URL validator pattern.
         * URL 验证器模式。
         */
        val URL = RegexValidator("^(https?|ftp)://[^\\s/$.?#].\\S*$", "Value '{value}' at '{path}' is not a valid URL")
        
        /**
         * IP address validator pattern.
         * IP 地址验证器模式。
         */
        val IP_ADDRESS = RegexValidator("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$", "Value '{value}' at '{path}' is not a valid IP address")
    }
} 