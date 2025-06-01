/**
 * Base interface for all condition types.
 * Provides a common contract for evaluating conditions against players.
 *
 * 所有条件类型的基础接口。
 * 为针对玩家评估条件提供通用契约。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition

import org.bukkit.entity.Player

/**
 * Base interface for all condition types.
 * Provides a common contract for evaluating conditions against players.
 *
 * 所有条件类型的基础接口。
 * 为针对玩家评估条件提供通用契约。
 */
interface Condition {
    /**
     * Evaluates the condition for the given player.
     * Returns true if the player meets the condition, false otherwise.
     *
     * 为给定玩家评估条件。
     * 如果玩家满足条件则返回true，否则返回false。
     *
     * @param player The player to evaluate the condition for.
     *               要评估条件的玩家。
     * @return True if the condition is met, false otherwise.
     *         如果满足条件则返回true，否则返回false。
     */
    fun evaluate(player: Player): Boolean
    
    /**
     * Gets a human-readable description of this condition.
     * Useful for debugging and logging purposes.
     *
     * 获取此条件的人类可读描述。
     * 用于调试和日志记录目的。
     *
     * @return A description of the condition.
     *         条件的描述。
     */
    fun getDescription(): String
} 