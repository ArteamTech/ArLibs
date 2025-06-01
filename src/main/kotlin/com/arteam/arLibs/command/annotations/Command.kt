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
 * Enum representing who can execute a command.
 * 表示谁可以执行命令的枚举。
 */
enum class CommandExecutor {
    /** Anyone can execute (players and console). 任何人都可以执行（玩家和控制台）。 */
    ALL,
    /** Only players can execute. 仅玩家可以执行。 */
    PLAYER,
    /** Only console can execute. 仅控制台可以执行。 */
    CONSOLE
}

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
 * @param usage Usage information for the command. If empty, will be auto-generated.
 *              命令的使用方法信息。如果为空，将自动生成。
 * @param executor Who can execute this command.
 *                 谁可以执行此命令。
 * @param minArgs Minimum number of arguments required.
 *                所需的最少参数数量。
 * @param maxArgs Maximum number of arguments allowed. -1 for unlimited.
 *                允许的最多参数数量。-1表示无限制。
 * @param async Whether this command should be executed asynchronously.
 *              此命令是否应异步执行。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String = "",
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val executor: CommandExecutor = CommandExecutor.ALL,
    val minArgs: Int = 0,
    val maxArgs: Int = -1,
    val async: Boolean = false
) 