package org.jetbrains.research.pluginUtilities

import org.jetbrains.research.pluginUtilities.util.subdirectories
import java.io.File

enum class BuildSystem(val buildFile: String) {
    Maven("pom.xml"),
    Gradle("build.gradle"),
}

/**
 * Search for a build file in the given directory and detect the build system
 */
fun File.detectBuildSystem(): BuildSystem? =
    BuildSystem.values().find { buildSystem -> resolve(buildSystem.buildFile).exists() }

/**
 * Recursively iterates over the file tree and looks for directories that have initialized build systems.
 * If it finds a folder that has a build system, it does not iterate deeper into that folder.
 *
 * Example file tree:
 * /
 * --/a   (Gradle project)
 * --/b   (Gradle project)
 * --/b/c (Gradle submodule)
 * --/d   (Maven project)
 *
 * For the given example file tree the function with acceptedBuildSystems = listOf(Gradle) will return /a and /b
 */
fun File.collectBuildSystemRoots(acceptedBuildSystems: List<BuildSystem> = BuildSystem.values().asList()): List<File> =
    sequence {
        if (this@collectBuildSystemRoots.detectBuildSystem() in acceptedBuildSystems) {
            yield(this@collectBuildSystemRoots)
        } else {
            for (subdirectory in this@collectBuildSystemRoots.subdirectories) {
                yieldAll(subdirectory.collectBuildSystemRoots(acceptedBuildSystems))
            }
        }
    }.toList()

fun File.collectBuildSystemRoots(vararg acceptedBuildSystem: BuildSystem): List<File> =
    collectBuildSystemRoots(acceptedBuildSystem.asList())
