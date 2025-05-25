/**
 * Data class containing parsed command information from annotations.
 * This class holds all the metadata about a command extracted from its annotations.
 * 
 * 包含从注解解析的命令信息的数据类。
 * 此类保存从注解中提取的关于命令的所有元数据。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import com.arteam.arLibs.command.annotations.PermissionDefault
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Information about a registered command.
 * 已注册命令的信息。
 *
 * @param name The command name.
 *             命令名称。
 * @param aliases Command aliases.
 *                命令别名。
 * @param description Command description.
 *                    命令描述。
 * @param usage Command usage information.
 *              命令使用信息。
 * @param permission Required permission.
 *                   所需权限。
 * @param playerOnly Whether command is player-only.
 *                   命令是否仅限玩家。
 * @param consoleOnly Whether command is console-only.
 *                    命令是否仅限控制台。
 * @param minArgs Minimum arguments required.
 *                所需的最少参数。
 * @param maxArgs Maximum arguments allowed.
 *                允许的最多参数。
 * @param commandClass The command class.
 *                     命令类。
 * @param instance The command instance.
 *                 命令实例。
 * @param subCommands Map of subcommand information.
 *                    子命令信息映射。
 * @param tabCompleters List of tab completion methods.
 *                      Tab补全方法列表。
 * @param permissions Additional permission requirements.
 *                    额外的权限要求。
 */
data class CommandInfo(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val playerOnly: Boolean,
    val consoleOnly: Boolean,
    val minArgs: Int,
    val maxArgs: Int,
    val commandClass: KClass<*>,
    val instance: BaseCommand,
    val subCommands: MutableMap<String, SubCommandInfo> = mutableMapOf(),
    val tabCompleters: MutableList<TabCompleterInfo> = mutableListOf(),
    val permissions: List<PermissionInfo> = emptyList()
)

/**
 * Information about a registered subcommand.
 * 已注册子命令的信息。
 *
 * @param name The subcommand name.
 *             子命令名称。
 * @param aliases Subcommand aliases.
 *                子命令别名。
 * @param description Subcommand description.
 *                    子命令描述。
 * @param usage Subcommand usage information.
 *              子命令使用信息。
 * @param permission Required permission.
 *                   所需权限。
 * @param playerOnly Whether subcommand is player-only.
 *                   子命令是否仅限玩家。
 * @param consoleOnly Whether subcommand is console-only.
 *                    子命令是否仅限控制台。
 * @param minArgs Minimum arguments required.
 *                所需的最少参数。
 * @param maxArgs Maximum arguments allowed.
 *                允许的最多参数。
 * @param order Display order in help.
 *              在帮助中的显示顺序。
 * @param method The method to execute.
 *               要执行的方法。
 * @param permissions Additional permission requirements.
 *                    额外的权限要求。
 */
data class SubCommandInfo(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val playerOnly: Boolean,
    val consoleOnly: Boolean,
    val minArgs: Int,
    val maxArgs: Int,
    val order: Int,
    val method: Method,
    val permissions: List<PermissionInfo> = emptyList()
)

/**
 * Information about a tab completer.
 * Tab补全器的信息。
 *
 * @param command Target command name.
 *                目标命令名称。
 * @param subCommand Target subcommand name.
 *                   目标子命令名称。
 * @param argument Target argument position.
 *                 目标参数位置。
 * @param priority Completion priority.
 *                 补全优先级。
 * @param permission Required permission.
 *                   所需权限。
 * @param staticValues Static completion values.
 *                     静态补全值。
 * @param method The method to execute for completion.
 *               用于补全的执行方法。
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
 *
 * @param value The permission node.
 *              权限节点。
 * @param op Whether operators bypass this permission.
 *           操作员是否绕过此权限。
 * @param defaultValue The default permission value.
 *                     默认权限值。
 * @param message Custom permission denied message.
 *                自定义权限被拒绝消息。
 * @param silent Whether to suppress permission denied messages.
 *               是否禁止显示权限被拒绝消息。
 */
data class PermissionInfo(
    val value: String,
    val op: Boolean,
    val defaultValue: PermissionDefault,
    val message: String,
    val silent: Boolean
) 