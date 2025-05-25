/**
 * Annotation for defining additional permission requirements for commands and subcommands.
 * This annotation provides more granular permission control beyond the basic permission string.
 * 
 * 用于为命令和子命令定义额外权限要求的注解。
 * 此注解提供比基本权限字符串更精细的权限控制。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command.annotations

/**
 * Permission annotation for defining advanced permission requirements.
 * 权限注解，用于定义高级权限要求。
 *
 * @param value The permission node required.
 *              所需的权限节点。
 * @param op Whether operators bypass this permission check.
 *           操作员是否绕过此权限检查。
 * @param defaultValue The default permission value for users without explicit permission.
 *                     对于没有明确权限的用户的默认权限值。
 * @param message Custom message to display when permission is denied.
 *                权限被拒绝时显示的自定义消息。
 * @param silent Whether to suppress the permission denied messages.
 *               是否禁止显示权限被拒绝的消息。
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Permission(
    val value: String,
    val op: Boolean = true,
    val defaultValue: PermissionDefault = PermissionDefault.FALSE,
    val message: String = "",
    val silent: Boolean = false
)

/**
 * Enum representing the default permission state.
 * 表示默认权限状态的枚举。
 */
enum class PermissionDefault {
    TRUE,    // Permission granted by default / 默认授予权限
    FALSE,   // Permission denied by default / 默认拒绝权限
    OP,      // Permission granted to operators only / 仅授予操作员权限
    NOT_OP   // Permission granted to non-operators only / 仅授予非操作员权限
} 