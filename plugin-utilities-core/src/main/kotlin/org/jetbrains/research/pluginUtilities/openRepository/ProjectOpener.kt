package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.codeInspection.InspectionApplicationBase
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.serviceContainer.AlreadyDisposedException
import org.slf4j.LoggerFactory
import java.nio.file.Path

interface ProjectPreprocessor {
    fun preprocess(project: Project): Project?

}

class ProjectOpener(
    private val projectPreprocessor: ((Project) -> ProjectPreprocessor)? = null,
    private val projectConfigurator: ((Project) -> CommandLineInspectionProjectConfigurator)? = null,
) : InspectionApplicationBase() {

    private val logger = LoggerFactory.getLogger(javaClass)
    fun openProject(
        projectRoot: Path,
        resolve: Boolean = false,
        disposable: Disposable,
    ): Project? {

        var project = openProjectWithoutResolve(projectRoot, disposable) ?: return null

        if (projectPreprocessor != null) {
            project = projectPreprocessor(project).preprocess(project) ?: return null
        }


        if (resolve) {
            projectConfigurator(project)?.let {
                project =
            } ?: let {
                logger.info("projectConfigurator required for project resolve")
                return null
            }
            val context = OpenRepositoryConfiguratorContext(projectRoot)
        }

        return project
    }

    private fun resolveProject(
        project: Project,
        context: OpenRepositoryConfiguratorContext
    ): Project? {
        projectConfigurator ?: {
            logger.error("Project configurator is not selected")
            return null
        }
        val future = ApplicationManager.getApplication().executeOnPooledThread<Project?> {
            projectConfigurator.preConfigureProject(project, context)
            projectConfigurator.configureProject(project, context)
            ApplicationManager.getApplication().invokeAndWait(
                {},
                ModalityState.any()
            )
            logger.info("Project ${project.name} was successfully configured!")
            project
        }

        future.get() ?: run {
            logger.warn("Can not run resolve the project ${project.name}!")
            return null
        }
    } ?: logger.error("Project ${project.name} was successfully configured!")
}

fun assertProjectResolved(project: Project) {
    if (!project.hasResolvedDependencies) {
        throw AssertionError("Project ${project.name} has no resolved dependencies")
    }
}


fun forceCloseProject(project: Project) {
    try {
        ProjectManagerEx.getInstanceEx().forceCloseProject(project)
    } catch (e: AlreadyDisposedException) {
        // TODO: figure out why this happened
        logger.warn("Failed to close project", e)
    }
}

private fun openProjectWithoutResolve(repositoryRoot: Path, disposable: Disposable): Project? {
    ApplicationManager.getApplication().assertIsNonDispatchThread()

    val project: Project = ApplicationManager.getApplication().executeOnPooledThread<Project> {
        val project = openProject(repositoryRoot, disposable)
        project ?: error("Can not open project $repositoryRoot")
    }.get() ?: run {
        logger.warn("Project is null")
        return null
    }

    logger.info("Project ${project.name} was successfully opened without resolve!")
    return project
}

private fun resolveProject(
    project: Project,
    configurator: CommandLineInspectionProjectConfigurator,
    context: CommandLineInspectionProjectConfigurator.ConfiguratorContext
) {
    configurator.preConfigureProject(project, context)
    configurator.configureProject(project, context)
    ApplicationManager.getApplication().invokeAndWait(
        {},
        ModalityState.any()
    )
}

private val Project.hasResolvedDependencies: Boolean
    get() = countDependencies(this) > 0

private fun countDependencies(project: Project): Int = project.modules.sumOf { countLibraries(it) }

private val Project.modules: Array<Module>
    get() = ModuleManager.getInstance(this).modules

private fun countLibraries(module: Module): Int {
    var nLibraries = 0
    ModuleRootManager.getInstance(module).orderEntries().forEachLibrary {
        nLibraries++
        true
    }
    return nLibraries
}
}
