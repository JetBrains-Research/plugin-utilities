package org.jetbrains.research.pluginUtilities.openRepository

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import org.apache.commons.io.FileUtils.cleanDirectory
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object TestOpenJavaStarter : ApplicationStarter {
    override fun getCommandName(): String = "testOpenJava"

    override fun main(args: MutableList<String>) {
        TestOpenJavaCommand().main(args.drop(1))
    }
}

class TestOpenJavaCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val input by option("--input").file(mustExist = true, mustBeReadable = true, canBeFile = false).required()
    private val preprocessOutput by option("--preprocessOutput").file(canBeFile = false).required()
    private val androidSdk by option("--androidSdk").required()

    override fun run() {
        logger.info("Preprocessing repositories")
        preprocessOutput.mkdirs()
        cleanDirectory(preprocessOutput)
        preprocessRepositories()
        logger.info("Opening repositories")
        try {
            openRepositories()
        } catch (e: Throwable) {
            logger.error("Failed to open projects", e)
            exitProcess(1)
        }
        exitProcess(0)
    }

    private fun preprocessRepositories() {
        val preprocessor = getKotlinJavaPreprocessorManager(androidSdk)
        preprocessor.preprocessDataset(input, preprocessOutput)
    }

    private fun openRepositories() {
        val repositoryOpener = getKotlinJavaRepositoryOpener()

        for (repositoryRoot in preprocessOutput.subdirectories) {
            repositoryOpener.assertRepositoryOpens(repositoryRoot)
        }
    }
}
