/**
 * Action implementation for conditional execution (If-Then-Else logic).
 * Executes different actions based on condition evaluation results.
 *
 * 条件执行的动作实现（If-Then-Else逻辑）。
 * 根据条件评估结果执行不同的动作。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.action.Action
import com.arteam.arLibs.action.ActionGroup
import com.arteam.arLibs.condition.Condition
import com.arteam.arLibs.utils.Logger
import org.bukkit.entity.Player

/**
 * Action that executes different action groups based on condition evaluation.
 * Supports If-Then-Else logic for conditional action execution.
 * 
 * 根据条件评估执行不同动作组的动作。
 * 支持条件动作执行的If-Then-Else逻辑。
 */
class ConditionalAction(
    private val condition: Condition,
    private val thenActions: ActionGroup,
    private val elseActions: ActionGroup? = null
) : Action {
    
    override suspend fun execute(player: Player) {
        try {
            val conditionResult = condition.evaluate(player)
            
            Logger.debug("ConditionalAction for ${player.name}: condition '${condition.getDescription()}' = $conditionResult")
            
            if (conditionResult) {
                // Execute 'then' actions
                Logger.debug("Executing 'then' actions for ${player.name}")
                val result = thenActions.execute(player)
                Logger.debug("Then actions result: ${result.getSummary()}")
            } else if (elseActions != null) {
                // Execute 'else' actions
                Logger.debug("Executing 'else' actions for ${player.name}")
                val result = elseActions.execute(player)
                Logger.debug("Else actions result: ${result.getSummary()}")
            } else {
                Logger.debug("Condition false and no else actions defined for ${player.name}")
            }
            
        } catch (e: Exception) {
            Logger.warn("Error executing conditional action for ${player.name}: ${e.message}")
            Logger.debug("Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    override fun getType(): String = "conditional"
    
    /**
     * Gets a description of this conditional action.
     * 获取此条件动作的描述。
     *
     * @return Description string.
     *         描述字符串。
     */
    fun getDescription(): String {
        val elseDescription = if (elseActions != null) {
            " else ${elseActions.getActionTypes().joinToString(",")}"
        } else {
            ""
        }
        return "if ${condition.getDescription()} then ${thenActions.getActionTypes().joinToString(",")}$elseDescription"
    }
    
    override fun toString(): String = "ConditionalAction(${getDescription()})"
    
    companion object {
        /**
         * Creates a simple If-Then conditional action.
         * 创建简单的If-Then条件动作。
         *
         * @param condition The condition to evaluate.
         *                  要评估的条件。
         * @param thenActions The actions to execute if condition is true.
         *                    条件为真时执行的动作。
         * @return A new ConditionalAction instance.
         *         新的ConditionalAction实例。
         */
        fun ifThen(condition: Condition, thenActions: ActionGroup): ConditionalAction {
            return ConditionalAction(condition, thenActions)
        }
        
        /**
         * Creates an If-Then-Else conditional action.
         * 创建If-Then-Else条件动作。
         *
         * @param condition The condition to evaluate.
         *                  要评估的条件。
         * @param thenActions The actions to execute if condition is true.
         *                    条件为真时执行的动作。
         * @param elseActions The actions to execute if condition is false.
         *                    条件为假时执行的动作。
         * @return A new ConditionalAction instance.
         *         新的ConditionalAction实例。
         */
        fun ifThenElse(condition: Condition, thenActions: ActionGroup, elseActions: ActionGroup): ConditionalAction {
            return ConditionalAction(condition, thenActions, elseActions)
        }
    }
} 