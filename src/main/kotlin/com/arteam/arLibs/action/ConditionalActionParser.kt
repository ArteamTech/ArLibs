/**
 * Parser for conditional action expressions from configuration strings.
 * Supports parsing If-Then-Else action expressions with embedded conditions and actions.
 *
 * 从配置字符串解析条件动作表达式的解析器。
 * 支持解析包含嵌入条件和动作的If-Then-Else动作表达式。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import com.arteam.arLibs.action.actions.ConditionalAction
import com.arteam.arLibs.condition.ConditionParser
import com.arteam.arLibs.utils.Logger

/**
 * Utility class for parsing conditional action strings.
 * 用于解析条件动作字符串的实用类。
 */
@Suppress("unused")
object ConditionalActionParser {
    
    /**
     * Parses a conditional action string in the format:
     * "if {condition} then {actions} [else {actions}]"
     * 
     * 解析格式为以下的条件动作字符串：
     * "if {condition} then {actions} [else {actions}]"
     *
     * @param expression The conditional action expression.
     *                   条件动作表达式。
     * @return The parsed ConditionalAction, or null if parsing failed.
     *         解析的ConditionalAction，如果解析失败则返回null。
     */
    fun parseConditionalAction(expression: String): ConditionalAction? {
        val trimmed = expression.trim()
        
        return try {
            parseIfThenElse(trimmed)
        } catch (e: Exception) {
            Logger.warn("Failed to parse conditional action: '$expression' - ${e.message}")
            Logger.debug("Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
    
    /**
     * Parses an If-Then-Else expression.
     * 解析If-Then-Else表达式。
     *
     * @param content The content to parse.
     *                要解析的内容。
     * @return The parsed ConditionalAction.
     *         解析的ConditionalAction。
     */
    private fun parseIfThenElse(content: String): ConditionalAction? {
        val normalized = normalizeWhitespace(content)
        
        // Check if it starts with "if"
        if (!normalized.lowercase().startsWith("if ")) {
            Logger.warn("Conditional action must start with 'if': $content")
            return null
        }
        
        // Find the positions of keywords
        val ifIndex = 0
        val thenIndex = findKeyword(normalized, "then", ifIndex + 3)
        if (thenIndex == -1) {
            Logger.warn("Missing 'then' keyword in conditional action: $content")
            return null
        }
        
        val elseIndex = findKeyword(normalized, "else", thenIndex + 4)
        
        // Extract condition part
        val conditionPart = normalized.substring(3, thenIndex).trim()
        val condition = parseConditionFromBraces(conditionPart)
        if (condition == null) {
            Logger.warn("Failed to parse condition: '$conditionPart'")
            return null
        }
        
        // Extract actions part then
        val thenPart = if (elseIndex != -1) {
            normalized.substring(thenIndex + 4, elseIndex).trim()
        } else {
            normalized.substring(thenIndex + 4).trim()
        }
        val thenActions = parseActionsFromBraces(thenPart)
        if (thenActions == null) {
            Logger.warn("Failed to parse 'then' actions: '$thenPart'")
            return null
        }
        
        // Extract else actions part (if present)
        val elseActions = if (elseIndex != -1) {
            val elsePart = normalized.substring(elseIndex + 4).trim()
            parseActionsFromBraces(elsePart)
        } else {
            null
        }
        
        return if (elseActions != null) {
            ConditionalAction.ifThenElse(condition, thenActions, elseActions)
        } else {
            ConditionalAction.ifThen(condition, thenActions)
        }
    }
    
    /**
     * Finds a keyword in the string, considering brace nesting.
     * 在字符串中查找关键字，考虑大括号嵌套。
     *
     * @param text The text to search in.
     *             要搜索的文本。
     * @param keyword The keyword to find.
     *                要查找的关键字。
     * @param startIndex The index to start searching from.
     *                   开始搜索的索引。
     * @return The index of the keyword, or -1 if not found.
     *         关键字的索引，如果未找到则返回-1。
     */
    private fun findKeyword(text: String, keyword: String, startIndex: Int): Int {
        var braceDepth = 0
        var i = startIndex
        
        while (i <= text.length - keyword.length) {
            when (text[i]) {
                '{' -> braceDepth++
                '}' -> braceDepth--
            }
            
            // Only consider keywords at brace depth 0
            if (braceDepth == 0) {
                val potentialKeyword = text.substring(i, i + keyword.length)
                if (potentialKeyword.lowercase() == keyword.lowercase()) {
                    // Check if it's a whole word (not part of another word)
                    val beforeChar = if (i > 0) text[i - 1] else ' '
                    val afterChar = if (i + keyword.length < text.length) text[i + keyword.length] else ' '
                    
                    if (beforeChar.isWhitespace() && (afterChar.isWhitespace() || afterChar == '{')) {
                        return i
                    }
                }
            }
            i++
        }
        
        return -1
    }
    
    /**
     * Parses a condition from a string that may be enclosed in braces.
     * 从可能用大括号括起来的字符串解析条件。
     *
     * @param conditionText The condition text.
     *                      条件文本。
     * @return The parsed Condition, or null if parsing failed.
     *         解析的条件，如果解析失败则返回null。
     */
    private fun parseConditionFromBraces(conditionText: String): com.arteam.arLibs.condition.Condition? {
        val trimmed = conditionText.trim()
        
        // Remove braces if present
        val content = if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed.substring(1, trimmed.length - 1).trim()
        } else {
            trimmed
        }
        
        return ConditionParser.parse(content)
    }
    
    /**
     * Parses actions from a string that may be enclosed in braces.
     * 从可能用大括号括起来的字符串解析动作。
     *
     * @param actionsText The actions text.
     *                    动作文本。
     * @return The parsed ActionGroup, or null if parsing failed.
     *         解析的ActionGroup，如果解析失败则返回null。
     */
    private fun parseActionsFromBraces(actionsText: String): ActionGroup? {
        val trimmed = normalizeWhitespace(actionsText)
        
        // Remove braces if present and normalize spacing around braces
        val content = if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            normalizeWhitespace(trimmed.substring(1, trimmed.length - 1))
        } else {
            trimmed
        }
        
        if (content.isEmpty()) {
            Logger.warn("Empty actions content")
            return null
        }
        
        // Split actions by semicolon or newline, with improved whitespace handling
        val actionStrings = content.split(Regex("[;\n]"))
            .map { normalizeWhitespace(it) }
            .filter { it.isNotEmpty() }
        
        if (actionStrings.isEmpty()) {
            Logger.warn("No valid actions found in: '$content'")
            return null
        }
        
        // Actions in conditional expressions don't need colons
        return ActionParser.parseActionGroup(actionStrings)
    }
    
    /**
     * Normalizes whitespace in expressions for more flexible parsing.
     * Handles extra spaces, mixed spacing, and spacing around braces.
     * 标准化表达式中的空格以实现更灵活的解析。
     * 处理多余的空格、混乱的空格以及大括号周围的空格。
     *
     * @param input The input string to normalize.
     *              要标准化的输入字符串。
     * @return The normalized string.
     *         标准化后的字符串。
     */
    private fun normalizeWhitespace(input: String): String {
        return input.trim()
            .replace(Regex("\\s+"), " ")  // Replace multiple whitespace with single space
            .replace(Regex("\\s*\\{\\s*"), " {")  // Normalize spacing around opening braces
            .replace(Regex("\\s*}\\s*"), "} ")    // Normalize spacing around closing braces
            .trim()
    }
    
    /**
     * Validates a conditional action expression without parsing it.
     * 验证条件动作表达式而不解析它。
     *
     * @param expression The expression to validate.
     *                   要验证的表达式。
     * @return True if the expression appears to be valid, false otherwise.
     *         如果表达式看起来有效则返回true，否则返回false。
     */
    fun isValidConditionalExpression(expression: String): Boolean {
        val trimmed = expression.trim()
        val normalized = normalizeWhitespace(trimmed)
        
        // Basic validation: must start with "if" and contain "then"
        return normalized.lowercase().startsWith("if ") && 
               normalized.lowercase().contains(" then ")
    }
    
    /**
     * Gets help text for conditional action format.
     * 获取条件动作格式的帮助文本。
     *
     * @return Help text describing the format.
     *         描述格式的帮助文本。
     */
    fun getHelpText(): String {
        return """
            Conditional Action Format:
            if {condition} then {actions} [else {actions}]
            
            Examples:
            if {permission essentials.fly} then {tell You can fly!} else {tell No fly permission}
            if {papi %player_level% >= 10} then {sound LEVEL_UP-1.0-1.0; tell Level up!}
            
            Notes:
            • Actions don't require colons (tell message, not tell: message)
            • Multiple actions are separated by semicolons
            • Condition formats: Same as regular condition expressions
            • Flexible spacing is supported around braces and keywords
        """.trimIndent()
    }
} 