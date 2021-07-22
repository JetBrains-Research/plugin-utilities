package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.util.subdirectories
import java.io.File
import java.util.logging.Logger

/**
 * Runs multiple preprocessors on one repository or dataset.
 * @param preprocessors The preprocessors that should be run
 */
class PreprocessorManager(private val preprocessors: List<Preprocessor>) {
    private val logger = Logger.getLogger(javaClass.name)

    /**
     * Preprocesses repository in [repoDirectory] by copying it to [outputDirectory].
     * Does not mutate the original repository
     */
    fun preprocessRepository(repoDirectory: File, outputDirectory: File) {
        repoDirectory.copyRecursively(outputDirectory)
        preprocessors.forEach { preprocessing ->
            logger.info("Running preprosessing ${preprocessing.name} for ${repoDirectory.name}")
            preprocessing.preprocess(outputDirectory)
        }
    }

    /**
     * Preprocesses a dataset that contains multiple repositories
     * @param datasetDirectory A directory where each subdirectory is an individual repository
     * @param outputDirectory A directory where preprocessed repositories will be stored.
     * For each repository it creates a separate output subdirectory.
     */
    fun preprocessDataset(datasetDirectory: File, outputDirectory: File) {
        for (repositoryRoot in datasetDirectory.subdirectories) {
            val repositoryOutput = outputDirectory.resolve(repositoryRoot.name)
            repositoryOutput.mkdir()
            logger.info("Preprocessing repository ${repositoryRoot.name}")
            preprocessRepository(repositoryRoot, repositoryOutput)
        }
    }
}

