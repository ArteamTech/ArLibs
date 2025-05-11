package dev.arteam.arlibs.utils

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

/**
 * Enhanced logger utility that supports colored output and proper plugin attribution
 * @param plugin The plugin this logger is for
 * @param debugEnabled Whether debug messages should be shown
 */
class Logger(private val plugin: Plugin, private val debugEnabled: Boolean = false) {
    
    companion object {
        /**
         * Create a new logger instance for the given plugin
         * @param plugin The plugin to create the logger for
         * @param debugEnabled Whether debug messages should be shown
         * @return A new Logger instance
         */
        fun getLogger(plugin: Plugin, debugEnabled: Boolean = false): Logger {
            return Logger(plugin, debugEnabled)
        }
    }
    
    /**
     * Log an info message with color support
     * @param message The message to log
     */
    fun info(message: String) {
        log(Level.INFO, message)
    }
    
    /**
     * Log a warning message with color support
     * @param message The message to log
     */
    fun warn(message: String) {
        log(Level.WARNING, message)
    }
    
    /**
     * Log an error message with color support
     * @param message The message to log
     */
    fun error(message: String) {
        log(Level.SEVERE, message)
    }
    
    /**
     * Log a debug message with color support (only shown if debug is enabled)
     * @param message The message to log
     */
    fun debug(message: String) {
        if (debugEnabled) {
            val colorized = ColorUtil.colorize("&7[DEBUG] &r$message")
            plugin.logger.log(Level.INFO, colorized)
        }
    }
    
    /**
     * Log a message with the specified level and color support
     * @param level The log level
     * @param message The message to log
     */
    fun log(level: Level, message: String) {
        // Use the plugin's logger to ensure proper attribution
        val colorized = ColorUtil.colorize(message)
        plugin.logger.log(level, colorized)
    }
    
    /**
     * Log an exception with a custom message
     * @param message The message to log
     * @param throwable The exception to log
     */
    fun exception(message: String, throwable: Throwable) {
        val colorized = ColorUtil.colorize("&c$message")
        plugin.logger.log(Level.SEVERE, colorized, throwable)
    }
    
    /**
     * Check if debug mode is enabled
     * @return True if debug mode is enabled
     */
    fun isDebugEnabled(): Boolean {
        return debugEnabled
    }
    
    /**
     * Create a new logger with debug mode enabled or disabled
     * @param enabled Whether debug mode should be enabled
     * @return A new logger with the specified debug mode
     */
    fun withDebug(enabled: Boolean): Logger {
        return Logger(plugin, enabled)
    }
} 