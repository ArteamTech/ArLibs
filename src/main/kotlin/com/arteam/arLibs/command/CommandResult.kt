/**
 * Enum representing the result of command execution.
 * This enum provides standardized return values for command execution methods.
 * 
 * 表示命令执行结果的枚举。
 * 此枚举为命令执行方法提供标准化的返回值。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

/**
 * Command execution result enumeration.
 * 命令执行结果枚举。
 */
enum class CommandResult {
    /**
     * Command executed successfully.
     * 命令执行成功。
     */
    SUCCESS,

    /**
     * Command failed due to insufficient permissions.
     * 由于权限不足导致命令失败。
     */
    NO_PERMISSION,

    /**
     * Command failed due to invalid usage (wrong arguments, etc.).
     * 由于无效使用（错误参数等）导致命令失败。
     */
    INVALID_USAGE,

    /**
     * Command failed due to an error during execution.
     * 由于执行期间发生错误导致命令失败。
     */
    ERROR,

    /**
     * Command is only available to players.
     * 命令仅对玩家可用。
     */
    PLAYER_ONLY,

    /**
     * Command is only available to console.
     * 命令仅对控制台可用。
     */
    CONSOLE_ONLY,

    /**
     * Command or subcommand not found.
     * 未找到命令或子命令。
     */
    NOT_FOUND,

    /**
     * Command was canceled or aborted.
     * 命令被取消或中止。
     */
    CANCELLED
} 