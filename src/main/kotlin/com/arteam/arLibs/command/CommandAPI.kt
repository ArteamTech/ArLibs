/**
 * Public API for the ArLibs command system.
 * ArLibs命令系统的公共API。
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
    
    /** Registers a single command class. 注册单个命令类。 */
    fun registerCommand(plugin: Plugin, commandClass: KClass<out BaseCommand>): Boolean = 
        CommandManager.registerCommand(plugin, commandClass)
    
    /** Registers multiple command classes at once. 一次注册多个命令类。 */
    fun registerCommands(plugin: Plugin, vararg commandClasses: KClass<out BaseCommand>): Int = 
        CommandManager.registerCommands(plugin, *commandClasses)
    
    /** Unregisters all commands for a specific plugin. 注销特定插件的所有命令。 */
    fun unregisterCommands(plugin: Plugin) = CommandManager.unregisterCommands(plugin)
    
    /** Gets information about a registered command. 获取已注册命令的信息。 */
    fun getCommandInfo(commandName: String): CommandInfo? = CommandManager.getCommandInfo(commandName)
    
    /** Gets all registered commands. 获取所有已注册的命令。 */
    fun getAllCommands(): Map<String, CommandInfo> = CommandManager.getAllCommands()
    
    /** Gets all commands registered by a specific plugin. 获取特定插件注册的所有命令。 */
    fun getPluginCommands(plugin: Plugin): List<String> = CommandManager.getPluginCommands(plugin)
    
    /** Checks if the command system is available. 检查命令系统是否可用。 */
    fun isAvailable(): Boolean = try {
        CommandManager.javaClass
        true
    } catch (_: Exception) {
        false
    }
} 