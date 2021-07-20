package org.jetbrains.research.pluginUtilities.openRepository

import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessing
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import org.jetbrains.research.pluginUtilities.util.BaseRunner
import org.jetbrains.research.pluginUtilities.util.IORunnerArgs
import org.jetbrains.research.pluginUtilities.util.IORunnerArgsParser

object OpenRepositoryRunner :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("open-repo", IORunnerArgsParser) {

    private val buildSystems = listOf(BuildSystem.Maven, BuildSystem.Gradle)
    private val preprocessing = listOf<Preprocessing>()

    private val preprocessor = Preprocessor(preprocessing)
    private val repositoryOpener = RepositoryOpener(preprocessor, buildSystems)

    override fun run(args: IORunnerArgs) {
        println("Input dir: ${args.inputDir}")
        repositoryOpener.assertRepositoryOpens(args.inputDir.toFile())
    }
}
