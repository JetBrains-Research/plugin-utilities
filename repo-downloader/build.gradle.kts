group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.5.21"
}

dependencies {
    // Utils
    implementation("commons-io:commons-io:2.11.0")
    // CLI
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("com.charleskorn.kaml:kaml:0.34.0")
    // Logging
    implementation("org.slf4j:slf4j-simple:1.7.29")
    // Tests
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

tasks {
    register<JavaExec>("downloadRepos") {
        val repos: String? by project
        val output: String? by project

        args = listOfNotNull(repos, output)
        group = "other"
        main = "MainKt"
        classpath = sourceSets["main"].runtimeClasspath
    }
}
