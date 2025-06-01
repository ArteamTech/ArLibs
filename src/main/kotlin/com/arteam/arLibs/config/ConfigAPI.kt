/**
 * Public API for the ArLibs configuration system.
 * ArLibs配置系统的公共API。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config

import kotlin.reflect.KClass

/**
 * Main API class for managing configurations with the ArLibs configuration system.
 * 用于通过ArLibs配置系统管理配置的主要API类。
 */
@Suppress("unused")
object ConfigAPI {
    
    /** Registers a single configuration class. 注册单个配置类。 */
    fun <T : Any> registerConfig(configClass: KClass<T>): T = 
        ConfigManager.register(configClass)
    
    /** Registers a configuration class by reified type parameter. 通过具体化类型参数注册配置类。 */
    inline fun <reified T : Any> registerConfig(): T = 
        registerConfig(T::class)
    
    /** Saves a configuration to file. 将配置保存到文件。 */
    fun saveConfig(configClass: KClass<*>) = 
        ConfigManager.saveConfig(configClass)
    
    /** Saves a configuration to file by reified type parameter. 通过具体化类型参数将配置保存到文件。 */
    inline fun <reified T : Any> saveConfig() = 
        saveConfig(T::class)
    
    /** Reloads a configuration from file. 从文件重新加载配置。 */
    fun reloadConfig(configClass: KClass<*>) = 
        ConfigManager.reloadConfig(configClass)
    
    /** Reloads a configuration from file by reified type parameter. 通过具体化类型参数从文件重新加载配置。 */
    inline fun <reified T : Any> reloadConfig() = 
        reloadConfig(T::class)
    
    /** Gets a registered configuration instance. 获取已注册的配置实例。 */
    fun <T : Any> getConfig(configClass: KClass<T>): T? = 
        ConfigManager.getConfig(configClass)
    
    /** Gets a registered configuration instance by reified type parameter. 通过具体化类型参数获取已注册的配置实例。 */
    inline fun <reified T : Any> getConfig(): T? = 
        getConfig(T::class)
    
    /** Checks if a configuration is registered. 检查配置是否已注册。 */
    fun isConfigRegistered(configClass: KClass<*>): Boolean = 
        ConfigManager.getConfig(configClass) != null
    
    /** Checks if a configuration is registered by reified type parameter. 通过具体化类型参数检查配置是否已注册。 */
    inline fun <reified T : Any> isConfigRegistered(): Boolean = 
        isConfigRegistered(T::class)
    
    /** Checks if the configuration system is available. 检查配置系统是否可用。 */
    fun isAvailable(): Boolean = try {
        ConfigManager::class.java
        true
    } catch (_: Exception) {
        false
    }
} 