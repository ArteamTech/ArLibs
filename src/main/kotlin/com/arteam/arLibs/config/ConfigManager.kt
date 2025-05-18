/**
 * Main manager for the annotation-based configuration system.
 * This class handles loading, saving, and processing configuration files.
 *
 * 基于注解的配置系统的主要管理器。
 * 该类处理配置文件的加载、保存和处理。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config

import com.arteam.arLibs.ArLibs
import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigField
import com.arteam.arLibs.config.annotations.ConfigValue
import com.arteam.arLibs.config.validation.ConfigValidator
import com.arteam.arLibs.utils.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

/**
 * The ConfigManager handles registration, loading, and saving of configuration files
 * using annotation-based configuration system.
 *
 * ConfigManager 负责使用基于注解的配置系统注册、加载和保存配置文件。
 */
class ConfigManager {
    companion object {
        private val plugin = ArLibs.getInstance()
        private val registeredConfigs = mutableMapOf<KClass<*>, Any>()
        private val configFiles = mutableMapOf<KClass<*>, File>()
        private val yamlConfigs = mutableMapOf<KClass<*>, YamlConfiguration>()
        private val validatorCache = mutableMapOf<String, ConfigValidator<*>>()
        
        /**
         * Registers and loads a configuration class.
         *
         * 注册并加载配置类。
         *
         * @param configClass The class of the configuration to register
         *                    要注册的配置的类
         * @return The instance of the registered configuration
         *         已注册配置的实例
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> register(configClass: KClass<T>): T {
            Logger.debug("Registering configuration: ${configClass.simpleName}")
            // Check if the class has the @Config annotation
            val configAnnotation = configClass.findAnnotation<Config>()
                ?: throw IllegalArgumentException("The class ${configClass.simpleName} is not annotated with @Config")
            
            Logger.debug("Found @Config annotation: fileName='${configAnnotation.fileName}', filePath='${configAnnotation.filePath}'")

            // Create an instance of the configuration class
            val configInstance = try {
                configClass.java.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create an instance of ${configClass.simpleName}. Ensure it has a public no-arg constructor.", e)
            }
            
            Logger.debug("Created instance of ${configClass.simpleName}: $configInstance")

            // Create or get the configuration file path
            val fileName = configAnnotation.fileName
            val filePath = configAnnotation.filePath
            
            // Build the full file path
            val configDir = if (filePath.isNotEmpty()) {
                File(plugin.dataFolder, filePath)
            } else {
                plugin.dataFolder
            }
            
            val configFile = File(configDir, "$fileName.yml")
            Logger.debug("Configuration file path determined: ${configFile.absolutePath}")
            
            // Create parent directories if they don't exist
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
                Logger.debug("Created parent directories for ${configFile.absolutePath}")
            }
            
            // Create the file if it doesn't exist
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile()
                    Logger.debug("Created new configuration file: ${configFile.path}")
                } catch (e: IOException) {
                    Logger.severe("Failed to create configuration file $fileName.yml: ${e.message}")
                }
            }
            
            Logger.debug("Loading YAML from file: ${configFile.path}")
            // Load the YAML configuration
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            
            // Store the config, file, and YAML for later use
            registeredConfigs[configClass] = configInstance
            configFiles[configClass] = configFile
            yamlConfigs[configClass] = yamlConfig
            
            Logger.debug("Initial YAML content for ${configClass.simpleName}:\n${yamlConfig.saveToString()}")

            // Load configuration values into the configInstance and populate yamlConfig with defaults
            // This now uses Kotlin reflection throughout
            loadValuesFromYaml(configInstance, yamlConfig, "")
            
            Logger.debug("Populating YAML from instance for ${configClass.simpleName}")
            // Ensure all values from configInstance (which now has defaults) are set in yamlConfig
            // This is crucial if loadValues didn't already comprehensively update yamlConfig for all fields
            // or if yamlConfig was initially empty and some paths weren't explicitly set.
            populateYamlFromInstance(configInstance, yamlConfig, "")
            Logger.debug("YAML content after populateYamlFromInstance for ${configClass.simpleName}:\n${yamlConfig.saveToString()}")

            // Save the configuration with any missing default values and comments
            saveConfig(configClass)
            
            return configInstance as T
        }
        
        /**
         * Loads configuration values into the configuration object using Kotlin reflection.
         * 将配置值加载到配置对象中 (使用 Kotlin 反射)。
         */
        private fun loadValuesFromYaml(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            Logger.debug("[loadValuesFromYaml] Instance: ${instance::class.simpleName}, ParentPath: '$parentPath'")
            instance::class.memberProperties.forEach { prop ->
                // Skip non-mutable properties if we intend to set them, though defaults might still apply.
                // However, @ConfigValue can be on 'val' if only default is used.
                // For now, process all member properties.
                if (prop.javaField == null && prop.getter.findAnnotation<JvmSynthetic>() == null) { // Skip properties without backing fields unless synthetic (like data class componentN)
                     // Potentially skip static fields more reliably here if needed, KProperty doesn't directly expose 'isStatic' like Java Field.
                     // But typically, companion object properties are separate.
                    return@forEach
                }


                loadProperty(prop, instance, yamlConfig, parentPath)
            }
        }

        /**
         * Processes a KProperty in the configuration class for loading values.
         * 处理配置类中的 KProperty 以加载值。
         */
        private fun loadProperty(
            prop: kotlin.reflect.KProperty1<out Any, *>,
            ownerInstance: Any,
            yamlConfig: YamlConfiguration,
            parentPath: String
        ) {
            val configFieldAnnotation = prop.findAnnotation<ConfigField>()
            val configValueAnnotation = prop.findAnnotation<ConfigValue>()

            Logger.debug("[loadProperty] Property: ${prop.name} in Instance: ${ownerInstance::class.simpleName}, ParentPath: '$parentPath', Has@ConfigField: ${configFieldAnnotation != null}, Has@ConfigValue: ${configValueAnnotation != null}")

            if (configFieldAnnotation != null) {
                val currentPath = if (parentPath.isEmpty()) configFieldAnnotation.path else "$parentPath.${configFieldAnnotation.path}"
                Logger.debug("[loadProperty] @ConfigField: ${prop.name}, Path: $currentPath")
                
                val nestedInstance = prop.call(ownerInstance)
                if (nestedInstance != null) {
                    // Recursively load values for the nested object
                    loadValuesFromYaml(nestedInstance, yamlConfig, currentPath)
                } else {
                    Logger.warn("[loadProperty] Nested instance for @ConfigField '${prop.name}' at path '$currentPath' is null. Skipping.")
                }
            } else if (configValueAnnotation != null) {
                val currentPath = if (parentPath.isEmpty()) configValueAnnotation.path else "$parentPath.${configValueAnnotation.path}"
                Logger.debug("[loadProperty] @ConfigValue: ${prop.name}, Path: $currentPath, Default: '${configValueAnnotation.defaultValue}'")

                if (prop !is KMutableProperty<*>) {
                    Logger.warn("[loadProperty] @ConfigValue found on non-mutable property '${prop.name}' at path '$currentPath'. Only default value can be applied if path is missing in YAML.")
                    // If not mutable, we can only ensure default is in YAML if missing. We can't set the property itself from YAML.
                    if (!yamlConfig.contains(currentPath) && configValueAnnotation.defaultValue.isNotEmpty()) {
                        Logger.debug("[loadProperty] Path $currentPath NOT in YAML, property non-mutable. Setting default in YAML: '${configValueAnnotation.defaultValue}'")
                        // Convert default value based on property type or type hint
                        val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type)
                        yamlConfig.set(currentPath, defaultValue)
                        Logger.debug("[loadProperty] Set default $currentPath = $defaultValue (type: ${defaultValue::class.simpleName}) in YAML for non-mutable property ${prop.name}")
                    }
                    return // Skip setting for non-mutable properties
                }

                val mutableProp = prop as KMutableProperty<*>

                if (yamlConfig.contains(currentPath)) {
                    val loadedValue = yamlConfig.get(currentPath)
                    Logger.debug("[loadProperty] Path $currentPath FOUND in YAML for property ${prop.name}. Raw loaded value: '$loadedValue' (YAML type: ${loadedValue?.let { it::class.simpleName } ?: "null"})")

                    if (loadedValue != null) {
                        try {
                            Logger.debug("[loadProperty] Property ${prop.name} current value in Kotlin object BEFORE setting: '${prop.call(ownerInstance)}'")
                            
                            val convertedValue = if (prop.returnType.jvmErasure.java.isAssignableFrom(loadedValue.javaClass)) {
                                Logger.debug("[loadProperty] Loaded value type (${loadedValue.javaClass.simpleName}) is assignable to property type (${prop.returnType}). Using directly.")
                                loadedValue
                            } else {
                                Logger.debug("[loadProperty] Loaded value type (${loadedValue.javaClass.simpleName}) is NOT assignable to property type (${prop.returnType}). Attempting conversion from string: '$loadedValue'")
                                convertKPropertyToTargetType(loadedValue.toString(), prop, configValueAnnotation.type)
                            }
                            
                            Logger.debug("[loadProperty] Converted loaded value for $currentPath: '$convertedValue' (Kotlin type: ${convertedValue::class.simpleName})")

                            // Apply validators if specified
                            if (configValueAnnotation.validators.isNotEmpty()) {
                                for (validatorName in configValueAnnotation.validators) {
                                    Logger.debug("[loadProperty] Validating $currentPath (value: $convertedValue) with $validatorName")
                                    validateValue(convertedValue, currentPath, validatorName) // Assuming validateValue is compatible
                                }
                            }
                            
                            mutableProp.setter.call(ownerInstance, convertedValue)
                            Logger.debug("[loadProperty] SUCCESSFULLY set property ${prop.name} in ${ownerInstance::class.simpleName} to loaded/converted value: '$convertedValue' (Kotlin object property value now: '${prop.call(ownerInstance)}')")
                        } catch (e: Exception) {
                            Logger.severe("Failed to set loaded value for property ${prop.name} at $currentPath (raw value: '$loadedValue'): ${e.message}")
                            e.printStackTrace() // Add stack trace for better debugging
                        }
                    } else {
                        Logger.warn("[loadProperty] Path $currentPath exists in YAML but loaded value is null. Property ${prop.name} in ${ownerInstance::class.simpleName} will retain its current value or default if applicable.")
                        // Option: if null and prop is nullable, set null. If not nullable, try default.
                        // For now, let's mimic old behavior: do nothing, retain current value (which might be initial default).
                        // If we want to apply default here if YAML has explicit null:
                        if (configValueAnnotation.defaultValue.isNotEmpty()) {
                             Logger.debug("[loadProperty] Path $currentPath is null in YAML, attempting to set default '${configValueAnnotation.defaultValue}' for property ${prop.name}")
                             val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type)
                             mutableProp.setter.call(ownerInstance, defaultValue)
                             yamlConfig.set(currentPath, defaultValue) // Also ensure default is back in YAML if it was explicit null
                             Logger.debug("[loadProperty] Set default $currentPath = $defaultValue to property and YAML due to null in YAML for ${prop.name}")
                        }
                    }
                } else if (configValueAnnotation.defaultValue.isNotEmpty()) {
                    Logger.debug("[loadProperty] Path $currentPath NOT in YAML, using default: '${configValueAnnotation.defaultValue}' for property ${prop.name}")
                    val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type)
                    mutableProp.setter.call(ownerInstance, defaultValue)
                    yamlConfig.set(currentPath, defaultValue) 
                    Logger.debug("[loadProperty] Set default $currentPath = $defaultValue (type: ${defaultValue::class.simpleName}) in instance AND YAML for property ${prop.name}")
                }
            }
        }
        
        /**
         * Populates the YamlConfiguration from the config instance.
         * This ensures all fields from the config object are represented in the YamlConfiguration.
         */
        private fun populateYamlFromInstance(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            Logger.debug("[populateYamlFromInstance] Class: ${instance::class.simpleName}, ParentPath: '$parentPath'")
            
            // Use Kotlin reflection to find annotated properties
            instance::class.memberProperties.forEach { prop ->
                // Skip properties without backing fields unless synthetic (like data class componentN)
                // This check helps avoid issues with extension properties or properties without actual storage
                if (prop.javaField == null && prop.getter.findAnnotation<JvmSynthetic>() == null) {
                    // Logger.debug("[populateYamlFromInstance] Skipping property ${prop.name} as it has no backing field or is not synthetic.")
                    return@forEach
                }

                // Ensure property is accessible if it has a Java field counterpart
                prop.javaField?.isAccessible = true
                
                // // Skip static fields more reliably (though KProperty doesn't directly expose this)
                // // Typically companion object properties are handled differently or excluded by context
                // if (Modifier.isStatic(field.modifiers)) { // This was for Java Field
                //     return@forEach 
                // }

                // Try to find annotations using KProperty
                val configFieldAnnotation = prop.findAnnotation<ConfigField>()
                val configValueAnnotation = prop.findAnnotation<ConfigValue>()

                Logger.debug("[populateYamlFromInstance] Processing KProperty: ${prop.name}, Has@ConfigField: ${configFieldAnnotation != null}, Has@ConfigValue: ${configValueAnnotation != null}")

                when {
                    configFieldAnnotation != null -> {
                        val currentPath = if (parentPath.isEmpty()) configFieldAnnotation.path else "$parentPath.${configFieldAnnotation.path}"
                        Logger.debug("[populateYamlFromInstance] @ConfigField: KProperty=${prop.name}, path=$currentPath")
                        if (!yamlConfig.isConfigurationSection(currentPath)) {
                            yamlConfig.createSection(currentPath)
                            Logger.debug("[populateYamlFromInstance] Created section: $currentPath")
                        }
                        val fieldValue = prop.call(instance) // Use KProperty to get value
                        if (fieldValue != null) {
                            populateYamlFromInstance(fieldValue, yamlConfig, currentPath)
                        }
                    }
                    configValueAnnotation != null -> {
                        val currentPath = if (parentPath.isEmpty()) configValueAnnotation.path else "$parentPath.${configValueAnnotation.path}"
                        val value = prop.call(instance) // Use KProperty to get value
                        Logger.debug("[populateYamlFromInstance] @ConfigValue: KProperty=${prop.name}, path=$currentPath, value=$value, type=${value?.let { it::class.simpleName } ?: "null"}")
                        yamlConfig.set(currentPath, value)
                    }
                }
            }
        }
        
        /**
         * Validates a configuration value using the specified validator.
         * 使用指定的验证器验证配置值。
         */
        @Suppress("UNCHECKED_CAST")
        private fun validateValue(value: Any, path: String, validatorName: String) {
            try {
                Logger.debug("[validateValue] Value: '$value' at Path: '$path' with Validator: '$validatorName'")
                
                val validator = validatorCache.getOrPut(validatorName) {
                    if ("#" in validatorName) {
                        // Factory method syntax: com.example.Factory#methodName(arg1,arg2)
                        val parts = validatorName.split("#", limit = 2)
                        val className = parts[0]
                        val methodPart = parts[1]

                        val regex = "(\\w+)\\((.*)\\)".toRegex()
                        val matchResult = regex.find(methodPart)
                            ?: throw IllegalArgumentException("Invalid factory method format in validator string: $methodPart")

                        val methodName = matchResult.groupValues[1]
                        val argsString = matchResult.groupValues[2]
                        
                        val stringArgs = if (argsString.isNotEmpty()) {
                            argsString.split(',').map { it.trim() }
                        } else {
                            emptyList()
                        }

                        val targetKotlinClass = Class.forName(className).kotlin
                        val targetInstance: Any? = targetKotlinClass.objectInstance // For Kotlin objects or companion objects

                        val functionsToSearch = if (targetInstance != null) {
                            targetKotlinClass.memberFunctions // Functions on the object instance (e.g. companion)
                        } else {
                            targetKotlinClass.staticFunctions // Static functions (Java style)
                        }

                        val method: KFunction<*>? = functionsToSearch.find { func ->
                            func.name == methodName && (func.parameters.size - (if (targetInstance != null && func.parameters.firstOrNull()?.kind == KParameter.Kind.INSTANCE) 1 else 0) == stringArgs.size)
                        } ?: targetKotlinClass.functions.find { func -> // Fallback to all functions including constructors if needed, though factory methods are typical
                             func.name == methodName && (func.parameters.size - (if (targetKotlinClass.objectInstance != null && func.parameters.firstOrNull()?.kind == KParameter.Kind.INSTANCE && className.endsWith("\$Companion")) 1 else if (func.parameters.firstOrNull()?.kind == KParameter.Kind.INSTANCE && !targetKotlinClass.isCompanion && targetKotlinClass.objectInstance == null) 1 else 0) == stringArgs.size)
                        }


                        if (method == null) {
                            throw NoSuchMethodException("Validator factory method '$methodName' with ${stringArgs.size} args not found in '$className'. Searched instance methods: ${targetInstance != null}, static methods: ${targetInstance == null}")
                        }
                        
                        val expectedParams = method.parameters.filterNot { it.kind == KParameter.Kind.INSTANCE || it.kind == KParameter.Kind.EXTENSION_RECEIVER }


                        if (expectedParams.size != stringArgs.size) {
                            throw IllegalArgumentException("Argument count mismatch for validator factory $validatorName. Method ${method.name} expects ${expectedParams.size} arguments, but ${stringArgs.size} were provided (${argsString}).")
                        }

                        val convertedArgs = stringArgs.zip(expectedParams).map { (strArg, kParam) ->
                            convertStringArgToType(strArg, kParam.type.jvmErasure, validatorName)
                        }
                        
                        val callArgs = mutableMapOf<KParameter, Any?>()
                        if (targetInstance != null && method.parameters.any { it.kind == KParameter.Kind.INSTANCE }) {
                             method.parameters.firstOrNull { it.kind == KParameter.Kind.INSTANCE }?.let { instParam ->
                                callArgs[instParam] = targetInstance
                            }
                        }
                        expectedParams.zip(convertedArgs).forEach { (kParam, argVal) ->
                            callArgs[kParam] = argVal
                        }


                        val validatorInstance = method.callBy(callArgs)
                        if (validatorInstance !is ConfigValidator<*>) {
                            throw IllegalArgumentException("Validator factory method $validatorName did not return a ConfigValidator instance.")
                        }
                        validatorInstance
                    } else {
                        // Simple class name syntax
                        val validatorClass = Class.forName(validatorName).kotlin
                        val instance = try {
                            // Try to get a Kotlin object instance (singleton)
                            validatorClass.objectInstance 
                                ?: validatorClass.java.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null) // Or Java static INSTANCE field
                        } catch (_: NoSuchFieldException) {
                            // Try to create a new instance if not a singleton
                            try {
                                validatorClass.java.getDeclaredConstructor().newInstance()
                            } catch (e: InstantiationException) {
                                throw IllegalArgumentException("Failed to instantiate validator $validatorName. Ensure it's a Kotlin object, has an INSTANCE field, or a no-arg constructor.", e)
                            } catch (e: NoSuchMethodException) {
                                 throw IllegalArgumentException("Validator class $validatorName does not have a no-arg constructor.", e)
                            }
                        } catch (e: Exception) { // Catch other reflection errors
                            throw IllegalArgumentException("Failed to get or create instance for validator $validatorName.", e)
                        }

                        if (instance !is ConfigValidator<*>) {
                            throw IllegalArgumentException("Class $validatorName is not a ConfigValidator.")
                        }
                        instance
                    }
                }

                val typedValidator = validator as ConfigValidator<Any> // This cast is potentially unsafe if 'value' type doesn't match validator's generic type
                val validationResult = typedValidator.validate(value, path)
                if (!validationResult.valid) {
                    Logger.warn("Validation failed for value at $path ('$value'): ${validationResult.errorMessage}")
                } else {
                    Logger.debug("Validation successful for $path ('$value') with validator $validatorName")
                }
            } catch (e: Exception) {
                Logger.severe("Failed to process validator $validatorName for path $path: ${e.message}")
                e.printStackTrace() // Print stack trace for better debugging of validator loading/parsing issues
            }
        }

        /**
         * Converts a string argument to the specified KClass type.
         * Used for parsing arguments for validator factory methods.
         */
        private fun convertStringArgToType(argString: String, targetType: KClass<*>, validatorNameHint: String): Any {
            return try {
                when (targetType) {
                    String::class -> argString
                    Int::class -> argString.toInt()
                    Long::class -> argString.toLong()
                    Double::class -> argString.toDouble()
                    Float::class -> argString.toFloat()
                    Boolean::class -> argString.toBooleanStrict()
                    else -> throw IllegalArgumentException("Unsupported parameter type '${targetType.simpleName}' in validator factory '$validatorNameHint'. Cannot convert from string '$argString'.")
                }
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Failed to convert argument '$argString' to type '${targetType.simpleName}' for validator factory '$validatorNameHint'. ${e.message}", e)
            } catch (e: IllegalArgumentException) { // For toBooleanStrict
                 throw IllegalArgumentException("Failed to convert argument '$argString' to Boolean for validator factory '$validatorNameHint'. Must be 'true' or 'false'. ${e.message}", e)
            }
        }
        
        /**
         * Converts a string value to the target type specified by a KProperty.
         * If a typeHint is provided, it takes precedence.
         */
        private fun convertKPropertyToTargetType(value: String, prop: kotlin.reflect.KProperty1<out Any, *>, typeHint: String): Any {
            val targetType = prop.returnType.jvmErasure.java
            if (typeHint.isNotEmpty()) {
                return when (typeHint.lowercase()) {
                    "string" -> value
                    "int", "integer" -> value.toInt()
                    "long" -> value.toLong()
                    "double" -> value.toDouble()
                    "float" -> value.toFloat()
                    "boolean", "bool" -> value.toBoolean()
                    // For lists, assume List<String> if only "list" or "stringlist" is given.
                    // More specific list types (e.g., List<Int>) would need more robust parsing or a different hint.
                    "list", "stringlist" -> value.split(",").map { it.trim() }
                    else -> {
                        Logger.warn("Unknown type hint '$typeHint' for property ${prop.name}. Falling back to property type.")
                        convertStringToType(value, targetType)
                    }
                }
            }
            return convertStringToType(value, targetType)
        }
        
        /**
         * Converts a string to the specified type.
         * 将字符串转换为指定的类型。
         */
        private fun convertStringToType(value: String, type: Class<*>): Any {
            return when (type) {
                String::class.java -> value
                Int::class.java, Integer::class.java -> value.toInt()
                Long::class.java, java.lang.Long::class.java -> value.toLong()
                Double::class.java, java.lang.Double::class.java -> value.toDouble()
                Float::class.java, java.lang.Float::class.java -> value.toFloat()
                Boolean::class.java, java.lang.Boolean::class.java -> value.toBoolean()
                List::class.java, java.util.List::class.java -> value.split(",").map { it.trim() }
                else -> value
            }
        }
        
        /**
         * Saves a configuration to file.
         * 将配置保存到文件。
         *
         * @param configClass The class of the configuration to save
         *                    要保存的配置的类
         */
        fun saveConfig(configClass: KClass<*>) {
            val configFile = configFiles[configClass] ?: return
            val yamlConfig = yamlConfigs[configClass] ?: return
            Logger.debug("[saveConfig] For KClass: ${configClass.simpleName} to File: ${configFile.path}")
            Logger.debug("[saveConfig] YAML content to be saved for ${configClass.simpleName}:\n${yamlConfig.saveToString()}")
            
            try {
                // Use the comment processor to save with comments
                ConfigCommentProcessor.saveWithComments(configClass, configFile, yamlConfig)
            } catch (e: IOException) {
                Logger.severe("Failed to save configuration file ${configFile.name}: ${e.message}")
            }
        }
        
        /**
         * Reloads a configuration from file.
         * 从文件重新加载配置。
         *
         * @param configClass The class of the configuration to reload
         *                    要重新加载的配置的类
         */
        fun reloadConfig(configClass: KClass<*>) {
            val configFile = configFiles[configClass] ?: return
            val configInstance = registeredConfigs[configClass] ?: return
            Logger.debug("[reloadConfig] For KClass: ${configClass.simpleName} from File: ${configFile.path}")
            
            // Reload the YAML configuration
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            yamlConfigs[configClass] = yamlConfig
            
            // Reload configuration values using the new KProperty-based loading
            loadValuesFromYaml(configInstance, yamlConfig, "")
        }
        
        /**
         * Gets a registered configuration instance.
         * 获取已注册的配置实例。
         *
         * @param configClass The class of the configuration to get
         *                    要获取的配置的类
         * @return The instance of the registered configuration, or null if not registered
         *         已注册配置的实例，如果未注册则为 null
         */
        @Suppress("UNCHECKED_CAST", "unused")
        fun <T : Any> getConfig(configClass: KClass<T>): T? {
            return registeredConfigs[configClass] as? T
        }
    }
} 