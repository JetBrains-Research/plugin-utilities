package org.jetbrains.research.pluginUtilities

import java.io.File
import java.util.logging.Logger

/**
 * Runs multiple preprosessings
 */
class Preprocessor(private val preprocessings: List<Preprocessing>) {
    private val LOG = Logger.getLogger(javaClass.name)

    /**
     * Preprocesses repository in [repoDirectory] by copying it to [outputDirectory].
     * Does not mutate the original repository
     */
    fun preprocess(repoDirectory: File, outputDirectory: File) {
        repoDirectory.copyRecursively(outputDirectory)
        for (preprocessing in preprocessings) {
            LOG.info("Running preprosessing ${preprocessing.name} for ${repoDirectory.name}")
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
    private val LOG = Logger.getLogger(javaClass.name)

    override val name: String = "Add Android SDK with local.properties"
    override fun preprocess(repoDirectory: File) {
        for (projectRoot in repoDirectory.collectJavaProjectRoots()) {
            LOG.info("Adding local.properties to ${projectRoot.path}")
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
