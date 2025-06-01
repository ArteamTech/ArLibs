/**
 * Condition implementation for logical OR operations.
 * Returns true if any of the sub-conditions are satisfied.
 *
 * 逻辑或操作的条件实现。
 * 如果任何子条件满足则返回true。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition.conditions

import com.arteam.arLibs.condition.Condition
import org.bukkit.entity.Player

class AnyCondition(
    private val conditions: List<Condition>
) : Condition {
    
    override fun evaluate(player: Player): Boolean {
        return conditions.any { it.evaluate(player) }
    }
    
    override fun getDescription(): String {
        val descriptions = conditions.map { it.getDescription() }
        return "any [${descriptions.joinToString("; ")}]"
    }
    
    companion object {
        /**
         * Creates an AnyCondition with the given sub-conditions.
         * 使用给定的子条件创建AnyCondition。
         *
         * @param conditions The list of conditions to evaluate.
         *                   要评估的条件列表。
         * @return A new AnyCondition instance.
         *         新的AnyCondition实例。
         */
        fun of(vararg conditions: Condition): AnyCondition {
            return AnyCondition(conditions.toList())
        }
        
        /**
         * Creates an AnyCondition from a list of conditions.
         * 从条件列表创建AnyCondition。
         *
         * @param conditions The list of conditions to evaluate.
         *                   要评估的条件列表。
         * @return A new AnyCondition instance.
         *         新的AnyCondition实例。
         */
        fun of(conditions: List<Condition>): AnyCondition {
            return AnyCondition(conditions)
        }
    }
} 