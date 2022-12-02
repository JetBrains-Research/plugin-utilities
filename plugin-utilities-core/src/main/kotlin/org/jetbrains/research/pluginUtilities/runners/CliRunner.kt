package org.jetbrains.research.pluginUtilities.runners

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import java.nio.file.Paths
import kotlin.system.exitProcess

/** Interface for all runners command line arguments. */
interface RunnerArgs

/** Interface for parsing any set of command line arguments to [runner arguments][RunnerArgs]. */
interface RunnerArgsParser<A : RunnerArgs> {

    /** Parse [runner arguments][RunnerArgs] from [command line arguments][args]. */
    fun parse(args: List<String>): A
}

/** Arguments data class for input and output directory names arguments. */
open class IORunnerArgs(val parser: ArgParser) : RunnerArgs {
    val inputDir by parser.storing(
        "-i",
        "--input",
        help = "Input directory with kotlin projects"
    ) { Paths.get(this) }

    val outputDir by parser.storing(
        "-o",
        "--output",
        help = "Output directory"
    ) { Paths.get(this) }
}

/** Parser for input and output directory names arguments. */
object IORunnerArgsParser : RunnerArgsParser<IORunnerArgs> {

    override fun parse(args: List<String>): IORunnerArgs {
        return ArgParser(args.drop(1).toTypedArray()).parseInto(::IORunnerArgs)
            .run {
                require(inputDir.toFile().isDirectory) { "Argument has to be directory" }
                outputDir.toFile().mkdirs()
                this
            }
    }
}

/**
 * Base class for all runners. This class process command line arguments from cli and runs analysis process.
 * To create new runner implements object which extends [BaseRunner] and specify it's name, arguments,
 * arguments parser and [invocation method][run].
 *
 * Don't forget add your runner into plugin.xml file.
 *
 * @param A the type of command line arguments data class.
 * @param P the type of the command line arguments parser.
 */
abstract class BaseRunner<A : RunnerArgs, P : RunnerArgsParser<A>>(
    private val commandName: String,
    private val parser: P
) : ApplicationStarter {
    private val logger = Logger.getInstance(javaClass)

    override fun getCommandName(): String = commandName

    override fun getRequiredModality(): Int = ApplicationStarter.NOT_IN_EDT

    /** Runs the specific for each analyzer analysis process using parsed [arguments][args]. */
    abstract fun run(args: A)

    override fun main(args: List<String>) {
        try {
            val commandArgs = parser.parse(args)
            run(commandArgs)
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
