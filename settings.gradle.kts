rootProject.name = "plugin-utilities"

include(
    "plugin-utilities-plugin",
    "plugin-utilities-core",
    "plugin-utilities-test",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
