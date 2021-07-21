package org.jetbrains.research.pluginUtilities.preprocessing

import org.apache.commons.io.FileUtils.deleteDirectory
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties
import java.util.logging.Logger

/**
 * Runs multiple preprosessings
 */
class Preprocessor(private val preprocessings: List<Preprocessing>) {
    private val logger = Logger.getLogger(javaClass.name)

    /**
     * Preprocesses repository in [repoDirectory] by copying it to [outputDirectory].
     * Does not mutate the original repository
     */
    fun preprocess(repoDirectory: File, outputDirectory: File) {
        repoDirectory.copyRecursively(outputDirectory)
        preprocessings.forEach { preprocessing ->
            logger.info("Running preprosessing ${preprocessing.name} for ${repoDirectory.name}")
            preprocessing.preprocess(outputDirectory)
        }
    }
}

/**
 * Object that preprocesses a given repository by mutating its file tree.
 * For example, it can add new files to the repository, remove or change the existing ones.
 * @property name The name of the preprocessing that is used for debugging and logging
 */
interface Preprocessing {
    val name: String
    fun preprocess(repoDirectory: File)
}

/**
 * Adds local.properties files with sdk.dir=[androidSdkAbsolutePath] where it detects a Java build system
 */
class AndroidSdkPreprocessing(private val androidSdkAbsolutePath: String) : Preprocessing {
    private val logger = Logger.getLogger(javaClass.name)

    override val name: String = "Add Android SDK with local.properties"

    companion object {
        const val LOCAL_PROPERTIES_FILE_NAME = "local.properties"
        const val SDK_PROPERTY_NAME = "sdk.dir"
        val acceptedBuildSystems = listOf(BuildSystem.Gradle)
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

/**
 * Removed directories from repositories with names [directoryNames]
 * @param directoryNames The names of directories which are to be removed
 */
class DeleteDirectoriesPreprocessing(private val directoryNames: List<String>) : Preprocessing {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val name: String = "Delete directories"

    override fun preprocess(repoDirectory: File) {
        removeIdeaDirectories(repoDirectory)
    }

    private fun removeIdeaDirectories(directory: File) {
        for (subdirectory in directory.subdirectories) {
            if (subdirectory.name in directoryNames) {
                logger.info("Deleting ${subdirectory.name} folder: ${subdirectory.path}")
                deleteDirectory(subdirectory)
            } else {
                removeIdeaDirectories(subdirectory)
            }
        }
    }
}
