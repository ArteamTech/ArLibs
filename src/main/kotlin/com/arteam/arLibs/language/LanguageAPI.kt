/**
 * Public API for the language system.
 * Provides easy-to-use methods for internationalization and localization.
 *
 * 语言系统的公共API。
 * 提供易于使用的国际化和本地化方法。
 *
 * @author ArteamTech
 * @since 2025-07-12
 * @version 1.0.0
 */
package com.arteam.arLibs.language

import org.bukkit.entity.Player

/**
 * Public API for the language system.
 * 语言系统的公共API。
 */
@Suppress("unused")
object LanguageAPI {

    /**
     * Gets a message for the default language.
     * 获取默认语言的消息。
     */
    fun getMessage(key: String, placeholders: Map<String, String> = emptyMap()): String {
        return LanguageManager.getMessage(LanguageManager.getDefaultLanguage(), key, placeholders)
    }

    /**
     * Gets a message for a specific language.
     * 获取特定语言的消息。
     */
    fun getMessage(language: String, key: String, placeholders: Map<String, String> = emptyMap()): String {
        return LanguageManager.getMessage(language, key, placeholders)
    }

    /**
     * Gets a message for a player using their preferred language.
     * 为玩家获取消息，使用其首选语言。
     */
    fun getMessage(player: Player, key: String, placeholders: Map<String, String> = emptyMap()): String {
        return LanguageManager.getMessageForPlayer(player.name, key, placeholders)
    }

    /**
     * Gets a message for a player by name using their preferred language.
     * 通过玩家名称获取消息，使用其首选语言。
     */
    fun getMessageForPlayer(playerName: String, key: String, placeholders: Map<String, String> = emptyMap()): String {
        return LanguageManager.getMessageForPlayer(playerName, key, placeholders)
    }

    /**
     * Gets a list of messages for the default language.
     * 获取默认语言的消息列表。
     */
    fun getMessageList(key: String, placeholders: Map<String, String> = emptyMap()): List<String> {
        return LanguageManager.getMessageList(LanguageManager.getDefaultLanguage(), key, placeholders)
    }

    /**
     * Gets a list of messages for a specific language.
     * 获取特定语言的消息列表。
     */
    fun getMessageList(language: String, key: String, placeholders: Map<String, String> = emptyMap()): List<String> {
        return LanguageManager.getMessageList(language, key, placeholders)
    }

    /**
     * Gets a list of messages for a player using their preferred language.
     * 为玩家获取消息列表，使用其首选语言。
     */
    fun getMessageList(player: Player, key: String, placeholders: Map<String, String> = emptyMap()): List<String> {
        return LanguageManager.getMessageList(LanguageManager.getPlayerLanguage(player.name), key, placeholders)
    }

    /**
     * Sets a player's preferred language.
     * 设置玩家的首选语言。
     */
    fun setPlayerLanguage(player: Player, language: String) {
        LanguageManager.setPlayerLanguage(player.name, language)
    }

    /**
     * Sets a player's preferred language by name.
     * 通过玩家名称设置首选语言。
     */
    fun setPlayerLanguage(playerName: String, language: String) {
        LanguageManager.setPlayerLanguage(playerName, language)
    }

    /**
     * Gets a player's preferred language.
     * 获取玩家的首选语言。
     */
    fun getPlayerLanguage(player: Player): String {
        return LanguageManager.getPlayerLanguage(player.name)
    }

    /**
     * Gets a player's preferred language by name.
     * 通过玩家名称获取首选语言。
     */
    fun getPlayerLanguage(playerName: String): String {
        return LanguageManager.getPlayerLanguage(playerName)
    }

    /**
     * Removes a player's language preference.
     * 移除玩家的语言偏好。
     */
    fun removePlayerLanguage(player: Player) {
        LanguageManager.removePlayerLanguage(player.name)
    }

    /**
     * Removes a player's language preference by name.
     * 通过玩家名称移除语言偏好。
     */
    fun removePlayerLanguage(playerName: String) {
        LanguageManager.removePlayerLanguage(playerName)
    }

    /**
     * Reloads all language files.
     * 重新加载所有语言文件。
     */
    fun reloadLanguages() {
        LanguageManager.reloadLanguages()
    }

    /**
     * Checks if a language is supported.
     * 检查语言是否受支持。
     *
     * @param language The language code to check.
     *                 要检查的语言代码。
     * @return True if the language is supported, false otherwise.
     *         如果语言受支持则返回true，否则返回false。
     */
    fun isLanguageSupported(language: String): Boolean {
        return LanguageManager.isLanguageSupported(language)
    }

    /**
     * Gets all supported languages.
     * 获取所有支持的语言。
     */
    fun getSupportedLanguages(): List<String> {
        return LanguageManager.getSupportedLanguages()
    }

    /**
     * Gets the default language.
     * 获取默认语言。
     */
    fun getDefaultLanguage(): String {
        return LanguageManager.getDefaultLanguage()
    }

    /**
     * Gets the fallback language.
     * 获取回退语言。
     */
    fun getFallbackLanguage(): String {
        return LanguageManager.getFallbackLanguage()
    }
} 