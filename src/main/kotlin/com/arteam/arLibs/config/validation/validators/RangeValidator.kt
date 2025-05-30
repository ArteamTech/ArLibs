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
 *
 * @param min The minimum allowed value (inclusive)
 *            最小允许值（包含）
 * @param max The maximum allowed value (inclusive)
 *            最大允许值（包含）
 */
@Suppress("unused")
class RangeValidator<T : Comparable<T>>(
    private val min: T,
    private val max: T
) : ConfigValidator<T> {
    
    override fun validate(value: Any?, path: String): ValidationResult {
        if (value == null) {
            return ValidationResult.failure("Value at '$path' cannot be null for RangeValidator")
        }
        
        // Try to cast to T, assuming T is the correct Comparable type for min/max
        val comparableValue: T = try {
            @Suppress("UNCHECKED_CAST")
            value as T
        } catch (e: ClassCastException) {
            // A more specific check might be needed if T could be e.g. Int but value is Long
            // For now, direct cast and rely on type compatibility with min/max
            return ValidationResult.failure("Value '$value' at '$path' has an incompatible type (${value::class.simpleName}) for this RangeValidator (expected type compatible with ${min::class.simpleName})")
        }

        return if (comparableValue < min) {
            ValidationResult.failure("Value '$comparableValue' at '$path' is less than the minimum allowed value: $min")
        } else if (comparableValue > max) {
            ValidationResult.failure("Value '$comparableValue' at '$path' is greater than the maximum allowed value: $max")
        } else {
            ValidationResult.success()
        }
    }
    
    companion object {
        /**
         * Creates a new RangeValidator for integers.
         * 为整数创建新的 RangeValidator。
         *
         * @param min The minimum allowed value (inclusive)
         *            最小允许值（包含）
         * @param max The maximum allowed value (inclusive)
         *            最大允许值（包含）
         * @return The new RangeValidator
         *         新的 RangeValidator
         */
        fun forInt(min: Int, max: Int): RangeValidator<Int> = RangeValidator(min, max)
        
        /**
         * Creates a new RangeValidator for doubles.
         * 为双精度浮点数创建新的 RangeValidator。
         *
         * @param min The minimum allowed value (inclusive)
         *            最小允许值（包含）
         * @param max The maximum allowed value (inclusive)
         *            最大允许值（包含）
         * @return The new RangeValidator
         *         新的 RangeValidator
         */
        fun forDouble(min: Double, max: Double): RangeValidator<Double> = RangeValidator(min, max)
        
        /**
         * Creates a new RangeValidator for longs.
         * 为长整数创建新的 RangeValidator。
         *
         * @param min The minimum allowed value (inclusive)
         *            最小允许值（包含）
         * @param max The maximum allowed value (inclusive)
         *            最大允许值（包含）
         * @return The new RangeValidator
         *         新的 RangeValidator
         */
        fun forLong(min: Long, max: Long): RangeValidator<Long> = RangeValidator(min, max)
    }
} 