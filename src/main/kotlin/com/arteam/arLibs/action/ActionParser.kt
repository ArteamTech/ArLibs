/**
 * Parser for action strings from configuration files.
 * This class converts action strings into executable Action objects.
 *
 * 用于解析配置文件中动作字符串的解析器。
 * 此类将动作字符串转换为可执行的Action对象。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import com.arteam.arLibs.action.actions.*
import com.arteam.arLibs.utils.Logger

/**
 * Utility class for parsing action strings into Action objects.
 * 用于将动作字符串解析为Action对象的实用类。
 */
@Suppress("unused")
object ActionParser {
    
    private val SUPPORTED_TYPES = setOf("tell", "sound", "title", "actionbar", "command", "console", "delay", "conditional")
    
    /**
     * Parses a single action string into an Action object.
     * 将单个动作字符串解析为Action对象。
     *
     * @param actionString The action string in format "type value".
     *                     格式为 "type value" 的动作字符串。
     * @return The parsed Action, or null if parsing failed.
     *         解析后的Action，如果解析失败则返回null。
     */
    fun parseAction(actionString: String): Action? {
        val trimmed = normalizeWhitespace(actionString)
        if (trimmed.isEmpty()) {
            Logger.debug("Empty action string provided")
            return null
        }
        
        // Special handling for conditional actions
        if (trimmed.lowercase().startsWith("if ") && trimmed.lowercase().contains(" then ")) {
            return ConditionalActionParser.parseConditionalAction(trimmed)
        }
        
        // Parse format: "type value"
        val parts = trimmed.split(Regex("\\s+"), 2)
        val (type, value) = if (parts.size >= 2) {
            parts[0].lowercase() to parts[1]
        } else if (parts.size == 1) {
            // Single word actions (like delay without value)
            parts[0].lowercase() to ""
        } else {
            Logger.warn("Invalid action format: '$actionString'. Expected format: 'type value'")
            return null
        }
        
        if (value.isEmpty() && type != "delay") {
            Logger.warn("Empty value for action type '$type' in: '$actionString'")
            return null
        }
        
        if (!SUPPORTED_TYPES.contains(type)) {
            Logger.warn("Unknown action type: '$type'. Supported types: ${SUPPORTED_TYPES.joinToString(", ")}")
            return null
        }
        
        return try {
            when (type) {
                "tell" -> TellAction(value)
                "sound" -> SoundAction.parse(value)
                "title" -> TitleAction.parse(value)
                "actionbar" -> ActionBarAction(value)
                "command" -> CommandAction(value)
                "console" -> ConsoleAction(value)
                "delay" -> DelayAction.fromString(value.ifEmpty { "1" })
                "conditional" -> ConditionalActionParser.parseConditionalAction(value)
                else -> {
                    Logger.warn("Unhandled action type: '$type'")
                    null
                }
            }
        } catch (e: Exception) {
            Logger.warn("Failed to parse action '$actionString': ${e.message}")
            Logger.debug("Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
    
    /**
     * Parses a list of action strings into an ActionGroup.
     * 将动作字符串列表解析为ActionGroup。
     *
     * @param actionStrings The list of action strings.
     *                      动作字符串列表。
     * @return The parsed ActionGroup.
     *         解析后的ActionGroup。
     */
    fun parseActionGroup(actionStrings: List<String>): ActionGroup {
        val actions = actionStrings.mapNotNull { actionString ->
            parseAction(actionString).also { action ->
                if (action == null) {
                    Logger.debug("Skipping invalid action: '$actionString'")
                }
            }
        }
        
        if (actions.size != actionStrings.size) {
            Logger.warn("Some actions failed to parse. Expected: ${actionStrings.size}, Parsed: ${actions.size}")
        }
        
        return ActionGroup(actions)
    }
    
    /**
     * Parses action strings from various input formats.
     * 从各种输入格式解析动作字符串。
     *
     * @param input The input can be a String, List<String>, or other supported types.
     *              输入，可以是String、List<String>或其他支持的类型。
     * @return The parsed ActionGroup.
     *         解析后的ActionGroup。
     */
    fun parseActions(input: Any?): ActionGroup {
        return when (input) {
            null -> {
                Logger.debug("Null input provided for action parsing")
                ActionGroup(emptyList())
            }
            is String -> {
                // Single action string
                val action = parseAction(input)
                ActionGroup(if (action != null) listOf(action) else emptyList())
            }
            is List<*> -> {
                // List of action strings
                val actionStrings = input.filterIsInstance<String>()
                if (actionStrings.size != input.size) {
                    Logger.warn("Some items in the list are not strings. Expected: ${input.size}, Valid: ${actionStrings.size}")
                }
                parseActionGroup(actionStrings)
            }
            else -> {
                Logger.warn("Unsupported action input type: ${input.javaClass.simpleName}")
                ActionGroup(emptyList())
            }
        }
    }
    
    /**
     * Gets all supported action types.
     * 获取所有支持的动作类型。
     *
     * @return A list of supported action types.
     *         支持的动作类型列表。
     */
    fun getSupportedActionTypes(): List<String> {
        return SUPPORTED_TYPES.toList().sorted()
    }
    
    /**
     * Gets help text for action formats.
     * 获取动作格式的帮助文本。
     *
     * @return A map of action type to format description.
     *         动作类型到格式描述的映射。
     */
    fun getActionHelp(): Map<String, String> {
        return mapOf(
            "tell" to "tell <message> - Send a text message",
            "sound" to "sound <sound>-<volume>-<pitch> - Play a sound",
            "title" to "title `<title>` `<subtitle>` <fadeIn> <stay> <fadeOut> - Send a title",
            "actionbar" to "actionbar <message> - Send action bar message",
            "command" to "command <command> - Execute command as player",
            "console" to "console <command> - Execute command as console",
            "delay" to "delay <ticks> - Add delay in ticks",
            "conditional" to "if {condition} then {actions} [else {actions}] - Conditional execution"
        )
    }
    
    /**
     * Validates an action string format without creating the action.
     * 验证动作字符串格式而不创建动作。
     *
     * @param actionString The action strings to validate.
     *                     要验证的动作字符串。
     * @return ValidationResult containing success status and error message if any.
     *         包含成功状态和错误消息（如果有）的验证结果。
     */
    fun validateActionFormat(actionString: String): ValidationResult {
        val trimmed = actionString.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Empty action string")
        }
        
        // Special validation for conditional actions
        if (trimmed.lowercase().startsWith("if ") && trimmed.lowercase().contains(" then ")) {
            return if (ConditionalActionParser.isValidConditionalExpression(trimmed)) {
                ValidationResult(true, null)
            } else {
                ValidationResult(false, "Invalid conditional action format")
            }
        }
        
        // Parse format: "type value"
        val parts = trimmed.split(Regex("\\s+"), 2)
        val (type, value) = if (parts.size >= 2) {
            parts[0].lowercase() to parts[1]
        } else if (parts.size == 1) {
            // Single word actions
            parts[0].lowercase() to ""
        } else {
            return ValidationResult(false, "Invalid action format. Expected format: 'type value'")
        }
        
        if (type.isEmpty()) {
            return ValidationResult(false, "Empty action type")
        }
        
        if (value.isEmpty() && type != "delay") {
            return ValidationResult(false, "Empty action value for type '$type'")
        }
        
        if (!SUPPORTED_TYPES.contains(type)) {
            return ValidationResult(false, "Unknown action type: '$type'. Supported: ${SUPPORTED_TYPES.joinToString(", ")}")
        }
        
        return ValidationResult(true, null)
    }
    
    /**
     * Data class for validation results.
     * 验证结果的数据类。
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
    
    /**
     * Normalizes whitespace in action strings for more flexible parsing.
     * 标准化动作字符串中的空格以实现更灵活的解析。
     *
     * @param input The input string to normalize.
     *              要标准化的输入字符串。
     * @return The normalized string.
     *         标准化后的字符串。
     */
    private fun normalizeWhitespace(input: String): String {
        return input.trim().replace(Regex("\\s+"), " ")
    }
} 