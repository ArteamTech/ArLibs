/**
 * Main manager for the annotation-based configuration system.
 * 基于注解的配置系统的主要管理器。
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
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

/**
 * ConfigManager handles registration, loading, and saving of configuration files.
 * ConfigManager 负责配置文件的注册、加载和保存。
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
         *                    要注册的配置类
         * @return The instance of the registered configuration
         *         已注册配置的实例
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> register(configClass: KClass<T>): T {
            Logger.debug("Registering configuration: ${configClass.simpleName}")
            
            val configAnnotation = configClass.findAnnotation<Config>()
                ?: throw IllegalArgumentException("Class ${configClass.simpleName} is not annotated with @Config")
            
            val configInstance = try {
                configClass.java.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create instance of ${configClass.simpleName}. Ensure it has a public no-arg constructor.", e)
            }
            
            val configDir = if (configAnnotation.filePath.isNotEmpty()) {
                File(plugin.dataFolder, configAnnotation.filePath)
            } else plugin.dataFolder
            
            val configFile = File(configDir, "${configAnnotation.fileName}.yml")
            
            configFile.parentFile?.mkdirs()
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile()
                    Logger.debug("Created new configuration file: ${configFile.name}")
                } catch (e: IOException) {
                    Logger.severe("Failed to create configuration file ${configAnnotation.fileName}.yml: ${e.message}")
                }
            }
            
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            
            registeredConfigs[configClass] = configInstance
            configFiles[configClass] = configFile
            yamlConfigs[configClass] = yamlConfig

            loadValuesFromYaml(configInstance, yamlConfig, "")
            populateYamlFromInstance(configInstance, yamlConfig, "")
            saveConfig(configClass)
            
            Logger.debug("Configuration ${configClass.simpleName} registered successfully")
            return configInstance as T
        }
        
        /**
         * Loads configuration values from YAML into the configuration object.
         * 从YAML加载配置值到配置对象中。
         * 
         * @param instance The configuration instance to load values into
         *                 要加载值的配置实例
         * @param yamlConfig The YamlConfiguration to load values from
         *                   要从中加载值的YamlConfiguration
         * @param parentPath The parent path for nested properties
         *                   嵌套属性的父路径
         */
        private fun loadValuesFromYaml(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            instance::class.memberProperties.forEach { prop ->
                if (prop.javaField != null || prop.getter.findAnnotation<JvmSynthetic>() != null) {
                    loadProperty(prop, instance, yamlConfig, parentPath)
                }
            }
        }

        /**
         * Processes a single property for loading values.
         * 处理单个属性以加载值。
         * 
         * @param prop The property to process
         *             要处理的属性
         * @param ownerInstance The instance that owns this property
         *                      拥有此属性的实例
         * @param yamlConfig The YAML configuration
         *                   YAML配置
         * @param parentPath The parent path for nested properties
         *                   嵌套属性的父路径
         */
        private fun loadProperty(
            prop: kotlin.reflect.KProperty1<out Any, *>,
            ownerInstance: Any,
            yamlConfig: YamlConfiguration,
            parentPath: String
        ) {
            val configFieldAnnotation = prop.findAnnotation<ConfigField>()
            val configValueAnnotation = prop.findAnnotation<ConfigValue>()

            when {
                configFieldAnnotation != null -> {
                    val currentPath = buildPath(parentPath, configFieldAnnotation.path)
                    val nestedInstance = prop.call(ownerInstance)
                    if (nestedInstance != null) {
                        loadValuesFromYaml(nestedInstance, yamlConfig, currentPath)
                    } else {
                        Logger.warn("Nested instance for @ConfigField '${prop.name}' at path '$currentPath' is null")
                    }
                }
                configValueAnnotation != null -> {
                    loadConfigValue(prop, ownerInstance, yamlConfig, parentPath, configValueAnnotation)
                }
            }
        }

        /**
         * Loads a configuration value with proper type conversion and validation.
         * 加载配置值并进行适当的类型转换和验证。
         * 
         * @param prop The property to load
         *             要加载的属性
         * @param ownerInstance The instance that owns this property
         *                      拥有此属性的实例
         * @param yamlConfig The YAML configuration
         *                   YAML配置
         * @param parentPath The parent path
         *                   父路径
         * @param annotation The ConfigValue annotation
         *                   ConfigValue注解
         */
        private fun loadConfigValue(
            prop: kotlin.reflect.KProperty1<out Any, *>,
            ownerInstance: Any,
            yamlConfig: YamlConfiguration,
            parentPath: String,
            annotation: ConfigValue
        ) {
            val currentPath = buildPath(parentPath, annotation.path)
            
            if (prop !is KMutableProperty<*>) {
                Logger.warn("@ConfigValue on non-mutable property '${prop.name}' at '$currentPath'")
                if (!yamlConfig.contains(currentPath) && annotation.defaultValue.isNotEmpty()) {
                    yamlConfig.set(currentPath, convertValue(annotation.defaultValue, prop, annotation.type))
                }
                return
            }

            val mutableProp = prop as KMutableProperty<*>
            val loadedValue = yamlConfig.get(currentPath)

            when {
                loadedValue != null -> {
                    try {
                        val convertedValue = when {
                            loadedValue is List<*> && prop.returnType.jvmErasure.isSubclassOf(List::class) -> {
                                convertListValue(loadedValue, annotation.type, prop.name)
                            }
                            prop.returnType.jvmErasure.java.isAssignableFrom(loadedValue.javaClass) -> loadedValue
                            else -> convertValue(loadedValue.toString(), prop, annotation.type)
                        }

                        annotation.validators.forEach { validatorName ->
                            convertedValue?.let { validateValue(it, currentPath, validatorName) }
                        }
                        
                        mutableProp.setter.call(ownerInstance, convertedValue)
                    } catch (e: Exception) {
                        Logger.severe("Failed to set value for ${prop.name} at $currentPath: ${e.message}")
                    }
                }
                annotation.defaultValue.isNotEmpty() -> {
                    val defaultValue = convertValue(annotation.defaultValue, prop, annotation.type)
                    mutableProp.setter.call(ownerInstance, defaultValue)
                    yamlConfig.set(currentPath, defaultValue)
                }
                prop.returnType.isMarkedNullable -> {
                    mutableProp.setter.call(ownerInstance, null)
                }
                annotation.required -> {
                    Logger.severe("Required value missing at '$currentPath' for property '${prop.name}'")
                }
            }
        }

        /**
         * Converts a list value based on the type hint.
         * 根据类型提示转换列表值。
         */
        private fun convertListValue(loadedValue: List<*>, typeHint: String, propName: String): List<*> {
            val lowerHint = typeHint.lowercase()
            return when {
                lowerHint.startsWith("list<") && lowerHint.endsWith(">") -> {
                    val elementType = lowerHint.substring(5, lowerHint.length - 1)
                    loadedValue.mapNotNull { element ->
                        try {
                            convertSingleElement(element, elementType)
                        } catch (_: Exception) {
                            Logger.warn("Failed to convert list element '$element' for property $propName")
                            null
                        }
                    }
                }
                lowerHint in listOf("list", "stringlist") -> loadedValue.map { it?.toString() }
                else -> loadedValue
            }
        }
        
        /**
         * Populates YamlConfiguration from the configuration instance.
         * 从配置实例填充YamlConfiguration。
         * 
         * @param instance The configuration instance
         *                 配置实例
         * @param yamlConfig The YAML configuration to populate
         *                   要填充的YAML配置
         * @param parentPath The parent path for nested properties
         *                   嵌套属性的父路径
         */
        private fun populateYamlFromInstance(instance: Any, yamlConfig: YamlConfiguration, parentPath: String) {
            instance::class.memberProperties.forEach { prop ->
                if (prop.javaField == null && prop.getter.findAnnotation<JvmSynthetic>() == null) return@forEach
                
                prop.javaField?.isAccessible = true
                
                when {
                    prop.findAnnotation<ConfigField>() != null -> {
                        val annotation = prop.findAnnotation<ConfigField>()!!
                        val currentPath = buildPath(parentPath, annotation.path)
                        if (!yamlConfig.isConfigurationSection(currentPath)) {
                            yamlConfig.createSection(currentPath)
                        }
                        prop.call(instance)?.let { populateYamlFromInstance(it, yamlConfig, currentPath) }
                    }
                    prop.findAnnotation<ConfigValue>() != null -> {
                        val annotation = prop.findAnnotation<ConfigValue>()!!
                        val currentPath = buildPath(parentPath, annotation.path)
                        yamlConfig.set(currentPath, prop.call(instance))
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
         * @param path The configuration path
         *             配置路径
         * @param validatorName The validator name or factory method
         *                      验证器名称或工厂方法
         */
        @Suppress("UNCHECKED_CAST")
        private fun validateValue(value: Any, path: String, validatorName: String) {
            try {
                val validator = validatorCache.getOrPut(validatorName) { createValidator(validatorName) }
                val result = validator.validate(value, path)
                if (!result.valid) {
                    Logger.warn("Validation failed for $path ('$value'): ${result.errorMessage}")
                }
            } catch (e: Exception) {
                Logger.severe("Failed to process validator $validatorName for $path: ${e.message}")
            }
        }

        /**
         * Creates a validator instance from the validator name.
         * 从验证器名称创建验证器实例。
         */
        private fun createValidator(validatorName: String): ConfigValidator<*> {
            return if ("#" in validatorName) {
                createFactoryValidator(validatorName)
            } else {
                createSimpleValidator(validatorName)
            }
        }

        /**
         * Creates a validator using factory method syntax.
         * 使用工厂方法语法创建验证器。
         */
        private fun createFactoryValidator(validatorName: String): ConfigValidator<*> {
            val parts = validatorName.split("#", limit = 2)
            val className = parts[0]
            val methodPart = parts[1]

            val regex = "(\\w+)\\((.*)\\)".toRegex()
            val matchResult = regex.find(methodPart)
                ?: throw IllegalArgumentException("Invalid factory method format: $methodPart")

            val methodName = matchResult.groupValues[1]
            val argsString = matchResult.groupValues[2]
            val stringArgs = if (argsString.isNotEmpty()) argsString.split(',').map { it.trim() } else emptyList()

            val targetClass = Class.forName(className).kotlin
            val targetInstance = targetClass.objectInstance

            val method = (if (targetInstance != null) targetClass.memberFunctions else targetClass.staticFunctions)
                .find { it.name == methodName && it.parameters.filterNot { p -> p.kind in listOf(KParameter.Kind.INSTANCE, KParameter.Kind.EXTENSION_RECEIVER) }.size == stringArgs.size }
                ?: throw NoSuchMethodException("Method '$methodName' with ${stringArgs.size} args not found in '$className'")

            val expectedParams = method.parameters.filterNot { it.kind in listOf(KParameter.Kind.INSTANCE, KParameter.Kind.EXTENSION_RECEIVER) }
            val convertedArgs = stringArgs.zip(expectedParams).map { (arg, param) ->
                convertStringArg(arg, param.type.jvmErasure, validatorName)
            }

            val callArgs = mutableMapOf<KParameter, Any?>()
            targetInstance?.let { inst ->
                method.parameters.firstOrNull { it.kind == KParameter.Kind.INSTANCE }?.let { callArgs[it] = inst }
            }
            expectedParams.zip(convertedArgs).forEach { (param, arg) -> callArgs[param] = arg }

            return (method.callBy(callArgs) as? ConfigValidator<*>)
                ?: throw IllegalArgumentException("Factory method $validatorName did not return ConfigValidator")
        }

        /**
         * Creates a simple validator by class name.
         * 通过类名创建简单验证器。
         */
        private fun createSimpleValidator(validatorName: String): ConfigValidator<*> {
            val validatorClass = Class.forName(validatorName).kotlin
            val instance = try {
                validatorClass.objectInstance
                    ?: validatorClass.java.getDeclaredField("INSTANCE").apply { isAccessible = true }.get(null)
            } catch (_: NoSuchFieldException) {
                try {
                    validatorClass.java.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    throw IllegalArgumentException("Failed to instantiate validator $validatorName", e)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to get instance for validator $validatorName", e)
            }

            return (instance as? ConfigValidator<*>)
                ?: throw IllegalArgumentException("Class $validatorName is not a ConfigValidator")
        }

        /**
         * Converts string arguments for validator factory methods.
         * 为验证器工厂方法转换字符串参数。
         */
        private fun convertStringArg(argString: String, targetType: KClass<*>, hint: String): Any {
            return try {
                when (targetType) {
                    String::class -> argString
                    Int::class -> argString.toInt()
                    Long::class -> argString.toLong()
                    Double::class -> argString.toDouble()
                    Float::class -> argString.toFloat()
                    Boolean::class -> argString.toBooleanStrict()
                    else -> throw IllegalArgumentException("Unsupported type '${targetType.simpleName}' in '$hint'")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to convert '$argString' to ${targetType.simpleName} for '$hint'", e)
            }
        }
        
        /**
         * Converts a value with the optional type hint.
         * 使用可选类型提示转换值。
         */
        private fun convertValue(value: String, prop: kotlin.reflect.KProperty1<out Any, *>, typeHint: String): Any? {
            return if (typeHint.isNotEmpty()) {
                convertWithHint(value, typeHint, prop)
            } else {
                convertToType(value, prop.returnType.jvmErasure.java, prop.name)
            }
        }

        /**
         * Converts value using the type hint.
         * 使用类型提示转换值。
         */
        private fun convertWithHint(value: String, typeHint: String, prop: kotlin.reflect.KProperty1<out Any, *>): Any? {
            val hint = typeHint.lowercase()
            return try {
                when {
                    hint == "string" -> value
                    hint in listOf("int", "integer") -> value.toInt()
                    hint == "long" -> value.toLong()
                    hint == "double" -> value.toDouble()
                    hint == "float" -> value.toFloat()
                    hint in listOf("boolean", "bool") -> value.toBoolean()
                    hint.startsWith("list<") && hint.endsWith(">") -> {
                        val elementType = hint.substring(5, hint.length - 1)
                        if (value.isBlank()) emptyList<Any?>()
                        else value.split(",").mapNotNull { convertSingleElement(it.trim(), elementType) }
                    }
                    hint in listOf("list", "stringlist") -> {
                        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }
                    }
                    else -> convertToType(value, prop.returnType.jvmErasure.java, prop.name)
                }
            } catch (_: Exception) {
                try { convertToType(value, prop.returnType.jvmErasure.java, prop.name) } catch (_: Exception) { null }
            }
        }

        /**
         * Converts a single element for list processing.
         * 为列表处理转换单个元素。
         */
        private fun convertSingleElement(element: Any?, targetType: String): Any? {
            element ?: return null
            val str = element.toString().trim()
            return try {
                when (targetType.lowercase()) {
                    "string" -> str
                    "int", "integer" -> str.toInt()
                    "long" -> str.toLong()
                    "double" -> str.toDouble()
                    "float" -> str.toFloat()
                    "boolean", "bool" -> str.toBoolean()
                    else -> str
                }
            } catch (e: Exception) {
                Logger.warn("Failed to convert '$str' to '$targetType': ${e.message}")
                null
            }
        }
        
        /**
         * Converts string to specific Java type.
         * 将字符串转换为特定Java类型。
         */
        private fun convertToType(value: String, type: Class<*>, propName: String): Any? {
            return try {
                when (type) {
                    String::class.java -> value
                    Int::class.java, Integer::class.java -> value.toInt()
                    Long::class.java, java.lang.Long::class.java -> value.toLong()
                    Double::class.java, java.lang.Double::class.java -> value.toDouble()
                    Float::class.java, java.lang.Float::class.java -> value.toFloat()
                    Boolean::class.java, java.lang.Boolean::class.java -> value.toBoolean()
                    List::class.java, java.util.List::class.java -> {
                        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }
                    }
                    else -> value
                }
            } catch (e: Exception) {
                Logger.severe("Error converting '$value' to ${type.simpleName} for $propName: ${e.message}")
                null
            }
        }

        /**
         * Builds configuration path from parent and child paths.
         * 从父路径和子路径构建配置路径。
         */
        private fun buildPath(parentPath: String, path: String): String {
            return if (parentPath.isEmpty()) path else "$parentPath.$path"
        }
        
        /**
         * Saves a configuration to file.
         * 将配置保存到文件。
         * 
         * @param configClass The configuration class to save
         *                    要保存的配置类
         */
        fun saveConfig(configClass: KClass<*>) {
            val configFile = configFiles[configClass] ?: return
            val yamlConfig = yamlConfigs[configClass] ?: return
            
            try {
                ConfigCommentProcessor.saveWithComments(configClass, configFile, yamlConfig)
                Logger.debug("Configuration ${configClass.simpleName} saved successfully")
            } catch (e: IOException) {
                Logger.severe("Failed to save configuration file ${configFile.name}: ${e.message}")
            }
        }
        
        /**
         * Reloads a configuration from file.
         * 从文件重新加载配置。
         * 
         * @param configClass The configuration class to reload
         *                    要重新加载的配置类
         */
        fun reloadConfig(configClass: KClass<*>) {
            val configFile = configFiles[configClass] ?: return
            val configInstance = registeredConfigs[configClass] ?: return
            
            val yamlConfig = YamlConfiguration.loadConfiguration(configFile)
            yamlConfigs[configClass] = yamlConfig
            
            loadValuesFromYaml(configInstance, yamlConfig, "")
            Logger.debug("Configuration ${configClass.simpleName} reloaded successfully")
        }
        
        /**
         * Gets a registered configuration instance.
         * 获取已注册的配置实例。
         * 
         * @param configClass The configuration class
         *                    配置类
         * @return The configuration instance, or null if not registered
         *         配置实例，如果未注册则为null
         */
        @Suppress("UNCHECKED_CAST", "unused")
        fun <T : Any> getConfig(configClass: KClass<T>): T? {
            return registeredConfigs[configClass] as? T
        }
    }
}