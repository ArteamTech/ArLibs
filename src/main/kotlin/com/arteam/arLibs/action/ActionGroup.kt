/**
 * Represents a group of actions that can be executed sequentially.
 * This class manages the execution of multiple actions with proper delay handling.
 *
 * 表示可以顺序执行的一组动作。
 * 此类管理多个动作的执行，并正确处理延迟。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import com.arteam.arLibs.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.bukkit.entity.Player

/**
 * A group of actions that can be executed for a player.
 * 可以为玩家执行的一组动作。
 */
@Suppress("unused")
class ActionGroup(private val actions: List<Action>) {
    
    /**
     * Executes all actions in this group for the specified player.
     * 为指定玩家执行此组中的所有动作。
     *
     * @param player The player to execute actions for.
     *               要执行动作的玩家。
     * @return ExecutionResult containing statistics about the execution.
     *         包含执行统计信息的执行结果。
     */
    suspend fun execute(player: Player): ExecutionResult {
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()
        
        try {
            Logger.debug("Starting execution of ${actions.size} actions for player ${player.name}")
            
            for ((index, action) in actions.withIndex()) {
                // Check if the player is still online before executing each action
                if (!player.isOnline) {
                    Logger.debug("Player ${player.name} is no longer online, stopping action execution at index $index")
                    break
                }
                
                try {
                    Logger.debug("Executing action ${index + 1}/${actions.size}: ${action.getType()} for player ${player.name}")
                    action.execute(player)
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    val errorMsg = "Failed to execute action ${action.getType()}: ${e.message}"
                    errors.add(errorMsg)
                    Logger.warn(errorMsg)
                    Logger.debug("Stack trace: ${e.stackTraceToString()}")
                }
            }
        } catch (e: Exception) {
            Logger.warn("Critical error during action group execution for player ${player.name}: ${e.message}")
            e.printStackTrace()
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        Logger.debug("Action group execution completed for ${player.name}. Success: $successCount, Failed: $failureCount, Duration: ${duration}ms")
        
        return ExecutionResult(
            totalActions = actions.size,
            successCount = successCount,
            failureCount = failureCount,
            duration = duration,
            errors = errors
        )
    }
    
    /**
     * Executes all actions in this group for the specified player asynchronously.
     * 为指定玩家异步执行此组中的所有动作。
     *
     * @param player The player to execute actions for.
     *               要执行动作的玩家。
     * @param onComplete Optional callback when execution completes.
     *                   执行完成时的可选回调。
     * @return A Job that can be used to cancel the execution.
     *         可用于取消执行的Job。
     */
    fun executeAsync(player: Player, onComplete: ((ExecutionResult) -> Unit)? = null): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            val result = execute(player)
            onComplete?.invoke(result)
        }
    }
    
    /**
     * Gets the number of actions in this group.
     * 获取此组中的动作数量。
     *
     * @return The number of actions.
     *         动作数量。
     */
    fun size(): Int = actions.size
    
    /**
     * Checks if this group is empty.
     * 检查此组是否为空。
     *
     * @return True if the group is empty, false otherwise.
     *         如果组为空则返回true，否则返回false。
     */
    fun isEmpty(): Boolean = actions.isEmpty()
    
    /**
     * Gets a list of action types in this group.
     * 获取此组中的动作类型列表。
     *
     * @return List of action types.
     *         动作类型列表。
     */
    fun getActionTypes(): List<String> = actions.map { it.getType() }
    
    /**
     * Gets a summary of this action group.
     * 获取此动作组的摘要。
     *
     * @return Summary string.
     *         摘要字符串。
     */
    fun getSummary(): String {
        if (actions.isEmpty()) {
            return "Empty action group"
        }
        
        val typeCount = actions.groupingBy { it.getType() }.eachCount()
        val summary = typeCount.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        return "ActionGroup(${actions.size} actions: $summary)"
    }
    
    override fun toString(): String = getSummary()
    
    /**
     * Data class representing the result of action group execution.
     * 表示动作组执行结果的数据类。
     */
    data class ExecutionResult(
        val totalActions: Int,
        val successCount: Int,
        val failureCount: Int,
        val duration: Long,
        val errors: List<String>
    ) {
        val isFullySuccessful: Boolean get() = failureCount == 0
        val successRate: Double get() = if (totalActions == 0) 1.0 else successCount.toDouble() / totalActions
        
        fun getSummary(): String {
            return "Execution: $successCount/$totalActions successful (${String.format("%.1f", successRate * 100)}%) in ${duration}ms"
        }
    }
} 