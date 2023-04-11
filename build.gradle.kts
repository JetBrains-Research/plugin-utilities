import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

val projectVersion = "2.0.4"

group = "org.jetbrains.research.pluginUtilities"
version = projectVersion

val platformVersion: String by project
val platformType: String by project
val platformDownloadSources: String by project
val platformPlugins: String by project
val pluginName: String by project

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

fun getLocalProperty(key: String, file: String = "local.properties"): String? {
    val properties = Properties()

    File("local.properties")
        .takeIf { it.isFile }
        ?.let { properties.load(it.inputStream()) }
        ?: println("File $file with properties not found")

    return properties.getProperty(key, null)
}

val spaceUsername = getLocalProperty("spaceUsername")
val spacePassword = getLocalProperty("spacePassword")

configure(subprojects.filter { it.name != "plugin-utilities-plugin" }) {

    apply(plugin = "maven-publish")

    val subprojectName = this.name

    publishing {
        publications {
            register<MavenPublication>("maven") {
                groupId = "org.jetbrains.research"
                artifactId = subprojectName
                version = projectVersion
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
}
