package org.jetbrains.research.pluginUtilities.examples

import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

class JvmProjectOpenerStarter :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("jvm-project-opener-runner-example", IORunnerArgsParser) {

    private val preprocessor = getKotlinJavaPreprocessorManager(null)
    private val repositoryOpener = getKotlinJavaRepositoryOpener()

    override fun run(args: IORunnerArgs) {
        val datasetDir = args.inputDir ?: error("input directory must not be null")
        preprocessor.preprocessDatasetInplace(datasetDir.toFile())
        getSubdirectories(datasetDir).forEach { repositoryRoot ->
            val allProjectsOpenedSuccessfully = repositoryOpener.openRepository(repositoryRoot.toFile()) { project ->
                println("Project $project opened")
            }
            println("All projects opened successfully: $allProjectsOpenedSuccessfully")
        }
    }
}
