/**
 * Public API for the Action system.
 * This class provides methods for other plugins to use the action system.
 *
 * Action系统的公共API。
 * 此类为其他插件提供使用动作系统的方法。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action

import com.arteam.arLibs.action.actions.ConditionalAction
import com.arteam.arLibs.condition.Condition
import com.arteam.arLibs.condition.ConditionParser
import com.arteam.arLibs.utils.Logger
import kotlinx.coroutines.Job
import org.bukkit.entity.Player

/**
 * Public API for executing actions and action groups.
 * 用于执行动作和动作组的公共API。
 */
@Suppress("unused")
object ActionAPI {
    
    /**
     * Executes a single action for a player.
     * 为玩家执行单个动作。
     *
     * @param player The player to execute the action for.
     *               要执行动作的玩家。
     * @param actionString The action string to parse and execute.
     *                     要解析和执行的动作字符串。
     * @return A Job that can be used to cancel the execution, or null if parsing failed.
     *         可用于取消执行的Job，如果解析失败则返回null。
     */
    fun executeAction(player: Player, actionString: String): Job? {
        val action = ActionParser.parseAction(actionString)
        if (action == null) {
            Logger.warn("Failed to parse action: $actionString")
            return null
        }
        
        val actionGroup = ActionGroup(listOf(action))
        return actionGroup.executeAsync(player) { result ->
            if (!result.isFullySuccessful) {
                Logger.warn("Action execution had failures: ${result.getSummary()}")
            }
        }
    }
    
    /**
     * Executes multiple actions for a player.
     * 为玩家执行多个动作。
     *
     * @param player The player to execute the actions for.
     *               要执行动作的玩家。
     * @param actionStrings The list of action strings to parse and execute.
     *                      要解析和执行的动作字符串列表。
     * @return A Job that can be used to cancel the execution.
     *         可用于取消执行的Job。
     */
    fun executeActions(player: Player, actionStrings: List<String>): Job {
        val actionGroup = ActionParser.parseActionGroup(actionStrings)
        return actionGroup.executeAsync(player) { result ->
            Logger.debug("Action group execution completed: ${result.getSummary()}")
            if (!result.isFullySuccessful) {
                Logger.warn("Some actions failed during execution for player ${player.name}")
                result.errors.forEach { error ->
                    Logger.debug("Error: $error")
                }
            }
        }
    }
    
    /**
     * Executes actions from configuration input for a player.
     * 为玩家执行来自配置输入的动作。
     *
     * @param player The player to execute the actions for.
     *               要执行动作的玩家。
     * @param configInput The configuration input (String or List<String>).
     *                    配置输入（String或List<String>）。
     * @return A Job that can be used to cancel the execution.
     *         可用于取消执行的Job。
     */
    fun executeActionsFromConfig(player: Player, configInput: Any?): Job {
        val actionGroup = ActionParser.parseActions(configInput)
        return actionGroup.executeAsync(player) { result ->
            Logger.debug("Config action execution completed: ${result.getSummary()}")
        }
    }
    
    /**
     * Executes a conditional action (If-Then-Else) for a player.
     * 为玩家执行条件动作（If-Then-Else）。
     *
     * @param player The player to execute the conditional action for.
     *               要执行条件动作的玩家。
     * @param conditionExpression The condition expression to evaluate.
     *                            要评估的条件表达式。
     * @param thenActions The actions to execute if condition is true.
     *                    条件为真时执行的动作。
     * @param elseActions The actions to execute if condition is false (optional).
     *                    条件为假时执行的动作（可选）。
     * @return A Job that can be used to cancel the execution, or null if parsing failed.
     *         可用于取消执行的Job，如果解析失败则返回null。
     */
    fun executeConditionalAction(
        player: Player, 
        conditionExpression: String, 
        thenActions: List<String>, 
        elseActions: List<String>? = null
    ): Job? {
        val condition = ConditionParser.parse(conditionExpression)
        if (condition == null) {
            Logger.warn("Failed to parse condition: $conditionExpression")
            return null
        }
        
        val thenActionGroup = ActionParser.parseActionGroup(thenActions)
        val elseActionGroup = elseActions?.let { ActionParser.parseActionGroup(it) }
        
        val conditionalAction = if (elseActionGroup != null) {
            ConditionalAction.ifThenElse(condition, thenActionGroup, elseActionGroup)
        } else {
            ConditionalAction.ifThen(condition, thenActionGroup)
        }
        
        val actionGroup = ActionGroup(listOf(conditionalAction))
        return actionGroup.executeAsync(player) { result ->
            Logger.debug("Conditional action execution completed: ${result.getSummary()}")
        }
    }
    
    /**
     * Creates a conditional action from components.
     * 从组件创建条件动作。
     *
     * @param condition The condition to evaluate.
     *                  要评估的条件。
     * @param thenActions The actions to execute if condition is true.
     *                    条件为真时执行的动作。
     * @param elseActions The actions to execute if condition is false (optional).
     *                    条件为假时执行的动作（可选）。
     * @return The created ConditionalAction.
     *         创建的ConditionalAction。
     */
    fun createConditionalAction(
        condition: Condition, 
        thenActions: ActionGroup, 
        elseActions: ActionGroup? = null
    ): ConditionalAction {
        return if (elseActions != null) {
            ConditionalAction.ifThenElse(condition, thenActions, elseActions)
        } else {
            ConditionalAction.ifThen(condition, thenActions)
        }
    }
    
    /**
     * Creates an ActionGroup from action strings.
     * 从动作字符串创建ActionGroup。
     *
     * @param actionStrings The list of action strings.
     *                      动作字符串列表。
     * @return The created ActionGroup.
     *         创建的ActionGroup。
     */
    fun createActionGroup(actionStrings: List<String>): ActionGroup {
        return ActionParser.parseActionGroup(actionStrings)
    }
    
    /**
     * Creates an ActionGroup from configuration input.
     * 从配置输入创建ActionGroup。
     *
     * @param configInput The configuration input (String or List<String>).
     *                    配置输入（String或List<String>）。
     * @return The created ActionGroup.
     *         创建的ActionGroup。
     */
    fun createActionGroupFromConfig(configInput: Any?): ActionGroup {
        return ActionParser.parseActions(configInput)
    }
    
    /**
     * Gets all supported action types.
     * 获取所有支持的动作类型。
     *
     * @return A list of supported action types.
     *         支持的动作类型列表。
     */
    fun getSupportedActionTypes(): List<String> {
        return ActionParser.getSupportedActionTypes()
    }
    
    /**
     * Gets help information for action formats.
     * 获取动作格式的帮助信息。
     *
     * @return A map of action type to format description.
     *         动作类型到格式描述的映射。
     */
    fun getActionHelp(): Map<String, String> {
        return ActionParser.getActionHelp()
    }
    
    /**
     * Gets help information for conditional action format.
     * 获取条件动作格式的帮助信息。
     *
     * @return Help text for conditional actions.
     *         条件动作的帮助文本。
     */
    fun getConditionalActionHelp(): String {
        return ConditionalActionParser.getHelpText()
    }
    
    /**
     * Validates an action string without executing it.
     * 验证动作字符串而不执行它。
     *
     * @param actionString The action strings to validate.
     *                     要验证的动作字符串。
     * @return True if the action string is valid, false otherwise.
     *         如果动作字符串有效则返回true，否则返回false。
     */
    fun validateAction(actionString: String): Boolean {
        return ActionParser.parseAction(actionString) != null
    }
    
    /**
     * Validates a conditional action expression without executing it.
     * 验证条件动作表达式而不执行它。
     *
     * @param conditionalExpression The conditional action expression to validate.
     *                              要验证的条件动作表达式。
     * @return True if the expression is valid, false otherwise.
     *         如果表达式有效则返回true，否则返回false。
     */
    fun validateConditionalAction(conditionalExpression: String): Boolean {
        return ConditionalActionParser.parseConditionalAction(conditionalExpression) != null
    }
    
    /**
     * Validates a list of action strings without executing them.
     * 验证动作字符串列表而不执行它们。
     *
     * @param actionStrings The list of action strings to validate.
     *                      要验证的动作字符串列表。
     * @return A map of action string to the validation result.
     *         动作字符串到验证结果的映射。
     */
    fun validateActions(actionStrings: List<String>): Map<String, Boolean> {
        return actionStrings.associateWith { validateAction(it) }
    }
} 