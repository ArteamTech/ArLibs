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
import com.arteam.arLibs.language.LanguageAPI
import org.bukkit.entity.Player

/**
 * Feedback message types for command results.
 * 命令结果的反馈消息类型。
 */
enum class FeedbackType {
    NO_PERMISSION,
    INVALID_USAGE,
    PLAYER_ONLY,
    CONSOLE_ONLY,
    SUBCOMMAND_NOT_FOUND,
    ERROR
}

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
    open fun execute(context: CommandContext): CommandResult = CommandResult.INVALID_USAGE

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
    open fun onTabComplete(context: CommandContext): List<String> = emptyList()

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
    open fun onPreExecute(context: CommandContext): Boolean = true

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
    open fun onPostExecute(context: CommandContext, result: CommandResult) {}

    /** Current command context. 当前命令上下文。 */
    private var currentContext: CommandContext? = null
    
    /** Set the current command context. 设置当前命令上下文。 */
    internal fun setContext(context: CommandContext) {
        this.currentContext = context
    }
    
    /** Clear the current command context. 清除当前命令上下文。 */
    internal fun clearContext() {
        this.currentContext = null
    }
    
    // Simplified message sending methods / 简化的消息发送方法
    
    /** Send a normal message. 发送普通消息。 */
    protected fun send(message: String) = getCurrentContext().sender.sendMessage(ColorUtil.process(message))
    
    /** Send multiple messages. 发送多条消息。 */
    protected fun send(vararg messages: String?) = messages.filterNotNull().forEach { send(it) }
    
    /** Send an error message. 发送错误消息。 */
    protected fun sendError(message: String) = getCurrentContext().sender.sendMessage(ColorUtil.process("&c$message"))
    
    /** Send a success message. 发送成功消息。 */
    protected fun sendSuccess(message: String) = getCurrentContext().sender.sendMessage(ColorUtil.process("&a$message"))
    
    /** Send a warning message. 发送警告消息。 */
    protected fun sendWarning(message: String) = getCurrentContext().sender.sendMessage(ColorUtil.process("&e$message"))
    
    /** Send an info message. 发送信息消息。 */
    protected fun sendInfo(message: String) = getCurrentContext().sender.sendMessage(ColorUtil.process("&b$message"))
    
    // Unified message sending methods with language support / 支持语言的统一消息发送方法
    
    /**
     * Send a localized message that adapts to player/console automatically.
     * 发送自动适应玩家/控制台的本地化消息。
     *
     * @param messageKey The language key for the message.
     *                   消息的语言键。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @param fallbackMessage Fallback message if language key is not found.
     *                        如果找不到语言键时的回退消息。
     */
    protected fun sendLocalized(messageKey: String, placeholders: Map<String, String> = emptyMap(), fallbackMessage: String? = null) {
        val context = getCurrentContext()
        val message = when (context.sender) {
            is Player -> LanguageAPI.getMessage(context.sender, messageKey, placeholders)
            else -> fallbackMessage ?: LanguageAPI.getMessage(messageKey, placeholders)
        }
        send(message)
    }
    
    /**
     * Send multiple localized messages.
     * 发送多条本地化消息。
     *
     * @param messages List of message keys and their fallback messages.
     *                 消息键和回退消息的列表。
     */
    protected fun sendLocalized(vararg messages: Pair<String, String?>) {
        messages.forEach { (key, fallback) ->
            sendLocalized(key, fallbackMessage = fallback)
        }
    }
    
    /**
     * Send a localized error message.
     * 发送本地化错误消息。
     *
     * @param messageKey The language key for the error message.
     *                   错误消息的语言键。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @param fallbackMessage Fallback error message.
     *                        回退错误消息。
     */
    protected fun sendLocalizedError(messageKey: String, placeholders: Map<String, String> = emptyMap(), fallbackMessage: String? = null) {
        val context = getCurrentContext()
        val message = when (context.sender) {
            is Player -> LanguageAPI.getMessage(context.sender, messageKey, placeholders)
            else -> fallbackMessage ?: LanguageAPI.getMessage(messageKey, placeholders)
        }
        sendError(message)
    }
    
    /**
     * Send a localized success message.
     * 发送本地化成功消息。
     *
     * @param messageKey The language key for the success message.
     *                   成功消息的语言键。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @param fallbackMessage Fallback success message.
     *                        回退成功消息。
     */
    protected fun sendLocalizedSuccess(messageKey: String, placeholders: Map<String, String> = emptyMap(), fallbackMessage: String? = null) {
        val context = getCurrentContext()
        val message = when (context.sender) {
            is Player -> LanguageAPI.getMessage(context.sender, messageKey, placeholders)
            else -> fallbackMessage ?: LanguageAPI.getMessage(messageKey, placeholders)
        }
        sendSuccess(message)
    }
    
    /**
     * Send a localized warning message.
     * 发送本地化警告消息。
     *
     * @param messageKey The language key for the warning message.
     *                   警告消息的语言键。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @param fallbackMessage Fallback warning message.
     *                        回退警告消息。
     */
    protected fun sendLocalizedWarning(messageKey: String, placeholders: Map<String, String> = emptyMap(), fallbackMessage: String? = null) {
        val context = getCurrentContext()
        val message = when (context.sender) {
            is Player -> LanguageAPI.getMessage(context.sender, messageKey, placeholders)
            else -> fallbackMessage ?: LanguageAPI.getMessage(messageKey, placeholders)
        }
        sendWarning(message)
    }
    
    /**
     * Check if the sender is a player.
     * 检查发送者是否为玩家。
     *
     * @return true if the sender is a player, false otherwise.
     *         如果发送者是玩家则返回true，否则返回false。
     */
    protected fun isPlayer(): Boolean = getCurrentContext().sender is Player
    
    /**
     * Get the player sender, or null if it's console.
     * 获取玩家发送者，如果是控制台则返回null。
     *
     * @return The player sender, or null if console.
     *         玩家发送者，如果是控制台则返回null。
     */
    protected fun getPlayerOrNull(): Player? = getCurrentContext().sender as? Player
    
    /**
     * Get the player sender, throwing an exception if it's console.
     * 获取玩家发送者，如果是控制台则抛出异常。
     *
     * @return The player sender.
     *         玩家发送者。
     * @throws IllegalStateException if the sender is not a player.
     *                               如果发送者不是玩家则抛出IllegalStateException。
     */
    protected fun getPlayer(): Player = getCurrentContext().sender as? Player 
        ?: throw IllegalStateException("This command requires a player sender")
    
    /**
     * Get a localized message for the current sender.
     * 为当前发送者获取本地化消息。
     *
     * @param messageKey The language key for the message.
     *                   消息的语言键。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @param fallback Fallback message if language key is not found.
     *                 如果找不到语言键时的回退消息。
     * @return The localized message.
     *         本地化消息。
     */
    protected fun getLocalizedMessage(messageKey: String, placeholders: Map<String, String> = emptyMap(), fallback: String? = null): String {
        val context = getCurrentContext()
        return when (context.sender) {
            is Player -> LanguageAPI.getMessage(context.sender, messageKey, placeholders)
            else -> fallback ?: LanguageAPI.getMessage(messageKey, placeholders)
        }
    }
    
    /** Get current command context. 获取当前命令上下文。 */
    protected fun getCurrentContext(): CommandContext = 
        currentContext ?: throw IllegalStateException("Command context not available. This method can only be called during command execution.")

    /**
     * Get the feedback message for a given command context and type.
     * 获取给定命令上下文和类型的反馈消息。
     */
    open fun getFeedbackMessage(context: CommandContext, type: FeedbackType): String = when (type) {
        FeedbackType.NO_PERMISSION -> "&cYou don't have permission to use this command."
        FeedbackType.INVALID_USAGE -> {
            val usage = CommandAPI.getCommandInfo(context.command)?.let { info ->
                info.subCommands[context.subCommand]?.usage ?: info.usage
            } ?: ""
            "&cUsage: $usage".takeIf { usage.isNotEmpty() } ?: "&cInvalid command usage."
        }
        FeedbackType.PLAYER_ONLY -> "&cThis command can only be used by players."
        FeedbackType.CONSOLE_ONLY -> "&cThis command can only be used from console."
        FeedbackType.SUBCOMMAND_NOT_FOUND -> "&cSubcommand not found."
        FeedbackType.ERROR -> "&cAn error occurred while executing the command."
    }
} 