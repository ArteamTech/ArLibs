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
 * using an annotation-based configuration system.
 *
 * ConfigManager 负责使用基于注解的配置系统注册、加载和保存配置文件。
 */
@Suppress("unused")
class ConfigManager {
    companion object {
        private val plugin = ArLibs.getInstance()
        private val registeredConfigs = mutableMapOf<KClass<*>, Any>()
        private val configFiles = mutableMapOf<KClass<*>, File>()
        private val yamlConfigs = mutableMapOf<KClass<*>, YamlConfiguration>()
        private val validatorCache = mutableMapOf<String, ConfigValidator<*>>()
        
        /**
         * Registers and loads a configuration class.
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
            
            // Create an instance of the configuration class
            val configInstance = try {
                configClass.java.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create an instance of ${configClass.simpleName}. Ensure it has a public no-arg constructor.", e)
            }
            
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
            
            // Create parent directories if they don't exist
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
            }
            
            // Create the file if it doesn't exist
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile()
                    Logger.debug("Created new configuration file: ${configFile.name}")
                } catch (e: IOException) {
                    Logger.severe("Failed to create configuration file $fileName.yml: ${e.message}")
                }
            }
            
            // Load the YAML configuration
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            
            // Store the config, file, and YAML for later use
            registeredConfigs[configClass] = configInstance
            configFiles[configClass] = configFile
            yamlConfigs[configClass] = yamlConfig

            // Load configuration values into the configInstance and populate yamlConfig with defaults
            loadValuesFromYaml(configInstance, yamlConfig, "")
            
            // Ensure all values from configInstance are set in yamlConfig
            populateYamlFromInstance(configInstance, yamlConfig, "")

            // Save the configuration with any missing default values and comments
            saveConfig(configClass)
            
            Logger.debug("Configuration ${configClass.simpleName} registered successfully")
            return configInstance as T
        }
        
        /**
         * Loads configuration values into the configuration object using Kotlin reflection.
         * 将配置值加载到配置对象中 (使用 Kotlin 反射)。
         * 
         * @param instance The configuration instance to load values into
         *                 要加载值的配置实例
         * @param yamlConfig The YamlConfiguration to load values from
         *                   要从中加载值的 YamlConfiguration
         * @param parentPath The parent path for nested properties
         *                   嵌套属性的父路径
         */
        private fun loadValuesFromYaml(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            instance::class.memberProperties.forEach { prop ->
                if (prop.javaField == null && prop.getter.findAnnotation<JvmSynthetic>() == null) {
                    return@forEach
                }

                // Load the property value from the YamlConfiguration
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

            if (configFieldAnnotation != null) {
                val currentPath = if (parentPath.isEmpty()) configFieldAnnotation.path else "$parentPath.${configFieldAnnotation.path}"
                
                val nestedInstance = prop.call(ownerInstance)
                if (nestedInstance != null) {
                    // Recursively load values for the nested object
                    loadValuesFromYaml(nestedInstance, yamlConfig, currentPath)
                } else {
                    Logger.warn("Nested instance for @ConfigField '${prop.name}' at path '$currentPath' is null. Skipping.")
                }
            } else if (configValueAnnotation != null) {
                val currentPath = if (parentPath.isEmpty()) configValueAnnotation.path else "$parentPath.${configValueAnnotation.path}"

                // Helper function to determine if property is a List type
                fun isPropertyAList(kProp: kotlin.reflect.KProperty1<out Any, *>) = kProp.returnType.jvmErasure.isSubclassOf(List::class)

                if (prop !is KMutableProperty<*>) {
                    Logger.warn("@ConfigValue found on non-mutable property '${prop.name}' at path '$currentPath'. Only default value can be applied if path is missing in YAML.")
                    if (!yamlConfig.contains(currentPath) && configValueAnnotation.defaultValue.isNotEmpty()) {
                        val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type, yamlConfig, currentPath)
                        yamlConfig.set(currentPath, defaultValue)
                    }
                    return // Skip setting for non-mutable properties
                }

                val mutableProp = prop as KMutableProperty<*>

                if (yamlConfig.contains(currentPath)) {
                    val loadedValue = yamlConfig.get(currentPath)

                    if (loadedValue != null) {
                        try {
                            val convertedValue: Any? = when {
                                // Case 1: Loaded value is a List and property is a List
                                loadedValue is List<*> && isPropertyAList(prop) -> {
                                    val listElementTypeHint = configValueAnnotation.type.lowercase()
                                    when {
                                        listElementTypeHint.startsWith("list<") && listElementTypeHint.endsWith(">") -> {
                                            val elementType = listElementTypeHint.substring(5, listElementTypeHint.length - 1)
                                            loadedValue.mapNotNull { element ->
                                                try {
                                                    convertSingleElementToType(element, elementType, prop.name)
                                                } catch (_: Exception) {
                                                    Logger.warn("Failed to convert list element '$element' to '$elementType' for property ${prop.name}. Skipping element.")
                                                    null
                                                }
                                            }
                                        }
                                        listElementTypeHint == "list" || listElementTypeHint == "stringlist" -> {
                                            loadedValue.map { it?.toString() }
                                        }
                                        else -> {
                                            loadedValue // Pass as is
                                        }
                                    }
                                }
                                // Case 2: The loaded value type is directly assignable to the property type
                                prop.returnType.jvmErasure.java.isAssignableFrom(loadedValue.javaClass) -> {
                                    loadedValue
                                }
                                // Case 3: Fallback to conversion from string
                                else -> {
                                    convertKPropertyToTargetType(loadedValue.toString(), prop, configValueAnnotation.type, yamlConfig, currentPath)
                                }
                            }

                            // Apply validators if specified
                            if (configValueAnnotation.validators.isNotEmpty()) {
                                for (validatorName in configValueAnnotation.validators) {
                                    if (convertedValue != null) {
                                        validateValue(convertedValue, currentPath, validatorName)
                                    }
                                }
                            }
                            
                            mutableProp.setter.call(ownerInstance, convertedValue)
                        } catch (e: Exception) {
                            Logger.severe("Failed to set loaded value for property ${prop.name} at $currentPath: ${e.message}")
                        }
                    } else {
                        if (configValueAnnotation.defaultValue.isNotEmpty()) {
                             val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type, yamlConfig, currentPath)
                             mutableProp.setter.call(ownerInstance, defaultValue)
                             yamlConfig.set(currentPath, defaultValue) 
                        } else if (prop.returnType.isMarkedNullable) {
                            mutableProp.setter.call(ownerInstance, null)
                        }
                    }
                } else if (configValueAnnotation.defaultValue.isNotEmpty()) {
                    val defaultValue = convertKPropertyToTargetType(configValueAnnotation.defaultValue, prop, configValueAnnotation.type, yamlConfig, currentPath)
                    mutableProp.setter.call(ownerInstance, defaultValue)
                    yamlConfig.set(currentPath, defaultValue) 
                }
            }
        }
        
        /**
         * Populates the YamlConfiguration from the config instance.
         * This ensures all fields from the config object are represented in the YamlConfiguration.
         * 从配置实例填充 YamlConfiguration。
         * 确保配置对象中的所有字段都表示在 YamlConfiguration 中。
         * 
         * @param instance The configuration instance to populate from
         *                 要从中填充的配置实例
         * @param yamlConfig The YamlConfiguration to populate
         *                   要填充的 YamlConfiguration
         * @param parentPath The parent path for nested properties
         *                   嵌套属性的父路径
         */
        private fun populateYamlFromInstance(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            instance::class.memberProperties.forEach { prop ->
                if (prop.javaField == null && prop.getter.findAnnotation<JvmSynthetic>() == null) {
                    return@forEach
                }

                // Ensure property is accessible if it has a Java field counterpart
                prop.javaField?.isAccessible = true
                
                // Try to find annotations using KProperty
                val configFieldAnnotation = prop.findAnnotation<ConfigField>()
                val configValueAnnotation = prop.findAnnotation<ConfigValue>()

                // Process the KProperty based on its annotations
                when {
                    configFieldAnnotation != null -> {
                        val currentPath = if (parentPath.isEmpty()) configFieldAnnotation.path else "$parentPath.${configFieldAnnotation.path}"
                        if (!yamlConfig.isConfigurationSection(currentPath)) {
                            yamlConfig.createSection(currentPath)
                        }
                        val fieldValue = prop.call(instance) // Use KProperty to get value
                        if (fieldValue != null) {
                            populateYamlFromInstance(fieldValue, yamlConfig, currentPath)
                        }
                    }
                    configValueAnnotation != null -> {
                        val currentPath = if (parentPath.isEmpty()) configValueAnnotation.path else "$parentPath.${configValueAnnotation.path}"
                        val value = prop.call(instance) // Use KProperty to get value
                        yamlConfig.set(currentPath, value)
                    }
                }
            }
        }
        
        /**
         * Validates a configuration value using the specified validator.
         * 使用指定的验证器验证配置值。
         * 
         * @param value The value to validate
         *              要验证的值
         * @param path The path in the configuration
         *             配置中的路径
         * @param validatorName The name of the validator to use
         *                      要使用的验证器名称
         */
        @Suppress("UNCHECKED_CAST")
        private fun validateValue(value: Any, path: String, validatorName: String) {
            try {
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
                            targetKotlinClass.memberFunctions // Functions on the object instance (e.g., companion)
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

                // Validate the value using the validator
                val validationResult = validator.validate(value, path)
                if (!validationResult.valid) {
                    Logger.warn("Validation failed for value at $path ('$value'): ${validationResult.errorMessage}")
                }
            } catch (e: Exception) {
                Logger.severe("Failed to process validator $validatorName for path $path: ${e.message}")
            }
        }

        /**
         * Converts a string argument to the specified KClass type.
         * Used for parsing arguments for validator factory methods.
         * 将字符串参数转换为指定的 KClass 类型。
         * 被用于验证器工厂方法的参数解析。
         * 
         * @param argString The string argument to convert
         *                  要转换的字符串参数
         * @param targetType The KClass type to convert to
         *                   要转换的目标类型
         * @param validatorNameHint The name of the validator factory method for error reporting
         *                          用于错误报告的验证器工厂方法名称
         * @return The converted value
         *         转换后的值
         * @throws IllegalArgumentException if the argument cannot be converted
         *         如果参数不能转换则抛出 IllegalArgumentException
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
         * 将字符串值转换为 KProperty 指定的目标类型。
         * 如果提供了 typeHint，则优先使用它。
         * 
         * @param value The string value to convert
         *              要转换的字符串值
         * @param prop The KProperty to convert to
         *             要转换的 KProperty
         * @param typeHint The type hint to use for conversion
         *                 要使用的类型提示
         * @param yamlConfig The YamlConfiguration to load values from
         *                    要从中加载值的 YamlConfiguration
         * @param currentPath The current path in the configuration
         *                    配置中的当前路径
         * @return The converted value
         *         转换后的值
         */
        private fun convertKPropertyToTargetType(value: String, prop: kotlin.reflect.KProperty1<out Any, *>, typeHint: String, yamlConfig: YamlConfiguration, currentPath: String): Any? {
            val targetPropType = prop.returnType
            val targetErasure = targetPropType.jvmErasure.java
            
            // Priority to typeHint
            if (typeHint.isNotEmpty()) {
                val lowerHint = typeHint.lowercase()
                try {
                    return when {
                        lowerHint == "string" -> value
                        lowerHint == "int" || lowerHint == "integer" -> value.toInt()
                        lowerHint == "long" -> value.toLong()
                        lowerHint == "double" -> value.toDouble()
                        lowerHint == "float" -> value.toFloat()
                        lowerHint == "boolean" || lowerHint == "bool" -> value.toBoolean()
                        lowerHint.startsWith("list<") && lowerHint.endsWith(">") -> {
                            val elementType = lowerHint.substring(5, lowerHint.length - 1)
                            if (value.isBlank()) emptyList<Any?>() else value.split(",").mapNotNull { item ->
                                convertSingleElementToType(item.trim(), elementType, prop.name)
                            }
                        }
                        lowerHint == "list" || lowerHint == "stringlist" -> {
                            if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }
                        }
                        else -> {
                            convertStringToTypeErasure(value, targetErasure, prop.name)
                        }
                    }
                } catch (_: Exception) {
                    return try { convertStringToTypeErasure(value, targetErasure, prop.name) } catch (_: Exception) { null }
                }
            }
            
            // If no typeHint, use property's type erasure
            return convertStringToTypeErasure(value, targetErasure, prop.name)
        }

        /**
         * Converts a single element (typically string or from a loaded list) to a target type string.
         * Used for list elements.
         * 将单个元素（通常是字符串或从加载的列表中获取的）转换为目标类型字符串。
         * 用于列表元素。
         * 
         * @param element The element to convert
         *               要转换的元素
         * @param targetTypeString The target type string
         *                        目标类型字符串
         * @param propertyNameForLog The property name for logging
         *                         用于日志记录的属性名称
         * @return The converted value
         *         转换后的值
         */
        private fun convertSingleElementToType(element: Any?, targetTypeString: String, propertyNameForLog: String): Any? {
            if (element == null) return null
            val valueStr = element.toString().trim() // Trim whitespace for robust parsing

            return try {
                when (targetTypeString.lowercase()) {
                    "string" -> valueStr
                    "int", "integer" -> valueStr.toInt()
                    "long" -> valueStr.toLong()
                    "double" -> valueStr.toDouble()
                    "float" -> valueStr.toFloat()
                    "boolean", "bool" -> valueStr.toBoolean() // "true" (any case) -> true, "false" (any case) -> false, others -> false
                    else -> {
                        Logger.warn("Unsupported element type '$targetTypeString' for property '$propertyNameForLog'. Returning string form.")
                        valueStr // Fallback to string if type is unknown
                    }
                }
            } catch (e: NumberFormatException) {
                Logger.severe("Failed to convert element '$valueStr' to numeric type '$targetTypeString' for property '$propertyNameForLog': ${e.message}")
                null // Or throw, or return original element
            } catch (e: IllegalArgumentException) { // For things like toBooleanStrict if used
                Logger.severe("Failed to convert element '$valueStr' to type '$targetTypeString' for property '$propertyNameForLog': ${e.message}")
                null
            }
        }
        
        /**
         * Converts a string to the specified erased type.
         * 将字符串转换为指定的擦除类型。
         * 
         * @param value The string value to convert
         *              要转换的字符串值
         * @param type The Class type to convert to
         *             要转换的目标类型
         * @param propertyNameForLog The property name for logging
         *                           用于日志记录的属性名称
         * @return The converted value
         *         转换后的值
         */
        private fun convertStringToTypeErasure(value: String, type: Class<*>, propertyNameForLog: String): Any? {
            return try {
                when (type) {
                    String::class.java -> value
                    Int::class.java, Integer::class.java -> value.toInt()
                    Long::class.java, java.lang.Long::class.java -> value.toLong()
                    Double::class.java, java.lang.Double::class.java -> value.toDouble()
                    Float::class.java, java.lang.Float::class.java -> value.toFloat()
                    Boolean::class.java, java.lang.Boolean::class.java -> value.toBoolean()
                    List::class.java, java.util.List::class.java -> {
                        // This case is for when the property is List<Something> and input is a String
                        // (e.g., from default value)
                        // It assumes a comma-separated string for the list.
                        // If typeHint was "list<int>", convertKPropertyToTargetType handles element conversion.
                        // If typeHint was "list" or "stringlist", it also handles it.
                        // This is a fallback if no specific list type hint was given but property is Listed.
                        // Assume List<String>.
                        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }
                    }
                    else -> {
                        value // Fallback for other types is not explicitly handled
                    }
                }
            } catch (e: NumberFormatException) {
                Logger.severe("Error converting string '$value' to numeric erased type ${type.simpleName} for $propertyNameForLog: ${e.message}")
                null
            } catch (e: Exception) {
                 Logger.severe("Error converting string '$value' to erased type ${type.simpleName} for $propertyNameForLog: ${e.message}")
                null
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
            
            try {
                // Use the comment processor to save with comments
                ConfigCommentProcessor.saveWithComments(configClass, configFile, yamlConfig)
                Logger.debug("Configuration ${configClass.simpleName} saved successfully")
            } catch (e: IOException) {
                Logger.severe("Failed to save configuration file ${configFile.name}: ${e.message}")
            }
        }
        
        /**
         * Reloads a configuration from the file.
         * 从文件重新加载配置。
         *
         * @param configClass The class of the configuration to reload
         *                    要重新加载的配置的类
         */
        fun reloadConfig(configClass: KClass<*>) {
            val configFile = configFiles[configClass] ?: return
            val configInstance = registeredConfigs[configClass] ?: return
            
            // Reload the YAML configuration
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            yamlConfigs[configClass] = yamlConfig
            
            // Reload configuration values using the new KProperty-based loading
            loadValuesFromYaml(configInstance, yamlConfig, "")
            Logger.debug("Configuration ${configClass.simpleName} reloaded successfully")
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