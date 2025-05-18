/**
 * Validates that a string value matches a specified regex pattern.
 *
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
 *
 * @param pattern The regex pattern to match against
 *                要匹配的正则表达式模式
 * @param errorMessageFormat Custom error message format. Use {value} and {path} placeholders.
 *                           自定义错误消息格式。使用 {value} 和 {path} 占位符。
 */
@Suppress("unused")
class RegexValidator(
    private val pattern: Regex,
    private val errorMessageFormat: String = "Value '{value}' at '{path}' does not match the required pattern"
) : ConfigValidator<String> {
    
    /**
     * Constructs a RegexValidator with a pattern string.
     * 使用模式字符串构造 RegexValidator。
     *
     * @param patternString The regex pattern string
     *                      正则表达式模式字符串
     */
    constructor(patternString: String, errorMessageFormat: String = "Value '{value}' at '{path}' does not match the required pattern") : 
        this(patternString.toRegex(), errorMessageFormat)
    
    override fun validate(value: String, path: String): ValidationResult {
        return if (pattern.matches(value)) {
            ValidationResult.success()
        } else {
            val errorMessage = errorMessageFormat
                .replace("{value}", value)
                .replace("{path}", path)
            ValidationResult.failure(errorMessage)
        }
    }
    
    companion object {
        /**
         * Email validator pattern.
         * 电子邮件验证器模式。
         */
        val EMAIL = RegexValidator(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex(),
            "Value '{value}' at '{path}' is not a valid email address"
        )
        
        /**
         * URL validator pattern.
         * URL 验证器模式。
         */
        val URL = RegexValidator(
            "^(https?|ftp)://[^\\s/$.?#].\\S*$".toRegex(),
            "Value '{value}' at '{path}' is not a valid URL"
        )
        
        /**
         * IP address validator pattern.
         * IP 地址验证器模式。
         */
        val IP_ADDRESS = RegexValidator(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$".toRegex(),
            "Value '{value}' at '{path}' is not a valid IP address"
        )
    }
} 