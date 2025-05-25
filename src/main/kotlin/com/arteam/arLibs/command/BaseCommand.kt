/**
 * Abstract base class for command implementations.
 * This class provides the foundation for all command classes in the system.
 * 
 * 命令实现的抽象基类。
 * 此类为系统中的所有命令类提供基础。
 *
 * @author ArteamTech
 * @since 2025-05-25
 * @version 1.0.0
 */
package com.arteam.arLibs.command

import com.arteam.arLibs.utils.ColorUtil
import org.bukkit.command.CommandSender

/**
 * Base class for all command implementations.
 * 所有命令实现的基类。
 */
@Suppress("unused")
abstract class BaseCommand {
    /**
     * Executes the main command when no subcommand is specified.
     * This method is called when the command is executed without any subcommand.
     * 
     * 当未指定子命令时执行主命令。
     * 当命令在没有任何子命令的情况下执行时调用此方法。
     *
     * @param context The command execution context.
     *                命令执行上下文。
     * @return The result of command execution.
     *         命令执行的结果。
     */
    open fun execute(context: CommandContext): CommandResult {
        return CommandResult.INVALID_USAGE
    }

    /**
     * Provides tab completion suggestions for the command.
     * This method is called when players press Tab while typing the command.
     * 
     * 为命令提供Tab补全建议。
     * 当玩家在输入命令时按Tab键时调用此方法。
     *
     * @param context The command execution context.
     *                命令执行上下文。
     * @return A list of completion suggestions.
     *         补全建议列表。
     */
    open fun onTabComplete(context: CommandContext): List<String> {
        return emptyList()
    }

    /**
     * Called before command execution for validation and preprocessing.
     * This method can be overridden to perform custom validation.
     * 
     * 在命令执行前调用以进行验证和预处理。
     * 可以重写此方法以执行自定义验证。
     *
     * @param context The command execution context.
     *                命令执行上下文。
     * @return true to continue execution, false to cancel.
     *         返回true继续执行，返回false取消执行。
     */
    open fun onPreExecute(context: CommandContext): Boolean {
        return true
    }

    /**
     * Called after command execution for cleanup and post-processing.
     * This method is called regardless of the execution result.
     * 
     * 在命令执行后调用以进行清理和后处理。
     * 无论执行结果如何都会调用此方法。
     *
     * @param context The command execution context.
     *                命令执行上下文。
     * @param result The result of command execution.
     *               命令执行的结果。
     */
    open fun onPostExecute(context: CommandContext, result: CommandResult) {
        // Default implementation does nothing
    }

    /**
     * Sends a message to the command sender with color support.
     * 向命令发送者发送支持颜色的消息。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param message The message to send.
     *                要发送的消息。
     */
    protected fun sendMessage(sender: CommandSender, message: String) {
        sender.sendMessage(ColorUtil.process(message))
    }

    /**
     * Sends a message to the command sender using the context.
     * 使用上下文向命令发送者发送消息。
     *
     * @param context The command context.
     *                命令上下文。
     * @param message The message to send.
     *                要发送的消息。
     */
    protected fun sendMessage(context: CommandContext, message: String) {
        sendMessage(context.sender, message)
    }

    /**
     * Sends an error message to the command sender.
     * 向命令发送者发送错误消息。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param message The error message to send.
     *                要发送的错误消息。
     */
    protected fun sendError(sender: CommandSender, message: String) {
        sendMessage(sender, "&c$message")
    }

    /**
     * Sends an error message to the command sender using the context.
     * 使用上下文向命令发送者发送错误消息。
     *
     * @param context The command context.
     *                命令上下文。
     * @param message The error message to send.
     *                要发送的错误消息。
     */
    protected fun sendError(context: CommandContext, message: String) {
        sendError(context.sender, message)
    }

    /**
     * Sends a success message to the command sender.
     * 向命令发送者发送成功消息。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param message The success message to send.
     *                要发送的成功消息。
     */
    protected fun sendSuccess(sender: CommandSender, message: String) {
        sendMessage(sender, "&a$message")
    }

    /**
     * Sends a success message to the command sender using the context.
     * 使用上下文向命令发送者发送成功消息。
     *
     * @param context The command context.
     *                命令上下文。
     * @param message The success message to send.
     *                要发送的成功消息。
     */
    protected fun sendSuccess(context: CommandContext, message: String) {
        sendSuccess(context.sender, message)
    }

    /**
     * Sends a warning message to the command sender.
     * 向命令发送者发送警告消息。
     *
     * @param sender The command sender.
     *               命令发送者。
     * @param message The warning message to send.
     *                要发送的警告消息。
     */
    protected fun sendWarning(sender: CommandSender, message: String) {
        sendMessage(sender, "&e$message")
    }

    /**
     * Sends a warning message to the command sender using the context.
     * 使用上下文向命令发送者发送警告消息。
     *
     * @param context The command context.
     *                命令上下文。
     * @param message The warning message to send.
     *                要发送的警告消息。
     */
    protected fun sendWarning(context: CommandContext, message: String) {
        sendWarning(context.sender, message)
    }
} 