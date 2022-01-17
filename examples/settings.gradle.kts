import java.net.URI

rootProject.name = "example"

include(
    "examples-plugin"
)

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
        producesModule("$utilitiesProjectName:plugin-utilities-python")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases")
    }
}
