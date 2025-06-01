/**
 * Base interface for all actions that can be executed.
 * This interface defines the contract for action execution.
 *
 * 所有可执行动作的基础接口。
 * 此接口定义了动作执行的契约。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import org.bukkit.entity.Player

/**
 * Represents an action that can be executed for a player.
 * 表示可以为玩家执行的动作。
 */
interface Action {
    /**
     * Executes the action for the specified player.
     * 为指定玩家执行动作。
     *
     * @param player The player to execute the action for.
     *               要执行动作的玩家。
     */
    suspend fun execute(player: Player)
    
    /**
     * Gets the type of this action.
     * 获取此动作的类型。
     *
     * @return The action type.
     *         动作类型。
     */
    fun getType(): String
} 