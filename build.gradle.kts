import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "com.arteam"
version = "1.0.0-SNAPSHOT"
description = "A Minecraft plugin framework for easy customization and extension."

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")

    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Adventure (for text components)
    implementation("net.kyori:adventure-api:4.21.0")
    implementation("net.kyori:adventure-text-minimessage:4.21.0")

    // Configuration
    implementation("org.yaml:snakeyaml:2.4")

    // Database
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("redis.clients:jedis:6.0.0")
    implementation("org.mongodb:mongodb-driver-sync:5.5.0")
    
    // Cache
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    
    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    
    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
    
    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        
        // Relocate dependencies to avoid conflicts
        relocate("kotlin", "com.twinkovo.mythlibs.lib.kotlin")
        relocate("org.yaml.snakeyaml", "com.twinkovo.mythlibs.lib.snakeyaml")
        relocate("net.kyori", "com.twinkovo.mythlibs.lib.kyori")
        relocate("com.github.benmanes.caffeine", "com.twinkovo.mythlibs.lib.caffeine")
        relocate("org.jetbrains.kotlinx", "com.twinkovo.mythlibs.lib.kotlinx")
    }
    
    build {
        dependsOn(shadowJar)
    }
}
