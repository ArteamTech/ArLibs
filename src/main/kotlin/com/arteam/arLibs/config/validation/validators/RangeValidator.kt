/**
 * Validates that a numeric value is within a specified range.
 * 验证数值是否在指定范围内。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.validation.validators

import com.arteam.arLibs.config.validation.ConfigValidator
import com.arteam.arLibs.config.validation.ValidationResult

/**
 * Validates that a numeric value is within a specified range.
 * 验证数值是否在指定范围内。
 */
@Suppress("unused")
class RangeValidator<T : Comparable<T>>(
    private val min: T,
    private val max: T
) : ConfigValidator<T> {
    
    override fun validate(value: Any?, path: String): ValidationResult {
        value ?: return ValidationResult.failure("Value at '$path' cannot be null for RangeValidator")
        
        if (!min::class.java.isInstance(value)) {
            return ValidationResult.failure("Value '$value' at '$path' has type ${value::class.simpleName}, but expected ${min::class.simpleName}")
        }

        @Suppress("UNCHECKED_CAST")
        val comparableValue = value as T

        return when {
            comparableValue < min -> ValidationResult.failure("Value '$comparableValue' at '$path' is less than minimum: $min")
            comparableValue > max -> ValidationResult.failure("Value '$comparableValue' at '$path' is greater than maximum: $max")
            else -> ValidationResult.success()
        }
    }
    
    companion object {
        /**
         * Creates a new RangeValidator for integers.
         * 为整数创建新的 RangeValidator。
         */
        fun forInt(min: Int, max: Int): RangeValidator<Int> = RangeValidator(min, max)
        
        /**
         * Creates a new RangeValidator for doubles.
         * 为双精度浮点数创建新的 RangeValidator。
         */
        fun forDouble(min: Double, max: Double): RangeValidator<Double> = RangeValidator(min, max)
        
        /**
         * Creates a new RangeValidator for longs.
         * 为长整数创建新的 RangeValidator。
         */
        fun forLong(min: Long, max: Long): RangeValidator<Long> = RangeValidator(min, max)
    }
} 