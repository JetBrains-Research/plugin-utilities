package org.jetbrains.research.pluginUtilities.examples

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.pluginUtilities.runners.BaseRunner
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgs
import org.jetbrains.research.pluginUtilities.runners.IORunnerArgsParser

class JvmProjectOpenerStarter:
    BaseRunner<IORunnerArgs, IORunnerArgsParser>("jvm-project-opener-runner-example", IORunnerArgsParser) {

    // TODO: use preprocessing and implemented functions
    override fun run(args: IORunnerArgs) {
        getSubdirectories(args.inputDir).forEach{ projectPath ->
            ApplicationManager.getApplication().invokeAndWait {
                ProjectManagerEx.getInstanceEx().openProject(
                    projectPath,
                    OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                )?.let { project ->
                    try {
                        println("The project ${project.basePath} was opened!")
                    } catch (ex: Exception) {
                        println(ex)
                    } finally {
                        ApplicationManager.getApplication().invokeAndWait {
                            val closeStatus = ProjectManagerEx.getInstanceEx().forceCloseProject(project)
                            println("Project ${project.name} is closed = $closeStatus")
                        }
                    }
                }
            }
        }
    }
}