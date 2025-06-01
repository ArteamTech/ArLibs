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
import com.arteam.arLibs.utils.Logger
import org.bukkit.Bukkit

/**
 * ArLibs main command providing system information and management features.
 * ArLibs主命令，提供系统信息和管理功能。
 */
@Suppress("unstableApiUsage", "unused")
@Command(
    name = "arlibs",
    description = "ArLibs main command - A powerful library for Bukkit plugins",
    usage = "/arlibs <subcommand>"
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
        
        send(
            "",
            "&6╔═════════════════════════════════╗",
            "&6║        &eArLibs Framework &bv$version        &6║",
            "&6╠═════════════════════════════════╣",
            "&6║ &7Author(s): &e$authors",
            "&6║ &7A powerful library for Bukkit plugins",
            "&6║",
            "&6║ &7Features:",
            "&6║ &a• &7Annotation-based command system",
            "&6║ &a• &7Advanced configuration management",
            "&6║ &a• &7Color processing utilities",
            "&6║ &a• &7Enhanced logging system",
            "&6║",
            "&6║ &7Use &e/arlibs help &7for available commands",
            "&6╚═════════════════════════════════╝",
            ""
        )
        return CommandResult.SUCCESS
    }

    /**
     * Help subcommand - shows available commands and usage.
     * 帮助子命令 - 显示可用命令和用法。
     */
    @SubCommand(
        name = "help",
        aliases = ["h", "?"],
        description = "Show help information and available commands"
    )
    fun helpCommand(context: CommandContext): CommandResult {
        val mainCommandInfo = CommandAPI.getCommandInfo("arlibs")
            ?: return sendError("Could not load help information.").let { CommandResult.ERROR }

        if (context.args.isEmpty()) {
            val messages = mutableListOf(
                "&6=== &eArLibs Help &6===",
                "&7Available commands:",
                ""
            )

            // Main command
            messages.add("&e${mainCommandInfo.usage} &7- ${mainCommandInfo.description}")

            // Subcommands (sorted alphabetically)
            mainCommandInfo.subCommands.values
                .distinctBy { it.name }
                .sortedBy { it.name }
                .filter { context.hasPermission(it.permission) }
                .forEach { subCmd ->
                    val usage = subCmd.usage.ifEmpty { "/${mainCommandInfo.name} ${subCmd.name}" }
                    val description = subCmd.description.ifEmpty { "No description available." }
                    messages.add("&e$usage &7- $description")
                }

            messages.addAll(listOf(
                "",
                "&7Use &e/arlibs help <subcommand_name> &7for specific help."
            ))

            send(*messages.toTypedArray())
        } else {
            val subCommandInfo = mainCommandInfo.subCommands[context.args[0].lowercase()]
                ?: return sendError("Unknown command: ${context.args[0]}").let { CommandResult.ERROR }

            if (!context.hasPermission(subCommandInfo.permission)) {
                return sendError("You don't have permission to view help for this command.").let { CommandResult.ERROR }
            }

            val effectiveExecutor = when {
                mainCommandInfo.executor == CommandExecutor.PLAYER || subCommandInfo.executor == CommandExecutor.PLAYER -> CommandExecutor.PLAYER
                mainCommandInfo.executor == CommandExecutor.CONSOLE || subCommandInfo.executor == CommandExecutor.CONSOLE -> CommandExecutor.CONSOLE
                else -> CommandExecutor.ALL
            }

            send(
                "&6Help: &e${subCommandInfo.usage.ifEmpty { "/${mainCommandInfo.name} ${subCommandInfo.name}" }}",
                "&7Description: &f${subCommandInfo.description.ifEmpty { "No description available." }}",
                if (subCommandInfo.aliases.isNotEmpty()) "&7Aliases: &f${subCommandInfo.aliases.joinToString(", ")}" else null,
                "&7Permission: &f${subCommandInfo.permission}",
                "&7Executor: &f$effectiveExecutor"
            )
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
        description = "Show detailed ArLibs system information"
    )
    fun infoCommand(context: CommandContext): CommandResult {
        val plugin = ArLibs.getInstance()
        val version = plugin.pluginMeta.version
        val server = Bukkit.getServer()
        val allCommands = CommandAPI.getAllCommands()
        val pluginCommandCounts = allCommands.values
            .groupingBy { it.instance.javaClass.`package`?.name?.split(".")?.getOrNull(2) ?: "Unknown" }
            .eachCount()
        
        val messages = mutableListOf(
            "&6=== &eArLibs System Information &6===",
            "",
            "&6Plugin Information:",
            "&7• Version: &e$version",
            "&7• Authors: &e${plugin.pluginMeta.authors.joinToString(", ")}",
            "&7• Description: &e${plugin.pluginMeta.description ?: "A powerful library for Bukkit plugins"}",
            "",
            "&6Server Information:",
            "&7• Server: &e${server.name} ${server.version}",
            "&7• Bukkit Version: &e${server.bukkitVersion}",
            "&7• Online Players: &e${server.onlinePlayers.size}/${server.maxPlayers}",
            "",
            "&6Command System:",
            "&7• Total Registered Commands: &e${allCommands.size}",
            "&7• Commands by Plugin:"
        )

        pluginCommandCounts.toList().sortedByDescending { it.second }.forEach { (pluginName, count) ->
            messages.add("&7  - &e$pluginName&7: &a$count command(s)")
        }
        
        messages.add("")
        messages.add("&6Configuration Status:")
        
        try {
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)
            messages.addAll(listOf(
                "&7• Debug Mode: &e${if (coreConfig?.debug == true) "Enabled" else "Disabled"}",
                "&7• Config Loaded: &aSuccessfully"
            ))
        } catch (e: Exception) {
            messages.add("&7• Config Status: &cError - ${e.message}")
        }
        
        send(*messages.toTypedArray())
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
        maxArgs = 1
    )
    fun commandsCommand(context: CommandContext): CommandResult {
        val allCommands = CommandAPI.getAllCommands()
        
        if (allCommands.isEmpty()) {
            return sendWarning("No commands are currently registered through ArLibs").let { CommandResult.SUCCESS }
        }
        
        val filterPlugin = context.getArg(0)
        val filteredCommands = if (filterPlugin != null) {
            allCommands.filter { it.value.instance.javaClass.`package`?.name?.contains(filterPlugin, ignoreCase = true) == true }
        } else {
            allCommands
        }
        
        if (filteredCommands.isEmpty()) {
            return sendError("No commands found for plugin: $filterPlugin").let { CommandResult.ERROR }
        }
        
        val title = if (filterPlugin != null) {
            "&6=== &eCommands for $filterPlugin &6==="
        } else {
            "&6=== &eAll Registered Commands &6==="
        }
        
        val messages = mutableListOf(
            title,
            "&7Total: &e${filteredCommands.size} command(s)",
            ""
        )
        
        filteredCommands.toList().sortedBy { it.first }.forEach { (commandName, commandInfo) ->
            val aliases = if (commandInfo.aliases.isNotEmpty()) " &8(${commandInfo.aliases.joinToString(", ")})" else ""
            val permission = if (commandInfo.permission.isNotEmpty()) " &8[${commandInfo.permission}]" else ""
            
            messages.add("&e/$commandName$aliases$permission")
            if (commandInfo.description.isNotEmpty()) {
                messages.add("&7  └─ ${commandInfo.description}")
            }
            if (commandInfo.subCommands.isNotEmpty()) {
                messages.add("&7  └─ Subcommands: &a${commandInfo.subCommands.size}")
            }
        }
        
        send(*messages.toTypedArray())
        return CommandResult.SUCCESS
    }

    /**
     * Reload subcommand - reloads ArLibs configuration.
     * 重载子命令 - 重新加载ArLibs配置。
     */
    @SubCommand(
        name = "reload",
        aliases = ["rl"],
        description = "Reload ArLibs configuration"
    )
    @Permission("arlibs.command.reload", defaultValue = PermissionDefault.OP)
    fun reloadCommand(context: CommandContext): CommandResult {
        send("&7Reloading ArLibs configuration...")
        
        return try {
            ConfigManager.reloadConfig(CoreConfig::class)
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)

            
            send(
                "&aArLibs configuration reloaded successfully!",
                "&7Debug Mode: &e${if (coreConfig?.debug == true) "Enabled" else "Disabled"}"
            )
            
            Logger.info("Configuration reloaded by ${context.sender.name}")
            CommandResult.SUCCESS
            
        } catch (e: Exception) {
            sendError("Failed to reload configuration: ${e.message}")
            Logger.severe("Failed to reload configuration: ${e.message}")
            CommandResult.ERROR
        }
    }

    /**
     * Debug subcommand - toggles debug mode.
     * 调试子命令 - 切换调试模式。
     */
    @SubCommand(
        name = "debug",
        aliases = ["d"],
        description = "Toggle debug mode on/off"
    )
    @Permission("arlibs.command.debug", defaultValue = PermissionDefault.OP)
    fun debugCommand(context: CommandContext): CommandResult {
        return try {
            val coreConfig = ConfigManager.getConfig(CoreConfig::class)
            val newDebugState = !(coreConfig?.debug ?: false)
            
            coreConfig?.debug = newDebugState
            ConfigManager.saveConfig(CoreConfig::class)
            
            val statusMessage = if (newDebugState) "&aEnabled" else "&cDisabled"
            
            send(
                "&aDebug mode $statusMessage",
                "&7Debug logging is now ${if (newDebugState) "enabled" else "disabled"}"
            )
            
            Logger.info("Debug mode ${if (newDebugState) "enabled" else "disabled"} by ${context.sender.name}")
            CommandResult.SUCCESS
            
        } catch (e: Exception) {
            sendError("Failed to toggle debug mode: ${e.message}")
            CommandResult.ERROR
        }
    }

    /**
     * Version subcommand - shows version information.
     * 版本子命令 - 显示版本信息。
     */
    @SubCommand(
        name = "version",
        aliases = ["ver", "v"],
        description = "Show ArLibs version information"
    )
    fun versionCommand(context: CommandContext): CommandResult {
        val plugin = ArLibs.getInstance()
        val version = plugin.pluginMeta.version
        val authors = plugin.pluginMeta.authors.joinToString(", ")
        
        val messages = mutableListOf(
            "&6ArLibs Framework",
            "&7Version: &e$version",
            "&7Authors: &e$authors",
            "&7Built for: &eBukkit/Spigot/Paper",
            "&7API Version: &e${plugin.pluginMeta.apiVersion ?: "Legacy"}"
        )
        
        if (context.hasPermission("arlibs.command.debug")) {
            messages.addAll(listOf(
                "",
                "&7Debug Information:",
                "&7• Plugin File: &e${plugin.dataFolder.parent}/${plugin.pluginMeta.name}.jar",
                "&7• Data Folder: &e${plugin.dataFolder.absolutePath}",
                "&7• Loaded: &e${if (plugin.isEnabled) "Yes" else "No"}"
            ))
        }
        
        send(*messages.toTypedArray())
        return CommandResult.SUCCESS
    }

    /**
     * Action subcommand - execute action expressions directly.
     * 动作子命令 - 直接执行动作表达式。
     */
    @SubCommand(
        name = "action",
        aliases = ["act"],
        description = "Execute action expressions directly",
        minArgs = 1,
        executor = CommandExecutor.PLAYER
    )
    @Permission("arlibs.command.action", defaultValue = PermissionDefault.OP)
    fun actionCommand(context: CommandContext): CommandResult {
        val actionExpression = context.args.joinToString(" ").trim()
        val player = context.getPlayer()
        
        send("&6Executing action: &e$actionExpression")
        
        return try {
            val job = com.arteam.arLibs.action.ActionAPI.executeAction(player, actionExpression)
            if (job != null) {
                sendSuccess("Action executed successfully")
                CommandResult.SUCCESS
            } else {
                sendError("Failed to parse action expression")
                showActionHelp()
                CommandResult.ERROR
            }
        } catch (e: Exception) {
            sendError("Failed to execute action: ${e.message}")
            showActionHelp()
            CommandResult.ERROR
        }
    }

    /**
     * Condition subcommand - evaluate condition expressions directly.
     * 条件子命令 - 直接评估条件表达式。
     */
    @SubCommand(
        name = "condition",
        aliases = ["cond", "conditions"],
        description = "Evaluate condition expressions directly",
        minArgs = 1,
        executor = CommandExecutor.PLAYER
    )
    @Permission("arlibs.command.condition", defaultValue = PermissionDefault.OP)
    fun conditionCommand(context: CommandContext): CommandResult {
        val conditionExpression = context.args.joinToString(" ").trim()
        val player = context.getPlayer()
        
        send("&6Evaluating condition: &e$conditionExpression")
        
        return try {
            if (!com.arteam.arLibs.condition.ConditionManager.isValidExpression(conditionExpression)) {
                sendError("Invalid condition syntax")
                showConditionHelp()
                return CommandResult.ERROR
            }
            
            val result = com.arteam.arLibs.condition.ConditionManager.evaluate(player, conditionExpression)
            
            if (result) {
                sendSuccess("Condition evaluated to: &atrue")
            } else {
                send("&6Condition evaluated to: &cfalse")
            }
            CommandResult.SUCCESS
            
        } catch (e: Exception) {
            sendError("Error evaluating condition: ${e.message}")
            showConditionHelp()
            CommandResult.ERROR
        }
    }

    private fun showActionHelp() {
        send(
            "&7Supported formats:",
            "&7• tell <message>",
            "&7• sound <sound>-<volume>-<pitch>",
            "&7• if {condition} then {actions} [else {actions}]",
            "&7• delay <ticks>",
            "&7• command <command>",
            "&7• console <command>",
            "&7• actionbar <message>",
            "&7• title `<title>` `<subtitle>` <fadeIn> <stay> <fadeOut>"
        )
    }

    private fun showConditionHelp() {
        send(
            "&7Supported formats:",
            "&7• permission <node>",
            "&7• papi <placeholder> [operator] [value]",
            "&7• any [condition1; condition2; ...]",
            "&7• all [condition1; condition2; ...]",
            "&7• not <condition>"
        )
    }

    /**
     * Tab completion for help subcommand.
     * 帮助子命令的Tab补全。
     */
    @TabComplete(subCommand = "help", argument = 0)
    fun helpTabComplete(context: CommandContext): List<String> = 
        listOf("info", "commands", "reload", "debug", "version", "action", "condition")

    /**
     * Tab completion for commands subcommand.
     * 命令子命令的Tab补全。
     */
    @TabComplete(subCommand = "commands", argument = 0)
    fun commandsTabComplete(context: CommandContext): List<String> {
        return CommandAPI.getAllCommands().values
            .mapNotNull { it.instance.javaClass.`package`?.name?.split(".")?.getOrNull(2) }
            .toSet()
            .sorted()
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
} 