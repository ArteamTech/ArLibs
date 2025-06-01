/**
 * Condition implementation for checking player permissions.
 * Supports both positive and negative permission checks.
 *
 * 用于检查玩家权限的条件实现。
 * 支持正向和负向权限检查。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.condition.conditions

import com.arteam.arLibs.condition.Condition
import org.bukkit.entity.Player

class PermissionCondition(
    private val permission: String,
    private val negate: Boolean = false
) : Condition {
    
    override fun evaluate(player: Player): Boolean {
        val hasPermission = player.hasPermission(permission)
        return if (negate) !hasPermission else hasPermission
    }
    
    override fun getDescription(): String {
        val prefix = if (negate) "NOT " else ""
        return "${prefix}permission: $permission"
    }
    
    companion object {
        /**
         * Creates a PermissionCondition from a string expression.
         * Supports formats like "permission.node" or "!permission.node" for negation.
         *
         * 从字符串表达式创建PermissionCondition。
         * 支持 "permission.node" 或 "!permission.node"（否定）等格式。
         *
         * @param expression The permission expression.
         *                   权限表达式。
         * @return A new PermissionCondition instance.
         *         新的PermissionCondition实例。
         */
        fun fromExpression(expression: String): PermissionCondition {
            val trimmed = expression.trim()
            return if (trimmed.startsWith("!")) {
                PermissionCondition(trimmed.substring(1), negate = true)
            } else {
                PermissionCondition(trimmed)
            }
        }
    }
} 