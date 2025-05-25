/**
 * Utility class for handling and processing custom color formats in text.
 * This includes regular Minecraft color codes, hex colors, gradients, and rainbow effects.
 *
 * 用于处理文本中自定义颜色格式的工具类。
 * 包括常规Minecraft颜色代码、十六进制颜色、渐变色和彩虹效果。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.utils

import java.awt.Color
import java.util.regex.Pattern

@Suppress("unused")
object ColorUtil {
    // Legacy format character codes
    private const val COLOR_CHAR = '§'
    private const val ALT_COLOR_CHAR = '&'
    
    // Regular expressions for different color formats
    private val HEX_PATTERN = Pattern.compile("<#([A-Fa-f\\d]{6})>")
    private val GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f\\d]{6}|[A-Fa-f\\d]{3})){2,})(:(?<loop>l|L|loop))?>")
    private val RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>")

    /**
     * Translates color codes starting with `&` to the Minecraft color code character (§).
     * 将以 & 开头的颜色代码转换为 Minecraft 颜色代码字符 (§)。
     *
     * @param text The text to translate color codes in.
     *             需要转换颜色代码的文本。
     * @return The text with translated color codes.
     *         转换后的文本。
     */
    fun translateColorCodes(text: String): String {
        return text.replace(ALT_COLOR_CHAR, COLOR_CHAR)
    }

    /**
     * Processes all supported color formats in the text.
     * This includes regular color codes, hex colors, gradients, and rainbow effects.
     *
     * 处理文本中所有支持的颜色格式。
     * 包括常规颜色代码、十六进制颜色、渐变色和彩虹效果。
     *
     * @param text The text to process.
     *             需要处理的文本。
     * @return The text with all color formats processed.
     *         处理后的文本。
     */
    fun process(text: String): String {
        var result = translateColorCodes(text)
        result = processHexColors(result)
        result = processGradients(result)
        result = processRainbows(result)
        return result
    }

    /**
     * Processes hex color codes in the format <#XXXXXX>.
     * 处理格式为 <#XXXXXX> 的十六进制颜色代码。
     *
     * @param text The text to process.
     *             需要处理的文本。
     * @return The text with hex colors processed.
     *         处理后的文本。
     */
    private fun processHexColors(text: String): String {
        var result = text
        val matcher = HEX_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val hexCode = matcher.group(1)
            val replacement = COLOR_CHAR.toString() + "x" + 
                COLOR_CHAR + hexCode[0] + 
                COLOR_CHAR + hexCode[1] + 
                COLOR_CHAR + hexCode[2] + 
                COLOR_CHAR + hexCode[3] + 
                COLOR_CHAR + hexCode[4] + 
                COLOR_CHAR + hexCode[5]
            
            result = result.replace(matcher.group(), replacement)
        }
        
        return result
    }

    /**
     * Processes gradient color codes.
     * 处理渐变颜色代码。
     *
     * @param text The text to process.
     *             需要处理的文本。
     * @return The text with gradient colors processed.
     *         处理后的文本。
     */
    private fun processGradients(text: String): String {
        var result = text
        val matcher = GRADIENT_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val fullTag = matcher.group()
            val hexList = extractHexColors(matcher.group("hex") ?: "")
            
            if (hexList.size < 2) continue
            
            val speed = matcher.group("speed")?.toIntOrNull() ?: 1
            val loop = matcher.group("loop") != null
            
            val content = findClosingTagContent(result, fullTag)
            if (content.isEmpty()) continue
            
            val processed = applyGradient(content, hexList, speed, loop)
            result = result.replace("$fullTag$content</${matcher.group("type")}>", processed)
        }
        
        return result
    }

    /**
     * Processes rainbow color codes.
     * 处理彩虹颜色代码。
     *
     * @param text The text to process.
     *             需要处理的文本。
     * @return The text with rainbow colors processed.
     *         处理后的文本。
     */
    private fun processRainbows(text: String): String {
        var result = text
        val matcher = RAINBOW_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val fullTag = matcher.group()
            val speed = matcher.group("speed")?.toIntOrNull() ?: 1
            val saturation = matcher.group("saturation")?.toFloatOrNull() ?: 1.0f
            val brightness = matcher.group("brightness")?.toFloatOrNull() ?: 1.0f
            val loop = matcher.group("loop") != null
            
            val content = findClosingTagContent(result, fullTag)
            if (content.isEmpty()) continue
            
            val processed = applyRainbow(content, speed, saturation, brightness, loop)
            result = result.replace("$fullTag$content</${matcher.group("type")}>", processed)
        }
        
        return result
    }

    /**
     * Extracts hex color codes from a string.
     * 从字符串中提取十六进制颜色代码。
     *
     * @param hexString The string containing hex color codes.
     *                  包含十六进制颜色代码的字符串。
     * @return A list of Color objects.
     *         Color对象列表。
     */
    private fun extractHexColors(hexString: String): List<Color> {
        val colors = mutableListOf<Color>()
        val hexCodes = hexString.split(":")
            .filter { it.startsWith("#") }
            .map { if (it.length == 4) expandShortHex(it) else it }
        
        for (hex in hexCodes) {
            try {
                val rgb = Integer.parseInt(hex.substring(1), 16)
                colors.add(Color(rgb))
            } catch (e: NumberFormatException) {
                // Skip invalid color
            }
        }
        
        return colors
    }

    /**
     * Expands a short hex code (#RGB) to a full hex code (#RRGGBB).
     * 将短十六进制代码 (#RGB) 扩展为完整的十六进制代码 (#RRGGBB)。
     *
     * @param shortHex The short hex code.
     *                 短十六进制代码。
     * @return The expanded hex code.
     *         扩展后的十六进制代码。
     */
    private fun expandShortHex(shortHex: String): String {
        if (shortHex.length != 4 || !shortHex.startsWith("#")) return shortHex
        
        return "#" + shortHex[1].toString().repeat(2) + 
                    shortHex[2].toString().repeat(2) + 
                    shortHex[3].toString().repeat(2)
    }

    /**
     * Finds the content between an opening tag and its corresponding closing tag.
     * 查找开始标签和对应的结束标签之间的内容。
     *
     * @param text The text to search in.
     *             要搜索的文本。
     * @param openingTag The opening tag.
     *                   开始标签。
     * @return The content between the tags.
     *         标签之间的内容。
     */
    private fun findClosingTagContent(text: String, openingTag: String): String {
        val tagType = if (openingTag.contains("gradient") || openingTag.contains("g")) {
            "gradient"
        } else if (openingTag.contains("rainbow") || openingTag.contains("r")) {
            "rainbow"
        } else {
            return ""
        }
        
        val start = text.indexOf(openingTag) + openingTag.length
        val closingTag = "</$tagType>"
        val end = text.indexOf(closingTag, start)
        
        return if (end > start) text.substring(start, end) else ""
    }

    /**
     * Applies a gradient effect to the text.
     * 对文本应用渐变效果。
     *
     * @param text The text to apply the gradient to.
     *             要应用渐变的文本。
     * @param colors The list of colors to use for the gradient.
     *               用于渐变的颜色列表。
     * @param speed The speed of the gradient, affecting how fast colors change.
     *              渐变的速度，影响颜色变化的快慢。
     * @param loop Whether the gradient should loop.
     *             渐变是否应该循环。
     * @return The text with the gradient applied.
     *         应用渐变后的文本。
     */
    private fun applyGradient(text: String, colors: List<Color>, speed: Int, loop: Boolean): String {
        if (text.isEmpty() || colors.size < 2) return text
        
        val plainText = stripColorCodes(text)
        if (plainText.isEmpty()) return text
        
        val result = StringBuilder()
        val textLength = plainText.length
        
        for (i in plainText.indices) {
            val percent = if (loop) {
                (i * speed % textLength) / textLength.toFloat()
            } else {
                i / textLength.toFloat()
            }
            
            val color = getGradientColor(colors, percent)
            val hexCode = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
            
            result.append("<#$hexCode>${plainText[i]}")
        }
        
        return process(result.toString())
    }

    /**
     * Applies a rainbow effect to the text.
     * 对文本应用彩虹效果。
     *
     * @param text The text to apply the rainbow to.
     *             要应用彩虹效果的文本。
     * @param speed The speed of the rainbow, affecting how fast colors change.
     *              彩虹效果的速度，影响颜色变化的快慢。
     * @param saturation The saturation of the rainbow colors.
     *                   彩虹颜色的饱和度。
     * @param brightness The brightness of the rainbow colors.
     *                   彩虹颜色的亮度。
     * @param loop Whether the rainbow should loop.
     *             彩虹效果是否应该循环。
     * @return The text with the rainbow applied.
     *         应用彩虹效果后的文本。
     */
    private fun applyRainbow(text: String, speed: Int, saturation: Float, brightness: Float, loop: Boolean): String {
        if (text.isEmpty()) return text
        
        val plainText = stripColorCodes(text)
        if (plainText.isEmpty()) return text
        
        val result = StringBuilder()
        val textLength = plainText.length
        
        for (i in plainText.indices) {
            val hue = if (loop) {
                (i * speed % 360) / 360.0f
            } else {
                (i * 360 / textLength) / 360.0f
            }
            
            val color = Color.getHSBColor(hue, saturation, brightness)
            val hexCode = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
            
            result.append("<#$hexCode>${plainText[i]}")
        }
        
        return process(result.toString())
    }

    /**
     * Gets a color at a specific point in a gradient.
     * 获取渐变中特定点的颜色。
     *
     * @param colors The list of colors in the gradient.
     *               渐变中的颜色列表。
     * @param percent The position in the gradient (0.0 to 1.0).
     *                渐变中的位置（0.0 到 1.0）。
     * @return The color at the specified position.
     *         指定位置的颜色。
     */
    private fun getGradientColor(colors: List<Color>, percent: Float): Color {
        if (colors.size == 1) return colors[0]
        
        val colorCount = colors.size - 1
        val sectionPercent = 1.0f / colorCount
        var sectionIndex = (percent / sectionPercent).toInt()
        
        // Clamp to valid range
        sectionIndex = sectionIndex.coerceAtMost(colorCount - 1)
        
        val startColor = colors[sectionIndex]
        val endColor = colors[sectionIndex + 1]
        
        // Calculate the percentage within this section
        val sectionPosition = (percent - sectionIndex * sectionPercent) / sectionPercent
        
        // Interpolate between the two colors
        return interpolateColor(startColor, endColor, sectionPosition)
    }

    /**
     * Interpolates between two colors.
     * 在两种颜色之间进行插值。
     *
     * @param color1 The starting color.
     *               起始颜色。
     * @param color2 The ending color.
     *               结束颜色。
     * @param ratio The interpolation ratio (0.0 to 1.0).
     *              插值比率（0.0 到 1.0）。
     * @return The interpolated color.
     *         插值后的颜色。
     */
    private fun interpolateColor(color1: Color, color2: Color, ratio: Float): Color {
        val r = (color2.red - color1.red) * ratio + color1.red
        val g = (color2.green - color1.green) * ratio + color1.green
        val b = (color2.blue - color1.blue) * ratio + color1.blue
        
        return Color(r.toInt(), g.toInt(), b.toInt())
    }

    /**
     * Removes all color codes from a string.
     * 从字符串中删除所有颜色代码。
     *
     * @param text The text to strip color codes from.
     *             要删除颜色代码的文本。
     * @return The text without color codes.
     *         没有颜色代码的文本。
     */
    fun stripColorCodes(text: String): String {
        var result = text
        
        // Remove regular color codes
        result = result.replace("$COLOR_CHAR[0-9a-fk-orx]".toRegex(), "")
        
        // Remove hex color codes in the format §x§r§r§g§g§b§b
        result = result.replace("$COLOR_CHAR(x)($COLOR_CHAR[0-9A-Fa-f]){6}".toRegex(), "")
        
        // Remove custom hex color codes in the format <#XXXXXX>
        result = result.replace("<#[A-Fa-f\\d]{6}>".toRegex(), "")
        
        return result
    }
} 