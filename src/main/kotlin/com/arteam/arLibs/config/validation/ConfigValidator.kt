/**
 * Interface for configuration value validators.
 * Implement this interface to create custom validators for configuration values.
 * 
 * 配置值验证器的接口。
 * 实现此接口以创建配置值的自定义验证器。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.validation

/**
 * Interface for validating configuration values.
 * 用于验证配置值的接口。
 */
interface ConfigValidator<T> {
    /**
     * Validates the given value.
     * 验证给定的值。
     *
     * @param value The value to validate, can be of any type.
     *              要验证的值，可以是任何类型。
     * @param path The configuration path of the value
     *             值的配置路径
     * @return ValidationResult indicating whether validation passed and any error messages
     *         ValidationResult，指示验证是否通过以及任何错误消息
     */
    fun validate(value: Any?, path: String): ValidationResult
}

/**
 * Result of a validation operation.
 * 验证操作的结果。
 *
 * @param valid Whether the validation passed
 *              验证是否通过
 * @param errorMessage Optional error message if validation failed
 *                     如果验证失败，则为可选的错误消息
 */
data class ValidationResult(
    val valid: Boolean,
    val errorMessage: String = ""
) {
    companion object {
        /**
         * Creates a successful validation result.
         * 创建成功的验证结果。
         *
         * @return The successful validation result
         *         成功的验证结果
         */
        fun success(): ValidationResult = ValidationResult(true)
        
        /**
         * Creates a failed validation result with the given error message.
         * 创建具有给定错误消息的失败验证结果。
         *
         * @param message The error message
         *                错误消息
         * @return The failed validation result
         *         失败验证结果
         */
        fun failure(message: String): ValidationResult = ValidationResult(false, message)
    }
}