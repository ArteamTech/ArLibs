/**
 * Main class for the ArLibs plugin.
 * This class initializes the library components and provides core functionality.
 *
 * ArLibs插件的主类。
 * 该类初始化库组件并提供核心功能。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs

import com.arteam.arLibs.config.ConfigManager
import com.arteam.arLibs.config.CoreConfig
import com.arteam.arLibs.utils.Logger
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ArLibs : JavaPlugin() {
    companion object {
        private lateinit var instance: ArLibs
        
        /**
         * Gets the plugin instance.
         * 获取插件实例。
         *
         * @return The plugin instance.
         *         插件实例。
         */
        fun getInstance(): ArLibs {
            return instance
        }
    }
    
    // Core configuration
    private lateinit var coreConfig: CoreConfig
    
    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        instance = this

        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        
        // Initialize core configuration first
        try {
            coreConfig = ConfigManager.register(CoreConfig::class)
            logger.info("Core configuration loaded successfully")
        } catch (e: Exception) {
            // Use Bukkit's logger directly if our Config fails critically early
            logger.severe("Failed to load core configuration: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }
        
        // Initialize the logger with debug setting from config
        Logger.init(this, debug = coreConfig.debug)
        
        // Log plugin startup
        Logger.info("Plugin is starting up...")
        Logger.info("Version: &e${pluginMeta.version}")
        Logger.info("Author: &e${pluginMeta.authors.joinToString(", ")}")
        Logger.info("Debug mode: &e${if (coreConfig.debug) "Enabled" else "Disabled"}")
        
        // Initialize other configurations if needed
        // TODO: Add other configuration registrations here
        
        Logger.info("Plugin has been enabled successfully!")
    }

    override fun onDisable() {
        // Log plugin shutdown
        Logger.info("Plugin is shutting down...")
        
        // Save all configurations before shutdown
        try {
            ConfigManager.saveConfig(CoreConfig::class)
            Logger.info("Configurations saved successfully")
        } catch (e: Exception) {
            Logger.warn("Failed to save configurations: ${e.message}")
        }
        
        Logger.info("Plugin has been disabled.")
        Logger.close() // Close the logger and release resources
    }
}
