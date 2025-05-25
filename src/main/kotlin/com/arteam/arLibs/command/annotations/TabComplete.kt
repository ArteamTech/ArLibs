/**
 * Annotation for marking methods as tab completion providers for commands and subcommands.
 * This annotation allows methods to provide custom tab completion suggestions.
 * 
 * 用于标记方法作为命令和子命令的Tab补全提供者的注解。
 * 此注解允许方法提供自定义的Tab补全建议。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command.annotations

/**
 * Tab completion annotation for defining custom tab completion behavior.
 * Tab补全注解，用于定义自定义Tab补全行为。
 *
 * @param command The command name this tab completion applies to. If empty, apply to the main command.
 *                此Tab补全适用的命令名称。如果为空，适用于主命令。
 * @param subCommand The subcommand name this tab completion applies to. If empty, apply to all subcommands.
 *                   此Tab补全适用的子命令名称。如果为空，适用于所有子命令。
 * @param argument The argument position this tab completion applies to. 0-based index, -1 for all positions.
 *                 此Tab补全适用的参数位置。基于0的索引，-1表示所有位置。
 * @param priority The priority of this tab completion. Higher values take precedence.
 *                 此Tab补全的优先级。数值越高优先级越高。
 * @param permission The permission required to see these tab completions.
 *                   查看这些Tab补全所需的权限。
 * @param staticValues Static values to suggest for tab completion.
 *                     用于Tab补全的静态值。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TabComplete(
    val command: String = "",
    val subCommand: String = "",
    val argument: Int = -1,
    val priority: Int = 0,
    val permission: String = "",
    val staticValues: Array<String> = []
) 