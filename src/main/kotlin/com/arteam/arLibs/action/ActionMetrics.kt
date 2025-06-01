/**
 * Metrics and performance tracking for the Action system.
 * This class provides statistics about action execution performance.
 *
 * Action系统的指标和性能跟踪。
 * 此类提供有关动作执行性能的统计信息。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Singleton object for tracking action execution metrics.
 * 用于跟踪动作执行指标的单例对象。
 */
@Suppress("unused")
object ActionMetrics {
    
    private val executionCounts = ConcurrentHashMap<String, AtomicLong>()
    private val executionTimes = ConcurrentHashMap<String, AtomicLong>()
    private val failureCounts = ConcurrentHashMap<String, AtomicLong>()
    
    private val totalExecutions = AtomicLong(0)
    private val totalFailures = AtomicLong(0)
    private val totalExecutionTime = AtomicLong(0)
    
    /**
     * Records a successful action execution.
     * 记录成功的动作执行。
     *
     * @param actionType The type of action executed.
     *                   执行的动作类型。
     * @param executionTime The time taken to execute in milliseconds.
     *                      执行所用时间（毫秒）。
     */
    fun recordExecution(actionType: String, executionTime: Long) {
        executionCounts.computeIfAbsent(actionType) { AtomicLong(0) }.incrementAndGet()
        executionTimes.computeIfAbsent(actionType) { AtomicLong(0) }.addAndGet(executionTime)
        
        totalExecutions.incrementAndGet()
        totalExecutionTime.addAndGet(executionTime)
    }
    
    /**
     * Records a failed action execution.
     * 记录失败的动作执行。
     *
     * @param actionType The type of action that failed.
     *                   失败的动作类型。
     */
    fun recordFailure(actionType: String) {
        failureCounts.computeIfAbsent(actionType) { AtomicLong(0) }.incrementAndGet()
        totalFailures.incrementAndGet()
    }
    
    /**
     * Gets execution statistics for a specific action type.
     * 获取特定动作类型的执行统计信息。
     *
     * @param actionType The action type to get statistics for.
     *                   要获取统计信息的动作类型。
     * @return ActionStats object containing the statistics.
     *         包含统计信息的ActionStats对象。
     */
    fun getActionStats(actionType: String): ActionStats {
        val executions = executionCounts[actionType]?.get() ?: 0
        val totalTime = executionTimes[actionType]?.get() ?: 0
        val failures = failureCounts[actionType]?.get() ?: 0
        
        val averageTime = if (executions > 0) totalTime.toDouble() / executions else 0.0
        val successRate = if (executions + failures > 0) {
            executions.toDouble() / (executions + failures)
        } else 1.0
        
        return ActionStats(
            actionType = actionType,
            executions = executions,
            failures = failures,
            totalExecutionTime = totalTime,
            averageExecutionTime = averageTime,
            successRate = successRate
        )
    }
    
    /**
     * Gets overall system statistics.
     * 获取整体系统统计信息。
     *
     * @return SystemStats object containing overall statistics.
     *         包含整体统计信息的SystemStats对象。
     */
    fun getSystemStats(): SystemStats {
        val totalExecs = totalExecutions.get()
        val totalFails = totalFailures.get()
        val totalTime = totalExecutionTime.get()
        
        val averageTime = if (totalExecs > 0) totalTime.toDouble() / totalExecs else 0.0
        val successRate = if (totalExecs + totalFails > 0) {
            totalExecs.toDouble() / (totalExecs + totalFails)
        } else 1.0
        
        return SystemStats(
            totalExecutions = totalExecs,
            totalFailures = totalFails,
            totalExecutionTime = totalTime,
            averageExecutionTime = averageTime,
            overallSuccessRate = successRate,
            actionTypeCount = executionCounts.size
        )
    }
    
    /**
     * Gets statistics for all action types.
     * 获取所有动作类型的统计信息。
     *
     * @return Map of action type to ActionStats.
     *         动作类型到ActionStats的映射。
     */
    fun getAllActionStats(): Map<String, ActionStats> {
        val allTypes = (executionCounts.keys + failureCounts.keys).toSet()
        return allTypes.associateWith { getActionStats(it) }
    }
    
    /**
     * Resets all metrics.
     * 重置所有指标。
     */
    fun reset() {
        executionCounts.clear()
        executionTimes.clear()
        failureCounts.clear()
        totalExecutions.set(0)
        totalFailures.set(0)
        totalExecutionTime.set(0)
    }
    
    /**
     * Data class for action-specific statistics.
     * 特定动作统计信息的数据类。
     */
    data class ActionStats(
        val actionType: String,
        val executions: Long,
        val failures: Long,
        val totalExecutionTime: Long,
        val averageExecutionTime: Double,
        val successRate: Double
    ) {
        fun getSummary(): String {
            return "$actionType: $executions executions, $failures failures, " +
                    "${String.format("%.2f", averageExecutionTime)}ms avg, " +
                    "${String.format("%.1f", successRate * 100)}% success"
        }
    }
    
    /**
     * Data class for system-wide statistics.
     * 系统级统计信息的数据类。
     */
    data class SystemStats(
        val totalExecutions: Long,
        val totalFailures: Long,
        val totalExecutionTime: Long,
        val averageExecutionTime: Double,
        val overallSuccessRate: Double,
        val actionTypeCount: Int
    ) {
        fun getSummary(): String {
            return "System: $totalExecutions total executions, $totalFailures failures, " +
                    "${String.format("%.2f", averageExecutionTime)}ms avg, " +
                    "${String.format("%.1f", overallSuccessRate * 100)}% success, " +
                    "$actionTypeCount action types"
        }
    }
} 