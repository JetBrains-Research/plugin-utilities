package org.jetbrains.research.pluginUtilities

import java.io.File

/**
 * Runs multiple preprosessings
 */
class Preprocessor(private val preprocessings: List<Preprocessing>) {
    /**
     * Preprocesses repository in [repoDirectory] by copying it to [outputDirectory].
     * Does not mutate the original repository
     */
    fun preprocess(repoDirectory: File, outputDirectory: File) {
        repoDirectory.copyRecursively(outputDirectory)
        for (preprocessing in preprocessings) {
            preprocessing.preprocess(outputDirectory)
        }
    }
}

/**
 * Object that preprocesses a given repository by mutating its file tree
 */
interface Preprocessing {
    val name: String
    fun preprocess(repoDirectory: File)
}

/**
 * Adds local.properties files with sdk.dir=[androidSdkAbsolutePath] where it detects a Java build system
 */
class AndroidSdkPreprocessing(private val androidSdkAbsolutePath: String) : Preprocessing {
    override val name: String = "Add Android SDK with local.properties"
    override fun preprocess(repoDirectory: File) {
        for (projectRoot in repoDirectory.collectJavaProjectRoots()) {
            println("Running $name in ${projectRoot.path}")
            createLocalPropertiesFile(projectRoot)
        }
    }

    private fun createLocalPropertiesFile(projectRoot: File) {
        val localPropertiesFile = projectRoot.resolve("local.properties")
        localPropertiesFile.createNewFile()
        localPropertiesFile.writeText(
            """
            sdk.dir=$androidSdkAbsolutePath
            """.trimIndent()
        )
    }
}
