package org.jetbrains.research.pluginUtilities.preprocessing

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import org.apache.commons.io.FileUtils.cleanDirectory
import kotlin.system.exitProcess

class PreprocessKotlinJavaCommand : CliktCommand(name = "preprocessKotlinJava") {
    private val input by option("--input").file(mustExist = true, mustBeReadable = true, canBeFile = false).required()
    private val output by option("--output").file(canBeFile = false).required()
    private val androidSdk by option("--androidSdk").required()

    override fun run() {
        output.mkdirs()
        cleanDirectory(output)

        val preprocessor = getKotlinJavaPreprocessorManager(androidSdk)
        preprocessor.preprocessDataset(input, output)
        exitProcess(0)
    }
}

object PreprocessKotlinJavaStarter : ApplicationStarter {
    override fun getCommandName(): String = "preprocessKotlinJava"
    override fun main(args: MutableList<String>) = PreprocessKotlinJavaCommand().main(args.drop(1))
}
