package com.arteam.arlibs.config

import com.arteam.arlibs.config.annotations.Config
import com.arteam.arlibs.config.annotations.ConfigSection
import com.arteam.arlibs.config.annotations.ConfigValue
import com.arteam.arlibs.config.migration.MigrationRegistry
import com.arteam.arlibs.utils.Logger
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Manager class for handling configuration files
 * 配置文件管理器类
 *
 * Features:
 * 特性：
 * 1. Load and save configuration files from/to YAML and populate Kotlin class instances.
 *    加载和保存配置文件，从YAML加载值到Kotlin类实例，并将实例值保存回YAML。
 * 2. Validate configuration values using custom validators.
 *    使用自定义验证器验证配置值。
 * 3. Auto-complete missing required values with defaults from Kotlin class properties.
 *    使用Kotlin类属性中的默认值自动补全缺失的必需值。
 * 4. Support for comments on values and sections, with options for single-line or multi-line comments.
 *    支持值和节的注释，可选择单行或多行注释。
 * 5. Version control and migration for configuration files.
 *    配置文件的版本控制和迁移。
 * 6. Optional sections with control over their creation during the first config file generation.
 *    可选配置节，并控制其在首次生成配置文件时的创建行为。
 */
object ConfigManager {
    private val configs = mutableMapOf<KClass<*>, Any>()
    private lateinit var plugin: Plugin

    /**
     * Initializes the config manager
     * 初始化配置管理器
     *
     * @param plugin The plugin instance
     *               插件实例
     */
    fun init(plugin: Plugin) {
        this.plugin = plugin
    }

    /**
     * Registers a configuration class
     * 注册配置类
     *
     * @param configClass The configuration class to register
     *                    要注册的配置类
     * @return The loaded configuration instance
     *         加载的配置实例
     */
    fun <T : Any> register(configClass: KClass<T>): T {
        val configAnnotation = configClass.findAnnotation<Config>() ?: throw IllegalArgumentException(
            "Configuration class ${configClass.simpleName} must be annotated with @Config"
        )

        val file = getConfigFileInternal(configAnnotation)
        val isNewFile = !file.exists()
        if (isNewFile) {
            Logger.info("Configuration file ${file.name} not found, creating a new one.")
        }

        val yamlConfig = YamlConfiguration.loadConfiguration(file)

        handleVersionMigration(yamlConfig, configAnnotation, file) // Pass file for backup name

        val instance = configClass.primaryConstructor?.call() 
            ?: throw IllegalArgumentException("Configuration class ${configClass.simpleName} must have a primary constructor.")
        
        Logger.info("Loading configuration for ${configAnnotation.name}...")
        if (loadConfigValues(instance, yamlConfig, isNewFile, configAnnotation.name)) {
            Logger.info("Configuration ${configAnnotation.name} was modified during loading (e.g., defaults applied). Saving changes.")
            saveConfigValues(instance, yamlConfig, configAnnotation.name)
            yamlConfig.save(file)
        } else if (isNewFile) {
            // If it's a new file and loadConfigValues didn't make changes (e.g. all optional with createIfOptionalOnFirstLoad=false),
            // we still need to save it to write out the structure that was defined (e.g. header, version key)
            Logger.info("Saving newly created configuration ${configAnnotation.name}.")
            saveConfigValues(instance, yamlConfig, configAnnotation.name) // Ensure header & version are written
            yamlConfig.save(file)
        }

        configs[configClass] = instance
        @Suppress("UNCHECKED_CAST")
        return instance as T
    }

    /**
     * Handles version migration for a configuration
     * 处理配置的版本迁移
     *
     * @param yaml The configuration to migrate
     *             要迁移的配置
     * @param config The configuration annotation
     *               配置注解
     * @param configFile The configuration file
     *                  配置文件
     */
    private fun handleVersionMigration(yaml: YamlConfiguration, config: Config, configFile: File) {
        val currentVersion = yaml.getInt(config.versionKey, 1)
        val targetVersion = config.version

        if (currentVersion < targetVersion) {
            Logger.info("Configuration '${config.name}' (current version: $currentVersion) needs migration to target version: $targetVersion.")
            createBackup(configFile, "_before_migration_v$currentVersion")

            val migrationPath = MigrationRegistry.findMigrationPath(currentVersion, targetVersion)
            if (migrationPath != null && migrationPath.isNotEmpty()) {
                Logger.info("Attempting to migrate configuration '${config.name}' from version $currentVersion to $targetVersion...")
                var migrationSuccessful = true
                for (migration in migrationPath) {
                    Logger.info("Applying migration from ${migration.fromVersion} to ${migration.toVersion} for '${config.name}'...")
                    if (!migration.migrate(yaml)) {
                        Logger.warn("Migration step from version ${migration.fromVersion} to ${migration.toVersion} for '${config.name}' FAILED. Subsequent migrations for this config might be affected or skipped.")
                        migrationSuccessful = false
                        break 
                    }
                    Logger.info("Successfully migrated '${config.name}' from version ${migration.fromVersion} to ${migration.toVersion}.")
                }
                
                if (migrationSuccessful) {
                    yaml.set(config.versionKey, targetVersion)
                    Logger.info("Configuration '${config.name}' successfully migrated to version $targetVersion. Saving changes.")
                } else {
                    Logger.error("Migration for '${config.name}' to version $targetVersion was not fully completed due to errors. The configuration might be in an inconsistent state. Version key NOT updated.")
                }
            } else {
                Logger.warn("No migration path found for configuration '${config.name}' from version $currentVersion to $targetVersion. The configuration might not be compatible. Proceeding with current data.")
            }
        } else if (currentVersion > targetVersion) {
            Logger.warn("Configuration file '${config.name}' version ($currentVersion) is newer than the expected version ($targetVersion). This might lead to unexpected behavior or errors. No downgrade path is supported.")
        }
    }

    /**
     * Creates a backup of a configuration file
     * 创建配置文件的备份
     *
     * @param file The file to back up
     *             要备份的文件
     * @param suffix The suffix for the backup file name
     *               备份文件名的后缀
     */
    private fun createBackup(file: File, suffix: String = "_backup") {
        if (!file.exists()) return

        val backupDir = File(file.parentFile, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File(backupDir, "${file.nameWithoutExtension}${suffix}_$timestamp.yml")

        try {
            file.copyTo(backupFile, overwrite = true)
            Logger.info("Created backup for ${file.name}: ${backupFile.name}")
        } catch (e: Exception) {
            Logger.warn("Failed to create backup for ${file.name}: ${e.message}")
        }
    }

    /**
     * Gets a registered configuration instance
     * 获取已注册的配置实例
     *
     * @param configClass The configuration class
     *                    配置类
     * @return The configuration instance
     *         配置实例
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(configClass: KClass<T>): T {
        return configs[configClass] as? T ?: throw IllegalStateException(
            "Configuration class ${configClass.simpleName} is not registered or has not been loaded yet."
        )
    }

    /**
     * Saves a registered configuration instance
     * 保存已注册的配置实例
     *
     * @param configClass The configuration class
     *                    配置类
     */
    fun <T : Any> save(configClass: KClass<T>) {
        val instance = get(configClass) // Throws if not registered
        val configAnnotation = configClass.findAnnotation<Config>() ?: throw IllegalStateException(
            "Configuration class ${configClass.simpleName} is not annotated with @Config. Cannot save."
        )
        
        val file = getConfigFileInternal(configAnnotation)
        // Load existing YAML to preserve comments and structure not directly managed by annotations
        val yamlConfig = YamlConfiguration.loadConfiguration(file) 

        Logger.info("Explicitly saving configuration for ${configAnnotation.name}...")
        saveConfigValues(instance, yamlConfig, configAnnotation.name)
        
        try {
            yamlConfig.save(file)
            Logger.info("Configuration ${configAnnotation.name} saved successfully to ${file.path}")
        } catch (e: Exception) {
            Logger.error("Failed to save configuration ${configAnnotation.name} to ${file.path}: ${e.message}", e)
        }
    }

    /**
     * Gets the configuration file for a registered class
     * 获取已注册类的配置文件
     *
     * @param configClass The configuration class
     *                    配置类
     * @return The configuration file
     *         配置文件
     */
    fun getConfigFile(configClass: KClass<*>): File {
        val configAnnotation = configClass.findAnnotation<Config>() ?: throw IllegalArgumentException(
            "Configuration class ${configClass.simpleName} must be annotated with @Config to get its file."
        )
        return getConfigFileInternal(configAnnotation)
    }

    /**
     * Gets the configuration file for a registered class
     * 获取已注册类的配置文件
     *
     * @param config The configuration annotation
     *               配置注解
     * @return The configuration file
     *         配置文件
     */
    private fun getConfigFileInternal(config: Config): File {
        val dataFolder = plugin.dataFolder
        val path = if (config.path.isNotEmpty()) File(dataFolder, config.path) else dataFolder
        if (!path.exists()) {
            path.mkdirs()
        }
        return File(path, "${config.name}.yml")
    }

    /**
     * Gets the configuration name from an instance
     * 从实例获取配置名称
     *
     * @param instance The instance
     *                 实例
     * @return The configuration name
     *         配置名称
     */
    private fun getConfigNameFromInstance(instance: Any): String {
        return instance::class.findAnnotation<Config>()?.name ?: instance::class.simpleName ?: "UnknownConfig"
    }

    /**
     * Gets the comments from an annotation
     * 从注解获取注释
     *
     * @param valueAnnotation The annotation
     *                        注解
     * @return The comments
     *         注释
     */
    private fun getCommentsFromAnnotation(valueAnnotation: ConfigValue): Array<String> {
        return if (valueAnnotation.comment.isNotEmpty()) {
            valueAnnotation.comment
        } else if (valueAnnotation.commentLine.isNotEmpty()) {
            arrayOf(valueAnnotation.commentLine)
        } else {
            emptyArray()
        }
    }

    /**
     * Loads the configuration values from a YAML file
     * 从YAML文件加载配置值
     *
     * @param instance The instance
     *                 实例
     * @param yaml The YAML configuration
     *             YAML配置
     * @param isNewFile Whether the file is new
     *                  是否是新文件
     * @param configName The configuration name
     *                   配置名称
     * @return Whether the configuration was modified
     *         是否修改了配置
     */
    private fun loadConfigValues(instance: Any, yaml: ConfigurationSection, isNewFile: Boolean, configName: String): Boolean {
        var needsSave = false
        val instanceClass = instance::class

        for (prop in instanceClass.memberProperties) {
            if (prop !is KMutableProperty<*>) {
                if (prop.findAnnotation<ConfigValue>() != null || prop.findAnnotation<ConfigSection>() != null) {
                    Logger.warn("Property '${prop.name}' in config class '${instanceClass.simpleName}' ($configName) is 'val'. Its value cannot be loaded from the configuration file and will always be its default. Consider making it 'var'.")
                }
                continue
            }

            val valueAnnotation = prop.findAnnotation<ConfigValue>()
            if (valueAnnotation != null) {
                val path = valueAnnotation.path.ifEmpty { prop.name }
                val currentDefaultValue = prop.getter.call(instance) // Get current value from instance (which is its default initially)

                if (yaml.contains(path)) {
                    val yamlValue = yaml.get(path)
                    val (convertedValue, conversionOk) = convertType(yamlValue, prop.returnType, configName, path)

                    if (conversionOk && validateValue(convertedValue, valueAnnotation, prop, configName)) {
                        try {
                            prop.setter.call(instance, convertedValue)
                            // If yaml representation differs significantly (e.g. type difference like int vs string "1") ensure yaml is updated
                            if (yamlValue != convertedValue && convertedValue != null) { // Avoid writing null if conversion led to it and yaml had something
                                yaml.set(path, convertedValue)
                                needsSave = true
                            }
                        } catch (e: Exception) {
                            Logger.warn("Error setting property '${prop.name}' in '$configName' with loaded value '$convertedValue'. Reverting to default. Error: ${e.message}")
                            prop.setter.call(instance, currentDefaultValue) // Revert to default
                            yaml.set(path, currentDefaultValue)
                            setCommentsInYaml(yaml, path, getCommentsFromAnnotation(valueAnnotation))
                            needsSave = true
                        }
                    } else {
                        val reason = if (!conversionOk) "type conversion failed for value '$yamlValue'" else "validation failed for value '$convertedValue'"
                        Logger.warn("Invalid value for '${prop.name}' in '$configName' (path: '$path'): $reason. Using default value: '$currentDefaultValue'.")
                        prop.setter.call(instance, currentDefaultValue) // Ensure instance has default
                        yaml.set(path, currentDefaultValue)
                        setCommentsInYaml(yaml, path, getCommentsFromAnnotation(valueAnnotation))
                        needsSave = true
                    }
                } else { // Value not in YAML
                    if (valueAnnotation.required || isNewFile) { // Always write defaults for new files, and for existing if required
                        Logger.info("Missing value for '${prop.name}' in '$configName' (path: '$path'). Setting default: '$currentDefaultValue'.")
                        prop.setter.call(instance, currentDefaultValue) // Instance already has its default
                        yaml.set(path, currentDefaultValue)
                        setCommentsInYaml(yaml, path, getCommentsFromAnnotation(valueAnnotation))
                        needsSave = true
                    } else {
                        // Optional value, not in YAML, not a new file. Instance property retains its default. No action on YAML.
                    }
                }
            }

            val sectionAnnotation = prop.findAnnotation<ConfigSection>()
            if (sectionAnnotation != null) {
                val sectionPath = sectionAnnotation.path.ifEmpty { prop.name }
                val sectionInstance = prop.getter.call(instance) ?: run {
                    Logger.warn("Section property '${prop.name}' in '$configName' (path: '$sectionPath') is null. Skipping section processing.")
                    continue@memberProperties
                }

                var sectionYaml: ConfigurationSection? = yaml.getConfigurationSection(sectionPath)

                if (sectionYaml == null) { // Section does not exist in YAML
                    if (sectionAnnotation.required || (isNewFile && sectionAnnotation.createIfOptionalOnFirstLoad)) {
                        Logger.info("Creating section '${sectionPath}' in '$configName' as it's required or first load of an optional section.")
                        sectionYaml = yaml.createSection(sectionPath)
                        setCommentsInYaml(yaml, sectionPath, sectionAnnotation.comment)
                        needsSave = true
                        // Recursively load/populate defaults for the new section
                        val nestedNeedsSave = loadConfigValues(sectionInstance, sectionYaml, isNewFile, "$configName.$sectionPath")
                        needsSave = needsSave || nestedNeedsSave
                    } else {
                        Logger.info("Optional section '${sectionPath}' in '$configName' not found and not created based on settings.")
                    }
                } else { // Section exists, recursively load its values
                     // Ensure comments are present if defined and section exists
                    if (sectionAnnotation.comment.isNotEmpty() && yaml.getComments(sectionPath).isEmpty()) {
                        setCommentsInYaml(yaml, sectionPath, sectionAnnotation.comment)
                        needsSave = true // Comments were added
                    }
                    val nestedNeedsSave = loadConfigValues(sectionInstance, sectionYaml, isNewFile, "$configName.$sectionPath")
                    needsSave = needsSave || nestedNeedsSave
                }
            }
        }
        return needsSave
    }

    /**
     * Saves the configuration values to a YAML file
     * 保存配置值到YAML文件
     *
     * @param instance The instance
     *                 实例
     * @param yaml The YAML configuration
     *             YAML配置
     * @param configName The configuration name
     *                  配置名称
     */
    private fun saveConfigValues(instance: Any, yaml: ConfigurationSection, configName: String) {
        val configAnnotation = instance::class.findAnnotation<Config>()
        if (configAnnotation != null) {
            if (configAnnotation.header.isNotEmpty()) {
                yaml.options().header(configAnnotation.header.joinToString("\n"))
            }
            // Ensure version key is present
            if (!yaml.contains(configAnnotation.versionKey) || yaml.getInt(configAnnotation.versionKey) != configAnnotation.version) {
                 yaml.set(configAnnotation.versionKey, configAnnotation.version)
            }
        }

        for (prop in instance::class.memberProperties) {
            val valueAnnotation = prop.findAnnotation<ConfigValue>()
            if (valueAnnotation != null) {
                val path = valueAnnotation.path.ifEmpty { prop.name }
                val currentValue = prop.getter.call(instance)
                yaml.set(path, currentValue)
                setCommentsInYaml(yaml, path, getCommentsFromAnnotation(valueAnnotation))
            }

            val sectionAnnotation = prop.findAnnotation<ConfigSection>()
            if (sectionAnnotation != null) {
                val sectionPath = sectionAnnotation.path.ifEmpty { prop.name }
                val sectionInstance = prop.getter.call(instance)

                if (sectionInstance != null) {
                    val sectionYaml = yaml.getConfigurationSection(sectionPath) ?: yaml.createSection(sectionPath)
                    setCommentsInYaml(yaml, sectionPath, sectionAnnotation.comment) // Set/update comments
                    saveConfigValues(sectionInstance, sectionYaml, "$configName.$sectionPath")
                } else if (sectionAnnotation.required) {
                     Logger.warn("Required section '${sectionPath}' in '$configName' has a null instance property '${prop.name}'. Cannot save its contents.")
                }
            }
        }
    }

    /**
     * Validates a value
     * 验证一个值
     *
     * @param value The value
     *              值
     * @param configValueAnnotation The configuration value annotation
     *                              配置值注解
     * @param prop The property
     *             属性
     * @param configName The configuration name
     *                   配置名称
     * @return Whether the value is valid
     *         是否有效
     */
    private fun validateValue(value: Any?, configValueAnnotation: ConfigValue, prop: KProperty<*>, configName: String): Boolean {
        if (value == null) {
            return !configValueAnnotation.required // Null is valid if not required
        }

        val validatorClass = configValueAnnotation.validator
        if (validatorClass == NoValidator::class) return true // No specific validator

        return try {
            val validatorInstance = validatorClass.primaryConstructor?.call()
                ?: throw IllegalStateException("Validator class ${validatorClass.simpleName} for '${prop.name}' in '$configName' must have a primary constructor.")

            val validateMethod = validatorInstance::class.members.find { it.name == "validate" } 
                ?: throw NoSuchMethodException("Validator ${validatorClass.simpleName} must have a 'validate' method.")
            
            // This attempts to call validate(value: T). Type safety relies on convertType producing a compatible value.
            validateMethod.call(validatorInstance, value) as? Boolean ?: false
        } catch (e: ClassCastException) {
             Logger.warn("Type mismatch when validating '${prop.name}' in '$configName'. Value '$value' (type: ${value::class.simpleName}) could not be cast for validator ${validatorClass.simpleName}. Error: ${e.message}")
             false
        } catch (e: Exception) {
            Logger.warn("Error during validation of '${prop.name}' in '$configName' with validator ${validatorClass.simpleName}: ${e.message}", e)
            false
        }
    }

    /**
     * Sets the comments in a YAML file
     * 在YAML文件中设置注释
     *
     * @param yaml The YAML configuration
     *             YAML配置
     * @param path The path
     *             路径
     * @param comments The comments
     *                注释
     */
    private fun setCommentsInYaml(yaml: ConfigurationSection, path: String, comments: Array<String>) {
        if (comments.isNotEmpty()) {
            // Bukkit's setComments expects a List<String>
            yaml.setComments(path, comments.toList())
        }
    }
    
    /**
     * Converts a value to a target type
     * 将值转换为目标类型
     *
     * @param value The value
     *             值
     * @param targetType The target type
     *                  目标类型
     * @param configName The configuration name
     *                  配置名称
     * @param propertyPath The property path
     *                    属性路径
     * @return A pair of the converted value and a boolean indicating success
     *         一个包含转换值和布尔值的Pair，表示成功
     */
    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    private fun convertType(value: Any?, targetType: KType, configName: String, propertyPath: String): Pair<Any?, Boolean> {
        val targetClass = targetType.classifier as? KClass<*> 
            ?: return Pair(value, true) // If no classifier, assume Any and proceed

        if (value == null) {
            return if (targetType.isMarkedNullable) Pair(null, true) else {
                Logger.warn("Null value received from YAML for non-nullable property '$propertyPath' in '$configName' (expected ${targetClass.simpleName}).")
                Pair(null, false) // Cannot assign null to non-nullable unless it's the KType itself that means something else
            }
        }
        
        if (targetClass.isInstance(value)) {
            // If it's a list, we might need to check element types if possible, but often YamlConfig does this well for simple lists
            if (value is List<*> && targetType.arguments.isNotEmpty()) {
                val listElementType = targetType.arguments.first().type
                if (listElementType != null) {
                    val convertedList = mutableListOf<Any?>()
                    var allElementsConverted = true
                    for ((index, element) in value.withIndex()) {
                        val (convertedElement, success) = convertType(element, listElementType, configName, "$propertyPath[$index]")
                        if (success) {
                            convertedList.add(convertedElement)
                        } else {
                            Logger.warn("Failed to convert element at index $index for list '$propertyPath' in '$configName'. Original: '$element'")
                            allElementsConverted = false
                            break
                        }
                    }
                    return if (allElementsConverted) Pair(convertedList, true) else Pair(value, false)
                }
            }
            return Pair(value, true) // Type already matches
        }

        // Common explicit conversions from what YAML might provide
        try {
            return when (targetClass) {
                String::class -> Pair(value.toString(), true)
                Int::class -> when (value) {
                    is Number -> Pair(value.toInt(), true)
                    is String -> Pair(value.toIntOrNull(), value.toIntOrNull() != null)
                    else -> Pair(value, false)
                }
                Long::class -> when (value) {
                    is Number -> Pair(value.toLong(), true)
                    is String -> Pair(value.toLongOrNull(), value.toLongOrNull() != null)
                    else -> Pair(value, false)
                }
                Double::class -> when (value) {
                    is Number -> Pair(value.toDouble(), true)
                    is String -> Pair(value.toDoubleOrNull(), value.toDoubleOrNull() != null)
                    else -> Pair(value, false)
                }
                Boolean::class -> when (value) {
                    is Boolean -> Pair(value, true)
                    is String -> when (value.lowercase()) {
                        "true" -> Pair(true, true)
                        "false" -> Pair(false, true)
                        else -> Pair(value, false)
                    }
                    else -> Pair(value, false)
                }
                List::class -> if (value is List<*>) Pair(value, true) else Pair(value, false) // Basic list check, elements handled by recursion if typed
                else -> {
                    if (targetClass.java.isEnum && value is String) {
                        try {
                            val enumConstants = targetClass.java.enumConstants as Array<Enum<*>>
                            val enumValue = enumConstants.find { it.name.equals(value, ignoreCase = true) }
                            if (enumValue != null) Pair(enumValue, true) else {
                                Logger.warn("Cannot convert string '$value' to enum ${targetClass.simpleName} for '$propertyPath' in '$configName'. Valid values: ${enumConstants.joinToString { it.name }}.")
                                Pair(value, false)
                            }
                        } catch (e: Exception) {
                            Logger.warn("Error converting '$value' to enum ${targetClass.simpleName} for '$propertyPath' in '$configName': ${e.message}")
                            Pair(value, false)
                        }
                    } else {
                        Logger.warn("Cannot convert value '$value' (type ${value::class.simpleName}) to expected type ${targetClass.simpleName} for '$propertyPath' in '$configName'.")
                        Pair(value, false)
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Logger.warn("NumberFormatException converting '$value' to ${targetClass.simpleName} for '$propertyPath' in '$configName'.")
            return Pair(value, false)
        } catch (e: Exception) {
            Logger.warn("Unexpected error converting '$value' to ${targetClass.simpleName} for '$propertyPath' in '$configName': ${e.message}")
            return Pair(value, false)
        }
    }
} 