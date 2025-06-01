/**
 * Condition implementation for logical NOT operations.
 * Returns true if the sub-condition is NOT satisfied.
 *
 * 逻辑非操作的条件实现。
 * 如果子条件不满足则返回true。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition.conditions

import com.arteam.arLibs.condition.Condition
import org.bukkit.entity.Player

/**
 * Condition implementation for logical NOT operations.
 * Returns true if the sub-condition is NOT satisfied.
 *
 * 逻辑非操作的条件实现。
 * 如果子条件不满足则返回true。
 */
class NotCondition(
    private val condition: Condition
) : Condition {
    
    override fun evaluate(player: Player): Boolean {
        return !condition.evaluate(player)
    }
    
    override fun getDescription(): String {
        return "not ${condition.getDescription()}"
    }
    
    companion object {
        /**
         * Creates a NotCondition with the given sub-condition.
         * 使用给定的子条件创建NotCondition。
         *
         * @param condition The condition to negate.
         *                  要否定的条件。
         * @return A new NotCondition instance.
         *         新的NotCondition实例。
         */
        fun of(condition: Condition): NotCondition {
            return NotCondition(condition)
        }
    }
} 