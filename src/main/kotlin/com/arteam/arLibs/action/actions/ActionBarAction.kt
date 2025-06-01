/**
 * Action for sending action bar messages to players.
 * This action sends a message to the player's action bar.
 *
 * 用于向玩家发送物品栏上方消息的动作。
 * 此动作向玩家的物品栏上方发送消息。
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
 * Action that sends an action bar message to the player.
 * 向玩家发送物品栏上方消息的动作。
 */
class ActionBarAction(private val message: String) : Action {
    
    override suspend fun execute(player: Player) {
        val formattedMessage = ColorUtil.process(message.replace("%player%", player.name))
        
        // Use Adventure API with proper legacy color handling
        val component = LegacyComponentSerializer.legacySection().deserialize(formattedMessage)
        player.sendActionBar(component)
    }
    
    override fun getType(): String = "actionbar"
    
    override fun toString(): String = "ActionBarAction(message='$message')"
} 