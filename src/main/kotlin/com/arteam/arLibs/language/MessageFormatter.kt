/**
 * Message formatter for processing and formatting language messages.
 * Handles color codes, placeholders, and message formatting.
 *
 * 用于处理和格式化语言消息的消息格式化器。
 * 处理颜色代码、占位符和消息格式化。
 *
 * @author ArteamTech
 * @since 2025-07-12
 * @version 1.0.0
 */
package com.arteam.arLibs.language

import com.arteam.arLibs.utils.ColorUtil
import com.arteam.arLibs.utils.Logger

/**
 * Message formatter for processing and formatting language messages.
 * 用于处理和格式化语言消息的消息格式化器。
 */
object MessageFormatter {

    /**
     * Formats a message with color codes and placeholders.
     * 使用颜色代码和占位符格式化消息。
     *
     * @param message The raw message to format.
     *                要格式化的原始消息。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @return The formatted message.
     *         格式化后的消息。
     */
    fun format(message: String, placeholders: Map<String, String> = emptyMap()): String {
        if (message.isEmpty()) return message

        var formattedMessage = message

        // Replace placeholders
        // 替换占位符
        placeholders.forEach { (key, value) ->
            formattedMessage = formattedMessage.replace("{$key}", value)
        }

        // Process color codes
        // 处理颜色代码
        formattedMessage = ColorUtil.process(formattedMessage)

        return formattedMessage
    }

    /**
     * Formats a list of messages.
     * 格式化消息列表。
     *
     * @param messages The list of messages to format.
     *                 要格式化的消息列表。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @return The list of formatted messages.
     *         格式化后的消息列表。
     */
    fun formatList(messages: List<String>, placeholders: Map<String, String> = emptyMap()): List<String> {
        return messages.map { format(it, placeholders) }
    }

    /**
     * Formats a message with support for plural forms.
     * 格式化支持复数形式的消息。
     *
     * @param singular The singular form of the message.
     *                 消息的单数形式。
     * @param plural The plural form of the message.
     *               消息的复数形式。
     * @param count The count to determine which form to use.
     *              用于确定使用哪种形式的计数。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @return The formatted message.
     *         格式化后的消息。
     */
    fun formatPlural(
        singular: String,
        plural: String,
        count: Int,
        placeholders: Map<String, String> = emptyMap()
    ): String {
        val message = if (count == 1) singular else plural
        val allPlaceholders = placeholders.toMutableMap()
        allPlaceholders["count"] = count.toString()
        
        return format(message, allPlaceholders)
    }

    /**
     * Formats a message with random selection from a list.
     * 从列表中随机选择并格式化消息。
     *
     * @param messages The list of possible messages.
     *                 可能的消息列表。
     * @param placeholders Map of placeholder keys to replacement values.
     *                    占位符键到替换值的映射。
     * @return A randomly selected and formatted message.
     *         随机选择并格式化的消息。
     */
    fun formatRandom(messages: List<String>, placeholders: Map<String, String> = emptyMap()): String {
        if (messages.isEmpty()) return ""
        
        val randomMessage = messages.random()
        return format(randomMessage, placeholders)
    }

    /**
     * Strips color codes from a message.
     * 从消息中移除颜色代码。
     *
     * @param message The message to strip color codes from.
     *                要移除颜色代码的消息。
     * @return The message without color codes.
     *         没有颜色代码的消息。
     */
    fun stripColors(message: String): String {
        return ColorUtil.stripColorCodes(message)
    }

    /**
     * Validates if a message contains valid placeholders.
     * 验证消息是否包含有效的占位符。
     *
     * @param message The message to validate.
     *                要验证的消息。
     * @param availablePlaceholders The list of available placeholder keys.
     *                              可用的占位符键列表。
     * @return True if all placeholders are valid, false otherwise.
     *         如果所有占位符都有效则返回true，否则返回false。
     */
    fun validatePlaceholders(message: String, availablePlaceholders: Set<String>): Boolean {
        val placeholderRegex = Regex("\\{([^}]+)}")
        val foundPlaceholders = placeholderRegex.findAll(message).map { it.groupValues[1] }.toSet()
        
        val invalidPlaceholders = foundPlaceholders - availablePlaceholders
        if (invalidPlaceholders.isNotEmpty()) {
            Logger.debug("Invalid placeholders found in message: $invalidPlaceholders")
            return false
        }
        
        return true
    }
} 