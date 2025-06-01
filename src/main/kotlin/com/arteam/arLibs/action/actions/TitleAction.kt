/**
 * Action for sending titles to players.
 * This action sends a title with subtitle and timing configuration.
 *
 * 用于向玩家发送标题的动作。
 * 此动作发送带有副标题和时间配置的标题。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.action.Action
import com.arteam.arLibs.utils.ColorUtil
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Action that sends a title to the player.
 * 向玩家发送标题的动作。
 */
class TitleAction(
    private val title: String,
    private val subtitle: String = "",
    private val fadeIn: Int = 10,
    private val stay: Int = 70,
    private val fadeOut: Int = 20
) : Action {
    
    override suspend fun execute(player: Player) {
        val formattedTitle = ColorUtil.process(title.replace("%player%", player.name))
        val formattedSubtitle = ColorUtil.process(subtitle.replace("%player%", player.name))
        
        // Use modern Adventure API with proper legacy color handling
        val titleComponent = LegacyComponentSerializer.legacySection().deserialize(formattedTitle)
        val subtitleComponent = LegacyComponentSerializer.legacySection().deserialize(formattedSubtitle)
        
        val times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50L),  // Convert ticks to milliseconds
            Duration.ofMillis(stay * 50L),
            Duration.ofMillis(fadeOut * 50L)
        )
        
        val titleObj = Title.title(titleComponent, subtitleComponent, times)
        player.showTitle(titleObj)
    }
    
    override fun getType(): String = "title"
    
    override fun toString(): String = "TitleAction(title='$title', subtitle='$subtitle', fadeIn=$fadeIn, stay=$stay, fadeOut=$fadeOut)"
    
    companion object {
        /**
         * Parses a title string with format: `title` `subtitle` fadeIn stay fadeOut
         * 解析格式为：`title` `subtitle` fadeIn stay fadeOut 的标题字符串。
         *
         * @param titleString The title string to parse.
         *                    要解析的标题字符串。
         * @return The parsed TitleAction.
         *         解析后的TitleAction。
         */
        fun parse(titleString: String): TitleAction {
            val parts = mutableListOf<String>()
            @Suppress("CanBeVal")
            var currentPart = StringBuilder()  // This will be modified, so var is correct
            var inBackticks = false
            var i = 0
            
            while (i < titleString.length) {
                val char = titleString[i]
                
                when {
                    char == '`' -> {
                        if (inBackticks) {
                            // End of the backtick section
                            parts.add(currentPart.toString())
                            currentPart.clear()
                            inBackticks = false
                        } else {
                            // Start of the backtick section
                            if (currentPart.isNotEmpty()) {
                                parts.add(currentPart.toString().trim())
                                currentPart.clear()
                            }
                            inBackticks = true
                        }
                    }
                    inBackticks -> {
                        currentPart.append(char)
                    }
                    char == ' ' -> {
                        if (currentPart.isNotEmpty()) {
                            parts.add(currentPart.toString())
                            currentPart.clear()
                        }
                    }
                    else -> {
                        currentPart.append(char)
                    }
                }
                i++
            }
            
            if (currentPart.isNotEmpty()) {
                parts.add(currentPart.toString())
            }
            
            val title = parts.getOrNull(0) ?: ""
            val subtitle = parts.getOrNull(1) ?: ""
            val fadeIn = parts.getOrNull(2)?.toIntOrNull() ?: 10
            val stay = parts.getOrNull(3)?.toIntOrNull() ?: 70
            val fadeOut = parts.getOrNull(4)?.toIntOrNull() ?: 20
            
            return TitleAction(title, subtitle, fadeIn, stay, fadeOut)
        }
    }
} 