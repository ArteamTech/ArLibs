/**
 * Example configuration class for server settings.
 * Demonstrates how to use the annotation-based configuration system.
 *
 * 服务器设置的示例配置类。
 * 演示如何使用基于注解的配置系统。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config.examples

import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigField
import com.arteam.arLibs.config.annotations.ConfigValue

/**
 * Server configuration example. Contains general server settings and database connection details.
 * 服务器配置示例。包含一般服务器设置和数据库连接详情。
 */
@Config(
    fileName = "server",
    comments = [
        "Server configuration file",
        "Contains general server settings and database connection details",
        "",
        "服务器配置文件",
        "包含一般服务器设置和数据库连接详情"
    ]
)
class ServerConfig {
    
    @ConfigValue(
        path = "server-name",
        defaultValue = "ArLibs Server",
        comments = [
            "The name of the server",
            "服务器的名称"
        ]
    )
    var serverName: String = "ArLibs Server"
    
    @ConfigValue(
        path = "max-players",
        defaultValue = "100",
        comments = [
            "Maximum number of players allowed on the server",
            "服务器允许的最大玩家数量"
        ]
    )
    var maxPlayers: Int = 100
    
    @ConfigValue(
        path = "debug-mode",
        defaultValue = "false",
        comments = [
            "Whether debug mode is enabled",
            "是否启用调试模式"
        ]
    )
    var debugMode: Boolean = false
    
    /**
     * Database connection settings.
     * 数据库连接设置。
     */
    @ConfigField(
        path = "database",
        comments = [
            "Database connection settings",
            "数据库连接设置"
        ]
    )
    val database = DatabaseConfig()
    
    /**
     * Advanced server settings.
     * 高级服务器设置。
     */
    @ConfigField(
        path = "advanced",
        comments = [
            "Advanced server settings",
            "Only modify these if you know what you're doing",
            "",
            "高级服务器设置",
            "只有在你知道自己在做什么时才修改这些"
        ]
    )
    val advanced = AdvancedConfig()
    
    /**
     * Database connection configuration.
     * 数据库连接配置。
     */
    class DatabaseConfig {
        @ConfigValue(
            path = "host",
            defaultValue = "localhost",
            comments = [
                "Database host address",
                "数据库主机地址"
            ]
        )
        var host: String = "localhost"
        
        @ConfigValue(
            path = "port",
            defaultValue = "3306",
            comments = [
                "Database port",
                "数据库端口"
            ]
        )
        var port: Int = 3306
        
        @ConfigValue(
            path = "username",
            defaultValue = "root",
            comments = [
                "Database username",
                "数据库用户名"
            ]
        )
        var username: String = "root"
        
        @ConfigValue(
            path = "password",
            defaultValue = "",
            comments = [
                "Database password",
                "数据库密码"
            ]
        )
        var password: String = ""
        
        @ConfigValue(
            path = "database-name",
            defaultValue = "arlibs",
            comments = [
                "Database name",
                "数据库名称"
            ]
        )
        var databaseName: String = "arlibs"
    }
    
    /**
     * Advanced server configuration.
     * 高级服务器配置。
     */
    class AdvancedConfig {
        @ConfigValue(
            path = "connection-timeout",
            defaultValue = "5000",
            comments = [
                "Connection timeout in milliseconds",
                "连接超时（毫秒）"
            ]
        )
        var connectionTimeout: Long = 5000
        
        @ConfigValue(
            path = "packet-compression-threshold",
            defaultValue = "256",
            comments = [
                "Packet compression threshold",
                "数据包压缩阈值"
            ]
        )
        var packetCompressionThreshold: Int = 256
        
        @ConfigValue(
            path = "use-native-transport",
            defaultValue = "true",
            comments = [
                "Whether to use native transport",
                "是否使用本机传输"
            ]
        )
        var useNativeTransport: Boolean = true
    }
} 