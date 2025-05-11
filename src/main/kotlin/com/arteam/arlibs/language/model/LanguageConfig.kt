package com.arteam.arlibs.language.model

import com.arteam.arlibs.language.annotations.Language
import com.arteam.arlibs.language.annotations.LanguageKey
import com.arteam.arlibs.utils.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.reflect.KClass

/**
 * Represents a loaded language configuration, including its file, YAML content,
 * annotation details, and defined keys with their defaults.
 * 表示已加载的语言配置，包括其文件、YAML内容、注解详细信息以及定义的键及其默认值。
 *
 * @property file The language file (e.g., en_US.yml).
 *                语言文件（例如：en_US.yml）。
 * @property config The live YamlConfiguration object for this language.
 *                  此语言的实时 YamlConfiguration 对象。
 * @property annotation The @Language annotation from the definition class.
 *                      来自定义类的 @Language 注解。
 * @property definedKeys A map of all language keys (paths) to their @LanguageKey annotation data (containing defaults and comments),
 *                       as defined in the language definition class.
 *                       所有语言键（路径）到其 @LanguageKey 注解数据（包含默认值和注释）的映射，
 *                       这些数据在语言定义类中定义。
 * @property annotationKClass The KClass of the annotation-marked class that defined this language.
 *                            定义此语言的注解标记类的 KClass。
 */
data class LanguageConfig(
    val file: File,
    var config: YamlConfiguration,
    val annotation: Language,
    val definedKeys: Map<String, LanguageKey>, // Path to LanguageKey data
    val annotationKClass: KClass<*> // Store KClass for reload purposes
) {
    /**
     * The cache for messages
     * 消息缓存
     */
    private val messageCache = mutableMapOf<String, LanguageMessage>()

    // Configuration for how missing keys are handled
    // TODO: Make these configurable via ArLibs main config if desired
    private val returnKeyIfMissing = true // If true, returns "MISSING_KEY: path" or path, otherwise null
    private val showMissingKeyPrefix = true // If true and returnKeyIfMissing, prefixes with "MISSING_KEY: "

    /**
     * Gets a message string from the configuration by its path.
     * If not found in the YAML, falls back to the default defined in @LanguageKey.
     * If still not found, behavior is determined by returnKeyIfMissing and showMissingKeyPrefix.
     * 通过路径从配置中获取消息字符串。
     * 如果在 YAML 中找不到，则回退到 @LanguageKey 中定义的默认值。
     * 如果仍然找不到，则行为由 returnKeyIfMissing 和 showMissingKeyPrefix 决定。
     *
     * @param path The path to the message (e.g., "command.success").
     *             消息的路径（例如："command.success"）。
     * @return The [LanguageMessage] if found or a fallback is provided, otherwise behavior depends on configuration.
     *         如果找到或提供了回退，则返回 [LanguageMessage]，否则行为取决于配置。
     */
    fun getMessage(path: String): LanguageMessage? {
        return messageCache.getOrPut(path) {
            var messageString = config.getString(path)

            if (messageString == null) {
                // Not in YAML, try default from annotations
                val keyAnnotation = definedKeys[path]
                if (keyAnnotation != null) {
                    messageString = keyAnnotation.default
                    Logger.debug("Message key '$path' not found in ${file.name} for language '${annotation.name}'. Using annotated default.")
                } else {
                    // Key not in YAML and not defined in annotations
                    Logger.warn("Message key '$path' not found in ${file.name} and no default in annotations for language '${annotation.name}'.")
                    if (returnKeyIfMissing) {
                        messageString = if (showMissingKeyPrefix) "MISSING_KEY: $path" else path
                    } else {
                        return@getOrPut null // Explicitly return null based on config
                    }
                }
            }
            LanguageMessage(messageString!!)
        }
    }

    /**
     * Reloads the configuration from the language file.
     * The message cache is cleared.
     * 从语言文件重新加载配置。
     * 消息缓存将被清除。
     */
    fun reload() {
        try {
            config = YamlConfiguration.loadConfiguration(file)
            messageCache.clear()
            Logger.info("Reloaded language config for ${annotation.name} from ${file.name}.")
        } catch (e: Exception) {
            Logger.error("Failed to reload language config for ${annotation.name} from ${file.name}: ${e.message}", e)
        }
    }

    /**
     * Saves the current YamlConfiguration state to its file.
     * Note: This saves the live YAML state. To update defaults or structure, re-register or use synchronization methods in LanguageManager.
     * 将当前 YamlConfiguration 状态保存到其文件。
     * 注意：这将保存实时 YAML 状态。要更新默认值或结构，请重新注册或使用 LanguageManager 中的同步方法。
     */
    fun save() {
        try {
            config.save(file)
            Logger.info("Saved language config for ${annotation.name} to ${file.name}.")
        } catch (e: IOException) {
            Logger.error("Could not save language file ${file.name} for ${annotation.name}: ${e.message}", e)
        }
    }

    /**
     * Gets the content version of the language file from its YAML configuration.
     * 从其 YAML 配置中获取语言文件的内容版本。
     *
     * @return The content version, or 0 if not found.
     *         内容版本，如果未找到则为 0。
     */
    fun getContentVersion(): Int {
        return config.getInt("arlibs-content-version", 0) // Match key in LanguageManager
    }

     /**
     * Gets the structure version of the language file from its YAML configuration.
     * 从其 YAML 配置中获取语言文件的结构版本。
     *
     * @return The structure version, or 0 if not found.
     *         结构版本，如果未找到则为 0。
     */
    fun getStructureVersion(): Int {
        return config.getInt("arlibs-structure-version", 0) // Match key in LanguageManager
    }
} 