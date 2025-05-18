/**
 * A logging utility that enhances Bukkit's default logger with color support and multi-plugin compatibility.
 * 一个日志工具，增强Bukkit的默认日志器，支持颜色和多插件兼容性。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.utils

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Level

@Suppress("unused")
object Logger {
    // Thread pool for asynchronous logging
    // 用于异步日志的线程池
    private val loggerExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Debug mode flag
    // 调试模式标志
    private var debugMode: Boolean = false
    
    // Plugin context for logging
    // 日志记录的插件上下文
    private var pluginContext = ThreadLocal<Plugin>()

    /**
     * Initializes this logging utility with plugin context and debug mode.
     * 使用插件上下文和调试模式初始化此日志记录实用程序。
     *
     * @param plugin The plugin to set as context
     *               要设置为上下文的插件
     * @param debug Whether to enable debug mode (default: false)
     *              是否启用调试模式（默认：false）
     */
    fun init(plugin: Plugin, debug: Boolean = false) {
        pluginContext.set(plugin)
        debugMode = debug
    }

    /**
     * Clears the plugin context for the current thread and shuts down this logging utility.
     * 清除当前线程的插件上下文并关闭此日志记录实用程序。
     */
    fun close() {
        pluginContext.remove()
        loggerExecutor.shutdown()
    }

    /**
     * Gets the current plugin context or throws an exception if not set
     * 获取当前插件上下文，如果未设置则抛出异常
     */
    private fun getPluginContext(): Plugin {
        return pluginContext.get() ?: throw IllegalStateException(
            "Logger not initialized. Please call Logger.init(plugin) first or use the plugin parameter version of the logging methods."
        )
    }

    /**
     * Logs an informational message
     * 记录信息级别的日志
     *
     * @param message The message to log
     *                要记录的消息
     */
    fun info(message: String) {
        log(getPluginContext(), Level.INFO, message)
    }

    /**
     * Logs a warning message
     * 记录警告级别的日志
     *
     * @param message The message to log
     *                要记录的消息
     */
    fun warn(message: String) {
        log(getPluginContext(), Level.WARNING, message)
    }

    /**
     * Logs a severe error message
     * 记录严重错误级别的日志
     *
     * @param message The message to log
     *                要记录的消息
     */
    fun severe(message: String) {
        // Always log severe messages synchronously for immediate attention
        // 始终同步记录严重错误消息以立即引起注意
        log(getPluginContext(), Level.SEVERE, message, forceSync = true)
    }

    /**
     * Logs a debug message (only shown when debug mode is enabled)
     * 记录调试信息（仅在调试模式启用时显示）
     *
     * @param message The message to log
     *                要记录的消息
     */
    fun debug(message: String) {
        if (debugMode) {
            log(getPluginContext(), Level.FINE, "[DEBUG] $message")
        }
    }

    /**
     * Internal method to handle the actual message logging.
     * 内部方法，用于处理实际的消息记录。
     *
     * @param plugin The plugin that is logging the message
     *               记录日志的插件
     * @param level The logging level
     *              日志级别
     * @param message The message to log
     *                要记录的消息
     * @param forceSync Whether to force synchronous logging
     *                  是否强制同步记录日志
     */
    private fun log(plugin: Plugin, level: Level, message: String, forceSync: Boolean = false) {
        val logTask = {
            val logger = plugin.logger
            when (level) {
                Level.INFO -> logger.info(message)
                Level.WARNING -> logger.warning(message)
                Level.SEVERE -> logger.severe(message)
                else -> logger.log(level, message)
            }
        }

        if (!forceSync && !Bukkit.isPrimaryThread()) {
            loggerExecutor.execute(logTask)
        } else {
            logTask.invoke()
        }
    }
} 