/**
 * Main command management system for the ArLibs framework.
 * This class handles registration, parsing, and execution of annotation-based commands.
 * 
 * ArLibs框架的主要命令管理系统。
 * 此类处理基于注解的命令的注册、解析和执行。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import com.arteam.arLibs.command.annotations.Command
import com.arteam.arLibs.command.annotations.Permission
import com.arteam.arLibs.command.annotations.PermissionDefault
import com.arteam.arLibs.command.annotations.SubCommand
import com.arteam.arLibs.command.annotations.TabComplete
import com.arteam.arLibs.utils.Logger
import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.plugin.Plugin
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Manager for handling annotation-based commands.
 * 处理基于注解的命令的管理器。
 */
object CommandManager {
    private val registeredCommands = mutableMapOf<String, CommandInfo>()
    internal val pluginCommands = mutableMapOf<Plugin, MutableList<String>>()
    
    /**
     * Registers a command class with the command system.
     * 向命令系统注册命令类。
     *
     * @param plugin The plugin registering the command.
     *               注册命令的插件。
     * @param commandClass The command class to register.
     *                     要注册的命令类。
     * @return true if registration was successful, false otherwise.
     *         如果注册成功则返回true，否则返回false。
     */
    fun registerCommand(plugin: Plugin, commandClass: KClass<out BaseCommand>): Boolean {
        return try {
            val commandAnnotationInstance = commandClass.annotations.find { it is Command }
            if (commandAnnotationInstance == null || commandAnnotationInstance !is Command) {
                throw IllegalArgumentException("Command class must be annotated with @Command")
            }
            val commandAnnotation = commandAnnotationInstance // Explicit cast
            
            val commandName = commandAnnotation.name.ifEmpty {
                commandClass.simpleName?.lowercase() ?: throw IllegalArgumentException("Cannot determine command name")
            }
            
            // Check if command already exists
            if (registeredCommands.containsKey(commandName)) {
                Logger.warn("Command '$commandName' is already registered")
                return false
            }
            
            // Create command instance
            val instance = createCommandInstance(commandClass)
            
            // Parse command information
            val commandInfo = parseCommandInfo(commandName, commandClass, instance, commandAnnotation, plugin)
            
            // Register with Bukkit
            if (registerBukkitCommand(plugin, commandInfo)) {
                registeredCommands[commandName] = commandInfo
                
                // Track commands by plugin
                pluginCommands.computeIfAbsent(plugin) { mutableListOf() }.add(commandName)
                
                Logger.info("Successfully registered command: &e$commandName")
                return true
            }
            
            false
        } catch (e: Exception) {
            Logger.severe("Failed to register command class ${commandClass.simpleName}: ${e.message}")
            e.printStackTrace()
            false
        }
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
     */
    fun registerCommands(plugin: Plugin, vararg commandClasses: KClass<out BaseCommand>): Int {
        var successCount = 0
        for (commandClass in commandClasses) {
            if (registerCommand(plugin, commandClass)) {
                successCount++
            }
        }
        return successCount
    }
    
    /**
     * Unregisters all commands for a specific plugin.
     * 注销特定插件的所有命令。
     *
     * @param plugin The plugin whose commands should be unregistered.
     *               要注销命令的插件。
     */
    fun unregisterCommands(plugin: Plugin) {
        val commandNames = pluginCommands[plugin] ?: return
        
        for (commandName in commandNames) {
            registeredCommands.remove(commandName)
            Logger.debug("Unregistered command: $commandName")
        }
        
        pluginCommands.remove(plugin)
        Logger.info("Unregistered ${commandNames.size} commands for plugin ${plugin.name}")
    }
    
    /**
     * Gets information about a registered command.
     * 获取已注册命令的信息。
     *
     * @param commandName The name of the command.
     *                    命令的名称。
     * @return The command information, or null if not found.
     *         命令信息，如果未找到则返回null。
     */
    fun getCommandInfo(commandName: String): CommandInfo? = registeredCommands[commandName]
    
    /**
     * Gets all registered commands.
     * 获取所有已注册的命令。
     *
     * @return A map of command names to command information.
     *         命令名称到命令信息的映射。
     */
    fun getAllCommands(): Map<String, CommandInfo> = registeredCommands.toMap()
    
    /**
     * Gets all commands registered by a specific plugin.
     * 获取特定插件注册的所有命令。
     *
     * @param plugin The plugin to get commands for.
     *               要获取命令的插件。
     * @return A list of command names.
     *         命令名称列表。
     */
    fun getPluginCommands(plugin: Plugin): List<String> = pluginCommands[plugin]?.toList() ?: emptyList()
    
    private fun createCommandInstance(commandClass: KClass<out BaseCommand>): BaseCommand {
        return try {
            // Try no-argument constructor first
            commandClass.createInstance()
        } catch (_: Exception) {
            // Try to find a constructor that we can use
            val constructors = commandClass.java.constructors
            val noArgConstructor = constructors.find { it.parameterCount == 0 }
            
            if (noArgConstructor != null) {
                @Suppress("UNCHECKED_CAST")
                (noArgConstructor as Constructor<BaseCommand>).newInstance()
            } else {
                throw IllegalArgumentException("Command class must have a no-argument constructor")
            }
        }
    }
    
    /**
     * Parses command information from annotations.
     * 从注解解析命令信息。
     *
     * @param commandName The name of the command.
     *                    命令的名称。
     * @param commandClass The command class.
     *                     命令类。
     * @param instance The command instance.
     *                 命令实例。
     * @param commandAnnotation The command annotation.
     *                          命令注解。
     * @param plugin The plugin registering the command.
     *               注册命令的插件。
     * @return The parsed command information.
     *         解析后的命令信息。
     */
    private fun parseCommandInfo(
        commandName: String,
        commandClass: KClass<out BaseCommand>,
        instance: BaseCommand,
        commandAnnotation: Command,
        plugin: Plugin
    ): CommandInfo {
        // Generate permission if not specified
        val permission = commandAnnotation.permission.ifEmpty {
            // Ensure plugin.name is handled safely, though it's unlikely to be null for a loaded plugin
            val pluginNameSafe = plugin.name.lowercase()
            "${pluginNameSafe}.command.${commandName.lowercase()}"
        }
        
        // Parse permission annotations
        val permissions = parsePermissionAnnotations(commandClass.annotations.toTypedArray())
        
        // Create base command info
        return CommandInfo(
            name = commandName,
            aliases = commandAnnotation.aliases.toList(),
            description = commandAnnotation.description,
            usage = commandAnnotation.usage,
            permission = permission,
            playerOnly = commandAnnotation.playerOnly,
            consoleOnly = commandAnnotation.consoleOnly,
            minArgs = commandAnnotation.minArgs,
            maxArgs = commandAnnotation.maxArgs,
            commandClass = commandClass,
            instance = instance,
            permissions = permissions
        ).also {
            // Parse subcommands and tab completers
            parseMethodAnnotations(commandClass, it)
        }
    }
    
    /**
     * Parses method annotations to extract subcommands and tab completers.
     * 解析方法注解以提取子命令和Tab补全器。
     *
     * @param commandClass The command class.
     *                     命令类。
     * @param commandInfo The command information to populate.
     *                    要填充的命令信息。
     */
    private fun parseMethodAnnotations(commandClass: KClass<out BaseCommand>, commandInfo: CommandInfo) {
        for (method in commandClass.java.declaredMethods) {
            method.isAccessible = true
            
            // Parse subcommands
            val subCommandAnnotationInstance = method.getAnnotation(SubCommand::class.java)
            if (subCommandAnnotationInstance != null) {
                parseSubCommand(method, subCommandAnnotationInstance, commandInfo)
            }
            
            // Parse tab completers
            val tabCompleteAnnotationInstance = method.getAnnotation(TabComplete::class.java)
            if (tabCompleteAnnotationInstance != null) {
                parseTabCompleter(method, tabCompleteAnnotationInstance, commandInfo)
            }
        }
        
        // Sort subcommands by order
        commandInfo.subCommands.values.sortedBy { it.order }
    }
    
    /**
     * Parses subcommand information from annotations.
     * 从注解解析子命令信息。
     *
     * @param method The method containing the subcommand annotation.
     *               包含子命令注解的方法。
     * @param annotationInstance The annotation instance.
     *                           注解实例。
     * @param commandInfo The command information to populate.
     *                    要填充的命令信息。
     */
    private fun parseSubCommand(method: Method, annotationInstance: SubCommand, commandInfo: CommandInfo) {
        val annotation = annotationInstance // Explicit cast, though already typed
        val subCommandName = annotation.name.ifEmpty { method.name }
        
        // Generate permission if not specified
        val permission = annotation.permission.ifEmpty {
            "${commandInfo.permission}.${subCommandName.lowercase()}"
        }
        
        val permissions = parsePermissionAnnotations(method.annotations)
        
        val subCommandInfo = SubCommandInfo(
            name = subCommandName,
            aliases = annotation.aliases.toList(),
            description = annotation.description,
            usage = annotation.usage,
            permission = permission,
            playerOnly = annotation.playerOnly,
            consoleOnly = annotation.consoleOnly,
            minArgs = annotation.minArgs,
            maxArgs = annotation.maxArgs,
            order = annotation.order,
            method = method,
            permissions = permissions
        )
        
        commandInfo.subCommands[subCommandName] = subCommandInfo
        
        // Add aliases
        for (alias in annotation.aliases) {
            commandInfo.subCommands[alias] = subCommandInfo
        }
    }
    
    /**
     * Parses tab completer information from annotations.
     * 从注解解析Tab补全器信息。
     *
     * @param method The method containing the tab completer annotation.
     *               包含Tab补全器注解的方法。
     * @param annotation The tab completer annotation.
     *                   Tab补全器注解。
     * @param commandInfo The command information to populate.
     *                    要填充的命令信息。
     */
    private fun parseTabCompleter(method: Method, annotation: TabComplete, commandInfo: CommandInfo) {
        val tabCompleterInfo = TabCompleterInfo(
            command = annotation.command,
            subCommand = annotation.subCommand,
            argument = annotation.argument,
            priority = annotation.priority,
            permission = annotation.permission,
            staticValues = annotation.staticValues.toList(),
            method = if (annotation.staticValues.isEmpty()) method else null
        )
        
        commandInfo.tabCompleters.add(tabCompleterInfo)
    }
    
    /**
     * Parses permission annotations to extract permission information.
     * 解析权限注解以提取权限信息。
     *
     * @param annotations The annotations to parse.
     *                    要解析的注解。
     * @return A list of permission information.
     *         权限信息列表。
     */
    private fun parsePermissionAnnotations(annotations: Array<Annotation>): List<PermissionInfo> {
        return annotations.filterIsInstance<Permission>().map { permission ->
            PermissionInfo(
                value = permission.value,
                op = permission.op,
                defaultValue = permission.defaultValue,
                message = permission.message,
                silent = permission.silent
            )
        }
    }
    
    /**
     * Registers a Bukkit command.
     * 注册Bukkit命令。
     *
     * @param plugin The plugin registering the command.
     *               注册命令的插件。
     * @param commandInfo The command information to register.
     *                    要注册的命令信息。
     * @return true if registration was successful, false otherwise.
     *         如果注册成功则返回true，否则返回false。
     */
    private fun registerBukkitCommand(plugin: Plugin, commandInfo: CommandInfo): Boolean {
        return try {
            val commandMap = getCommandMap()
            val pluginCommand = createPluginCommand(commandInfo.name, plugin)
            
            // Set command properties
            pluginCommand.description = commandInfo.description
            pluginCommand.usage = commandInfo.usage
            pluginCommand.aliases = commandInfo.aliases
            pluginCommand.permission = commandInfo.permission
            
            // Set executor and tab completer
            val executor = ArLibsCommandExecutor(commandInfo)
            pluginCommand.setExecutor(executor)
            pluginCommand.tabCompleter = executor
            
            // Register with the command map
            commandMap.register(plugin.name, pluginCommand)
            
            true
        } catch (e: Exception) {
            Logger.severe("Failed to register Bukkit command ${commandInfo.name}: ${e.message}")
            false
        }
    }
    
    /**
     * Gets the CommandMap instance.
     * 获取CommandMap实例。
     *
     * @return The CommandMap instance.
     *         命令映射实例。
     */
    private fun getCommandMap(): CommandMap {
        val field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
        field.isAccessible = true
        return field.get(Bukkit.getServer()) as CommandMap
    }

    /**
     * Creates a new PluginCommand instance.
     * 创建一个新的PluginCommand实例。
     *
     * @param name The name of the command.
     *             命令的名称。
     * @param plugin The plugin registering the command.
     *               注册命令的插件。
     * @return The new PluginCommand instance.
     *         新的PluginCommand实例。
     */
    private fun createPluginCommand(name: String, plugin: Plugin): PluginCommand {
        val constructor = PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(name, plugin)
    }
}

/**
 * Bukkit command executor that bridges to our annotation-based system.
 * Bukkit命令执行器，桥接到我们基于注解的系统。
 * 
 * @param commandInfo The command information.
 *                    命令信息。
 */
private class ArLibsCommandExecutor(private val commandInfo: CommandInfo) : CommandExecutor, TabCompleter {
    /**
     * Handles the execution of a command.
     * 处理命令的执行。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param command The command object.
     *                命令对象。
     * @param label The command label.
     *              命令标签。
     * @param args The command arguments.
     *             命令参数。
     * @return true if the command was successful, false otherwise.
     *         如果命令成功则返回true，否则返回false。
     */
    override fun onCommand(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        label: String,
        args: Array<String>
    ): Boolean {
        try {
            // Find the plugin that registered this command
            val plugin = CommandManager.pluginCommands.entries.find { entry ->
                entry.value.contains(commandInfo.name)
            }?.key ?: Bukkit.getPluginManager().getPlugin("ArLibs")!!
            
            val context = CommandContext(
                sender = sender,
                plugin = plugin,
                command = commandInfo.name,
                subCommand = null,
                args = args,
                rawArgs = args,
                label = label
            )
            
            // Check basic permissions and restrictions
            val permissionResult = checkPermissions(context, commandInfo)
            if (permissionResult != CommandResult.SUCCESS) {
                handleCommandResult(context, permissionResult)
                return true
            }
            
            val executionResult = if (args.isNotEmpty()) {
                executeSubCommand(context, args)
            } else {
                executeMainCommand(context)
            }
            
            handleCommandResult(context, executionResult)
            return true
        } catch (e: Exception) {
            Logger.severe("Error executing command ${commandInfo.name}: ${e.message}")
            e.printStackTrace()
            sender.sendMessage("§cAn error occurred while executing the command.")
            return true
        }
    }

    /**
     * Handles tab completion for a command.
     * 处理命令的Tab补全。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param command The command object.
     *                命令对象。
     * @param alias The command alias.
     *              命令别名。
     * @param args The command arguments.
     *             命令参数。
     * @return A list of tab completions.
     *         一个Tab补全列表。
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: org.bukkit.command.Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        try {
            // Find the plugin that registered this command
            val plugin = CommandManager.pluginCommands.entries.find { entry ->
                entry.value.contains(commandInfo.name)
            }?.key ?: Bukkit.getPluginManager().getPlugin("ArLibs")!!
            
            val context = CommandContext(
                sender = sender,
                plugin = plugin,
                command = commandInfo.name,
                subCommand = null,
                args = args,
                rawArgs = args,
                label = alias
            )
            
            return generateTabCompletions(context)
        } catch (e: Exception) {
            Logger.debug("Error generating tab completions: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Executes the main command.
     * 执行主命令。
     *
     * @param context The command context.
     *                命令上下文。
     * @return The result of the command execution.
     *         命令执行的结果。
     */
    private fun executeMainCommand(context: CommandContext): CommandResult {
        if (!commandInfo.instance.onPreExecute(context)) {
            return CommandResult.CANCELLED
        }
        
        val result = commandInfo.instance.execute(context)
        commandInfo.instance.onPostExecute(context, result)
        return result
    }
    
    /**
     * Executes a subcommand.
     * 执行子命令。
     *
     * @param context The command context.
     *                命令上下文。
     * @param args The command arguments.
     *             命令参数。
     * @return The result of the subcommand execution.
     *         子命令执行的结果。
     */
    private fun executeSubCommand(context: CommandContext, args: Array<String>): CommandResult {
        val subCommandName = args[0].lowercase()
        val subCommandInfo = commandInfo.subCommands[subCommandName]
            ?: return CommandResult.NOT_FOUND
        
        val subCommandContext = context.copy(
            subCommand = subCommandInfo.name,
            args = args.sliceArray(1 until args.size)
        )
        
        // Check subcommand permissions
        val permissionResult = checkPermissions(subCommandContext, subCommandInfo)
        if (permissionResult != CommandResult.SUCCESS) {
            return permissionResult
        }
        
        if (!commandInfo.instance.onPreExecute(subCommandContext)) {
            return CommandResult.CANCELLED
        }
        
        val result = try {
            subCommandInfo.method.invoke(commandInfo.instance, subCommandContext) as? CommandResult
                ?: CommandResult.SUCCESS
        } catch (e: Exception) {
            Logger.severe("Error executing subcommand ${subCommandInfo.name}: ${e.message}")
            CommandResult.ERROR
        }
        
        commandInfo.instance.onPostExecute(subCommandContext, result)
        return result
    }
    
    /**
     * Checks permissions for a command.
     * 检查命令的权限。
     *
     * @param context The command context.
     *               命令上下文。
     * @param info The command information.
     *             命令信息。
     * @return The result of the permission check.
     *         权限检查的结果。
     */
    private fun checkPermissions(context: CommandContext, info: CommandInfo): CommandResult {
        return checkPermissionsCommon(context, info.permission, info.playerOnly, info.consoleOnly, info.permissions)
    }
    
    /**
     * Checks permissions for a subcommand.
     * 检查子命令的权限。
     *
     * @param context The command context.
     *                命令上下文。
     * @param info The subcommand information.
     *             子命令信息。
     * @return The result of the permission check.
     *         权限检查的结果。
     */
    private fun checkPermissions(context: CommandContext, info: SubCommandInfo): CommandResult {
        return checkPermissionsCommon(context, info.permission, info.playerOnly, info.consoleOnly, info.permissions)
    }
    
    /**
     * Checks permissions for a command.
     * 检查命令的权限。
     *
     * @param context The command context.
     *                命令上下文。
     * @param permission The permission string.
     *                   权限字符串。
     * @param playerOnly Whether the command is player-only.
     *                   是否仅限玩家。
     * @param consoleOnly Whether the command is console-only.
     *                    是否仅限控制台。
     * @param permissions The list of permission information.
     *                    权限信息列表。
     * @return The result of the permission check.
     *         权限检查的结果。
     */
    private fun checkPermissionsCommon(
        context: CommandContext,
        permission: String,
        playerOnly: Boolean,
        consoleOnly: Boolean,
        permissions: List<PermissionInfo>
    ): CommandResult {
        // Check sender type restrictions
        if (playerOnly && !context.isPlayer()) {
            return CommandResult.PLAYER_ONLY
        }
        
        if (consoleOnly && context.isPlayer()) {
            return CommandResult.CONSOLE_ONLY
        }
        
        // Check basic permission
        if (permission.isNotEmpty() && !context.hasPermission(permission)) {
            return CommandResult.NO_PERMISSION
        }
        
        // Check additional permissions
        for (permInfo in permissions) {
            if (!checkPermission(context.sender, permInfo)) {
                if (!permInfo.silent && permInfo.message.isNotEmpty()) {
                    context.sender.sendMessage(permInfo.message)
                }
                return CommandResult.NO_PERMISSION
            }
        }
        
        return CommandResult.SUCCESS
    }

    /**
     * Checks a permission for a sender.
     * 检查一个权限给定发送者。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param permInfo The permission information.
     *                 权限信息。
     * @return true if the permission is granted, false otherwise.
     *         如果权限被授予则返回true，否则返回false。
     */
    private fun checkPermission(sender: CommandSender, permInfo: PermissionInfo): Boolean {
        val isOp = sender.isOp
        
        if (permInfo.op && isOp) {
            return true
        }
        
        return when (permInfo.defaultValue) {
            PermissionDefault.TRUE -> sender.hasPermission(permInfo.value) || !sender.isPermissionSet(permInfo.value)
            PermissionDefault.FALSE -> sender.hasPermission(permInfo.value)
            PermissionDefault.OP -> isOp || sender.hasPermission(permInfo.value)
            PermissionDefault.NOT_OP -> !isOp && (sender.hasPermission(permInfo.value) || !sender.isPermissionSet(permInfo.value))
        }
    }

    /**
     * Generates tab completions for a command.
     * 生成命令的Tab补全。
     *
     * @param context The command context.
     *               命令上下文。
     * @return A list of tab completions.
     *         一个Tab补全列表。
     */
    private fun generateTabCompletions(context: CommandContext): List<String> {
        val completions = mutableListOf<String>()
        
        if (context.args.size == 1) {
            // Complete subcommand names and aliases
            val input = context.args[0]
            val uniqueSubCommands = mutableSetOf<SubCommandInfo>()
            
            // Collect all unique subcommands (avoiding duplicates from aliases)
            for (subCommand in commandInfo.subCommands.values) {
                uniqueSubCommands.add(subCommand)
            }
            
            // Add matching subcommand names and aliases
            for (subCommand in uniqueSubCommands) {
                if (hasSubCommandPermission(context.sender, subCommand)) {
                    // Add the main name if it matches
                    if (subCommand.name.startsWith(input, ignoreCase = true)) {
                        completions.add(subCommand.name)
                    }
                    
                    // Add aliases if they match
                    for (alias in subCommand.aliases) {
                        if (alias.startsWith(input, ignoreCase = true)) {
                            completions.add(alias)
                        }
                    }
                }
            }
            
            // Also add main command tab completions for the first argument
            val mainCommandCompletions = getTabCompleterResults(context)
            completions.addAll(mainCommandCompletions)
            
        } else if (context.args.size > 1) {
            // Complete subcommand arguments
            val subCommandName = context.args[0].lowercase()
            val subCommandInfo = commandInfo.subCommands[subCommandName]
            
            if (subCommandInfo != null && hasSubCommandPermission(context.sender, subCommandInfo)) {
                val subContext = context.copy(
                    subCommand = subCommandInfo.name,
                    args = context.args.sliceArray(1 until context.args.size)
                )
                
                completions.addAll(getSubCommandCompletions(subContext, subCommandInfo))
            }
        } else {
            // No arguments yet, show all available subcommands
            val uniqueSubCommands = mutableSetOf<SubCommandInfo>()
            
            for (subCommand in commandInfo.subCommands.values) {
                uniqueSubCommands.add(subCommand)
            }
            
            for (subCommand in uniqueSubCommands) {
                if (hasSubCommandPermission(context.sender, subCommand)) {
                    completions.add(subCommand.name)
                    completions.addAll(subCommand.aliases)
                }
            }
            
            // Also add main command completions
            val mainCommandCompletions = getTabCompleterResults(context)
            completions.addAll(mainCommandCompletions)
        }
        
        return completions.distinct().sorted()
    }

    /**
     * Checks if a subcommand has permission.
     * 检查子命令是否有权限。
     *
     * @param sender The command sender.
     *              命令发送者。
     * @param subCommand The subcommand information.
     *                  子命令信息。
     * @return true if the subcommand has permission, false otherwise.
     *         如果子命令有权限则返回true，否则返回false。
     */
    private fun hasSubCommandPermission(sender: CommandSender, subCommand: SubCommandInfo): Boolean {
        if (subCommand.permission.isNotEmpty() && !sender.hasPermission(subCommand.permission)) {
            return false
        }
        
        for (permInfo in subCommand.permissions) {
            if (!checkPermission(sender, permInfo)) {
                return false
            }
        }
        
        return true
    }

    /**
     * Gets completions for a subcommand.
     * 获取子命令的补全。
     *
     * @param context The command context.
     *               命令上下文。
     * @param subCommandInfo The subcommand information.
     *                      子命令信息。
     * @return A list of completions.
     *         一个补全列表。
     */
    private fun getSubCommandCompletions(context: CommandContext, subCommandInfo: SubCommandInfo): List<String> {
        val completions = mutableListOf<String>()
        
        // 1. First, try to get completions from the command instance's onTabComplete method
        try {
            val baseCompletions = commandInfo.instance.onTabComplete(context)
            completions.addAll(baseCompletions)
        } catch (e: Exception) {
            Logger.debug("Error calling onTabComplete for subcommand ${subCommandInfo.name}: ${e.message}")
        }
        
        // 2. Then, get completions from @TabComplete annotations specific to this subcommand
        val subCommandTabCompletions = getTabCompleterResultsForSubCommand(context, subCommandInfo)
        completions.addAll(subCommandTabCompletions)
        
        // 3. Filter completions based on current input
        val currentArg = context.args.lastOrNull() ?: ""
        val filteredCompletions = completions.filter { 
            it.startsWith(currentArg, ignoreCase = true) 
        }
        
        return filteredCompletions.distinct().sorted()
    }

    /**
     * Get tab completion results specifically for a subcommand from @TabComplete annotations.
     * 获取子命令特定的@TabComplete注解的补全结果。
     *
     * @param context The command context.
     *               命令上下文。
     * @param subCommandInfo The subcommand information.
     *                      子命令信息。
     * @return A list of tab completion results.
     *         一个Tab补全结果列表。
     */
    private fun getTabCompleterResultsForSubCommand(context: CommandContext, subCommandInfo: SubCommandInfo): List<String> {
        val results = mutableListOf<String>()
        val currentArgIndex = context.args.size - 1 // Current argument being typed
        
        for (completer in commandInfo.tabCompleters.sortedByDescending { it.priority }) {
            // To apply in a subcommand context, the completer MUST specify this subcommand by name.
            if (completer.subCommand != subCommandInfo.name) {
                continue
            }

            // If the completer also specifies a main command, it must match the current one.
            // (This is mostly for sanity, as TabCompleterInfo is already part of a CommandInfo)
            if (completer.command.isNotEmpty() && completer.command != commandInfo.name) {
                continue
            }
            
            // Check if this completer applies to the current argument position
            if (completer.argument >= 0 && completer.argument != currentArgIndex) continue
            
            // Check permission
            if (completer.permission.isNotEmpty() && !context.hasPermission(completer.permission)) continue
            
            // Get completions
            if (completer.staticValues.isNotEmpty()) {
                results.addAll(completer.staticValues)
            } else if (completer.method != null) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val methodResults = completer.method.invoke(commandInfo.instance, context) as? List<String>
                    if (methodResults != null) {
                        results.addAll(methodResults)
                    }
                } catch (e: Exception) {
                    Logger.debug("Error executing tab completer method for subcommand ${subCommandInfo.name}: ${e.message}")
                }
            }
        }
        
        return results
    }

    /**
     * Gets tab completion results for the main command.
     * 获取主命令的Tab补全结果。
     *
     * @param context The command context.
     *               命令上下文。
     * @return A list of tab completion results.
     *         一个Tab补全结果列表。
     */
    private fun getTabCompleterResults(context: CommandContext): List<String> {
        val results = mutableListOf<String>()
        val currentArgIndex = if (context.args.isEmpty()) 0 else context.args.size - 1
        
        for (completer in commandInfo.tabCompleters.sortedByDescending { it.priority }) {
            // For main command completions (no subcommand context)
            if (context.subCommand != null) {
                // If we're in a subcommand context, skip main command completers
                // unless they specifically target this subcommand
                if (completer.subCommand.isEmpty() || completer.subCommand != context.subCommand) {
                    continue
                }
            } else {
                // If we're in main command context, only use completers that don't specify a subcommand
                // or specifically target the main command
                if (completer.subCommand.isNotEmpty()) continue
            }
            
            // Check if this completer applies to the current argument position
            if (completer.argument >= 0 && completer.argument != currentArgIndex) continue
            
            // Check permission
            if (completer.permission.isNotEmpty() && !context.hasPermission(completer.permission)) continue
            
            // Get completions
            if (completer.staticValues.isNotEmpty()) {
                val currentInput = context.args.lastOrNull() ?: ""
                val matchingValues = completer.staticValues.filter { 
                    it.startsWith(currentInput, ignoreCase = true) 
                }
                results.addAll(matchingValues)
            } else if (completer.method != null) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val methodResults = completer.method.invoke(commandInfo.instance, context) as? List<String>
                    if (methodResults != null) {
                        val currentInput = context.args.lastOrNull() ?: ""
                        val matchingResults = methodResults.filter { 
                            it.startsWith(currentInput, ignoreCase = true) 
                        }
                        results.addAll(matchingResults)
                    }
                } catch (e: Exception) {
                    Logger.debug("Error executing tab completer: ${e.message}")
                }
            }
        }
        
        return results
    }

    /**
     * Handles the result of a command execution.
     * 处理命令执行的结果。
     *
     * @param context The command context.
     *               命令上下文。
     * @param result The result of the command execution.
     *              命令执行的结果。
     */
    private fun handleCommandResult(context: CommandContext, result: CommandResult) {
        when (result) {
            CommandResult.NO_PERMISSION -> {
                context.sender.sendMessage("§cYou don't have permission to use this command.")
            }
            CommandResult.INVALID_USAGE -> {
                val usage = if (context.subCommand != null) {
                    commandInfo.subCommands[context.subCommand]?.usage ?: commandInfo.usage
                } else {
                    commandInfo.usage
                }
                if (usage.isNotEmpty()) {
                    context.sender.sendMessage("§cUsage: $usage")
                } else {
                    context.sender.sendMessage("§cInvalid command usage.")
                }
            }
            CommandResult.PLAYER_ONLY -> {
                context.sender.sendMessage("§cThis command can only be used by players.")
            }
            CommandResult.CONSOLE_ONLY -> {
                context.sender.sendMessage("§cThis command can only be used from console.")
            }
            CommandResult.NOT_FOUND -> {
                context.sender.sendMessage("§cSubcommand not found.")
            }
            CommandResult.ERROR -> {
                context.sender.sendMessage("§cAn error occurred while executing the command.")
            }
            else -> {
                // SUCCESS or CANCELLED - do nothing
            }
        }
    }
} 