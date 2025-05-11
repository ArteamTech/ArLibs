package dev.arteam.arlibs.utils

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.sin

/**
 * Utility class for handling Minecraft text colors
 * Supports:
 * - Standard color codes (&a, §b)
 * - Hex colors (&#FFFFFF, &{#FFFFFF})
 * - Gradient colors (<g:#FFFFFF:#000000>)
 * - Rainbow colors (<r:0.4>)
 */
object ColorUtil {
    
    // Regex patterns for different color formats
    private val HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})")
    private val HEX_PATTERN_ALTERNATIVE = Pattern.compile("&\\{#([A-Fa-f0-9]{6})}")
    private val GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f\\d]{6}|[A-Fa-f\\d]{3})){2,})(:(?<loop>l|L|loop))?>")
    private val RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>")
    
    /**
     * Colorize a string with all supported color formats
     * @param text The text to colorize
     * @return The colorized text
     */
    fun colorize(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text
        
        // Process standard color codes
        result = ChatColor.translateAlternateColorCodes('&', result)
        
        // Process hex colors
        result = processHexColors(result)
        
        // Process gradient colors
        result = processGradientColors(result)
        
        // Process rainbow colors
        result = processRainbowColors(result)
        
        return result
    }
    
    /**
     * Process hex color codes in the format &#RRGGBB or &{#RRGGBB}
     */
    private fun processHexColors(text: String): String {
        var result = text
        
        // Process &#RRGGBB format
        var matcher = HEX_PATTERN.matcher(result)
        while (matcher.find()) {
            val color = ChatColor.of("#${matcher.group(1)}")
            result = result.replace(matcher.group(0), color.toString())
            matcher = HEX_PATTERN.matcher(result)
        }
        
        // Process &{#RRGGBB} format
        matcher = HEX_PATTERN_ALTERNATIVE.matcher(result)
        while (matcher.find()) {
            val color = ChatColor.of("#${matcher.group(1)}")
            result = result.replace(matcher.group(0), color.toString())
            matcher = HEX_PATTERN_ALTERNATIVE.matcher(result)
        }
        
        return result
    }
    
    /**
     * Process gradient colors in the format <g:#RRGGBB:#RRGGBB>
     */
    private fun processGradientColors(text: String): String {
        var result = text
        val matcher = GRADIENT_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val fullMatch = matcher.group(0)
            val hexColors = matcher.group("hex")
                .substring(1) // Remove the first colon
                .split(":")
                .filter { it.startsWith("#") }
                .map { Color.decode(it) }
            
            val speed = matcher.group("speed")?.toIntOrNull() ?: 1
            val isLoop = matcher.group("loop") != null
            
            val content = result.substring(
                result.indexOf(fullMatch) + fullMatch.length,
                findClosingTag(result, result.indexOf(fullMatch))
            )
            
            val gradientText = applyGradient(content, hexColors, speed, isLoop)
            result = result.replace("$fullMatch$content</g>", gradientText)
                .replace("$fullMatch$content</gradient>", gradientText)
        }
        
        return result
    }
    
    /**
     * Process rainbow colors in the format <r:saturation:brightness>
     */
    private fun processRainbowColors(text: String): String {
        var result = text
        val matcher = RAINBOW_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val fullMatch = matcher.group(0)
            val saturation = matcher.group("saturation")?.toFloatOrNull() ?: 1.0f
            val brightness = matcher.group("brightness")?.toFloatOrNull() ?: 1.0f
            val speed = matcher.group("speed")?.toIntOrNull() ?: 1
            val isLoop = matcher.group("loop") != null
            
            val content = result.substring(
                result.indexOf(fullMatch) + fullMatch.length,
                findClosingTag(result, result.indexOf(fullMatch))
            )
            
            val rainbowText = applyRainbow(content, saturation, brightness, speed, isLoop)
            result = result.replace("$fullMatch$content</r>", rainbowText)
                .replace("$fullMatch$content</rainbow>", rainbowText)
        }
        
        return result
    }
    
    /**
     * Apply gradient colors to text
     */
    private fun applyGradient(text: String, colors: List<Color>, speed: Int, loop: Boolean): String {
        if (text.isEmpty() || colors.size < 2) return text
        
        val chars = text.toCharArray()
        val result = StringBuilder()
        
        val step = if (loop) {
            1.0 / chars.size.toDouble()
        } else {
            1.0 / (chars.size - 1).toDouble()
        }
        
        for (i in chars.indices) {
            val percent = i * step * speed % 1.0
            val color = getGradientColor(percent, colors)
            result.append(ChatColor.of(color))
            result.append(chars[i])
        }
        
        return result.toString()
    }
    
    /**
     * Apply rainbow effect to text
     */
    private fun applyRainbow(text: String, saturation: Float, brightness: Float, speed: Int, loop: Boolean): String {
        if (text.isEmpty()) return text
        
        val chars = text.toCharArray()
        val result = StringBuilder()
        
        val step = if (loop) {
            1.0 / chars.size.toDouble()
        } else {
            1.0 / (chars.size - 1).toDouble()
        }
        
        for (i in chars.indices) {
            val percent = (i * step * speed) % 1.0
            val hue = percent.toFloat()
            val color = Color.getHSBColor(hue, saturation, brightness)
            result.append(ChatColor.of(color))
            result.append(chars[i])
        }
        
        return result.toString()
    }
    
    /**
     * Get a color at a specific point in a gradient
     */
    private fun getGradientColor(percent: Double, colors: List<Color>): Color {
        if (colors.size == 1) return colors[0]
        
        val segmentSize = 1.0 / (colors.size - 1)
        val segment = (percent / segmentSize).toInt()
        val segmentPercent = (percent - segment * segmentSize) / segmentSize
        
        val color1 = colors[segment.coerceIn(0, colors.size - 1)]
        val color2 = colors[(segment + 1).coerceIn(0, colors.size - 1)]
        
        return interpolateColor(color1, color2, segmentPercent)
    }
    
    /**
     * Interpolate between two colors
     */
    private fun interpolateColor(color1: Color, color2: Color, percent: Double): Color {
        val r = interpolateValue(color1.red, color2.red, percent)
        val g = interpolateValue(color1.green, color2.green, percent)
        val b = interpolateValue(color1.blue, color2.blue, percent)
        return Color(r, g, b)
    }
    
    /**
     * Interpolate between two values
     */
    private fun interpolateValue(value1: Int, value2: Int, percent: Double): Int {
        return (value1 + (value2 - value1) * percent).toInt().coerceIn(0, 255)
    }
    
    /**
     * Find the closing tag for a color tag
     */
    private fun findClosingTag(text: String, openingTagIndex: Int): Int {
        val openingTag = text.substring(openingTagIndex, text.indexOf('>', openingTagIndex) + 1)
        val tagType = when {
            openingTag.startsWith("<g") || openingTag.startsWith("<gradient") -> "g"
            openingTag.startsWith("<r") || openingTag.startsWith("<rainbow") -> "r"
            else -> return text.length
        }
        
        val closingTag = "</$tagType>"
        val alternativeClosingTag = when (tagType) {
            "g" -> "</gradient>"
            "r" -> "</rainbow>"
            else -> ""
        }
        
        val closingIndex = text.indexOf(closingTag, openingTagIndex + openingTag.length)
        val alternativeClosingIndex = text.indexOf(alternativeClosingTag, openingTagIndex + openingTag.length)
        
        return when {
            closingIndex == -1 && alternativeClosingIndex == -1 -> text.length
            closingIndex == -1 -> alternativeClosingIndex
            alternativeClosingIndex == -1 -> closingIndex
            else -> minOf(closingIndex, alternativeClosingIndex)
        }
    }
} 