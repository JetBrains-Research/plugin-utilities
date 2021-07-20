package org.jetbrains.research.pluginUtilities.openRepository

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.preprocessing.AndroidSdkPreprocessing
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessing
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import org.jetbrains.research.pluginUtilities.util.BaseRunner
import org.jetbrains.research.pluginUtilities.util.IORunnerArgs
import org.jetbrains.research.pluginUtilities.util.IORunnerArgsParser
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


object TestOpenMocksStarter : ApplicationStarter {
    override fun getCommandName(): String = "testOpenMocks"

    override fun main(args: MutableList<String>) {
        TestOpenMocksCommand().main(args.drop(1))
    }
}

class TestOpenMocksCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val input by argument("input").file(mustExist = true, mustBeReadable = true, canBeFile = false)
    private val androidSdk by option("--androidSdk").required()

    private val repositoryOpener
        get() = RepositoryOpener(
            Preprocessor(listOf(AndroidSdkPreprocessing(androidSdk))),
            listOf(BuildSystem.Gradle, BuildSystem.Maven)
        )

    override fun run() {
        val repositories = input.subdirectories
        logger.info("All repositories: $repositories")
        for (repositoryRoot in repositories) {
            repositoryOpener.assertRepositoryOpens(repositoryRoot)
        }
        exitProcess(0)
    }
}
