import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("java")
}

group = "dev.arteam"
version = "1.0.0-SNAPSHOT"
description = "A Minecraft plugin framework for easy customization and extension."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper API
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot API
}

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    
    // Minecraft server API - using Paper, but you can switch to Spigot if needed
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<ProcessResources> {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set(project.name)
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
      
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
