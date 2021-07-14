package org.jetbrains.research.ml.pluginUtilities

import org.jetbrains.research.ml.pluginUtilities.util.BaseRunner
import org.jetbrains.research.ml.pluginUtilities.util.IORunnerArgs
import org.jetbrains.research.ml.pluginUtilities.util.IORunnerArgsParser

/**
 * A simple CLI runner with two arguments: input and output
 * An example of arguments list: -Prunner=cli-runner-example -Pinput=<input path> -Poutput=<output path>
 * */
object CliRunnerExample :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("cli-runner-example", IORunnerArgsParser) {

    override fun run(args: IORunnerArgs) {
        println("Input dir: ${args.inputDir}")
        println("Output dir: ${args.outputDir}")
    }
}
