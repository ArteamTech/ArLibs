package dev.arteam.arlibs

import dev.arteam.arlibs.utils.Logger
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * ArLibs - A Minecraft library plugin for extending other projects
 */
class ArLibs : JavaPlugin() {
    
    companion object {
        lateinit var instance: ArLibs
            private set
            
        /**
         * Get a logger instance for the specified plugin
         * @param plugin The plugin to get a logger for
         * @param debugEnabled Whether debug mode should be enabled
         * @return A new logger instance
         */
        @JvmStatic
        @JvmOverloads
        fun getLogger(plugin: Plugin, debugEnabled: Boolean = false): Logger {
            return Logger.getLogger(plugin, debugEnabled)
        }
    }
    
    // Internal logger for ArLibs itself
    private lateinit var logger: Logger
    
    override fun onEnable() {
        instance = this
        
        // Initialize our custom logger
        logger = Logger.getLogger(this)
        
        // Log startup messages with colorized output
        logger.info("&b${description.name} &av${description.version} &eenabled!")
        logger.info("&aArLibs library plugin has been successfully loaded")
    }
    
    override fun onDisable() {
        logger.info("&c${description.name} disabled!")
    }
} 