/**
 * Action for playing sounds to players.
 * This action plays a sound with specified volume and pitch.
 *
 * 用于向玩家播放音效的动作。
 * 此动作以指定的音量和音调播放音效。
 *
 * @author ArteamTech
 * @since 2025-06-01
 * @version 1.0.0
 */
package com.arteam.arLibs.action.actions

import com.arteam.arLibs.action.Action
import com.arteam.arLibs.utils.Logger
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * Action that plays a sound to the player.
 * 向玩家播放音效的动作。
 */
@Suppress("unused")
class SoundAction(
    private val sound: String,
    private val volume: Float = 1.0f,
    private val pitch: Float = 1.0f
) : Action {
    
    override suspend fun execute(player: Player) {
        try {
            val bukkitSound = findSound(sound)
            if (bukkitSound == null) {
                Logger.warn("Unknown sound: '$sound'. Use /arlibs action sound help for valid sounds")
                return
            }
            
            // Validate volume and pitch ranges
            val clampedVolume = volume.coerceIn(0.0f, 10.0f)
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            
            if (clampedVolume != volume) {
                Logger.debug("Volume clamped from $volume to $clampedVolume for sound '$sound'")
            }
            if (clampedPitch != pitch) {
                Logger.debug("Pitch clamped from $pitch to $clampedPitch for sound '$sound'")
            }
            
            player.playSound(player.location, bukkitSound, clampedVolume, clampedPitch)
            Logger.debug("Played sound '$sound' for player ${player.name} (volume: $clampedVolume, pitch: $clampedPitch)")
            
        } catch (e: Exception) {
            Logger.warn("Failed to play sound '$sound' for player ${player.name}: ${e.message}")
        }
    }
    
    override fun getType(): String = "sound"
    
    override fun toString(): String = "SoundAction(sound='$sound', volume=$volume, pitch=$pitch)"
    
    companion object {
        /**
         * Parses a sound string in the format "SOUND-VOLUME-PITCH".
         * 解析格式为"SOUND-VOLUME-PITCH"的音效字符串。
         *
         * @param soundString The sound string to parse.
         *                    要解析的音效字符串。
         * @return The parsed SoundAction.
         *         解析后的SoundAction。
         * @throws IllegalArgumentException if the sound string format is invalid.
         *                                  如果音效字符串格式无效。
         */
        fun parse(soundString: String): SoundAction {
            val parts = soundString.split("-")
            if (parts.isEmpty()) {
                throw IllegalArgumentException("Empty sound string")
            }
            
            val sound = parts[0].trim()
            if (sound.isEmpty()) {
                throw IllegalArgumentException("Empty sound name")
            }
            
            val volume = parts.getOrNull(1)?.toFloatOrNull() ?: 1.0f
            val pitch = parts.getOrNull(2)?.toFloatOrNull() ?: 1.0f
            
            if (volume < 0) {
                Logger.warn("Negative volume ($volume) will be clamped to 0")
            }
            if (pitch < 0.5f || pitch > 2.0f) {
                Logger.warn("Pitch ($pitch) outside recommended range (0.5-2.0)")
            }
            
            return SoundAction(sound, volume, pitch)
        }
        
        /**
         * Finds a Bukkit Sound by name, supporting various formats.
         * 通过名称查找Bukkit Sound，支持各种格式。
         *
         * @param soundName The sound name to search for.
         *                  要搜索的音效名称。
         * @return The found Sound, or null if not found.
         *         找到的Sound，如果未找到则返回null。
         */
        private fun findSound(soundName: String): Sound? {
            // Try exact match first
            try {
                return Sound.valueOf(soundName.uppercase())
            } catch (_: IllegalArgumentException) {
                // Continue to fuzzy search
            }
            
            // Try fuzzy matching
            return Sound.entries.find { sound ->
                sound.name.equals(soundName, ignoreCase = true) ||
                sound.key.key.equals(soundName, ignoreCase = true) ||
                sound.key.key.replace("minecraft:", "").equals(soundName, ignoreCase = true)
            }
        }
        
        /**
         * Gets a list of all available sound names.
         * 获取所有可用音效名称的列表。
         *
         * @return List of sound names.
         *         音效名称列表。
         */
        fun getAvailableSounds(): List<String> {
            return Sound.entries.map { it.name }.sorted()
        }
        
        /**
         * Gets a list of common sound names for suggestions.
         * 获取常用音效名称列表以供建议。
         *
         * @return List of common sound names.
         *         常用音效名称列表。
         */
        fun getCommonSounds(): List<String> {
            return listOf(
                "ENTITY_EXPERIENCE_ORB_PICKUP",
                "ENTITY_PLAYER_LEVELUP",
                "BLOCK_NOTE_BLOCK_PLING",
                "ENTITY_VILLAGER_YES",
                "ENTITY_VILLAGER_NO",
                "BLOCK_ANVIL_USE",
                "ENTITY_ITEM_PICKUP",
                "UI_BUTTON_CLICK"
            )
        }
    }
} 