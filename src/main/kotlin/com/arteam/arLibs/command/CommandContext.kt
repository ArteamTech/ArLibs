/**
 * Context class that provides information about command execution.
 * 提供命令执行信息的上下文类。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Command execution context containing all relevant information.
 * 包含所有相关信息的命令执行上下文。
 */
@Suppress("unused")
data class CommandContext(
    val sender: CommandSender,
    val plugin: Plugin,
    val command: String,
    val subCommand: String? = null,
    val args: Array<String>,
    val rawArgs: Array<String>,
    val label: String
) {
    
    // Basic sender checks
    fun isPlayer(): Boolean = sender is Player
    fun isConsole(): Boolean = !isPlayer()
    
    // Player retrieval
    fun getPlayer(): Player = sender as? Player ?: throw IllegalStateException("Command sender is not a player")
    fun getPlayerOrNull(): Player? = sender as? Player

    // Argument retrieval
    fun getArg(index: Int): String? = args.getOrNull(index)
    fun getArgOrDefault(index: Int, default: String): String = getArg(index) ?: default
    fun getArgsFrom(startIndex: Int, separator: String = " "): String = 
        args.drop(startIndex).joinToString(separator)

    // Typed argument getters
    fun getArgAsInt(index: Int): Int? = getArg(index)?.toIntOrNull()
    fun getArgAsDouble(index: Int): Double? = getArg(index)?.toDoubleOrNull()
    fun getArgAsBoolean(index: Int): Boolean? = when (getArg(index)?.lowercase()) {
        "true", "yes", "on" -> true
        "false", "no", "off" -> false
        else -> null
    }
    fun getArgAsPlayer(index: Int): Player? = getArg(index)?.let { Bukkit.getPlayerExact(it) }

    // Permission check
    fun hasPermission(permission: String): Boolean = sender.hasPermission(permission)
    
    // Data class overrides for arrays
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CommandContext
        return sender == other.sender &&
               plugin == other.plugin &&
               command == other.command &&
               subCommand == other.subCommand &&
               args.contentEquals(other.args) &&
               rawArgs.contentEquals(other.rawArgs) &&
               label == other.label
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + plugin.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + (subCommand?.hashCode() ?: 0)
        result = 31 * result + args.contentHashCode()
        result = 31 * result + rawArgs.contentHashCode()
        result = 31 * result + label.hashCode()
        return result
    }
} 