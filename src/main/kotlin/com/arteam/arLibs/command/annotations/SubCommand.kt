/**
 * Annotation for marking methods as subcommands within a command class.
 * This annotation allows methods to be registered as subcommands of the main command.
 * 
 * 用于标记命令类中的方法作为子命令的注解。
 * 此注解允许方法被注册为主命令的子命令。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command.annotations

/**
 * Subcommand annotation for defining subcommand properties.
 * 子命令注解，用于定义子命令属性。
 *
 * @param name The name of the subcommand. If empty, use the method name.
 *             子命令的名称。如果为空，使用方法名。
 * @param aliases Alternative names for the subcommand.
 *                子命令的别名。
 * @param description A brief description of what the subcommand does.
 *                    子命令功能的简要描述。
 * @param usage Usage information for the subcommand.
 *              子命令的使用方法信息。
 * @param permission The permission required to use this subcommand. If empty, inherits from parent command.
 *                   使用此子命令所需的权限。如果为空，继承父命令的权限。
 * @param playerOnly Whether this subcommand can only be executed by players.
 *                   此子命令是否只能由玩家执行。
 * @param consoleOnly Whether this subcommand can only be executed from console.
 *                    此子命令是否只能从控制台执行。
 * @param minArgs Minimum number of arguments required for this subcommand.
 *                此子命令所需的最少参数数量。
 * @param maxArgs Maximum number of arguments allowed for this subcommand. -1 for unlimited.
 *                此子命令允许的最多参数数量。-1表示无限制。
 * @param order The order in which this subcommand should be displayed in help. Lower numbers appear first.
 *              此子命令在帮助中显示的顺序。数字越小越靠前。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    val name: String = "",
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val playerOnly: Boolean = false,
    val consoleOnly: Boolean = false,
    val minArgs: Int = 0,
    val maxArgs: Int = -1,
    val order: Int = 100
) 