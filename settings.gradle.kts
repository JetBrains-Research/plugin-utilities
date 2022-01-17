rootProject.name = "plugin-utilities"

include(
    "plugin-utilities-plugin",
    "plugin-utilities-core",
    "plugin-utilities-test",
    "plugin-utilities-python"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
