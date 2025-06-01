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
            sendMessage(context, "&e/arlibs action <type> [args] &7- Test action system")
            sendMessage(context, "&e/arlibs condition <action> [args] &7- Manage condition expressions")
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
                "action" -> {
                    sendMessage(context, "&6Help: &e/arlibs action <type> [args]")
                    sendMessage(context, "&7Tests the action system functionality")
                    sendMessage(context, "&7Available action types:")
                    sendMessage(context, "&7• tell - Send text message")
                    sendMessage(context, "&7• sound - Play sound effect")
                    sendMessage(context, "&7• title - Send title with subtitle")
                    sendMessage(context, "&7• actionbar - Send action bar message")
                    sendMessage(context, "&7• command - Execute command as player")
                    sendMessage(context, "&7• console - Execute command as console")
                    sendMessage(context, "&7• delay - Add delay in ticks")
                    sendMessage(context, "&7• multi - Execute multiple actions")
                    sendMessage(context, "&7Use &e/arlibs action help &7for detailed examples")
                }
                "condition" -> {
                    sendMessage(context, "&6Help: &e/arlibs condition <action> [args]")
                    sendMessage(context, "&7Manage and test condition expressions")
                    sendMessage(context, "&7Available actions:")
                    sendMessage(context, "&7• test <expression> - Test condition against yourself")
                    sendMessage(context, "&7• validate <expression> - Validate condition syntax")
                    sendMessage(context, "&7• debug <expression> - Debug condition parsing")
                    sendMessage(context, "&7• cache <clear|size|info> - Manage condition cache")
                    sendMessage(context, "&7• help - Show detailed condition help")
                    sendMessage(context, "&7Condition types: permission, papi, any, all, not")
                    sendMessage(context, "&7Examples:")
                    sendMessage(context, "&7• &e/arlibs condition test \"permission essentials.fly\"")
                    sendMessage(context, "&7• &e/arlibs condition validate \"not papi %player_level% < 10\"")
                    sendMessage(context, "&7Use &e/arlibs condition help &7for more information")
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
     * Action subcommand - test action system functionality.
     * 动作子命令 - 测试动作系统功能。
     */
    @SubCommand(
        name = "action",
        aliases = ["act", "test"],
        description = "Test action system functionality",
        usage = "/arlibs action <type> [args...]",
        minArgs = 1,
        order = 7
    )
    @Permission("arlibs.command.action", defaultValue = PermissionDefault.OP)
    fun actionCommand(context: CommandContext): CommandResult {
        if (context.sender !is org.bukkit.entity.Player) {
            sendError(context, "This command can only be used by players")
            return CommandResult.ERROR
        }
        
        val player = context.sender as org.bukkit.entity.Player
        val actionType = context.args[0].lowercase()
        
        when (actionType) {
            "help", "list" -> {
                sendMessage(context, "&6=== &eAction System Help &6===")
                sendMessage(context, "&7Available action types:")
                sendMessage(context, "")
                
                val actionHelp = com.arteam.arLibs.action.ActionAPI.getActionHelp()
                for ((type, description) in actionHelp) {
                    sendMessage(context, "&e$description")
                }
                
                sendMessage(context, "")
                sendMessage(context, "&7Examples:")
                sendMessage(context, "&e/arlibs action tell &aHello World!")
                sendMessage(context, "&e/arlibs action sound ENTITY_EXPERIENCE_ORB_PICKUP-1.0-1.0")
                sendMessage(context, "&e/arlibs action title `&6Welcome` `&7to the server` 10 70 20")
                sendMessage(context, "&e/arlibs action actionbar &bAction bar message")
                sendMessage(context, "&e/arlibs action command help")
                sendMessage(context, "&e/arlibs action console say Hello from console!")
                sendMessage(context, "&e/arlibs action delay 20")
                sendMessage(context, "&e/arlibs action multi - Execute multiple actions")
            }
            
            "tell" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action tell <message>")
                    return CommandResult.ERROR
                }
                val message = context.args.drop(1).joinToString(" ")
                val actionString = "tell: $message"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed tell action")
            }
            
            "sound" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action sound <sound>[-volume][-pitch]")
                    return CommandResult.ERROR
                }
                
                val soundArg = context.args[1]
                if (soundArg.equals("help", ignoreCase = true)) {
                    sendMessage(context, "&6=== &eAvailable Sounds &6===")
                    sendMessage(context, "&7Common sounds:")
                    
                    val commonSounds = com.arteam.arLibs.action.actions.SoundAction.getCommonSounds()
                    for (sound in commonSounds) {
                        sendMessage(context, "&e$sound")
                    }
                    
                    sendMessage(context, "")
                    sendMessage(context, "&7Format: &esound-volume-pitch")
                    sendMessage(context, "&7Example: &eENTITY_EXPERIENCE_ORB_PICKUP-1.0-1.0")
                    sendMessage(context, "&7Volume range: &e0.0-10.0 &7(default: 1.0)")
                    sendMessage(context, "&7Pitch range: &e0.5-2.0 &7(default: 1.0)")
                    return CommandResult.SUCCESS
                }
                
                val actionString = "sound: $soundArg"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed sound action")
            }
            
            "title" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action title `<title>` `<subtitle>` [fadeIn] [stay] [fadeOut]")
                    return CommandResult.ERROR
                }
                val titleArgs = context.args.drop(1).joinToString(" ")
                val actionString = "title: $titleArgs"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed title action")
            }
            
            "actionbar" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action actionbar <message>")
                    return CommandResult.ERROR
                }
                val message = context.args.drop(1).joinToString(" ")
                val actionString = "actionbar: $message"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed actionbar action")
            }
            
            "command" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action command <command>")
                    return CommandResult.ERROR
                }
                val command = context.args.drop(1).joinToString(" ")
                val actionString = "command: $command"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed command action")
            }
            
            "console" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action console <command>")
                    return CommandResult.ERROR
                }
                val command = context.args.drop(1).joinToString(" ")
                val actionString = "console: $command"
                
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed console action")
            }
            
            "delay" -> {
                if (context.args.size < 2) {
                    sendError(context, "Usage: /arlibs action delay <ticks>")
                    return CommandResult.ERROR
                }
                val ticks = context.args[1]
                val actionString = "delay: $ticks"
                
                sendMessage(context, "&7Starting delay action...")
                com.arteam.arLibs.action.ActionAPI.executeAction(player, actionString)
                sendSuccess(context, "Executed delay action ($ticks ticks)")
            }
            
            "multi" -> {
                sendMessage(context, "&7Executing multiple actions...")
                val actions = listOf(
                    "tell: &6Testing multiple actions!",
                    "sound: ENTITY_EXPERIENCE_ORB_PICKUP-1.0-1.0",
                    "delay: 20",
                    "title: `&aSuccess!` `&7Multiple actions work` 10 40 10",
                    "delay: 40",
                    "actionbar: &bAll actions completed!"
                )
                
                com.arteam.arLibs.action.ActionAPI.executeActions(player, actions)
                sendSuccess(context, "Started multi-action sequence")
            }
            
            else -> {
                sendError(context, "Unknown action type: $actionType")
                sendMessage(context, "&7Use &e/arlibs action help &7for available actions")
                return CommandResult.ERROR
            }
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Tab completion for help subcommand.
     * 帮助子命令的Tab补全。
     */
    @TabComplete(subCommand = "help", argument = 0)
    fun helpTabComplete(context: CommandContext): List<String> {
        return listOf("info", "commands", "reload", "debug", "version", "action", "condition")
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
     * Tab completion for action subcommand.
     * 动作子命令的Tab补全。
     */
    @TabComplete(subCommand = "action", argument = 0)
    fun actionTabComplete(context: CommandContext): List<String> {
        return listOf("help", "tell", "sound", "title", "actionbar", "command", "console", "delay", "multi")
    }

    /**
     * Global tab completion for the main command.
     * 主命令的全局Tab补全。
     */
    @TabComplete(argument = 0, priority = 1)
    fun globalTabComplete(context: CommandContext): List<String> {
        val subCommands = listOf("help", "info", "commands", "reload", "debug", "version", "action", "condition")
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
                    completions.addAll(listOf("info", "commands", "reload", "debug", "version", "action", "condition"))
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
            "action" -> {
                // For action subcommand, suggest available action types
                if (context.args.isEmpty()) {
                    completions.addAll(listOf("help", "tell", "sound", "title", "actionbar", "command", "console", "delay", "multi"))
                }
            }
            "condition" -> {
                // For condition subcommand, suggest available actions and condition types
                if (context.args.isEmpty()) {
                    completions.addAll(listOf("test", "validate", "debug", "cache", "help", "permission", "perm", "papi", "placeholder", "any", "all", "not"))
                } else if (context.args.size == 1 && context.args[0].lowercase() == "cache") {
                    completions.addAll(listOf("clear", "size", "info"))
                }
            }
            else -> {
                // For main command (no subcommand), suggest subcommands
                if (context.args.isEmpty()) {
                    completions.addAll(listOf("help", "info", "commands", "reload", "debug", "version", "action", "condition"))
                }
            }
        }
        
        return completions
    }

    /**
     * Condition subcommand - manage and test condition expressions.
     * 条件子命令 - 管理和测试条件表达式。
     */
    @SubCommand(
        name = "condition",
        aliases = ["cond", "conditions"],
        description = "Manage and test condition expressions",
        usage = "/arlibs condition <test|validate|cache|help> [args...]",
        minArgs = 0,
        order = 8
    )
    @Permission("arlibs.command.condition", defaultValue = PermissionDefault.OP)
    fun conditionCommand(context: CommandContext): CommandResult {
        // If no arguments provided, show help
        if (context.args.isEmpty()) {
            return showConditionHelp(context)
        }
        
        val subAction = context.args[0].lowercase()
        
        when (subAction) {
            "test" -> {
                return testCondition(context)
            }
            "validate" -> {
                return validateCondition(context)
            }
            "cache" -> {
                return manageCacheCondition(context)
            }
            "help" -> {
                return showConditionHelp(context)
            }
            "debug" -> {
                return debugCondition(context)
            }
            // Support direct condition testing without "test" keyword
            "permission", "perm", "papi", "placeholder", "any", "all", "not" -> {
                return testConditionDirect(context)
            }
            else -> {
                // Check if it looks like a condition expression
                val expression = context.args.joinToString(" ")
                if (isLikelyConditionExpression(expression)) {
                    return testConditionDirect(context)
                } else {
                    sendError(context, "Unknown subcommand: $subAction")
                    sendMessage(context, "&7Available commands: test, validate, debug, cache, help")
                    sendMessage(context, "&7Or use direct condition testing: permission, papi, any, all, not")
                    sendMessage(context, "&7Use &e/arlibs condition help &7for more information")
                    return CommandResult.ERROR
                }
            }
        }
    }
    
    /**
     * Test a condition expression directly (without "test" keyword).
     * 直接测试条件表达式（不需要"test"关键字）。
     */
    private fun testConditionDirect(context: CommandContext): CommandResult {
        if (context.sender !is org.bukkit.entity.Player) {
            sendError(context, "Condition testing can only be used by players")
            sendMessage(context, "&7Use &e/arlibs condition validate <expression> &7from console")
            return CommandResult.ERROR
        }
        
        val player = context.sender as org.bukkit.entity.Player
        val expression = context.args.joinToString(" ")
        
        sendMessage(context, "&6Testing condition: &e$expression")
        sendMessage(context, "&7Player: &e${player.name}")
        
        try {
            val result = com.arteam.arLibs.condition.ConditionManager.evaluate(player, expression)
            val description = com.arteam.arLibs.condition.ConditionManager.getConditionDescription(expression)
            
            sendMessage(context, "&7Result: ${if (result) "&aTrue (Condition met)" else "&cFalse (Condition not met)"}")
            if (description != null) {
                sendMessage(context, "&7Description: &e$description")
            }
            
            // Log the test
            com.arteam.arLibs.utils.Logger.debug("Condition test by ${player.name}: '$expression' = $result")
            
        } catch (e: Exception) {
            sendError(context, "Error testing condition: ${e.message}")
            sendMessage(context, "&7Use &e/arlibs condition help &7for syntax information")
            com.arteam.arLibs.utils.Logger.warn("Error testing condition '$expression': ${e.message}")
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Test a condition expression against the command sender.
     * 针对命令发送者测试条件表达式。
     */
    private fun testCondition(context: CommandContext): CommandResult {
        if (context.sender !is org.bukkit.entity.Player) {
            sendError(context, "Condition testing can only be used by players")
            sendMessage(context, "&7Use &e/arlibs condition validate <expression> &7from console")
            return CommandResult.ERROR
        }
        
        if (context.args.size < 2) {
            sendError(context, "Usage: /arlibs condition test <expression>")
            sendMessage(context, "&7Example: &e/arlibs condition test \"permission essentials.fly\"")
            sendMessage(context, "&7Or directly: &e/arlibs condition permission essentials.fly")
            return CommandResult.ERROR
        }
        
        val player = context.sender as org.bukkit.entity.Player
        val expression = context.args.drop(1).joinToString(" ")
        
        sendMessage(context, "&6Testing condition: &e$expression")
        sendMessage(context, "&7Player: &e${player.name}")
        
        try {
            val result = com.arteam.arLibs.condition.ConditionManager.evaluate(player, expression)
            val description = com.arteam.arLibs.condition.ConditionManager.getConditionDescription(expression)
            
            sendMessage(context, "&7Result: ${if (result) "&aTrue (Condition met)" else "&cFalse (Condition not met)"}")
            if (description != null) {
                sendMessage(context, "&7Description: &e$description")
            }
            
            // Log the test
            com.arteam.arLibs.utils.Logger.debug("Condition test by ${player.name}: '$expression' = $result")
            
        } catch (e: Exception) {
            sendError(context, "Error testing condition: ${e.message}")
            sendMessage(context, "&7Use &e/arlibs condition help &7for syntax information")
            com.arteam.arLibs.utils.Logger.warn("Error testing condition '$expression': ${e.message}")
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Validate a condition expression syntax.
     * 验证条件表达式语法。
     */
    private fun validateCondition(context: CommandContext): CommandResult {
        if (context.args.size < 2) {
            sendError(context, "Usage: /arlibs condition validate <expression>")
            sendMessage(context, "&7Example: &e/arlibs condition validate \"papi %player_level% >= 10\"")
            return CommandResult.ERROR
        }
        
        val expression = context.args.drop(1).joinToString(" ")
        
        sendMessage(context, "&6Validating condition: &e$expression")
        
        try {
            val isValid = com.arteam.arLibs.condition.ConditionManager.isValidExpression(expression)
            val description = com.arteam.arLibs.condition.ConditionManager.getConditionDescription(expression)
            
            if (isValid) {
                sendSuccess(context, "Condition syntax is valid")
                if (description != null) {
                    sendMessage(context, "&7Description: &e$description")
                }
            } else {
                sendError(context, "Invalid condition syntax")
                sendMessage(context, "&7Please check your expression format")
                sendMessage(context, "&7Use &e/arlibs condition help &7for syntax information")
            }
            
        } catch (e: Exception) {
            sendError(context, "Error validating condition: ${e.message}")
            sendMessage(context, "&7Use &e/arlibs condition help &7for syntax information")
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Manage condition cache.
     * 管理条件缓存。
     */
    private fun manageCacheCondition(context: CommandContext): CommandResult {
        if (context.args.size < 2) {
            sendError(context, "Usage: /arlibs condition cache <clear|size|info>")
            return CommandResult.ERROR
        }
        
        val cacheAction = context.args[1].lowercase()
        
        when (cacheAction) {
            "clear" -> {
                com.arteam.arLibs.condition.ConditionManager.clearCache()
                sendSuccess(context, "Condition cache cleared")
                com.arteam.arLibs.utils.Logger.info("Condition cache cleared by ${context.sender.name}")
            }
            "size" -> {
                val size = com.arteam.arLibs.condition.ConditionManager.getCacheSize()
                sendMessage(context, "&7Current cache size: &e$size condition(s)")
            }
            "info" -> {
                val size = com.arteam.arLibs.condition.ConditionManager.getCacheSize()
                sendMessage(context, "&6=== &eCondition Cache Information &6===")
                sendMessage(context, "&7Cached conditions: &e$size")
                sendMessage(context, "&7Cache helps improve performance by avoiding re-parsing")
                sendMessage(context, "&7Use &e/arlibs condition cache clear &7to clear cache")
            }
            else -> {
                sendError(context, "Unknown cache action: $cacheAction")
                sendMessage(context, "&7Available actions: clear, size, info")
                return CommandResult.ERROR
            }
        }
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Debug condition parsing for troubleshooting.
     * 调试条件解析以进行故障排除。
     */
    private fun debugCondition(context: CommandContext): CommandResult {
        if (context.args.size < 2) {
            sendError(context, "Usage: /arlibs condition debug <expression>")
            sendMessage(context, "&7Example: &e/arlibs condition debug \"all [papi %player_level% >= 10; %player_sneaking%]\"")
            return CommandResult.ERROR
        }
        
        val expression = context.args.drop(1).joinToString(" ")
        
        sendMessage(context, "&6=== &eCondition Debug Information &6===")
        sendMessage(context, "&7Expression: &e$expression")
        sendMessage(context, "")
        
        try {
            // Test parsing
            val condition = com.arteam.arLibs.condition.ConditionParser.parse(expression)
            if (condition != null) {
                sendSuccess(context, "Parsing: SUCCESS")
                sendMessage(context, "&7Description: &e${condition.getDescription()}")
                
                // Test evaluation if sender is a player
                if (context.sender is org.bukkit.entity.Player) {
                    val player = context.sender as org.bukkit.entity.Player
                    val result = condition.evaluate(player)
                    sendMessage(context, "&7Evaluation: ${if (result) "&aTrue" else "&cFalse"}")
                } else {
                    sendMessage(context, "&7Evaluation: &7Skipped (console sender)")
                }
            } else {
                sendError(context, "Parsing: FAILED")
                sendMessage(context, "&7The expression could not be parsed")
            }
            
            // Show cache information
            val cacheSize = com.arteam.arLibs.condition.ConditionManager.getCacheSize()
            sendMessage(context, "&7Cache size: &e$cacheSize")
            
        } catch (e: Exception) {
            sendError(context, "Debug failed: ${e.message}")
            sendMessage(context, "&7Stack trace logged to console")
            e.printStackTrace()
            return CommandResult.ERROR
        }
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Show condition help information.
     * 显示条件帮助信息。
     */
    private fun showConditionHelp(context: CommandContext): CommandResult {
        sendMessage(context, "&6=== &eCondition System Help &6===")
        sendMessage(context, "")
        sendMessage(context, "&eAvailable Commands:")
        sendMessage(context, "&7• &e/arlibs condition test <expression> &7- Test condition against yourself")
        sendMessage(context, "&7• &e/arlibs condition validate <expression> &7- Validate condition syntax")
        sendMessage(context, "&7• &e/arlibs condition debug <expression> &7- Debug condition parsing")
        sendMessage(context, "&7• &e/arlibs condition cache <clear|size|info> &7- Manage condition cache")
        sendMessage(context, "&7• &e/arlibs condition help &7- Show this help")
        sendMessage(context, "")
        sendMessage(context, "&eQuick Testing (for players):")
        sendMessage(context, "&7• &e/arlibs condition permission essentials.fly")
        sendMessage(context, "&7• &e/arlibs condition papi %player_level% >= 10")
        sendMessage(context, "&7• &e/arlibs condition not permission essentials.fly")
        sendMessage(context, "&7• &e/arlibs condition not papi %player_sneaking%")
        sendMessage(context, "")
        sendMessage(context, "&eCondition Types:")
        sendMessage(context, "&7• &apermission <node> &7- Check player permission")
        sendMessage(context, "&7• &apapi <placeholder> [operator] [value] &7- Check PlaceholderAPI value")
        sendMessage(context, "&7• &aany [condition1; condition2; ...] &7- OR logic")
        sendMessage(context, "&7• &aall [condition1; condition2; ...] &7- AND logic")
        sendMessage(context, "&7• &anot <condition> &7- NOT logic (negation)")
        sendMessage(context, "")
        sendMessage(context, "&eOperators:")
        sendMessage(context, "&7• &a> >= < <= == != &7- Comparison operators")
        
        return CommandResult.SUCCESS
    }
    
    /**
     * Tab completion for condition subcommand.
     * 条件子命令的Tab补全。
     */
    @TabComplete(subCommand = "condition", argument = 0)
    fun conditionTabComplete(context: CommandContext): List<String> {
        return listOf("test", "validate", "debug", "cache", "help", "permission", "perm", "papi", "placeholder", "any", "all", "not")
    }
    
    /**
     * Tab completion for condition cache subcommand.
     * 条件缓存子命令的Tab补全。
     */
    @TabComplete(subCommand = "condition", argument = 1)
    fun conditionCacheTabComplete(context: CommandContext): List<String> {
        if (context.args.isNotEmpty() && context.args[0].lowercase() == "cache") {
            return listOf("clear", "size", "info")
        }
        return emptyList()
    }

    /**
     * Checks if a string looks like a condition expression.
     * 检查字符串是否看起来像条件表达式。
     */
    private fun isLikelyConditionExpression(expression: String): Boolean {
        val trimmed = expression.trim().lowercase()
        
        // Check for known condition patterns
        return when {
            // Starts with known condition types
            trimmed.startsWith("permission ") || trimmed.startsWith("perm ") -> true
            trimmed.startsWith("placeholder ") || trimmed.startsWith("papi ") -> true
            trimmed.startsWith("any[") || trimmed.startsWith("any [") -> true
            trimmed.startsWith("all[") || trimmed.startsWith("all [") -> true
            trimmed.startsWith("not ") -> true
            
            // Starts with placeholder
            trimmed.startsWith("%") && trimmed.contains("%") -> true
            
            // Contains comparison operators (likely placeholder condition)
            trimmed.contains(">=") || trimmed.contains("<=") || 
            trimmed.contains("==") || trimmed.contains("!=") ||
            (trimmed.contains(">") && !trimmed.contains(">>")) ||
            (trimmed.contains("<") && !trimmed.contains("<<")) -> true
            
            // Contains brackets (likely logical condition)
            trimmed.contains("[") && trimmed.contains("]") -> true
            
            else -> false
        }
    }
} 