/**
 * Parser for condition expressions from configuration strings.
 * Supports parsing permission, placeholder, any, and all conditions.
 *
 * 从配置字符串解析条件表达式的解析器。
 * 支持解析权限、占位符、任意和全部条件。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition

import com.arteam.arLibs.condition.conditions.*
import com.arteam.arLibs.utils.Logger

@Suppress("unused")
object ConditionParser {
    
    /**
     * Normalizes whitespace in expressions for more flexible parsing.
     * Replaces multiple spaces with single spaces and trims.
     *
     * 标准化表达式中的空格以实现更灵活的解析。
     * 将多个空格替换为单个空格并修剪。
     *
     * @param input The input string to normalize.
     *              要标准化的输入字符串。
     * @return The normalized string.
     *         标准化后的字符串。
     */
    private fun normalizeWhitespace(input: String): String {
        return input.trim().replace(Regex("\\s+"), " ")
    }
    
    /**
     * Parses a condition from a string expression.
     * Supports various formats including permission, placeholder, any, and all conditions.
     *
     * 从字符串表达式解析条件。
     * 支持包括权限、占位符、任意和全部条件在内的各种格式。
     *
     * @param expression The condition expression to parse.
     *                   要解析的条件表达式。
     * @return The parsed Condition, or null if parsing fails.
     *         解析的条件，如果解析失败则返回null。
     */
    fun parse(expression: String): Condition? {
        val trimmed = normalizeWhitespace(expression)
        
        return try {
            when {
                // Permission conditions
                trimmed.startsWith("permission ") || trimmed.startsWith("perm ") -> {
                    val permissionPart = if (trimmed.startsWith("permission ")) {
                        normalizeWhitespace(trimmed.substring(11))
                    } else {
                        normalizeWhitespace(trimmed.substring(5))
                    }
                    PermissionCondition.fromExpression(permissionPart)
                }
                
                // Placeholder conditions
                trimmed.startsWith("placeholder ") || trimmed.startsWith("papi ") -> {
                    val placeholderPart = if (trimmed.startsWith("placeholder ")) {
                        normalizeWhitespace(trimmed.substring(12))
                    } else {
                        normalizeWhitespace(trimmed.substring(5))
                    }
                    PlaceholderCondition.fromExpression(placeholderPart)
                }
                
                // Any conditions (OR logic) - more flexible parsing
                trimmed.startsWith("any") && trimmed.contains("[") && trimmed.endsWith("]") -> {
                    val startIndex = trimmed.indexOf('[')
                    val content = trimmed.substring(startIndex + 1, trimmed.length - 1)
                    val subConditions = parseConditionList(content)
                    if (subConditions.isNotEmpty()) {
                        AnyCondition.of(subConditions)
                    } else {
                        null
                    }
                }
                
                // All conditions (AND logic) - more flexible parsing
                trimmed.startsWith("all") && trimmed.contains("[") && trimmed.endsWith("]") -> {
                    val startIndex = trimmed.indexOf('[')
                    val content = trimmed.substring(startIndex + 1, trimmed.length - 1)
                    val subConditions = parseConditionList(content)
                    if (subConditions.isNotEmpty()) {
                        AllCondition.of(subConditions)
                    } else {
                        null
                    }
                }
                
                // Not conditions (NOT logic) - flexible format without brackets
                trimmed.startsWith("not ") -> {
                    val content = normalizeWhitespace(trimmed.substring(4))
                    val subCondition = parseSubCondition(content)
                    if (subCondition != null) {
                        NotCondition.of(subCondition)
                    } else {
                        null
                    }
                }
                
                // Check if it's a placeholder without prefix (for backward compatibility)
                trimmed.startsWith("%") && trimmed.endsWith("%") -> {
                    PlaceholderCondition.fromExpression(trimmed)
                }
                
                // Check if it's a placeholder with comparison operator
                trimmed.startsWith("%") && trimmed.contains("%") -> {
                    PlaceholderCondition.fromExpression(trimmed)
                }
                
                // Default: try to parse as permission if it doesn't contain special characters
                !trimmed.contains("[") && !trimmed.contains("]") -> {
                    PermissionCondition.fromExpression(trimmed)
                }
                
                else -> {
                    Logger.warn("Unknown condition format: $expression")
                    null
                }
            }
        } catch (e: Exception) {
            Logger.warn("Failed to parse condition: $expression - ${e.message}")
            null
        }
    }
    
    /**
     * Parses a list of conditions separated by semicolons.
     * Used for parsing sub-conditions in any/all expressions.
     * More tolerant of whitespace and formatting.
     *
     * 解析由分号分隔的条件列表。
     * 用于解析any/all表达式中的子条件。
     * 对空格和格式更宽容。
     *
     * @param content The content containing multiple conditions.
     *                包含多个条件的内容。
     * @return List of parsed conditions.
     *         解析的条件列表。
     */
    private fun parseConditionList(content: String): List<Condition> {
        val normalizedContent = normalizeWhitespace(content)
        if (normalizedContent.isBlank()) return emptyList()
        
        val conditions = mutableListOf<Condition>()
        
        // Split by semicolon and handle each part
        val parts = normalizedContent.split(";")
        
        for (part in parts) {
            val trimmedPart = normalizeWhitespace(part)
            if (trimmedPart.isEmpty()) continue
            
            // Try to parse the sub-condition
            val condition = parseSubCondition(trimmedPart)
            if (condition != null) {
                conditions.add(condition)
            } else {
                Logger.warn("Failed to parse sub-condition: $trimmedPart")
            }
        }
        
        return conditions
    }
    
    /**
     * Parses a sub-condition with more flexible rules.
     * Handles conditions that might not have explicit prefixes.
     *
     * 使用更灵活的规则解析子条件。
     * 处理可能没有明确前缀的条件。
     *
     * @param expression The sub-condition expression.
     *                   子条件表达式。
     * @return The parsed condition or null if parsing fails.
     *         解析的条件，如果解析失败则返回null。
     */
    private fun parseSubCondition(expression: String): Condition? {
        val trimmed = normalizeWhitespace(expression)
        
        return when {
            // Explicit permission conditions
            trimmed.startsWith("permission ") || trimmed.startsWith("perm ") -> {
                val permissionPart = if (trimmed.startsWith("permission ")) {
                    normalizeWhitespace(trimmed.substring(11))
                } else {
                    normalizeWhitespace(trimmed.substring(5))
                }
                PermissionCondition.fromExpression(permissionPart)
            }
            
            // Explicit placeholder conditions
            trimmed.startsWith("placeholder ") || trimmed.startsWith("papi ") -> {
                val placeholderPart = if (trimmed.startsWith("placeholder ")) {
                    normalizeWhitespace(trimmed.substring(12))
                } else {
                    normalizeWhitespace(trimmed.substring(5))
                }
                PlaceholderCondition.fromExpression(placeholderPart)
            }
            
            // Placeholder conditions without prefix (starts with %)
            trimmed.startsWith("%") -> {
                PlaceholderCondition.fromExpression(trimmed)
            }
            
            // Nested any/all conditions
            (trimmed.startsWith("any") || trimmed.startsWith("all")) && trimmed.contains("[") -> {
                parse(trimmed) // Recursively parse nested conditions
            }
            
            // Not conditions without brackets
            trimmed.startsWith("not ") -> {
                parse(trimmed) // Recursively parse nested conditions
            }
            
            // Default: treat as permission
            else -> {
                PermissionCondition.fromExpression(trimmed)
            }
        }
    }
    
    /**
     * Parses multiple conditions from a list of strings.
     * Useful for parsing conditions from configuration lists.
     *
     * 从字符串列表解析多个条件。
     * 用于从配置列表解析条件。
     *
     * @param expressions List of condition expressions.
     *                    条件表达式列表。
     * @return List of parsed conditions.
     *         解析的条件列表。
     */
    fun parseMultiple(expressions: List<String>): List<Condition> {
        return expressions.mapNotNull { parse(it) }
    }
    
    /**
     * Validates if a condition expression is syntactically correct.
     * Does not evaluate the condition, only checks syntax.
     *
     * 验证条件表达式在语法上是否正确。
     * 不评估条件，只检查语法。
     *
     * @param expression The condition expression to validate.
     *                   要验证的条件表达式。
     * @return True if the expression is valid, false otherwise.
     *         如果表达式有效则返回true，否则返回false。
     */
    fun isValid(expression: String): Boolean {
        return parse(expression) != null
    }
} 