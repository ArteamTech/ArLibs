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
    
    // Combined pattern for efficient color code stripping
    private val STRIP_PATTERN = Pattern.compile("($COLOR_CHAR[0-9a-fk-orx])|(${COLOR_CHAR}x($COLOR_CHAR[0-9A-Fa-f]){6})|(<#[A-Fa-f\\d]{6}>)")

    /**
     * Translates color codes starting with `&` to the Minecraft color code character (§).
     * 将以 & 开头的颜色代码转换为 Minecraft 颜色代码字符 (§)。
     */
    fun translateColorCodes(text: String): String = text.replace(ALT_COLOR_CHAR, COLOR_CHAR)

    /**
     * Processes all supported color formats in the text.
     * This includes regular color codes, hex colors, gradients, and rainbow effects.
     *
     * 处理文本中所有支持的颜色格式。
     * 包括常规颜色代码、十六进制颜色、渐变色和彩虹效果。
     */
    fun process(text: String): String {
        return processHexColors(processRainbows(processGradients(translateColorCodes(text))))
    }

    /**
     * Processes hex color codes in the format <#XXXXXX>.
     * 处理格式为 <#XXXXXX> 的十六进制颜色代码。
     */
    private fun processHexColors(text: String): String {
        var result = text
        val matcher = HEX_PATTERN.matcher(result)
        
        while (matcher.find()) {
            val replacement = buildString {
                append(COLOR_CHAR).append("x")
                matcher.group(1).forEach { append(COLOR_CHAR).append(it) }
            }
            result = result.replace(matcher.group(), replacement)
        }
        return result
    }

    // Processes gradient and rainbow color codes using a unified approach.
    // 使用统一方法处理渐变和彩虹颜色代码。
    private fun processGradients(text: String): String = processColorEffect(text, GRADIENT_PATTERN) { matcher, content ->
        val hexList = extractHexColors(matcher.group("hex") ?: "")
        if (hexList.size >= 2) {
            applyGradient(content, hexList, 
                matcher.group("speed")?.toIntOrNull() ?: 1,
                matcher.group("loop") != null)
        } else content
    }

    private fun processRainbows(text: String): String = processColorEffect(text, RAINBOW_PATTERN) { matcher, content ->
        applyRainbow(content,
            matcher.group("speed")?.toIntOrNull() ?: 1,
            matcher.group("saturation")?.toFloatOrNull() ?: 1.0f,
            matcher.group("brightness")?.toFloatOrNull() ?: 1.0f,
            matcher.group("loop") != null)
    }

    /**
     * Unified processor for color effects (gradients and rainbows).
     * 颜色效果（渐变和彩虹）的统一处理器。
     */
    private fun processColorEffect(text: String, pattern: Pattern, processor: (java.util.regex.Matcher, String) -> String): String {
        var result = text
        val matcher = pattern.matcher(result)
        
        while (matcher.find()) {
            val fullTag = matcher.group()
            val tagType = matcher.group("type")
            val content = findClosingTagContent(result, fullTag, tagType)
            
            if (content.isNotEmpty()) {
                val processed = processor(matcher, content)
                val closingTag = resolveClosingTag(tagType)
                result = result.replace("$fullTag$content</$closingTag>", processed)
            }
        }
        return result
    }

    /**
     * Resolves the closing tag name from the opening tag type.
     * 从开始标签类型解析结束标签名称。
     */
    private fun resolveClosingTag(tagType: String): String = when {
        tagType.startsWith("g") -> "gradient"
        tagType.startsWith("r") -> "rainbow"
        else -> tagType
    }

    /**
     * Extracts hex color codes from a string.
     * 从字符串中提取十六进制颜色代码。
     */
    private fun extractHexColors(hexString: String): List<Color> {
        return hexString.split(":")
            .filter { it.startsWith("#") }
            .map { if (it.length == 4) expandShortHex(it) else it }
            .mapNotNull { hex ->
                try { Color(Integer.parseInt(hex.substring(1), 16)) }
                catch (e: NumberFormatException) { null }
            }
    }

    /**
     * Expands a short hex code (#RGB) to a full hex code (#RRGGBB).
     * 将短十六进制代码 (#RGB) 扩展为完整的十六进制代码 (#RRGGBB)。
     */
    private fun expandShortHex(shortHex: String): String {
        return if (shortHex.length == 4 && shortHex.startsWith("#")) {
            buildString {
                append("#")
                (1..3).forEach { i -> shortHex[i].let { append(it).append(it) } }
            }
        } else shortHex
    }

    /**
     * Finds the content between an opening tag and its corresponding closing tag.
     * 查找开始标签和对应的结束标签之间的内容。
     */
    private fun findClosingTagContent(text: String, openingTag: String, tagType: String): String {
        val start = text.indexOf(openingTag) + openingTag.length
        val end = text.indexOf("</${resolveClosingTag(tagType)}>", start)
        return if (end > start) text.substring(start, end) else ""
    }

    /**
     * Applies a gradient effect to the text.
     * 对文本应用渐变效果。
     */
    private fun applyGradient(text: String, colors: List<Color>, speed: Int, loop: Boolean): String {
        val plainText = stripColorCodes(text)
        if (plainText.isEmpty() || colors.size < 2) return text
        
        return buildString {
            plainText.forEachIndexed { i, char ->
                val percent = if (loop) (i * speed % plainText.length) / plainText.length.toFloat()
                             else i / plainText.length.toFloat()
                val color = getGradientColor(colors, percent)
                append("<#${String.format("%02x%02x%02x", color.red, color.green, color.blue)}>$char")
            }
        }
    }

    /**
     * Applies a rainbow effect to the text.
     * 对文本应用彩虹效果。
     */
    private fun applyRainbow(text: String, speed: Int, saturation: Float, brightness: Float, loop: Boolean): String {
        val plainText = stripColorCodes(text)
        if (plainText.isEmpty()) return text
        
        return buildString {
            plainText.forEachIndexed { i, char ->
                val hue = if (loop) (i * speed % 360) / 360.0f
                         else (i * 360 / plainText.length) / 360.0f
                val color = Color.getHSBColor(hue, saturation, brightness)
                append("<#${String.format("%02x%02x%02x", color.red, color.green, color.blue)}>$char")
            }
        }
    }

    /**
     * Gets a color at a specific point in a gradient.
     * 获取渐变中特定点的颜色。
     */
    private fun getGradientColor(colors: List<Color>, percent: Float): Color {
        if (colors.size == 1) return colors[0]
        
        val colorCount = colors.size - 1
        val sectionIndex = ((percent / (1.0f / colorCount)).toInt()).coerceAtMost(colorCount - 1)
        val sectionPosition = (percent - sectionIndex * (1.0f / colorCount)) / (1.0f / colorCount)
        
        return interpolateColor(colors[sectionIndex], colors[sectionIndex + 1], sectionPosition)
    }

    /**
     * Interpolates between two colors.
     * 在两种颜色之间进行插值。
     */
    private fun interpolateColor(color1: Color, color2: Color, ratio: Float): Color {
        return Color(
            ((color2.red - color1.red) * ratio + color1.red).toInt(),
            ((color2.green - color1.green) * ratio + color1.green).toInt(),
            ((color2.blue - color1.blue) * ratio + color1.blue).toInt()
        )
    }

    /**
     * Removes all color codes from a string using a single optimized pattern.
     * 使用单一优化模式从字符串中删除所有颜色代码。
     */
    fun stripColorCodes(text: String): String = STRIP_PATTERN.matcher(text).replaceAll("")
} 
