/**
 * Validates that a string or collection is not empty.
 * 验证字符串或集合是否非空。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.validation.validators

import com.arteam.arLibs.config.validation.ConfigValidator
import com.arteam.arLibs.config.validation.ValidationResult

/**
 * Validates that a string is not empty or blank.
 * 验证字符串是否非空或非空白。
 */
class NotEmptyStringValidator : ConfigValidator<String> {
    /**
     * Validates the given value.
     * 验证给定的值。
     */
    override fun validate(value: Any?, path: String): ValidationResult {
        value ?: return ValidationResult.failure("Value at '$path' cannot be null for NotEmptyStringValidator")
        
        if (value !is String) {
            return ValidationResult.failure("Value at '$path' must be a String, but was ${value::class.simpleName}")
        }
        
        return if (value.isBlank()) ValidationResult.failure("Value at '$path' cannot be empty or blank") 
               else ValidationResult.success()
    }
    
    /**
     * Companion object for creating predefined not empty validators.
     * 用于创建预定义的非空验证器的伴生对象。
     */
    companion object {
        val INSTANCE = NotEmptyStringValidator()
    }
}

/**
 * Validates that a collection is not empty.
 * 验证集合是否非空。
 */
@Suppress("unused")
class NotEmptyCollectionValidator<T : Collection<*>> : ConfigValidator<T> {
    /**
     * Validates the given value.
     * 验证给定的值。
     */
    override fun validate(value: Any?, path: String): ValidationResult {
        value ?: return ValidationResult.failure("Value at '$path' cannot be null for NotEmptyCollectionValidator")
        
        if (value !is Collection<*>) {
            return ValidationResult.failure("Value at '$path' must be a Collection, but was ${value::class.simpleName}")
        }
        
        return if (value.isEmpty()) ValidationResult.failure("Collection at '$path' cannot be empty") 
               else ValidationResult.success()
    }

    /**
     * Companion object for creating predefined not empty validators.
     * 用于创建预定义的非空验证器的伴生对象。
     */
    companion object {
        val INSTANCE = NotEmptyCollectionValidator<Collection<*>>()
    }
}

/**
 * Factory methods for not-empty validators.
 * 非空验证器的工厂方法。
 */
@Suppress("unused")
object NotEmptyValidators {
    /**
     * Returns a validator for non-empty strings.
     * 返回非空字符串的验证器。
     */
    fun string(): ConfigValidator<String> = NotEmptyStringValidator.INSTANCE
    
    /**
     * Returns a validator for non-empty collections.
     * 返回非空集合的验证器。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Collection<*>> collection(): ConfigValidator<T> = NotEmptyCollectionValidator.INSTANCE as ConfigValidator<T>
} 