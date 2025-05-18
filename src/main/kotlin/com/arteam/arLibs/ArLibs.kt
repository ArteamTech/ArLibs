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
import com.arteam.arLibs.config.examples.ConfigCommand
import com.arteam.arLibs.config.examples.ServerConfig
import com.arteam.arLibs.config.examples.StructuredConfig
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
    
    // Optional example configurations
    private var serverConfig: ServerConfig? = null
    private var structuredConfig: StructuredConfig? = null
    
    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        instance = this

        // Initialize the logger
        Logger.init(this, debug = coreConfig.debug.enabled)
        
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        
        // Initialize core configuration first
        try {
            coreConfig = ConfigManager.register(CoreConfig::class)
        } catch (e: Exception) {
            // Use Bukkit's logger directly if our Logger or Config fails critically early
            getLogger().severe("Failed to load core configuration: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }
        
        // Initialize the logger again with debug setting from config (if needed, or remove this if hardcoding for test)
        // For this test, we can comment this out to keep the hardcoded debug = true
        // Logger.init(this, debug = coreConfig.debug.enabled)
        // Logger.info("Logger re-initialized with config debug: ${coreConfig.debug.enabled}")
        
        // Log plugin startup
        Logger.info("Plugin is starting up...")
        Logger.info("Version: &e${pluginMeta.version}")
        Logger.info("Author: &e${pluginMeta.authors.joinToString(", ")}")
        
        // Initialize the rest of the configuration system
        initConfigurations()
        
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
    
    /**
     * Initializes the configuration system and loads configuration files.
     * 初始化配置系统并加载配置文件。
     */
    private fun initConfigurations() {
        try {
            // Log configuration info
            Logger.info("Debug mode: ${if (coreConfig.debug.enabled) "&aenabled" else "&cdisabled"}")
            Logger.info("Log level: ${coreConfig.debug.logLevel}")
            
            // Initialize example configurations if examples are enabled
            if (coreConfig.debug.enableExamples) {
                Logger.info("Examples are enabled, loading example configurations...")
                
                // Register and load the server configuration
                serverConfig = ConfigManager.register(ServerConfig::class)
                Logger.info("Loaded server configuration")
                
                // Register and load the structured configuration
                structuredConfig = ConfigManager.register(StructuredConfig::class)
                Logger.info("Loaded structured configuration")
                
                // Log example configuration values
                Logger.info("Example config - Server name: ${serverConfig?.serverName}")
                Logger.info("Example config - Structured app name: ${structuredConfig?.general?.name}")
                
                // Register the example command
                ConfigCommand.register(this)
                Logger.info("Registered example configuration command")
            } else {
                Logger.info("Examples are disabled. Set 'debug.enable-examples' to true in core.yml to enable examples.")
            }
        } catch (e: Exception) {
            Logger.severe("Failed to initialize configurations: ${e.message}")
            e.printStackTrace()
        }
    }
}
