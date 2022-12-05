import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.pluginUtilities"
version = "1.0"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    kotlin("jvm") version "1.7.21" apply true
    id("org.jetbrains.intellij") version "1.10.0" apply true
    id("org.jetbrains.dokka") version "1.7.20" apply true
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0" apply true
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
        plugin("org.jetbrains.dokka")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    repositories {
        mavenCentral()
    }

    intellij {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        downloadSources.set(properties("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    ktlint {
        enableExperimentalRules.set(true)
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
        }
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }
}
