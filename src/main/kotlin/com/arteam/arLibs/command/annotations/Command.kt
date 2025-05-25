/**
 * Annotation for marking command classes and defining their basic properties.
 * This annotation is used to register command classes with the command system.
 * 
 * 用于标记命令类并定义其基本属性的注解。
 * 此注解用于在命令系统中注册命令类。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command.annotations

/**
 * Main command annotation for defining command properties.
 * 主命令注解，用于定义命令属性。
 *
 * @param name The name of the command. If empty, use the class name in lowercase.
 *             命令的名称。如果为空，使用小写的类名。
 * @param aliases Alternative names for the command.
 *                命令的别名。
 * @param description A brief description of what the command does.
 *                    命令功能的简要描述。
 * @param usage Usage information for the command.
 *              命令的使用方法信息。
 * @param permission The permission required to use this command. If empty, auto-generates based on command name.
 *                   使用此命令所需的权限。如果为空，根据命令名称自动生成。
 * @param playerOnly Whether this command can only be executed by players.
 *                   此命令是否只能由玩家执行。
 * @param consoleOnly Whether this command can only be executed from console.
 *                    此命令是否只能从控制台执行。
 * @param minArgs Minimum number of arguments required.
 *                所需的最少参数数量。
 * @param maxArgs Maximum number of arguments allowed. -1 for unlimited.
 *                允许的最多参数数量。-1表示无限制。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String = "",
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val playerOnly: Boolean = false,
    val consoleOnly: Boolean = false,
    val minArgs: Int = 0,
    val maxArgs: Int = -1
) 