/**
 * Data classes containing parsed command information from annotations.
 * 包含从注解解析的命令信息的数据类。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import com.arteam.arLibs.command.annotations.CommandExecutor
import com.arteam.arLibs.command.annotations.PermissionDefault
import org.bukkit.plugin.Plugin
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Information about a registered command.
 * 已注册命令的信息。
 */
data class CommandInfo(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val executor: CommandExecutor,
    val minArgs: Int,
    val maxArgs: Int,
    val commandClass: KClass<*>,
    val instance: BaseCommand,
    val subCommands: MutableMap<String, SubCommandInfo> = mutableMapOf(),
    val tabCompleters: MutableList<TabCompleterInfo> = mutableListOf(),
    val permissions: List<PermissionInfo> = emptyList(),
    val async: Boolean,
    val plugin: Plugin
)

/**
 * Information about a registered subcommand.
 * 已注册子命令的信息。
 */
data class SubCommandInfo(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val executor: CommandExecutor,
    val minArgs: Int,
    val maxArgs: Int,
    val method: Method,
    val permissions: List<PermissionInfo> = emptyList(),
    val async: Boolean
)

/**
 * Information about a tab completer.
 * Tab补全器的信息。
 */
data class TabCompleterInfo(
    val command: String,
    val subCommand: String,
    val argument: Int,
    val priority: Int,
    val permission: String,
    val staticValues: List<String>,
    val method: Method?
)

/**
 * Information about a permission requirement.
 * 权限要求的信息。
 */
data class PermissionInfo(
    val value: String,
    val op: Boolean,
    val defaultValue: PermissionDefault,
    val message: String,
    val silent: Boolean
) 