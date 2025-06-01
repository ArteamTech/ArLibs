/**
 * Manager class for handling condition evaluation and caching.
 * Provides convenient methods for evaluating conditions and managing condition sets.
 *
 * 用于处理条件评估和缓存的管理器类。
 * 提供评估条件和管理条件集的便捷方法。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition

import com.arteam.arLibs.utils.Logger
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object ConditionManager {
    
    // Cache for parsed conditions to avoid reparsing
    private val conditionCache = ConcurrentHashMap<String, Condition?>()
    
    /**
     * Evaluates a condition expression for a player.
     * Uses caching to improve performance for repeated evaluations.
     *
     * 为玩家评估条件表达式。
     * 使用缓存来提高重复评估的性能。
     *
     * @param player The player to evaluate the condition for.
     *               要评估条件的玩家。
     * @param expression The condition expression.
     *                   条件表达式。
     * @return True if the condition is met, false otherwise.
     *         如果满足条件则返回true，否则返回false。
     */
    fun evaluate(player: Player, expression: String): Boolean {
        val condition = getOrParseCondition(expression) ?: return false
        return condition.evaluate(player)
    }
    
    /**
     * Evaluates multiple condition expressions for a player.
     * Returns true only if all conditions are satisfied.
     *
     * 为玩家评估多个条件表达式。
     * 只有当所有条件都满足时才返回true。
     *
     * @param player The player to evaluate the conditions for.
     *               要评估条件的玩家。
     * @param expressions List of condition expressions.
     *                    条件表达式列表。
     * @return True if all conditions are met, false otherwise.
     *         如果所有条件都满足则返回true，否则返回false。
     */
    fun evaluateAll(player: Player, expressions: List<String>): Boolean {
        return expressions.all { evaluate(player, it) }
    }
    
    /**
     * Evaluates multiple condition expressions for a player.
     * Returns true if any condition is satisfied.
     *
     * 为玩家评估多个条件表达式。
     * 如果任何条件满足则返回true。
     *
     * @param player The player to evaluate the conditions for.
     *               要评估条件的玩家。
     * @param expressions List of condition expressions.
     *                    条件表达式列表。
     * @return True if any condition is met, false otherwise.
     *         如果任何条件满足则返回true，否则返回false。
     */
    fun evaluateAny(player: Player, expressions: List<String>): Boolean {
        return expressions.any { evaluate(player, it) }
    }
    
    /**
     * Gets a condition from cache or parses it if not cached.
     * 从缓存获取条件或在未缓存时解析它。
     *
     * @param expression The condition expression.
     *                   条件表达式。
     * @return The parsed condition, or null if parsing fails.
     *         解析的条件，如果解析失败则返回null。
     */
    private fun getOrParseCondition(expression: String): Condition? {
        return conditionCache.computeIfAbsent(expression) { expr ->
            ConditionParser.parse(expr)
        }
    }
    
    /**
     * Validates a condition expression without evaluating it.
     * 验证条件表达式而不评估它。
     *
     * @param expression The condition expression to validate.
     *                   要验证的条件表达式。
     * @return True if the expression is valid, false otherwise.
     *         如果表达式有效则返回true，否则返回false。
     */
    fun isValidExpression(expression: String): Boolean {
        return getOrParseCondition(expression) != null
    }
    
    /**
     * Gets a description of a condition expression.
     * Useful for debugging and displaying condition information.
     *
     * 获取条件表达式的描述。
     * 用于调试和显示条件信息。
     *
     * @param expression The condition expression.
     *                   条件表达式。
     * @return The condition description, or null if parsing fails.
     *         条件描述，如果解析失败则返回null。
     */
    fun getConditionDescription(expression: String): String? {
        return getOrParseCondition(expression)?.getDescription()
    }
    
    /**
     * Clears the condition cache.
     * Useful for reloading configurations or freeing memory.
     *
     * 清除条件缓存。
     * 用于重新加载配置或释放内存。
     */
    fun clearCache() {
        conditionCache.clear()
        Logger.debug("Condition cache cleared")
    }
    
    /**
     * Gets the current cache size.
     * Useful for monitoring and debugging.
     *
     * 获取当前缓存大小。
     * 用于监控和调试。
     *
     * @return The number of cached conditions.
     *         缓存条件的数量。
     */
    fun getCacheSize(): Int {
        return conditionCache.size
    }
} 