package org.jetbrains.research.pluginUtilities.openRepository

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object TestOpenJavaStarter : ApplicationStarter {
    override fun getCommandName(): String = "testOpenKotlinJava"

    override fun main(args: MutableList<String>) {
        TestOpenJavaCommand().main(args.drop(1))
    }
}

class TestOpenJavaCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val input by option("--input").file(mustExist = true, mustBeReadable = true, canBeFile = false).required()

    override fun run() {
        logger.info("Opening repositories")
        try {
            openRepositories()
        } catch (e: Throwable) {
            logger.error("Failed to open projects", e)
            exitProcess(1)
        }
        exitProcess(0)
    }

    private fun openRepositories() {
        val repositoryOpener = getKotlinJavaRepositoryOpener()

        for (repositoryRoot in input.subdirectories) {
            logger.info("Opening repository ${repositoryRoot.name}")
            repositoryOpener.assertRepositoryOpens(repositoryRoot)
        }
    }
}
