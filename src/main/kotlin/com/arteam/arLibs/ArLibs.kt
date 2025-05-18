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
    
    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        instance = this
        
        // Initialize the logger uses ThreadLocal, so init() sets the context for the current thread.
        // If other threads or parts of your plugin need to log, ensure Logger.init() is called
        // appropriately or modify Logger to handle context differently (e.g., passing plugin instance).
        Logger.init(this, debug = false)
        
        // Log plugin startup
        Logger.info("Plugin is starting up...")
        Logger.info("Version: &e${pluginMeta.version}")
        Logger.info("Author: &e${pluginMeta.authors.joinToString(", ")}")
        
        // TODO: Initialize other components
        
        Logger.info("Plugin has been enabled successfully!")
    }

    override fun onDisable() {
        // Log plugin shutdown
        Logger.info("Plugin is shutting down...")
        
        // TODO: Clean up resources
        
        // It's good practice to clean up resources used by the Logger if necessary
        // For example, if the Logger uses an ExecutorService, it should be shut down.
        // The current Logger.close() method handles this.
        Logger.info("Plugin has been disabled.")
        Logger.close() // Close the logger and release resources
    }
    
}
