import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.jetbrains.research.pluginUtilities"
version = "1.0"

val platformVersion: String by project
val platformType: String by project
val platformDownloadSources: String by project
val platformPlugins: String by project
val pluginName: String by project

val spaceUsername: String by project
val spacePassword: String by project

plugins {
    java
    kotlin("jvm") version "1.7.21" apply true
    id("org.jetbrains.intellij") version "1.10.0" apply true
    id("org.jetbrains.dokka") version "1.7.20" apply true
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0" apply true
    `maven-publish`
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
        version.set(platformVersion)
        type.set(platformType)
        downloadSources.set(platformDownloadSources.toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty))
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.jetbrains.research"
            artifactId = "plugin-utilities"
            version = "1.0"
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://packages.jetbrains.team/maven/p/big-code/bigcode")
            credentials {
                username = spaceUsername
                password = spacePassword
            }
        }
    }
}
