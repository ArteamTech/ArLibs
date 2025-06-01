/**
 * Action for executing commands as a player.
 * This action executes a command as if the player typed it.
 *
 * 用于以玩家身份执行命令的动作。
 * 此动作执行命令，就像玩家输入的一样。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.ArLibs
import com.arteam.arLibs.action.Action
import com.arteam.arLibs.utils.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.coroutines.resume

/**
 * Action that executes a command as the player.
 * 以玩家身份执行命令的动作。
 */
class CommandAction(private val command: String) : Action {
    
    override suspend fun execute(player: Player) {
        val formattedCommand = command.replace("%player%", player.name)
        
        try {
            // Execute command on the main thread using suspendCancellableCoroutine
            suspendCancellableCoroutine { continuation ->
                Bukkit.getScheduler().runTask(ArLibs.getInstance(), Runnable {
                    try {
                        player.performCommand(formattedCommand)
                        continuation.resume(Unit)
                    } catch (e: Exception) {
                        Logger.warn("Failed to execute command '$formattedCommand' as player ${player.name}: ${e.message}")
                        continuation.resume(Unit)
                    }
                })
            }
        } catch (e: Exception) {
            Logger.warn("Failed to schedule command '$formattedCommand' for player ${player.name}: ${e.message}")
        }
    }
    
    override fun getType(): String = "command"
    
    override fun toString(): String = "CommandAction(command='$command')"
} 