package com.arteam.arlibs.language

import com.arteam.arlibs.language.annotations.Language
import com.arteam.arlibs.language.annotations.LanguageKey
import com.arteam.arlibs.language.cloud.LanguageDownloader
import com.arteam.arlibs.language.model.LanguageConfig
import com.arteam.arlibs.language.model.LanguageMessage
import com.arteam.arlibs.utils.Logger
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Manages language files, translations, and player-specific language settings.
 * Supports defining language keys and default translations via annotations.
 * Handles automatic generation of language files from these defaults and cloud updates.
 * 管理语言文件、翻译和玩家特定的语言设置。
 * 支持通过注解定义语言键和默认翻译。
 * 处理从这些默认值自动生成语言文件以及云端更新。
 */
object LanguageManager {
    private lateinit var plugin: Plugin
    private lateinit var languagesFolder: File
    private val languages = ConcurrentHashMap<String, LanguageConfig>()
    private val playerLanguages = ConcurrentHashMap<UUID, String>() // Consider persistent storage for this
    private val defaultLanguageName = AtomicReference<String?>(null)

    private const val YAML_STRUCTURE_VERSION_KEY = "arlibs-structure-version" // Updated key name
    private const val YAML_CONTENT_VERSION_KEY = "arlibs-content-version"   // Updated key name

    /**
     * Initializes the LanguageManager.
     * Must be called once, typically in the plugin's onEnable method.
     * 初始化 LanguageManager。
     * 必须调用一次，通常在插件的 onEnable 方法中。
     *
     * @param plugin The plugin instance.
     *               插件实例。
     */
    fun init(plugin: Plugin) {
        this.plugin = plugin
        this.languagesFolder = File(plugin.dataFolder, "languages")
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs()
        }
        LanguageDownloader.init(plugin) // Initialize downloader with plugin info for User-Agent
    }

    /**
     * Registers a language definition class.
     * The class must be annotated with @Language and may contain properties annotated with @LanguageKey.
     * 注册语言定义类。
     * 该类必须使用 @Language 注解，并且可以包含使用 @LanguageKey 注解的属性。
     *
     * @param languageDefinitionClass The KClass of the language definition.
     *                                语言定义的 KClass。
     */
    fun register(languageDefinitionClass: KClass<*>) {
        val langAnnotation = languageDefinitionClass.findAnnotation<Language>()
            ?: throw IllegalArgumentException("Language definition class ${languageDefinitionClass.simpleName} must be annotated with @Language.")

        Logger.info("Registering language: ${langAnnotation.displayName} (${langAnnotation.name})...")

        val definedKeys = extractDefinedKeys(languageDefinitionClass, langAnnotation.name)
        val languageFile = getLanguageFile(langAnnotation)

        // Step 1: Ensure local language file exists and is synchronized with definitions
        val currentYaml = extractOrSynchronizeLanguageFile(langAnnotation, definedKeys, languageFile)
        var currentContentVersion = currentYaml.getInt(YAML_CONTENT_VERSION_KEY, 0)

        // Step 2: Handle cloud update if URL is provided
        if (langAnnotation.cloudUrl.isNotEmpty()) {
            processCloudUpdate(langAnnotation, languageFile, definedKeys).thenAccept { cloudApplied ->
                if (cloudApplied) {
                    // Reload YAML and LanguageConfig if cloud update was applied
                    val updatedYaml = YamlConfiguration.loadConfiguration(languageFile)
                    currentContentVersion = updatedYaml.getInt(YAML_CONTENT_VERSION_KEY, currentContentVersion)
                    loadLanguageIntoMemory(langAnnotation, languageFile, updatedYaml, definedKeys)
                    Logger.info("Cloud update applied for ${langAnnotation.name}. Language reloaded.")
                } else {
                    // Load with current local YAML (which might have been just created/synced)
                    loadLanguageIntoMemory(langAnnotation, languageFile, currentYaml, definedKeys)
                }
                // Schedule auto-updates regardless of initial download success, if enabled
                if (langAnnotation.autoUpdate) {
                    LanguageDownloader.scheduleAutoUpdate(langAnnotation) {
                        processCloudUpdate(it, languageFile, definedKeys)
                    }
                }
            }.exceptionally { ex ->
                Logger.warn("Error during initial cloud processing for ${langAnnotation.name}: ${ex.message}", ex)
                // Fallback to loading local version even if cloud check failed
                loadLanguageIntoMemory(langAnnotation, languageFile, currentYaml, definedKeys)
                null
            }
        } else {
            // No cloud URL, just load the local (potentially newly created/synced) file
            loadLanguageIntoMemory(langAnnotation, languageFile, currentYaml, definedKeys)
        }
    }
    
    private fun extractDefinedKeys(languageDefinitionClass: KClass<*>, langName: String): Map<String, LanguageKey> {
        val keys = mutableMapOf<String, LanguageKey>()
        languageDefinitionClass.memberProperties.forEach { prop ->
            prop.findAnnotation<LanguageKey>()?.let {
                if (keys.containsKey(it.path)) {
                    Logger.warn("Duplicate language key path '${it.path}' defined in ${languageDefinitionClass.simpleName} for language '$langName'. Check @LanguageKey annotations. The later one will be ignored.")
                } else {
                    keys[it.path] = it
                }
            }
        }
        return keys
    }

    private fun extractOrSynchronizeLanguageFile(
        annotation: Language,
        definedKeys: Map<String, LanguageKey>,
        languageFile: File
    ): YamlConfiguration {
        val yamlConfig = YamlConfiguration.loadConfiguration(languageFile)
        var needsSave = !languageFile.exists() // If new, always save

        if (needsSave) {
            Logger.info("Language file '${languageFile.name}' not found. Creating with defaults from annotations for ${annotation.name}.")
            // Set header for new files
            val header = "${plugin.description.name} - Language: ${annotation.displayName} (${annotation.name})\nStructure Version: ${annotation.version}"
            yamlConfig.options().header(header)
            yamlConfig.options().copyHeader(true)
        }

        // Ensure ArLibs versions are present and up-to-date
        val currentStructureVersion = yamlConfig.getInt(YAML_STRUCTURE_VERSION_KEY, 0)
        if (currentStructureVersion != annotation.version) {
            yamlConfig.set(YAML_STRUCTURE_VERSION_KEY, annotation.version)
            Logger.info("Updating structure version for '${annotation.name}' in ${languageFile.name} from $currentStructureVersion to ${annotation.version}.")
            needsSave = true
        }
        if (!yamlConfig.contains(YAML_CONTENT_VERSION_KEY)) {
            yamlConfig.set(YAML_CONTENT_VERSION_KEY, 1) // Initial content version
            needsSave = true
        }

        // Synchronize keys from annotations to YAML
        definedKeys.forEach { (path, keyAnnotation) ->
            if (!yamlConfig.contains(path)) {
                yamlConfig.set(path, keyAnnotation.default)
                if (keyAnnotation.comment.isNotEmpty()) {
                    yamlConfig.setComments(path, keyAnnotation.comment.toList())
                }
                Logger.debug("Added missing key '$path' with default to ${languageFile.name} for ${annotation.name}.")
                needsSave = true
            } else {
                // Ensure comments are present even if key exists (users might delete them)
                val existingComments = yamlConfig.getComments(path)
                if (keyAnnotation.comment.isNotEmpty() && existingComments.isNullOrEmpty()) {
                    yamlConfig.setComments(path, keyAnnotation.comment.toList())
                    needsSave = true
                }
            }
        }

        if (needsSave) {
            try {
                yamlConfig.save(languageFile)
                Logger.info("Saved language file: ${languageFile.name} for ${annotation.name}.")
            } catch (e: IOException) {
                Logger.error("Could not save language file ${languageFile.name} for ${annotation.name}: ${e.message}", e)
            }
        }
        return yamlConfig
    }

    private fun processCloudUpdate(
        annotation: Language,
        languageFile: File,
        definedKeys: Map<String, LanguageKey> // Pass definedKeys for context if needed in future merge logic
    ): CompletableFuture<Boolean> {
        Logger.debug("Checking cloud for updates for language '${annotation.name}'...")
        return LanguageDownloader.fetchUpdateData(annotation).thenApply { cloudData ->
            if (cloudData == null) {
                Logger.debug("No update data fetched from cloud for ${annotation.name}.")
                return@thenApply false
            }

            if (cloudData.structureVersion != annotation.version) {
                Logger.warn(
                    "Cloud version for '${annotation.name}' has structure version ${cloudData.structureVersion}, " +
                            "but plugin expects ${annotation.version}. Skipping update to avoid incompatibility."
                )
                return@thenApply false
            }

            val localYaml = YamlConfiguration.loadConfiguration(languageFile)
            val localContentVersion = localYaml.getInt(YAML_CONTENT_VERSION_KEY, 0)

            if (cloudData.contentVersion > localContentVersion) {
                Logger.info(
                    "Cloud has newer content for '${annotation.name}' (Cloud: v${cloudData.contentVersion}, Local: v${localContentVersion}). Updating..."
                )
                try {
                    // Create a new YamlConfiguration from cloud content to ensure it's clean
                    val newCloudYaml = YamlConfiguration()
                    newCloudYaml.loadFromString(cloudData.content)

                    // Preserve header from local file if it exists, otherwise use a default
                    val header = localYaml.options().header() ?: "${plugin.description.name} - Language: ${annotation.displayName} (${annotation.name})\nStructure Version: ${annotation.version}"
                    newCloudYaml.options().header(header)
                    newCloudYaml.options().copyHeader(true)
                    
                    // Ensure ArLibs versions are correctly set from cloud/annotation data
                    newCloudYaml.set(YAML_STRUCTURE_VERSION_KEY, cloudData.structureVersion) // or annotation.version, should be same
                    newCloudYaml.set(YAML_CONTENT_VERSION_KEY, cloudData.contentVersion)
                    
                    // Simple strategy: Overwrite local file with validated cloud content.
                    // More complex merging could be done here if needed by comparing newCloudYaml with definedKeys and localYaml.
                    newCloudYaml.save(languageFile)
                    Logger.info("Successfully updated '${annotation.name}' from cloud to content version ${cloudData.contentVersion}.")
                    return@thenApply true
                } catch (e: Exception) {
                    Logger.error("Error applying cloud update for '${annotation.name}': ${e.message}", e)
                    return@thenApply false
                }
            } else {
                Logger.debug("Local content for '${annotation.name}' (v$localContentVersion) is up-to-date or newer than cloud (v${cloudData.contentVersion}).")
                return@thenApply false
            }
        }
    }

    private fun loadLanguageIntoMemory(
        annotation: Language,
        file: File,
        yamlConfig: YamlConfiguration,
        definedKeys: Map<String, LanguageKey>
    ) {
        val langConf = LanguageConfig(file, yamlConfig, annotation, definedKeys)
        languages[annotation.name] = langConf

        if (annotation.isDefault) {
            val currentDefault = defaultLanguageName.get()
            if (currentDefault == null) {
                if (!defaultLanguageName.compareAndSet(null, annotation.name)) {
                    // This case should ideally not happen if logic is sound, means another thread set it between get and CAS
                    Logger.warn("Race condition: Multiple default languages defined! '${annotation.name}' attempted to overwrite newly set default '${defaultLanguageName.get()}'. Using '${defaultLanguageName.get()}'.")
                }
            } else if (currentDefault != annotation.name) {
                Logger.warn("Multiple default languages defined! Default is already '$currentDefault'. Ignoring 'isDefault=true' for '${annotation.name}'.")
            }
        }
        Logger.info("Loaded language: ${annotation.displayName} (${annotation.name}) from ${file.name}. Keys: ${definedKeys.size}.")
    }

    /**
     * Finalizes language initialization. Should be called after all language classes are registered.
     * Ensures a default language is set.
     * 完成语言初始化。应在注册所有语言类后调用。
     * 确保设置了默认语言。
     */
    fun finalizeInitialization() {
        if (defaultLanguageName.get() == null) {
            val firstLangName = languages.keys.firstOrNull()
            if (firstLangName != null) {
                defaultLanguageName.set(firstLangName)
                Logger.warn("No default language was explicitly marked with 'isDefault=true'. Using the first loaded language ('$firstLangName') as default.")
            } else {
                Logger.error("No languages were loaded and no default language is set. Language features will be unavailable.")
            }
        }
        Logger.info("LanguageManager finalized. Default language: ${defaultLanguageName.get() ?: "Not Set"}. Total languages: ${languages.size}.")
    }

    fun getAvailableLanguages(): Map<String, String> {
        return languages.mapValues { it.value.annotation.displayName }
    }

    private fun getResolvedLanguageName(languageCode: String?): String {
        return languageCode?.takeIf { languages.containsKey(it) } ?: defaultLanguageName.get()
        ?: throw IllegalStateException("No default language configured and requested language '$languageCode' not found.")
    }
    
    fun getMessage(language: String, path: String, placeholders: Map<String, Any> = emptyMap()): LanguageMessage? {
        val langName = getResolvedLanguageName(language)
        return languages[langName]?.getMessage(path)?.withPlaceholders(placeholders)
    }

    fun getMessage(player: Player, path: String, placeholders: Map<String, Any> = emptyMap()): LanguageMessage? {
        val langName = getPlayerLanguage(player) // This already falls back to default
        return languages[langName]?.getMessage(path)?.withPlaceholders(placeholders)
    }

    fun setPlayerLanguage(player: Player, language: String): Boolean {
        return if (languages.containsKey(language)) {
            playerLanguages[player.uniqueId] = language
            Logger.debug("Set language for player ${player.name} to $language")
            true
        } else {
            Logger.warn("Attempted to set unknown language '$language' for player ${player.name}")
            false
        }
    }

    fun getPlayerLanguage(player: Player): String {
        return playerLanguages[player.uniqueId] ?: defaultLanguageName.get()
        ?: run {
             Logger.warn("Player ${player.name} has no preferred language and no default language is set. This is problematic!")
             // Fallback to the first available language if absolutely no default, though finalizeInitialization should prevent this.
             languages.keys.firstOrNull() ?: throw IllegalStateException("No languages available and no default language set.")
        }
    }

    fun reload() {
        Logger.info("Reloading all language configurations...")
        val registeredLanguageClasses = languages.values.map { it.annotationKClass } // Assuming LanguageConfig stores this
        
        // Clear current state (or be more selective)
        // languages.clear() // This might be too aggressive if a reload fails for one lang
        // defaultLanguageName.set(null) // Re-evaluated by registrations

        // Re-register. This is a simple approach. A more granular reload might be possible.
        // For now, this effectively re-runs the registration logic for each known language.
        // This assumes you have a way to get the KClass back or you re-scan for @Language annotations.
        // For simplicity, let's assume LanguageConfig stores the KClass it was created from, or we re-evaluate.
        // This part needs a robust way to re-trigger registration or a dedicated reload per language.
        // A simpler reload for now:

        val tempLanguages = ConcurrentHashMap<String, LanguageConfig>()
        val tempDefaultLangName = AtomicReference<String?>(null)

        languages.forEach { (name, langConfig) ->
            try {
                Logger.info("Reloading ${langConfig.annotation.name}...")
                val langFile = getLanguageFile(langConfig.annotation)
                val definedKeys = extractDefinedKeys(langConfig.annotationKClass, langConfig.annotation.name)
                val currentYaml = extractOrSynchronizeLanguageFile(langConfig.annotation, definedKeys, langFile)
                var currentContentVersion = currentYaml.getInt(YAML_CONTENT_VERSION_KEY, 0)

                if (langConfig.annotation.cloudUrl.isNotEmpty()) {
                    // Synchronous for reload simplicity, or make it fully async again
                    val cloudApplied = processCloudUpdate(langConfig.annotation, langFile, definedKeys).join() // Block for reload
                    if (cloudApplied) {
                        val updatedYaml = YamlConfiguration.loadConfiguration(langFile)
                        currentContentVersion = updatedYaml.getInt(YAML_CONTENT_VERSION_KEY, currentContentVersion)
                        val newConf = LanguageConfig(langFile, updatedYaml, langConfig.annotation, definedKeys, langConfig.annotationKClass)
                        tempLanguages[name] = newConf
                    } else {
                        val newConf = LanguageConfig(langFile, currentYaml, langConfig.annotation, definedKeys, langConfig.annotationKClass)
                        tempLanguages[name] = newConf
                    }
                } else {
                    val newConf = LanguageConfig(langFile, currentYaml, langConfig.annotation, definedKeys, langConfig.annotationKClass)
                    tempLanguages[name] = newConf
                }

                if (langConfig.annotation.isDefault) {
                     val currentDefault = tempDefaultLangName.get()
                     if (currentDefault == null) {
                         if (!tempDefaultLangName.compareAndSet(null, langConfig.annotation.name)) {
                             Logger.warn("[Reload] Race condition: Multiple default languages! '${langConfig.annotation.name}' vs '${tempDefaultLangName.get()}'.")
                         }
                     } else if (currentDefault != langConfig.annotation.name) {
                         Logger.warn("[Reload] Multiple default languages! Default is '$currentDefault'. Ignoring 'isDefault=true' for '${langConfig.annotation.name}'.")
                     }
                 }

            } catch (e: Exception) {
                Logger.error("Failed to reload language ${langConfig.annotation.name}: ${e.message}", e)
                // Optionally, keep the old config for this language if reload fails
                tempLanguages[name] = langConfig // Keep old one on error
                 if (langConfig.annotation.isDefault && tempDefaultLangName.get() == null) { // try to preserve default if this was it
                    tempDefaultLangName.set(langConfig.annotation.name)
                }
            }
        }
        languages.clear()
        languages.putAll(tempLanguages)
        defaultLanguageName.set(tempDefaultLangName.get()) // Set the determined default name
        finalizeInitialization() // Re-check default language logic

        Logger.info("All language configurations reloaded. Current default: ${defaultLanguageName.get()}")
    }

    private fun getLanguageFile(annotation: Language): File {
        val langSpecificPath = if (annotation.path.isNotEmpty()) File(languagesFolder, annotation.path) else languagesFolder
        if (!langSpecificPath.exists()) {
            langSpecificPath.mkdirs()
        }
        return File(langSpecificPath, "${annotation.name}.yml")
    }

    fun shutdown() {
        LanguageDownloader.shutdown()
        playerLanguages.clear()
        languages.clear()
        defaultLanguageName.set(null)
        Logger.info("LanguageManager shutdown complete.")
    }
} 