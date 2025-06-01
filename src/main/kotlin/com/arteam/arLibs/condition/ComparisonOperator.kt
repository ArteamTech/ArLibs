/**
 * Enumeration of comparison operators for arithmetic expressions.
 * Used to compare numeric and string values in conditions.
 *
 * 算术表达式比较操作符的枚举。
 * 用于在条件中比较数值和字符串值。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition

enum class ComparisonOperator(val symbol: String) {
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    EQUAL("=="),
    NOT_EQUAL("!=");
    
    companion object {
        /**
         * Parses a comparison operator from a string symbol.
         * 从字符串符号解析比较操作符。
         *
         * @param symbol The operator symbol.
         *               操作符符号。
         * @return The corresponding ComparisonOperator, or null if not found.
         *         对应的ComparisonOperator，如果未找到则返回null。
         */
        fun fromSymbol(symbol: String): ComparisonOperator? {
            return entries.find { it.symbol == symbol }
        }
        
        /**
         * Gets all operator symbols sorted by length (longest first).
         * This ensures proper parsing of multi-character operators.
         *
         * 获取按长度排序的所有操作符符号（最长的在前）。
         * 这确保了多字符操作符的正确解析。
         *
         * @return List of operator symbols.
         *         操作符符号列表。
         */
        fun getAllSymbols(): List<String> {
            return entries.map { it.symbol }.sortedByDescending { it.length }
        }
    }
    
    /**
     * Compares two values using this operator.
     * Attempts' numeric comparison first, falls back to string comparison.
     *
     * 使用此操作符比较两个值。
     * 首先尝试数值比较，回退到字符串比较。
     *
     * @param left The left operand.
     *             左操作数。
     * @param right The right operand.
     *              右操作数。
     * @return True if the comparison is satisfied, false otherwise.
     *         如果比较满足则返回true，否则返回false。
     */
    fun compare(left: String, right: String): Boolean {
        // Try numeric comparison first
        val leftNum = left.toDoubleOrNull()
        val rightNum = right.toDoubleOrNull()
        
        if (leftNum != null && rightNum != null) {
            return when (this) {
                GREATER_THAN -> leftNum > rightNum
                GREATER_THAN_OR_EQUAL -> leftNum >= rightNum
                LESS_THAN -> leftNum < rightNum
                LESS_THAN_OR_EQUAL -> leftNum <= rightNum
                EQUAL -> leftNum == rightNum
                NOT_EQUAL -> leftNum != rightNum
            }
        }
        
        // Fall back to string comparison
        return when (this) {
            GREATER_THAN -> left > right
            GREATER_THAN_OR_EQUAL -> left >= right
            LESS_THAN -> left < right
            LESS_THAN_OR_EQUAL -> left <= right
            EQUAL -> left == right
            NOT_EQUAL -> left != right
        }
    }
} 