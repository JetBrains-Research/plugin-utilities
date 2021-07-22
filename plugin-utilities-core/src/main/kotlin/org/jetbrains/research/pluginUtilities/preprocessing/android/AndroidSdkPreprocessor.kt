package org.jetbrains.research.pluginUtilities.preprocessing.android

import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import java.io.File
import java.util.Properties
import java.util.logging.Logger

/**
 * Adds local.properties files with sdk.dir=[androidSdkAbsolutePath] where it detects a Gradle or Gradle with KotlinDsl build system
 */
class AndroidSdkPreprocessor(private val androidSdkAbsolutePath: String) : Preprocessor {
    private val logger = Logger.getLogger(javaClass.name)

    override val name: String = "Add Android SDK with local.properties"

    companion object {
        const val LOCAL_PROPERTIES_FILE_NAME = "local.properties"
        const val SDK_PROPERTY_NAME = "sdk.dir"
        val acceptedBuildSystems = listOf(BuildSystem.Gradle, BuildSystem.GradleKotlinDsl)
    }

    override fun preprocess(repoDirectory: File) {
        repoDirectory.collectBuildSystemRoots(acceptedBuildSystems).forEach { projectRoot ->
            logger.info("Updating $LOCAL_PROPERTIES_FILE_NAME in ${projectRoot.path}")
            updateLocalProperties(projectRoot)
        }
    }

    /**
     * Sets the `sdk.dir` property in the local.properties file.
     * If the file already exists, modifies it by adding the `sdk.dir` property or changing the existing value.
     * Creates a new file with the new property otherwise.
     */
    private fun updateLocalProperties(projectRoot: File) {
        val localPropertiesFile = projectRoot.resolve(LOCAL_PROPERTIES_FILE_NAME)
        val properties = Properties()
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        } else {
            localPropertiesFile.createNewFile()
        }
        properties.setProperty(SDK_PROPERTY_NAME, androidSdkAbsolutePath)
        properties.store(localPropertiesFile.outputStream(), null)
    }
}
