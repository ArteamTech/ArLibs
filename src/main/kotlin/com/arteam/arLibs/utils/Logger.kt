/**
 * Utility class for logging messages with proper color formatting.
 * This logger provides debug level configuration and automatic plugin context detection.
 *
 * 用于记录带有适当颜色格式化的消息的工具类。
 * 此记录器提供调试级别配置和自动插件上下文检测。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.utils

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

object Logger {
    
    @Volatile private var isDebugEnabled: Boolean = false
    @Volatile private var isInitialized: Boolean = false
    private var pluginName: String? = null
    
    /**
     * Initializes the logger with debug mode setting.
     * This method is thread-safe and can be called multiple times safely.
     *
     * 使用调试模式设置初始化记录器。
     * 此方法是线程安全的，可以安全地多次调用。
     */
    @Synchronized
    fun init(debugMode: Boolean) {
        isDebugEnabled = debugMode
        if (!isInitialized) {
            pluginName = getPluginContext()
            isInitialized = true
        }
    }

    // Logs messages at different levels.
    // 在不同级别记录消息。
    fun info(message: String) = log(Level.INFO, message)
    fun warn(message: String) = log(Level.WARNING, message)
    fun error(message: String) = log(Level.SEVERE, message)

    /**
     * Logs a debug message if debug mode is enabled.
     * Debug messages use INFO level but are only shown when debug is enabled.
     *
     * 如果启用了调试模式，则记录调试消息。
     * 调试消息使用INFO级别，但仅在启用调试时显示。
     */
    fun debug(message: String) {
        if (isDebugEnabled) log(Level.INFO, "[DEBUG] $message")
    }

    /**
     * Logs a message with the specified level.
     * The message is processed for color codes before logging.
     *
     * 使用指定级别记录消息。
     * 消息在记录前会处理颜色代码。
     */
    private fun log(level: Level, message: String) {
        try {
            val processedMessage = if (message.contains('&') || message.contains('<')) {
                ColorUtil.stripColorCodes(ColorUtil.process(message))
            } else message
            
            val formattedMessage = pluginName?.let { "[$it] $processedMessage" } ?: processedMessage
            Bukkit.getLogger().log(level, formattedMessage)
        } catch (e: Exception) {
            System.err.println("Logger error: ${e.message}")
            System.err.println("Original message: $message")
        }
    }

    /**
     * Attempts to determine the plugin context from the call stack.
     * Returns null if the plugin context cannot be determined.
     *
     * 尝试从调用堆栈确定插件上下文。
     * 如果无法确定插件上下文，则返回null。
     */
    private fun getPluginContext(): String? = try {
        Thread.currentThread().stackTrace.asSequence()
            .map { it.className }
            .mapNotNull { className ->
                try {
                    val clazz = Class.forName(className)
                    if (JavaPlugin::class.java.isAssignableFrom(clazz)) {
                        @Suppress("UNCHECKED_CAST")
                        JavaPlugin.getPlugin(clazz as Class<out JavaPlugin>).name
                    } else null
                } catch (_: Exception) { null }
            }
            .firstOrNull()
    } catch (_: Exception) { null }
} 