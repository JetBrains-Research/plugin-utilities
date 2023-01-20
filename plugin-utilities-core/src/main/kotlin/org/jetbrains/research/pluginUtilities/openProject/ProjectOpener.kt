package org.jetbrains.research.pluginUtilities.openProject

import com.intellij.codeInspection.InspectionApplicationBase
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.serviceContainer.AlreadyDisposedException
import org.jetbrains.research.pluginUtilities.preprocessing.ProjectPreprocessor
import org.slf4j.LoggerFactory
import java.nio.file.Path


typealias ProjectBasedInstanceProvider<T> = ((Project) -> T)


class ProjectOpener(
    private val projectPreprocessorProvider: ProjectBasedInstanceProvider<ProjectPreprocessor>? = null,
    private val projectConfiguratorProvider: ProjectBasedInstanceProvider<CommandLineInspectionProjectConfigurator>? = null,
) : InspectionApplicationBase() {

    private val logger = LoggerFactory.getLogger(javaClass)
    fun open(
        projectRoot: Path,
        disposable: Disposable,
        resolve: Boolean = false,
    ): Project? {

        // Opening project without resolve
        var project = openProjectWithoutResolve(projectRoot, disposable) ?: run {
            logger.info("Failed to open project from $projectRoot.")
            return null
        }
        logger.info("Project ${project.name} was successfully opened.")


        // Preprocessing project without resolve (e.x. selling env)
        project = projectPreprocessorProvider?.let {
            val preprocessor = it(project)
            preprocessProject(project, preprocessor) ?: run {
                logger.info("Failed to preprocessed project ${project.name}.")
                return null
            }
        } ?: run {
            logger.info("Preprocessor not provided, skip preprocessing step.")
            project
        }
        logger.info("Project ${project.name} was successfully preprocessed.")


        // Resolving project if required
        if (resolve) {
            project = projectConfiguratorProvider?.let {
                val configurator = it(project)
                val context = OpenProjectConfiguratorContext(projectRoot)

                resolveProject(project, configurator, context) ?: run {
                    logger.info("Failed to resolve project ${project.name}.")
                    return null
                }
            } ?: run {
                logger.error("Configurator not provided, can not perform resolve step.")
                return null
            }
            logger.info("Project ${project.name} was successfully resolved.")
        }

        return project
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

    private fun preprocessProject(project: Project, preprocessor: ProjectPreprocessor): Project? =
        preprocessor.preprocess(project)?.also {
            logger.info("Project ${project.name} was successfully preprocessed.")
        } ?: run {
            logger.info("Failed to preprocessed project ${project.name}.")
            return null
        }

    private fun resolveProject(
        project: Project,
        configurator: CommandLineInspectionProjectConfigurator,
        context: OpenProjectConfiguratorContext,
    ): Project? {
        val future = ApplicationManager.getApplication().executeOnPooledThread<Project?> {
            configurator.preConfigureProject(project, context)
            configurator.configureProject(project, context)
            ApplicationManager.getApplication().invokeAndWait(
                {}, ModalityState.any()
            )
            logger.info("Project ${project.name} was successfully configured!")
            project
        }

        return future.get() ?: run {
            logger.warn("Can not run resolve the project ${project.name}!")
            return null
        }
    }
}
