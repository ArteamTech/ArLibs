/**
 * Context class that provides information about command execution.
 * This class encapsulates all the information available during command execution.
 * 
 * 提供命令执行信息的上下文类。
 * 此类封装了命令执行期间可用的所有信息。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Command execution context containing all relevant information.
 * 包含所有相关信息的命令执行上下文。
 *
 * @param sender The command sender (player, console, etc.).
 *               命令发送者（玩家、控制台等）。
 * @param plugin The plugin that registered this command.
 *               注册此命令的插件。
 * @param command The main command name that was executed.
 *                执行的主命令名称。
 * @param subCommand The subcommand name if applicable.
 *                   如果适用，子命令名称。
 * @param args The command arguments.
 *             命令参数。
 * @param rawArgs The raw command arguments as provided.
 *                提供的原始命令参数。
 * @param label The command label that was used to execute this command.
 *              用于执行此命令的命令标签。
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
    /**
     * Checks if the sender is a player.
     * 检查发送者是否为玩家。
     *
     * @return true if sender is a player, false otherwise.
     *         如果发送者是玩家则返回true，否则返回false。
     */
    fun isPlayer(): Boolean = sender is Player

    /**
     * Gets the sender as a player, throwing an exception if not a player.
     * 将发送者获取为玩家，如果不是玩家则抛出异常。
     *
     * @return The sender as a Player.
     *         作为Player的发送者。
     * @throws IllegalStateException if the sender is not a player.
     *                               如果发送者不是玩家。
     */
    fun getPlayer(): Player {
        if (!isPlayer()) {
            throw IllegalStateException("Command sender is not a player")
        }
        return sender as Player
    }

    /**
     * Gets the sender as a player, or null if not a player.
     * 将发送者获取为玩家，如果不是玩家则返回null。
     *
     * @return The sender as a Player, or null.
     *         作为Player的发送者，或null。
     */
    fun getPlayerOrNull(): Player? = sender as? Player

    /**
     * Checks if the sender is the console.
     * 检查发送者是否为控制台。
     *
     * @return true if sender is console, false otherwise.
     *         如果发送者是控制台则返回true，否则返回false。
     */
    fun isConsole(): Boolean = !isPlayer()

    /**
     * Gets an argument at the specified index.
     * 获取指定索引处的参数。
     *
     * @param index The index of the argument.
     *              参数的索引。
     * @return The argument at the specified index, or null if index is out of bounds.
     *         指定索引处的参数，如果索引超出范围则返回null。
     */
    fun getArg(index: Int): String? = args.getOrNull(index)

    /**
     * Gets an argument at the specified index with a default value.
     * 获取指定索引处的参数，如果不存在则返回默认值。
     *
     * @param index The index of the argument.
     *              参数的索引。
     * @param default The default value if argument doesn't exist.
     *                如果参数不存在时的默认值。
     * @return The argument at the specified index, or the default value.
     *         指定索引处的参数，或默认值。
     */
    fun getArgOrDefault(index: Int, default: String): String = args.getOrNull(index) ?: default

    /**
     * Gets all arguments from the specified index onwards as a single string.
     * 获取从指定索引开始的所有参数作为单个字符串。
     *
     * @param startIndex The starting index.
     *                   起始索引。
     * @param separator The separator to use between arguments.
     *                  参数之间使用的分隔符。
     * @return The joined arguments.
     *         连接后的参数。
     */
    fun getArgsFrom(startIndex: Int, separator: String = " "): String {
        return if (startIndex < args.size) {
            args.sliceArray(startIndex until args.size).joinToString(separator)
        } else {
            ""
        }
    }

    /**
     * Checks if the sender has the specified permission.
     * 检查发送者是否具有指定权限。
     *
     * @param permission The permission to check.
     *                   要检查的权限。
     * @return true if the sender has permission, false otherwise.
     *         如果发送者具有权限则返回true，否则返回false。
     */
    fun hasPermission(permission: String): Boolean = sender.hasPermission(permission)
    
    /**
     * Checks if this CommandContext is equal to another object.
     * 检查此CommandContext是否与另一个对象相等。
     *
     * @param other The object to compare with.
     *              要比较的对象。
     * @return true if this CommandContext is equal to the other object, false otherwise.
     *         如果此CommandContext与另一个对象相等，则返回true，否则返回false。
     */
    override fun equals(other: Any?): Boolean {
        // Check if the current object is the same as the other object.
        if (this === other) return true
        
        // Check if the classes of the objects are different.
        if (javaClass != other?.javaClass) return false

        // Cast the other object to CommandContext.
        other as CommandContext

        // Compare each field of the CommandContext to ensure they are equal.
        if (sender != other.sender) return false
        if (plugin != other.plugin) return false
        if (command != other.command) return false
        if (subCommand != other.subCommand) return false
        if (!args.contentEquals(other.args)) return false
        if (!rawArgs.contentEquals(other.rawArgs)) return false
        if (label != other.label) return false

        // If all fields are equal, return true.
        return true
    }

    /**
     * Generates a hash code for this CommandContext.
     * 为此CommandContext生成哈希码。
     *
     * @return The hash code of this CommandContext.
     *         此CommandContext的哈希码。
     */
    override fun hashCode(): Int {
        // Initialize the result with the hash code of the sender.
        var result = sender.hashCode()

        // Combine the hash codes of each field to generate a unique hash code.
        result = 31 * result + plugin.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + (subCommand?.hashCode() ?: 0)
        result = 31 * result + args.contentHashCode()
        result = 31 * result + rawArgs.contentHashCode()
        result = 31 * result + label.hashCode()

        // Return the final hash code.
        return result
    }
} 