package org.jetbrains.research.pluginUtilities.preprocessing.common

import org.apache.commons.io.FileUtils.deleteDirectory
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Deletes files and directories with names [fileNames]
 * @param fileNames The names of files which are to be removed
 */
class DeleteFilesPreprocessor(private val fileNames: List<String>) : Preprocessor {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val name: String = "Delete files by name"

    override fun preprocess(repoDirectory: File) {
        deleteFiles(repoDirectory)
    }

    private fun deleteFiles(directory: File) {
        directory.listFiles()?.filter { it.name in fileNames }?.forEach { file ->
            logger.info("Deleting ${file.path}")
            if (file.isDirectory) {
                deleteDirectory(file)
            } else {
                file.delete()
            }
        }
        directory.subdirectories.forEach { subdirectory -> deleteFiles(subdirectory) }
    }
}
