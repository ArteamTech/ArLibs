/**
 * Condition implementation for checking PlaceholderAPI placeholder values.
 * Supports arithmetic comparisons and string equality checks.
 *
 * 用于检查PlaceholderAPI占位符值的条件实现。
 * 支持算术比较和字符串相等检查。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition.conditions

import com.arteam.arLibs.condition.Condition
import com.arteam.arLibs.condition.ComparisonOperator
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.text.Regex

/**
 * Condition implementation for checking PlaceholderAPI placeholder values.
 * Supports arithmetic comparisons and string equality checks.
 *
 * 用于检查PlaceholderAPI占位符值的条件实现。
 * 支持算术比较和字符串相等检查。
 */
class PlaceholderCondition(
    private val placeholder: String,
    private val operator: ComparisonOperator? = null,
    private val expectedValue: String? = null
) : Condition {
    
    override fun evaluate(player: Player): Boolean {
        // Check if PlaceholderAPI is available
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return false
        }
        
        val actualValue = PlaceholderAPI.setPlaceholders(player, placeholder)
        
        // If no operator is specified, check if the placeholder exists and is not empty
        if (operator == null || expectedValue == null) {
            return actualValue.isNotEmpty() && actualValue != placeholder
        }
        
        return operator.compare(actualValue, expectedValue)
    }
    
    override fun getDescription(): String {
        return if (operator != null && expectedValue != null) {
            "placeholder: $placeholder ${operator.symbol} $expectedValue"
        } else {
            "placeholder exists: $placeholder"
        }
    }
    
    companion object {
        /**
         * Creates a PlaceholderCondition from a string expression.
         * Supports formats like "%player_name%" or "%player_level% >= 10".
         *
         * 从字符串表达式创建PlaceholderCondition。
         * 支持"%player_name%"或"%player_level% >= 10"等格式。
         *
         * @param expression The placeholder expression.
         *                   占位符表达式。
         * @return A new PlaceholderCondition instance.
         *         新的PlaceholderCondition实例。
         */
        fun fromExpression(expression: String): PlaceholderCondition {
            val trimmed = expression.trim().replace(Regex("\\s+"), " ")
            
            // Find the operator in the expression
            val operators = ComparisonOperator.getAllSymbols()
            for (operatorSymbol in operators) {
                // Look for operator with flexible spacing
                val pattern = "\\s*${Regex.escape(operatorSymbol)}\\s*"
                val regex = Regex(pattern)
                val match = regex.find(trimmed)
                
                if (match != null) {
                    val placeholder = trimmed.substring(0, match.range.first).trim()
                    val value = trimmed.substring(match.range.last + 1).trim()
                    val operator = ComparisonOperator.fromSymbol(operatorSymbol)
                    
                    return PlaceholderCondition(placeholder, operator, value)
                }
            }
            
            // No operator found, just check if placeholder exists
            return PlaceholderCondition(trimmed)
        }
    }
} 