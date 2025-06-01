/**
 * Condition implementation for logical AND operations.
 * Returns true only if all sub-conditions are satisfied.
 *
 * 逻辑与操作的条件实现。
 * 只有当所有子条件都满足时才返回true。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition.conditions

import com.arteam.arLibs.condition.Condition
import org.bukkit.entity.Player

/**
 * Condition implementation for logical AND operations.
 * Only returns true if all sub-conditions are satisfied.
 *
 * 逻辑与操作的条件实现。
 * 只有当所有子条件都满足时才返回true。
 */
class AllCondition(
    private val conditions: List<Condition>
) : Condition {
    
    override fun evaluate(player: Player): Boolean {
        return conditions.all { it.evaluate(player) }
    }
    
    override fun getDescription(): String {
        val descriptions = conditions.map { it.getDescription() }
        return "all [${descriptions.joinToString("; ")}]"
    }
    
    companion object {
        /**
         * Creates an AllCondition with the given sub-conditions.
         * 使用给定的子条件创建AllCondition。
         *
         * @param conditions The list of conditions to evaluate.
         *                   要评估的条件列表。
         * @return A new AllCondition instance.
         *         新的AllCondition实例。
         */
        fun of(vararg conditions: Condition): AllCondition {
            return AllCondition(conditions.toList())
        }
        
        /**
         * Creates an AllCondition from a list of conditions.
         * 从条件列表创建AllCondition。
         *
         * @param conditions The list of conditions to evaluate.
         *                   要评估的条件列表。
         * @return A new AllCondition instance.
         *         新的AllCondition实例。
         */
        fun of(conditions: List<Condition>): AllCondition {
            return AllCondition(conditions)
        }
    }
} 