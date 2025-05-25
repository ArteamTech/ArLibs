/**
 * Public API for the ArLibs command system.
 * This class provides a simple interface for other plugins to register their commands.
 * 
 * ArLibs命令系统的公共API。
 * 此类为其他插件提供简单的接口来注册其命令。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

/**
 * Main API class for registering commands with the ArLibs command system.
 * 用于向ArLibs命令系统注册命令的主要API类。
 */
@Suppress("unused")
object CommandAPI {
    /**
     * Registers a single command class with the command system.
     * 向命令系统注册单个命令类。
     *
     * @param plugin The plugin registering the command.
     *               注册命令的插件。
     * @param commandClass The command class to register.
     *                     要注册的命令类。
     * @return true if registration was successful, false otherwise.
     *         如果注册成功则返回true，否则返回false。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * // In your plugin's onEnable method / 在你的插件的onEnable方法中
     * CommandAPI.registerCommand(this, MyCommand::class)
     * ```
     */
    fun registerCommand(plugin: Plugin, commandClass: KClass<out BaseCommand>): Boolean {
        return CommandManager.registerCommand(plugin, commandClass)
    }
    
    /**
     * Registers multiple command classes at once.
     * 一次注册多个命令类。
     *
     * @param plugin The plugin registering the commands.
     *               注册命令的插件。
     * @param commandClasses The command classes to register.
     *                       要注册的命令类。
     * @return The number of successfully registered commands.
     *         成功注册的命令数量。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * // Register multiple commands at once / 一次注册多个命令
     * val registered = CommandAPI.registerCommands(this, 
     *     MyCommand::class, 
     *     AnotherCommand::class, 
     *     ThirdCommand::class
     * )
     * logger.info("Registered $registered commands")
     * ```
     */
    fun registerCommands(plugin: Plugin, vararg commandClasses: KClass<out BaseCommand>): Int {
        return CommandManager.registerCommands(plugin, *commandClasses)
    }
    
    /**
     * Unregisters all commands for a specific plugin.
     * This is automatically called when plugins are disabled, but can be called manually if needed.
     * 
     * 注销特定插件的所有命令。
     * 当插件被禁用时会自动调用，但如果需要也可以手动调用。
     *
     * @param plugin The plugin whose commands should be unregistered.
     *               要注销命令的插件。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * // Manually unregister commands / 手动注销命令
     * CommandAPI.unregisterCommands(this)
     * ```
     */
    fun unregisterCommands(plugin: Plugin) {
        CommandManager.unregisterCommands(plugin)
    }
    
    /**
     * Gets information about a registered command.
     * 获取已注册命令的信息。
     *
     * @param commandName The name of the command.
     *                    命令的名称。
     * @return The command information, or null if not found.
     *         命令信息，如果未找到则返回null。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * val commandInfo = CommandAPI.getCommandInfo("mycommand")
     * if (commandInfo != null) {
     *     logger.info("Command ${commandInfo.name} has ${commandInfo.subCommands.size} subcommands")
     * }
     * ```
     */
    fun getCommandInfo(commandName: String): CommandInfo? {
        return CommandManager.getCommandInfo(commandName)
    }
    
    /**
     * Gets all registered commands.
     * 获取所有已注册的命令。
     *
     * @return A map of command names to command information.
     *         命令名称到命令信息的映射。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * val allCommands = CommandAPI.getAllCommands()
     * logger.info("Total registered commands: ${allCommands.size}")
     * ```
     */
    fun getAllCommands(): Map<String, CommandInfo> {
        return CommandManager.getAllCommands()
    }
    
    /**
     * Gets all commands registered by a specific plugin.
     * 获取特定插件注册的所有命令。
     *
     * @param plugin The plugin to get commands for.
     *               要获取命令的插件。
     * @return A list of command names.
     *         命令名称列表。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * val myCommands = CommandAPI.getPluginCommands(this)
     * logger.info("This plugin has registered: ${myCommands.joinToString(", ")}")
     * ```
     */
    fun getPluginCommands(plugin: Plugin): List<String> {
        return CommandManager.getPluginCommands(plugin)
    }
    
    /**
     * Checks if the command system is available and ready to use.
     * 检查命令系统是否可用并准备就绪。
     *
     * @return true if the command system is available, false otherwise.
     *         如果命令系统可用则返回true，否则返回false。
     * 
     * Example usage / 使用示例:
     * ```kotlin
     * if (CommandAPI.isAvailable()) {
     *     // Register commands
     *     CommandAPI.registerCommand(this, MyCommand::class)
     * } else {
     *     logger.warning("ArLibs command system is not available!")
     * }
     * ```
     */
    fun isAvailable(): Boolean {
        return try {
            // Try to access the CommandManager to see if it's loaded
            CommandManager.javaClass
            true
        } catch (_: Exception) {
            false
        }
    }
} 