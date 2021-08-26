package org.jetbrains.research.pluginUtilities.examples

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.research.pluginUtilities.preprocessing.IDEA_FOLDER_NAME
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject

class PythonProjectOpenerStarter :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("python-project-opener-runner-example", IORunnerArgsParser) {

    // Create a preprocessor manager that removes .idea folders from the dataset
    private val preprocessor = PreprocessorManager(
        listOf(
            DeleteFilesPreprocessor(listOf(IDEA_FOLDER_NAME))
        )
    )

    override fun run(args: IORunnerArgs) {
        val datasetDir = args.inputDir ?: error("input directory must not be null")
        preprocessor.preprocessDatasetInplace(datasetDir.toFile())
        // open projects directly using intellij api because there are no repository openers for Python yet
        getSubdirectories(args.inputDir).forEach { projectPath ->
            ApplicationManager.getApplication().invokeAndWait {
                ProjectUtil.openOrImport(projectPath, null, true)?.let { project ->
                    try {
                        if (project.pythonSdk == null) {
                            setSdkToProject(project, projectPath.toString())
                            println("New SDK was connected!")
                        }
                    } catch (ex: Exception) {
                        println("Project $projectPath was not open")
                    }
                }
            }
        }
    }
}
