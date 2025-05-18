/**
 * Helper class for processing and saving configuration comments.
 * This class handles adding comments to configuration files during save operations.
 *
 * 处理和保存配置注释的辅助类。
 * 该类在保存操作期间处理向配置文件添加注释。
 *
 * @author ArteamTech
 * @since 2025-05-18
 * @version 1.0.0
 */
package com.arteam.arLibs.config

import com.arteam.arLibs.config.annotations.Config
import com.arteam.arLibs.config.annotations.ConfigField
import com.arteam.arLibs.config.annotations.ConfigValue
import com.arteam.arLibs.utils.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * Processes and writes comments to configuration files based on annotations.
 * 根据注解处理并将注释写入配置文件。
 */
class ConfigCommentProcessor {
    companion object {
        /**
         * Saves a configuration with comments.
         * 保存带有注释的配置。
         *
         * @param configClass The class of the configuration
         *                    配置的类
         * @param configFile The configuration file to save to
         *                   要保存到的配置文件
         * @param yamlConfig The YamlConfiguration containing the configuration data
         *                   包含配置数据的 YamlConfiguration
         */
        fun saveWithComments(configClass: KClass<*>, configFile: File, yamlConfig: YamlConfiguration) {
            Logger.debug("[saveWithComments] For KClass: ${configClass.simpleName}, File: ${configFile.path}")
            
            // Store and clear the header from the YamlConfiguration object itself
            // to prevent saveToString() from outputting any header it might have loaded.
            // The actual header will be added by processComments based on @Config annotation.
            val loadedHeaderLines = yamlConfig.options().header // Non-deprecated way to get header
            yamlConfig.options().setHeader(null as List<String>?) // Non-deprecated way to clear header

            // Get the string representation of the YAML config (values only)
            val yamlString = yamlConfig.saveToString()
            Logger.debug("[saveWithComments] yamlString from yamlConfig.saveToString() (should be values only):\n$yamlString")

            // Restore the original header string to the YamlConfiguration object if needed for its state, though not strictly for this save.
            if (loadedHeaderLines.isNotEmpty()) { // Check if it's not null and not empty
                yamlConfig.options().setHeader(loadedHeaderLines)
            }
            // We don't need to restore copyHeader as it's a setting for save operations.

            val content = if (yamlString.isNotEmpty()) {
                yamlString.lines().toMutableList()
            } else {
                mutableListOf() // Ensure this is List<String> for processComments
            }
            
            // Process comments (add header, field, value comments to 'content')
            // processComments will add the header from @Config annotation.
            val commentedLines = processComments(configClass, content)
            Logger.debug("[saveWithComments] Total commented lines to write: ${commentedLines.size}")
            
            // Write the content with comments back to the file, overwriting it.
            BufferedWriter(FileWriter(configFile)).use { writer ->
                commentedLines.forEachIndexed { index, line ->
                    writer.write(line)
                    if (index < commentedLines.size - 1) {
                        writer.newLine()
                    }
                }
            }
        }
        
        /**
         * Processes comments for a configuration class.
         * 处理配置类的注释。
         */
        private fun processComments(configClass: KClass<*>, content: MutableList<String>): List<String> {
            Logger.debug("[processComments] For KClass: ${configClass.simpleName}, Initial content lines: ${content.size}")
            val result = mutableListOf<String>()
            
            // Add file header comments if any
            val configAnnotation = configClass.findAnnotation<Config>()
            if (configAnnotation != null && configAnnotation.comments.isNotEmpty()) {
                for (comment in configAnnotation.comments) {
                    result.add("# $comment")
                    Logger.debug("[processComments] Added file header comment: # $comment")
                }
                result.add("")
            }
            
            // Process field comments
            val pathComments = collectComments(configClass)
            Logger.debug("[processComments] Collected pathComments: $pathComments")
            
            // Add comments to the content
            var currentPath = ""
            val indentStack = mutableListOf<String>()
            
            for (line in content) {
                val trimmedLine = line.trim()
                var colonIndex = -1 // Initialize colonIndex here

                if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                    // Calculate current indentation
                    val indent = line.takeWhile { it.isWhitespace() }
                    
                    // Detect if we're moving to a parent level
                    while (indentStack.isNotEmpty() && indent.length < indentStack.last().length) {
                        indentStack.removeAt(indentStack.size - 1)
                        val lastDot = currentPath.lastIndexOf('.')
                        currentPath = if (lastDot != -1) {
                            currentPath.substring(0, lastDot)
                        } else {
                            ""
                        }
                    }
                    
                    // Find the path for this line
                    colonIndex = trimmedLine.indexOf(':') // Assign to the broader scoped variable
                    if (colonIndex > 0) {
                        val key = trimmedLine.substring(0, colonIndex).trim()
                        
                        // Update the current path
                        if (currentPath.isEmpty()) {
                            currentPath = key
                        } else {
                            // Check if we're at a new nesting level
                            if (indent.length > (indentStack.lastOrNull()?.length ?: -1)) {
                                indentStack.add(indent)
                            } else if (indent.length < indentStack.last().length) {
                                // Moved up one or more levels
                                while(indentStack.isNotEmpty() && indent.length < indentStack.last().length) {
                                    indentStack.removeAt(indentStack.size - 1)
                                    val lastDot = currentPath.lastIndexOf('.')
                                    currentPath = if (lastDot != -1) currentPath.substring(0, lastDot) else ""
                                }
                                val lastDotCurrent = currentPath.lastIndexOf('.')
                                currentPath = if (lastDotCurrent != -1) currentPath.substring(0, lastDotCurrent) + ".$key" else key

                            } else { // Same level
                                // Same level, just update the last key
                                val lastDot = currentPath.lastIndexOf('.')
                                currentPath = if (lastDot != -1) {
                                    currentPath.substring(0, lastDot) + ".$key"
                                } else {
                                    key
                                }
                            }
                        }
                        
                        // Add comments for this path if any
                        val comments = pathComments[currentPath]
                        if (comments != null) {
                            for (comment in comments) {
                                result.add("${indent}# $comment")
                                Logger.debug("[processComments] Added comment for path '$currentPath': ${indent}# $comment")
                            }
                        }
                    }
                }
                result.add(line)
                // If the line processed was a key-value pair (not a list item or empty), and it's not a section header itself (which usually doesn't have a value on the same line)
                if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") && colonIndex > 0 && !trimmedLine.endsWith(":")) {
                    // Add a blank line after a key-value pair for spacing, but not if it's the last line in the content
                    // or if the next line is already blank or a comment (to avoid double-spacing)
                    val nextLineIndex = content.indexOf(line) + 1
                    if (nextLineIndex < content.size) {
                        val nextTrimmedLine = content[nextLineIndex].trim()
                        if (nextTrimmedLine.isNotEmpty() && !nextTrimmedLine.startsWith("#")) {
                            result.add("") 
                        }
                    } else {
                        // If it is the last line, add a blank line.
                        result.add("")
                    }
                }
            }
            
            // Remove trailing blank lines that might have been added excessively
            while (result.isNotEmpty() && result.last().isBlank()) {
                result.removeAt(result.size - 1)
            }
            // Ensure there is at least one blank line at the very end of the file if content was written.
            if (result.isNotEmpty() && (result.last().isNotEmpty() || (result.size > 1 && result[result.size-2].isNotEmpty() ) )) {
                 result.add("")
            }

            return result
        }
        
        /**
         * Collects comments from a configuration class.
         * 从配置类收集注释。
         */
        private fun collectComments(configClass: KClass<*>): Map<String, List<String>> {
            Logger.debug("[ConfigCommentProcessor.collectComments] For KClass: ${configClass.simpleName}")
            val comments = mutableMapOf<String, List<String>>()

            // Iterate over KProperties to correctly find annotations, similar to populateYamlFromInstance
            configClass.memberProperties.forEach { prop ->
                val field = try { prop.javaField } catch (_: Exception) { null } ?: return@forEach
                field.isAccessible = true
                if (Modifier.isStatic(field.modifiers)) return@forEach

                collectFieldComments(prop, field, "", comments, configClass) 
            }
            return comments
        }
        
        /**
         * Collects comments from a KProperty and its backing Field.
         */
        private fun collectFieldComments(prop: kotlin.reflect.KProperty1<out Any, *>, field: Field, parentPath: String, comments: MutableMap<String, List<String>>, ownerClass: KClass<*>) {
            Logger.debug("[ConfigCommentProcessor.collectFieldComments] KProperty: ${prop.name}, Field: ${field.name}, ParentPath: '$parentPath' on ownerClass: ${ownerClass.simpleName}")

            val configFieldAnnotation = prop.findAnnotation<ConfigField>() 
            val configValueAnnotation = prop.findAnnotation<ConfigValue>()

            Logger.debug("[ConfigCommentProcessor.collectFieldComments] KProperty '${prop.name}', Has@ConfigField: ${configFieldAnnotation != null}, Has@ConfigValue: ${configValueAnnotation != null}")

            if (configFieldAnnotation != null) {
                val fieldPath = if (parentPath.isEmpty()) configFieldAnnotation.path else "$parentPath.${configFieldAnnotation.path}"
                if (configFieldAnnotation.comments.isNotEmpty()) {
                    comments[fieldPath] = configFieldAnnotation.comments.toList()
                    Logger.debug("[ConfigCommentProcessor.collectFieldComments] Collected comments for @ConfigField '$fieldPath': ${configFieldAnnotation.comments.joinToString()}")
                }
                try {
                    // For recursion, we need the class of the field's type
                    val fieldTypeKClass = field.type.kotlin
                    fieldTypeKClass.memberProperties.forEach { nestedProp ->
                        val nestedField = try { nestedProp.javaField } catch (_: Exception) { null } ?: return@forEach
                        nestedField.isAccessible = true
                        if (Modifier.isStatic(nestedField.modifiers)) return@forEach
                        collectFieldComments(nestedProp, nestedField, fieldPath, comments, fieldTypeKClass)
                    }
                } catch (_: Exception) {
                    Logger.warn("[ConfigCommentProcessor.collectFieldComments] Could not reflect on nested fields of ${field.name} (type ${field.type.simpleName})")
                }
                return
            }
            
            if (configValueAnnotation != null) {
                val valuePath = if (parentPath.isEmpty()) configValueAnnotation.path else "$parentPath.${configValueAnnotation.path}"
                if (configValueAnnotation.comments.isNotEmpty()) {
                    comments[valuePath] = configValueAnnotation.comments.toList()
                    Logger.debug("[ConfigCommentProcessor.collectFieldComments] Collected comments for @ConfigValue '$valuePath': ${configValueAnnotation.comments.joinToString()}")
                }
            }
        }
    }
} 