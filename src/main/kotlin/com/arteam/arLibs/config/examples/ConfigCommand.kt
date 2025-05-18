/**
 * Configuration module usage examples.
 * This file demonstrates how to use the annotation-based configuration system.
 *
 * 配置模块使用示例。
 * 此文件演示如何使用基于注解的配置系统。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.examples

import com.arteam.arLibs.ArLibs
import com.arteam.arLibs.config.ConfigManager
import com.arteam.arLibs.utils.Logger
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Example command handler for configuration management.
 * Demonstrates how to work with configurations in-game.
 * 
 * 配置管理的示例命令处理程序。
 * 演示如何在游戏中使用配置。
 */
class ConfigCommand : CommandExecutor {

    // Register and load the configurations
    private val serverConfig: ServerConfig = ConfigManager.register(ServerConfig::class)
    private val structuredConfig: StructuredConfig = ConfigManager.register(StructuredConfig::class)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "reload" -> {
                if (!sender.hasPermission("arlibs.config.reload")) {
                    sender.sendMessage("§cYou don't have permission to reload configurations.")
                    return true
                }
                
                val configType = if (args.size > 1) args[1].lowercase() else "all"
                
                when (configType) {
                    "server" -> {
                        ConfigManager.reloadConfig(ServerConfig::class)
                        sender.sendMessage("§aServer configuration reloaded successfully!")
                    }
                    "structured" -> {
                        ConfigManager.reloadConfig(StructuredConfig::class)
                        sender.sendMessage("§aStructured configuration reloaded successfully!")
                    }
                    "all" -> {
                        ConfigManager.reloadConfig(ServerConfig::class)
                        ConfigManager.reloadConfig(StructuredConfig::class)
                        sender.sendMessage("§aAll configurations reloaded successfully!")
                    }
                    else -> {
                        sender.sendMessage("§cUnknown configuration type. Use 'server', 'structured', or 'all'.")
                    }
                }
                return true
            }
            "get" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUsage: /config get <config-type> <key>")
                    return true
                }
                
                if (args.size < 3) {
                    // Show available keys for the config type
                    when (args[1].lowercase()) {
                        "server" -> {
                            sender.sendMessage("§eAvailable server config keys:")
                            sender.sendMessage("§7server-name, max-players, debug-mode")
                            sender.sendMessage("§7database.host, database.port, database.username, database.password, database.name")
                            sender.sendMessage("§7advanced.connection-timeout, advanced.packet-compression-threshold, advanced.use-native-transport")
                        }
                        "structured" -> {
                            sender.sendMessage("§eAvailable structured config sections:")
                            sender.sendMessage("§7general, network, storage")
                            sender.sendMessage("§eUse §7/config get structured <section>§e to see available keys in a section.")
                        }
                        else -> {
                            sender.sendMessage("§cUnknown configuration type. Use 'server' or 'structured'.")
                        }
                    }
                    return true
                }
                
                val configType = args[1].lowercase()
                val key = args[2]
                
                val value = when (configType) {
                    "server" -> getServerConfigValue(key)
                    "structured" -> getStructuredConfigValue(key)
                    else -> "Unknown configuration type"
                }
                
                sender.sendMessage("§aConfig value for §e${configType}.${key}§a: §f$value")
                return true
            }
            "set" -> {
                if (!sender.hasPermission("arlibs.config.edit")) {
                    sender.sendMessage("§cYou don't have permission to edit configurations.")
                    return true
                }
                
                if (args.size < 4) {
                    sender.sendMessage("§cUsage: /config set <config-type> <key> <value>")
                    return true
                }
                
                val configType = args[1].lowercase()
                val key = args[2]
                val value = args[3]
                
                val success = when (configType) {
                    "server" -> {
                        val result = setServerConfigValue(key, value)
                        if (result) ConfigManager.saveConfig(ServerConfig::class)
                        result
                    }
                    "structured" -> {
                        val result = setStructuredConfigValue(key, value)
                        if (result) ConfigManager.saveConfig(StructuredConfig::class)
                        result
                    }
                    else -> false
                }
                
                if (success) {
                    sender.sendMessage("§aConfig value §e${configType}.${key}§a set to §f${value}")
                } else {
                    sender.sendMessage("§cFailed to set config value. Invalid type, key, or value.")
                }
                return true
            }
            "info" -> {
                if (args.size > 1) {
                    when (args[1].lowercase()) {
                        "server" -> showServerConfigInfo(sender)
                        "structured" -> showStructuredConfigInfo(sender)
                        else -> sender.sendMessage("§cUnknown configuration type. Use 'server' or 'structured'.")
                    }
                } else {
                    showServerConfigInfo(sender)
                }
                return true
            }
            else -> {
                showHelp(sender)
                return true
            }
        }
    }
    
    private fun showHelp(sender: CommandSender) {
        sender.sendMessage("§e=== ArLibs Configuration Commands ===")
        sender.sendMessage("§f/config reload [type] §7- Reload configuration(s)")
        sender.sendMessage("§f/config get <type> [key] §7- Get configuration value(s)")
        sender.sendMessage("§f/config set <type> <key> <value> §7- Set a configuration value")
        sender.sendMessage("§f/config info [type] §7- Show configuration info")
        sender.sendMessage("§7Types: server, structured, all")
    }
    
    private fun showServerConfigInfo(sender: CommandSender) {
        sender.sendMessage("§e=== Server Configuration ===")
        sender.sendMessage("§fServer Name: §a${serverConfig.serverName}")
        sender.sendMessage("§fMax Players: §a${serverConfig.maxPlayers}")
        sender.sendMessage("§fDebug Mode: §a${serverConfig.debugMode}")
        sender.sendMessage("§e=== Database Configuration ===")
        sender.sendMessage("§fHost: §a${serverConfig.database.host}")
        sender.sendMessage("§fPort: §a${serverConfig.database.port}")
        sender.sendMessage("§fDatabase: §a${serverConfig.database.databaseName}")
    }
    
    private fun showStructuredConfigInfo(sender: CommandSender) {
        sender.sendMessage("§e=== Structured Configuration ===")
        sender.sendMessage("§e--- General Settings ---")
        sender.sendMessage("§fName: §a${structuredConfig.general.name}")
        sender.sendMessage("§fVersion: §a${structuredConfig.general.version}")
        sender.sendMessage("§fLanguage: §a${structuredConfig.general.language}")
        sender.sendMessage("§e--- Network Settings ---")
        sender.sendMessage("§fHost: §a${structuredConfig.network.host}")
        sender.sendMessage("§fPort: §a${structuredConfig.network.port}")
        sender.sendMessage("§e--- Storage Settings ---")
        sender.sendMessage("§fType: §a${structuredConfig.storage.type}")
    }
    
    private fun getServerConfigValue(key: String): Any? {
        return when (key.lowercase()) {
            "server-name", "servername" -> serverConfig.serverName
            "max-players", "maxplayers" -> serverConfig.maxPlayers
            "debug-mode", "debugmode" -> serverConfig.debugMode
            "database.host" -> serverConfig.database.host
            "database.port" -> serverConfig.database.port
            "database.username" -> serverConfig.database.username
            "database.password" -> "********" // Don't show actual password
            "database.name", "database.database-name" -> serverConfig.database.databaseName
            "advanced.connection-timeout" -> serverConfig.advanced.connectionTimeout
            "advanced.packet-compression-threshold" -> serverConfig.advanced.packetCompressionThreshold
            "advanced.use-native-transport" -> serverConfig.advanced.useNativeTransport
            else -> "Unknown key"
        }
    }
    
    private fun getStructuredConfigValue(key: String): Any? {
        val parts = key.split(".")
        
        return when {
            key.startsWith("general.") -> {
                when (parts.getOrNull(1)?.lowercase()) {
                    "name" -> structuredConfig.general.name
                    "version" -> structuredConfig.general.version
                    "language" -> structuredConfig.general.language
                    "ui.theme" -> structuredConfig.general.ui.theme
                    "ui.font-size" -> structuredConfig.general.ui.fontSize
                    "ui.show-tooltips" -> structuredConfig.general.ui.showTooltips
                    else -> "Unknown general key"
                }
            }
            key.startsWith("network.") -> {
                when (parts.getOrNull(1)?.lowercase()) {
                    "host" -> structuredConfig.network.host
                    "port" -> structuredConfig.network.port
                    "connection.timeout" -> structuredConfig.network.connection.timeout
                    "connection.max-retries" -> structuredConfig.network.connection.maxRetries
                    "connection.use-ssl" -> structuredConfig.network.connection.useSsl
                    "api.key" -> "********" // Don't show actual API key
                    "api.version" -> structuredConfig.network.api.version
                    "api.rate-limit" -> structuredConfig.network.api.rateLimit
                    else -> "Unknown network key"
                }
            }
            key.startsWith("storage.") -> {
                when (parts.getOrNull(1)?.lowercase()) {
                    "type" -> structuredConfig.storage.type
                    "file.path" -> structuredConfig.storage.file.path
                    "file.compress" -> structuredConfig.storage.file.compress
                    "file.backup-frequency" -> structuredConfig.storage.file.backupFrequency
                    "database.host" -> structuredConfig.storage.database.host
                    "database.port" -> structuredConfig.storage.database.port
                    "database.name" -> structuredConfig.storage.database.name
                    "database.username" -> structuredConfig.storage.database.username
                    "database.password" -> "********" // Don't show actual password
                    "database.pool.min-size" -> structuredConfig.storage.database.pool.minSize
                    "database.pool.max-size" -> structuredConfig.storage.database.pool.maxSize
                    "database.pool.idle-timeout" -> structuredConfig.storage.database.pool.idleTimeout
                    else -> "Unknown storage key"
                }
            }
            key == "general" -> "Section: name, version, language, ui.*"
            key == "network" -> "Section: host, port, connection.*, api.*"
            key == "storage" -> "Section: type, file.*, database.*"
            else -> "Unknown key"
        }
    }
    
    private fun setServerConfigValue(key: String, value: String): Boolean {
        try {
            when (key.lowercase()) {
                "server-name", "servername" -> serverConfig.serverName = value
                "max-players", "maxplayers" -> serverConfig.maxPlayers = value.toInt()
                "debug-mode", "debugmode" -> serverConfig.debugMode = value.toBoolean()
                "database.host" -> serverConfig.database.host = value
                "database.port" -> serverConfig.database.port = value.toInt()
                "database.username" -> serverConfig.database.username = value
                "database.password" -> serverConfig.database.password = value
                "database.name", "database.database-name" -> serverConfig.database.databaseName = value
                "advanced.connection-timeout" -> serverConfig.advanced.connectionTimeout = value.toLong()
                "advanced.packet-compression-threshold" -> serverConfig.advanced.packetCompressionThreshold = value.toInt()
                "advanced.use-native-transport" -> serverConfig.advanced.useNativeTransport = value.toBoolean()
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Logger.severe("Failed to set server config value: ${e.message}")
            return false
        }
    }
    
    private fun setStructuredConfigValue(key: String, value: String): Boolean {
        try {
            val parts = key.split(".")
            
            when {
                key.startsWith("general.") -> {
                    when (parts.getOrNull(1)?.lowercase()) {
                        "name" -> structuredConfig.general.name = value
                        "version" -> structuredConfig.general.version = value
                        "language" -> structuredConfig.general.language = value
                        "ui.theme" -> structuredConfig.general.ui.theme = value
                        "ui.font-size" -> structuredConfig.general.ui.fontSize = value.toInt()
                        "ui.show-tooltips" -> structuredConfig.general.ui.showTooltips = value.toBoolean()
                        else -> return false
                    }
                }
                key.startsWith("network.") -> {
                    when (parts.getOrNull(1)?.lowercase()) {
                        "host" -> structuredConfig.network.host = value
                        "port" -> structuredConfig.network.port = value.toInt()
                        "connection.timeout" -> structuredConfig.network.connection.timeout = value.toInt()
                        "connection.max-retries" -> structuredConfig.network.connection.maxRetries = value.toInt()
                        "connection.use-ssl" -> structuredConfig.network.connection.useSsl = value.toBoolean()
                        "api.key" -> structuredConfig.network.api.key = value
                        "api.version" -> structuredConfig.network.api.version = value
                        "api.rate-limit" -> structuredConfig.network.api.rateLimit = value.toInt()
                        else -> return false
                    }
                }
                key.startsWith("storage.") -> {
                    when (parts.getOrNull(1)?.lowercase()) {
                        "type" -> structuredConfig.storage.type = value
                        "file.path" -> structuredConfig.storage.file.path = value
                        "file.compress" -> structuredConfig.storage.file.compress = value.toBoolean()
                        "file.backup-frequency" -> structuredConfig.storage.file.backupFrequency = value.toInt()
                        "database.host" -> structuredConfig.storage.database.host = value
                        "database.port" -> structuredConfig.storage.database.port = value.toInt()
                        "database.name" -> structuredConfig.storage.database.name = value
                        "database.username" -> structuredConfig.storage.database.username = value
                        "database.password" -> structuredConfig.storage.database.password = value
                        "database.pool.min-size" -> structuredConfig.storage.database.pool.minSize = value.toInt()
                        "database.pool.max-size" -> structuredConfig.storage.database.pool.maxSize = value.toInt()
                        "database.pool.idle-timeout" -> structuredConfig.storage.database.pool.idleTimeout = value.toInt()
                        else -> return false
                    }
                }
                else -> return false
            }
            return true
        } catch (e: Exception) {
            Logger.severe("Failed to set structured config value: ${e.message}")
            return false
        }
    }
    
    /**
     * Registers this command with the plugin.
     * 
     * 向插件注册此命令。
     */
    companion object {
        fun register(plugin: ArLibs) {
            val command = ConfigCommand()
            plugin.getCommand("config")?.setExecutor(command)
        }
    }
} 