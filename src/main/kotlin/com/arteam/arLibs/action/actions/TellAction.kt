/**
 * Action for sending text messages to players.
 * This action sends a formatted message to the player.
 *
 * 用于向玩家发送文本消息的动作。
 * 此动作向玩家发送格式化的消息。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.action.Action
import com.arteam.arLibs.utils.ColorUtil
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

/**
 * Action that sends a text message to the player.
 * 向玩家发送文本消息的动作。
 */
class TellAction(private val message: String) : Action {
    
    override suspend fun execute(player: Player) {
        val formattedMessage = ColorUtil.process(message.replace("%player%", player.name))
        
        // Use Adventure API with proper legacy color handling
        val component = LegacyComponentSerializer.legacySection().deserialize(formattedMessage)
        player.sendMessage(component)
    }
    
    override fun getType(): String = "tell"
    
    override fun toString(): String = "TellAction(message='$message')"
} 