/**
 * Action for adding delays between actions.
 * This action pauses execution for a specified number of ticks.
 *
 * 用于在动作之间添加延迟的动作。
 * 此动作暂停执行指定的tick数。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.action.Action
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

/**
 * Action that adds a delay before the next action.
 * 在下一个动作之前添加延迟的动作。
 */
class DelayAction(private val ticks: Long) : Action {
    
    override suspend fun execute(player: Player) {
        // Convert ticks to milliseconds (1 tick = 50 ms)
        val milliseconds = ticks * 50L
        delay(milliseconds)
    }
    
    override fun getType(): String = "delay"
    
    override fun toString(): String = "DelayAction(ticks=$ticks)"
    
    companion object {
        /**
         * Creates a DelayAction from a tick count string.
         * 从tick计数字符串创建DelayAction。
         *
         * @param tickString The tick count as a string.
         *                   作为字符串的tick计数。
         * @return The DelayAction.
         *         DelayAction实例。
         */
        fun fromString(tickString: String): DelayAction {
            val ticks = tickString.toLongOrNull() ?: 0L
            return DelayAction(ticks)
        }
    }
} 