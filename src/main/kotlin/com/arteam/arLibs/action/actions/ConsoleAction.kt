/**
 * Action for executing commands as console.
 * This action executes a command from the console with full permissions.
 *
 * 用于以控制台身份执行命令的动作。
 * 此动作以完整权限从控制台执行命令。
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
 * Action that executes a command as console.
 * 以控制台身份执行命令的动作。
 */
class ConsoleAction(private val command: String) : Action {
    
    override suspend fun execute(player: Player) {
        val formattedCommand = command.replace("%player%", player.name)
        
        try {
            // Execute command on the main thread using suspendCancellableCoroutine
            suspendCancellableCoroutine { continuation ->
                Bukkit.getScheduler().runTask(ArLibs.getInstance(), Runnable {
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand)
                        continuation.resume(Unit)
                    } catch (e: Exception) {
                        Logger.warn("Failed to execute console command '$formattedCommand' for player ${player.name}: ${e.message}")
                        continuation.resume(Unit)
                    }
                })
            }
        } catch (e: Exception) {
            Logger.warn("Failed to schedule console command '$formattedCommand' for player ${player.name}: ${e.message}")
        }
    }
    
    override fun getType(): String = "console"
    
    override fun toString(): String = "ConsoleAction(command='$command')"
} 