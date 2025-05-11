package com.arteam.arlibs.language.cloud

import com.arteam.arlibs.language.annotations.Language
import com.arteam.arlibs.utils.Logger
import org.bukkit.plugin.Plugin
import org.yaml.snakeyaml.Yaml
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Data class to hold information fetched from a cloud language file.
 * 保存从云语言文件中获取的数据。
 *
 * @property content The raw string content of the language file.
 *                   语言文件的原始字符串内容。
 * @property structureVersion The structure version defined within the language file (e.g., a "structure-version" key).
 *                          语言文件中定义的结构版本（例如："structure-version" 键）。
 * @property contentVersion The content version defined within the language file (e.g., a "content-version" key).
 *                        语言文件中定义的内容版本（例如："content-version" 键）。
 */
data class CloudLanguageData(
    val content: String,
    val structureVersion: Int,
    val contentVersion: Int
)

/**
 * Handles fetching language file data from cloud URLs.
 * 处理从云端URL获取语言文件数据。
 */
object LanguageDownloader {
    private lateinit var userAgent: String
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        val t = Thread(r, "ArLibs-LanguageUpdater")
        t.isDaemon = true
        t
    }
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    /**
     * Initializes the LanguageDownloader with plugin information.
     * 使用插件信息初始化 LanguageDownloader。
     *
     * @param plugin The plugin instance.
     *               插件实例。
     */
    fun init(plugin: Plugin) {
        val pluginVersion = plugin.description.version ?: "Unknown"
        val arLibsVersion = "ArLibs" // Replace with actual ArLibs version if available dynamically
        userAgent = "${plugin.description.name}/$pluginVersion ($arLibsVersion LanguageDownloader)"
    }

    /**
     * Fetches language file data from the specified cloud URL in the Language annotation.
     * 从 Language 注解中指定的云端URL获取语言文件数据。
     *
     * @param annotation The language annotation containing the cloudUrl.
     *                   包含 cloudUrl 的语言注解。
     * @return A CompletableFuture that will complete with [CloudLanguageData] if successful, or null otherwise.
     *         一个 CompletableFuture，如果成功，将完成并返回 [CloudLanguageData]，否则返回 null。
     */
    fun fetchUpdateData(annotation: Language): CompletableFuture<CloudLanguageData?> {
        if (annotation.cloudUrl.isEmpty()) {
            return CompletableFuture.completedFuture(null)
        }

        return CompletableFuture.supplyAsync {
            try {
                Logger.debug("Attempting to download language update for '${annotation.name}' from ${annotation.cloudUrl}")
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(annotation.cloudUrl))
                    .header("User-Agent", userAgent)
                    .GET()
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())

                if (response.statusCode() != 200) {
                    Logger.warn("Failed to download language file '${annotation.name}' from ${annotation.cloudUrl}: HTTP ${response.statusCode()}")
                    return@supplyAsync null
                }

                val content = response.body().use { inputStream -> inputStream.bufferedReader().readText() }
                
                // Parse YAML to get versions without relying on Bukkit API for thread safety
                val yamlParser = Yaml()
                val yamlMap = yamlParser.load<Map<String, Any>>(content)
                
                val structureVersion = (yamlMap["structure-version"] as? Number)?.toInt() ?: -1
                val contentVersion = (yamlMap["content-version"] as? Number)?.toInt() ?: -1

                if (structureVersion == -1 || contentVersion == -1) {
                    Logger.warn("Downloaded language file '${annotation.name}' from ${annotation.cloudUrl} is missing valid 'structure-version' or 'content-version' keys.")
                    return@supplyAsync null
                }
                
                Logger.debug("Successfully downloaded language data for '${annotation.name}'. Structure: $structureVersion, Content: $contentVersion")
                CloudLanguageData(content, structureVersion, contentVersion)
            } catch (e: Exception) {
                Logger.warn("Failed to download or parse language file '${annotation.name}' from ${annotation.cloudUrl}: ${e.message}")
                // Consider logging e for more details in debug mode
                null
            }
        , scheduler}
    }
    
    /**
     * Schedules automatic updates for a language file if enabled in its annotation.
     * This method is intended to be called by LanguageManager.
     * 如果在语言注解中启用了自动更新，则为其安排自动更新。
     * 此方法旨在由 LanguageManager 调用。
     *
     * @param annotation The language annotation.
     *                   语言注解。
     * @param updateAction The action to perform when an update check is triggered. 
     *                     It should take the Language annotation and return a CompletableFuture.
     *                     当触发更新检查时执行的操作。它应该接受 Language 注解并返回 CompletableFuture。
     */
    fun scheduleAutoUpdate(annotation: Language, updateAction: (Language) -> CompletableFuture<*>) {
        if (!annotation.autoUpdate || annotation.cloudUrl.isEmpty() || annotation.updateInterval <= 0) {
            return
        }

        scheduler.scheduleAtFixedRate({
            try {
                Logger.debug("Scheduled auto-update check for language '${annotation.name}'.")
                updateAction(annotation)
            } catch (e: Exception) {
                Logger.warn("Error during scheduled language update for '${annotation.name}': ${e.message}", e)
            }
        }, annotation.updateInterval, annotation.updateInterval, TimeUnit.MINUTES)
        Logger.info("Scheduled auto-update for language '${annotation.name}' every ${annotation.updateInterval} minutes.")
    }

    /**
     * Shuts down the downloader scheduler.
     * 关闭下载器调度程序。
     */
    fun shutdown() {
        scheduler.shutdownNow() // Interrupt ongoing tasks
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.warn("LanguageDownloader scheduler did not terminate gracefully.")
            }
        } catch (e: InterruptedException) {
            Logger.warn("Interrupted while waiting for LanguageDownloader scheduler to terminate.")
            Thread.currentThread().interrupt()
        }
    }
} 