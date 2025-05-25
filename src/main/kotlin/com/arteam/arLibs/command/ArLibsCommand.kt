/**
 * Main command for the ArLibs plugin.
 * This command provides information about ArLibs and its features.
 * 
 * ArLibs插件的主命令。
 * 此命令提供有关ArLibs及其功能的信息。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import com.arteam.arLibs.ArLibs
import com.arteam.arLibs.command.annotations.*
import com.arteam.arLibs.config.ConfigManager
import com.arteam.arLibs.config.CoreConfig
import org.bukkit.Bukkit

/**
 * ArLibs main command providing system information and management features.
 * ArLibs主命令，提供系统信息和管理功能。
 * 
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
@Suppress("unstableApiUsage", "unused")
@Command(
    name = "arlibs",
    description = "ArLibs main command - A powerful library for Bukkit plugins",
    usage = "/arlibs <subcommand>",
    permission = "arlibs.command"
)
class ArLibsCommand : BaseCommand() {

    /**
     * Main command execution - shows ArLibs information.
     * 主命令执行 - 显示ArLibs信息。
     */
    override fun execute(context: CommandContext): CommandResult {
        val plugin = ArLibs.getInstance()
        val version = plugin.pluginMeta.version
        val authors = plugin.pluginMeta.authors.joinToString(", ")
        
        sendMessage(context, "")
        sendMessage(context, "&6╔═════════════════════════════════╗")
        sendMessage(context, "&6║        &eArLibs Framework &bv$version        &6║")
        sendMessage(context, "&6╠═════════════════════════════════╣")
        sendMessage(context, "&6║ &7Author(s): &e$authors")
        sendMessage(context, "&6║ &7A powerful library for Bukkit plugins")
        sendMessage(context, "&6║")
        sendMessage(context, "&6║ &7Features:")
        sendMessage(context, "&6║ &a• &7Annotation-based command system")
        sendMessage(context, "&6║ &a• &7Advanced configuration management")
        sendMessage(context, "&6║ &a• &7Color processing utilities")
        sendMessage(context, "&6║ &a• &7Enhanced logging system")
        sendMessage(context, "&6║")
        sendMessage(context, "&6║ &7Use &e/arlibs help &7for available commands")
        sendMessage(context, "&6╚═════════════════════════════════╝")
        sendMessage(context, "")
        
        return CommandResult.SUCCESS
    }

    /**
     * Help subcommand - shows available commands and usage.
     * 帮助子命令 - 显示可用命令和用法。
     */
    @SubCommand(
        name = "help",
        aliases = ["h", "?"],
        description = "Show help information and available commands",
        usage = "/arlibs help [command]",
        order = 1
    )
    fun helpCommand(context: CommandContext): CommandResult {
        if (context.args.isEmpty()) {
            sendMessage(context, "&6=== &eArLibs Help &6===")
            sendMessage(context, "&7Available commands:")
            sendMessage(context, "")
            sendMessage(context, "&e/arlibs &7- Show ArLibs information")
            sendMessage(context, "&e/arlibs help [command] &7- Show help information")
            sendMessage(context, "&e/arlibs info &7- Show detailed system information")
            sendMessage(context, "&e/arlibs commands [plugin] &7- List registered commands")
            sendMessage(context, "&e/arlibs reload &7- Reload ArLibs configuration")
            sendMessage(context, "&e/arlibs debug &7- Toggle debug mode")
            sendMessage(context, "&e/arlibs version &7- Show version information")
            sendMessage(context, "")
            sendMessage(context, "&7Use &e/arlibs help <command> &7for specific help")
        } else {
            val subCommand = context.args[0].lowercase()
            when (subCommand) {
                "info" -> {
                    sendMessage(context, "&6Help: &e/arlibs info")
                    sendMessage(context, "&7Shows detailed system information including:")
                    sendMessage(context, "&7• Server version and platform details")
                    sendMessage(context, "&7• ArLibs configuration status")
                    sendMessage(context, "&7• Loaded plugins using ArLibs")
                    sendMessage(context, "&7• System performance metrics")
                }
                "commands" -> {
                    sendMessage(context, "&6Help: &e/arlibs commands [plugin]")
                    sendMessage(context, "&7Lists all commands registered through ArLibs")
                    sendMessage(context, "&7If a plugin name is specified, shows only that plugin's commands")
                    sendMessage(context, "&7Includes command permissions and descriptions")
                }
                "reload" -> {
                    sendMessage(context, "&6Help: &e/arlibs reload")
                    sendMessage(context, "&7Reloads the ArLibs configuration")
                    sendMessage(context, "&7This will refresh settings without restarting the server")
                    sendMessage(context, "&cWarning: This may affect other plugins using ArLibs")
                }
                "debug" -> {
                    sendMessage(context, "&6Help: &e/arlibs debug")
                    sendMessage(context, "&7Toggles debug mode on/off")
                    sendMessage(context, "&7When enabled, shows detailed logging information")
                    sendMessage(context, "&7Useful for troubleshooting issues")
                }
                else -> {
                    sendError(context, "Unknown command: $subCommand")
                    sendMessage(context, "&7Use &e/arlibs help &7to see available commands")
                }
            }
        }
        return CommandResult.SUCCESS
    }

    /**
     * Info subcommand - shows detailed system information.
     * 信息子命令 - 显示详细的系统信息。
     */
    @SubCommand(
        name = "info",
        aliases = ["information", "about"],
        description = "Show detailed ArLibs system information",
        usage = "/arlibs info",
        order = 2
    )
    fun infoCommand(context: CommandContext): CommandResult {
        val plugin = ArLibs.getInstance()
        val version = plugin.pluginMeta.version
        val server = Bukkit.getServer()
        val allCommands = CommandAPI.getAllCommands()
        val pluginCommandCounts = mutableMapOf<String, Int>()
        
        // Count commands by plugin
        for ((_, commandInfo) in allCommands) {
            val pluginName = commandInfo.instance.javaClass.`package`?.name?.split(".")?.getOrNull(2) ?: "Unknown"
            pluginCommandCounts[pluginName] = (pluginCommandCounts[pluginName] ?: 0) + 1
        }
        
        sendMessage(context, "&6=== &eArLibs System Information &6===")
        sendMessage(context, "")
        sendMessage(context, "&6Plugin Information:")
        sendMessage(context, "&7• Version: &e$version")
        sendMessage(context, "&7• Authors: &e${plugin.pluginMeta.authors.joinToString(", ")}")
        sendMessage(context, "&7• Description: &e${plugin.pluginMeta.description ?: "A powerful library for Bukkit plugins"}")
        sendMessage(context, "")
        sendMessage(context, "&6Server Information:")
        sendMessage(context, "&7• Server: &e${server.name} ${server.version}")
        sendMessage(context, "&7• Bukkit Version: &e${server.bukkitVersion}")
        sendMessage(context, "&7• Online Players: &e${server.onlinePlayers.size}/${server.maxPlayers}")
        sendMessage(context, "")
        sendMessage(context, "&6Command System:")
        sendMessage(context, "&7• Total Registered Commands: &e${allCommands.size}")
        sendMessage(context, "&7• Commands by Plugin:")
        for ((pluginName, count) in pluginCommandCounts.toList().sortedByDescending { it.second }) {
            sendMessage(context, "&7  - &e$pluginName&7: &a$count command(s)")
        }
        sendMessage(context, "")
        sendMessage(context, "&6Configuration Status:")
        try {
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)
            sendMessage(context, "&7• Debug Mode: &e${if (coreConfig?.debug == true) "Enabled" else "Disabled"}")
            sendMessage(context, "&7• Config Loaded: &aSuccessfully")
        } catch (e: Exception) {
            sendMessage(context, "&7• Config Status: &cError - ${e.message}")
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Commands subcommand - lists registered commands.
     * 命令子命令 - 列出已注册的命令。
     */
    @SubCommand(
        name = "commands",
        aliases = ["cmd", "list"],
        description = "List all commands registered through ArLibs",
        usage = "/arlibs commands [plugin]",
        maxArgs = 1,
        order = 3
    )
    fun commandsCommand(context: CommandContext): CommandResult {
        val allCommands = CommandAPI.getAllCommands()
        
        if (allCommands.isEmpty()) {
            sendWarning(context, "No commands are currently registered through ArLibs")
            return CommandResult.SUCCESS
        }
        
        val filterPlugin = context.getArg(0)
        val filteredCommands = if (filterPlugin != null) {
            allCommands.filter { it.value.instance.javaClass.`package`?.name?.contains(filterPlugin, ignoreCase = true) == true }
        } else {
            allCommands
        }
        
        if (filteredCommands.isEmpty()) {
            sendError(context, "No commands found for plugin: $filterPlugin")
            return CommandResult.ERROR
        }
        
        val title = if (filterPlugin != null) {
            "&6=== &eCommands for $filterPlugin &6==="
        } else {
            "&6=== &eAll Registered Commands &6==="
        }
        
        sendMessage(context, title)
        sendMessage(context, "&7Total: &e${filteredCommands.size} command(s)")
        sendMessage(context, "")
        
        for ((commandName, commandInfo) in filteredCommands.toList().sortedBy { it.first }) {
            val aliases = if (commandInfo.aliases.isNotEmpty()) {
                " &8(${commandInfo.aliases.joinToString(", ")})"
            } else ""
            
            val permission = if (commandInfo.permission.isNotEmpty()) {
                " &8[${commandInfo.permission}]"
            } else ""
            
            sendMessage(context, "&e/$commandName$aliases$permission")
            if (commandInfo.description.isNotEmpty()) {
                sendMessage(context, "&7  └─ ${commandInfo.description}")
            }
            
            if (commandInfo.subCommands.isNotEmpty()) {
                sendMessage(context, "&7  └─ Subcommands: &a${commandInfo.subCommands.size}")
            }
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Reload subcommand - reloads ArLibs configuration.
     * 重载子命令 - 重新加载ArLibs配置。
     */
    @SubCommand(
        name = "reload",
        aliases = ["rl"],
        description = "Reload ArLibs configuration",
        usage = "/arlibs reload",
        order = 4
    )
    @Permission("arlibs.command.reload", defaultValue = PermissionDefault.OP)
    fun reloadCommand(context: CommandContext): CommandResult {
        sendMessage(context, "&7Reloading ArLibs configuration...")
        
        try {
            ConfigManager.reloadConfig(CoreConfig::class)
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)
            
            // Update logger debug mode
            com.arteam.arLibs.utils.Logger.init(ArLibs.getInstance(), debug = coreConfig?.debug == true)
            
            sendSuccess(context, "ArLibs configuration reloaded successfully!")
            sendMessage(context, "&7Debug Mode: &e${if (coreConfig?.debug == true) "Enabled" else "Disabled"}")
            
            // Log the reload action
            com.arteam.arLibs.utils.Logger.info("Configuration reloaded by ${context.sender.name}")
            
        } catch (e: Exception) {
            sendError(context, "Failed to reload configuration: ${e.message}")
            com.arteam.arLibs.utils.Logger.severe("Failed to reload configuration: ${e.message}")
            e.printStackTrace()
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Debug subcommand - toggles debug mode.
     * 调试子命令 - 切换调试模式。
     */
    @SubCommand(
        name = "debug",
        aliases = ["d"],
        description = "Toggle debug mode on/off",
        usage = "/arlibs debug",
        order = 5
    )
    @Permission("arlibs.command.debug", defaultValue = PermissionDefault.OP)
    fun debugCommand(context: CommandContext): CommandResult {
        try {
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)
            val newDebugState = !(coreConfig?.debug ?: false)
            
            // Update config
            coreConfig?.debug = newDebugState
            ConfigManager.saveConfig(CoreConfig::class)
            
            // Update logger
            com.arteam.arLibs.utils.Logger.init(ArLibs.getInstance(), debug = newDebugState)
            
            val statusMessage = if (newDebugState) {
                "&aEnabled"
            } else {
                "&cDisabled"
            }
            
            sendSuccess(context, "Debug mode $statusMessage")
            sendMessage(context, "&7Debug logging is now ${if (newDebugState) "enabled" else "disabled"}")
            
            // Log the change
            com.arteam.arLibs.utils.Logger.info("Debug mode ${if (newDebugState) "enabled" else "disabled"} by ${context.sender.name}")
            
        } catch (e: Exception) {
            sendError(context, "Failed to toggle debug mode: ${e.message}")
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Version subcommand - shows version information.
     * 版本子命令 - 显示版本信息。
     */
    @SubCommand(
        name = "version",
        aliases = ["ver", "v"],
        description = "Show ArLibs version information",
        usage = "/arlibs version",
        order = 6
    )
    fun versionCommand(context: CommandContext): CommandResult {
        val plugin = ArLibs.getInstance()
        val version = plugin.pluginMeta.version
        val authors = plugin.pluginMeta.authors.joinToString(", ")
        
        sendMessage(context, "&6ArLibs Framework")
        sendMessage(context, "&7Version: &e$version")
        sendMessage(context, "&7Authors: &e$authors")
        sendMessage(context, "&7Built for: &eBukkit/Spigot/Paper")
        sendMessage(context, "&7API Version: &e${plugin.pluginMeta.apiVersion ?: "Legacy"}")
        
        if (context.hasPermission("arlibs.command.debug")) {
            sendMessage(context, "")
            sendMessage(context, "&7Debug Information:")
            sendMessage(context, "&7• Plugin File: &e${plugin.dataFolder.parent}/${plugin.pluginMeta.name}.jar")
            sendMessage(context, "&7• Data Folder: &e${plugin.dataFolder.absolutePath}")
            sendMessage(context, "&7• Loaded: &e${if (plugin.isEnabled) "Yes" else "No"}")
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Tab completion for help subcommand.
     * 帮助子命令的Tab补全。
     */
    @TabComplete(subCommand = "help", argument = 0)
    fun helpTabComplete(context: CommandContext): List<String> {
        return listOf("info", "commands", "reload", "debug", "version")
    }

    /**
     * Tab completion for commands subcommand.
     * 命令子命令的Tab补全。
     */
    @TabComplete(subCommand = "commands", argument = 0)
    fun commandsTabComplete(context: CommandContext): List<String> {
        val allCommands = CommandAPI.getAllCommands()
        val plugins = mutableSetOf<String>()
        
        for ((_, commandInfo) in allCommands) {
            val pluginName = commandInfo.instance.javaClass.`package`?.name?.split(".")?.getOrNull(2)
            if (pluginName != null) {
                plugins.add(pluginName)
            }
        }
        
        return plugins.sorted()
    }

    /**
     * Global tab completion for the main command.
     * 主命令的全局Tab补全。
     */
    @TabComplete(argument = 0, priority = 1)
    fun globalTabComplete(context: CommandContext): List<String> {
        val subCommands = listOf("help", "info", "commands", "reload", "debug", "version")
        val input = context.args.lastOrNull() ?: ""
        return subCommands.filter { it.startsWith(input, ignoreCase = true) }
    }
    
    /**
     * Enhanced tab completion that provides contextual suggestions.
     * 增强的Tab补全，提供上下文建议。
     */
    override fun onTabComplete(context: CommandContext): List<String> {
        val completions = mutableListOf<String>()
        
        // Provide different completions based on subcommand context
        when (context.subCommand) {
            "help" -> {
                // For help subcommand, suggest available commands to get help for
                if (context.args.isEmpty()) {
                    completions.addAll(listOf("info", "commands", "reload", "debug", "version"))
                }
            }
            "commands" -> {
                // For commands subcommand, suggest plugin names
                if (context.args.isEmpty()) {
                    val allCommands = CommandAPI.getAllCommands()
                    val plugins = mutableSetOf<String>()
                    
                    for ((_, commandInfo) in allCommands) {
                        val pluginName = commandInfo.instance.javaClass.`package`?.name?.split(".")?.getOrNull(2)
                        if (pluginName != null) {
                            plugins.add(pluginName)
                        }
                    }
                    
                    completions.addAll(plugins.sorted())
                }
            }
            "debug", "reload", "version", "info" -> {
                // These commands don't take arguments, so no completions
            }
            else -> {
                // For main command (no subcommand), suggest subcommands
                if (context.args.isEmpty()) {
                    completions.addAll(listOf("help", "info", "commands", "reload", "debug", "version"))
                }
            }
        }
        
        return completions
    }
} 