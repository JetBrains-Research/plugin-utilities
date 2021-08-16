package org.jetbrains.research.pluginUtilities.examples

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject

class PythonProjectOpenerStarter :
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("python-project-opener-runner-example", IORunnerArgsParser) {

    // TODO: use preprocessing and implemented functions
    override fun run(args: IORunnerArgs) {
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
