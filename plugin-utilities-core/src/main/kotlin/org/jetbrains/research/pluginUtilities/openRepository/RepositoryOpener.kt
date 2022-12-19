package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.codeInspection.InspectionApplicationBase
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.Disposer
import com.intellij.serviceContainer.AlreadyDisposedException
import org.jetbrains.idea.maven.MavenCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.GradleCommandLineProjectConfigurator
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

/**
 * Locates projects in repositories and opens them.
 * Each repository may contain several projects, it locates them with [collectBuildSystemRoots] and [acceptedBuildSystems]
 */
class RepositoryOpener(private val acceptedBuildSystems: List<BuildSystem>) : InspectionApplicationBase() {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Opens all projects inside the repository.
     * @param repoDirectory root of the repository
     * @param action the function that is called for each opened project
     * @returns true if and only if all projects were opened successfully.
     */
    fun openRepository(repoDirectory: File, action: (Project) -> Boolean): Boolean {
        logger.info("Opening repository $repoDirectory")
        val buildSystemRoots = repoDirectory.collectBuildSystemRoots(acceptedBuildSystems)
        val projectRoots = buildSystemRoots.ifEmpty {
            logger.info("No folders with build systems found! Opening the root of the repository")
            listOf(repoDirectory)
        }
        logger.info("Opening project roots: $projectRoots")

        var allProjectsOpenedSuccessfully = true

        for (projectRoot in projectRoots) {
            try {
                openProjectWithResolve(Path.of(projectRoot.path), action)
            } catch (e: Exception) {
                logger.error("Failed to open project ${projectRoot.path}", e)
                allProjectsOpenedSuccessfully = false
                continue
            }
        }
        return allProjectsOpenedSuccessfully
    }

    private fun Project.resolve(
        configurator: CommandLineInspectionProjectConfigurator,
        context: CommandLineInspectionProjectConfigurator.ConfiguratorContext
    ) {
        configurator.preConfigureProject(this, context)
        configurator.configureProject(this, context)
        waitForInvokeLaterActivities()
    }

    fun openProjectWithResolve(
        repositoryRoot: Path,
        action: (Project) -> Boolean,
        configurator: (Project) -> CommandLineInspectionProjectConfigurator,
    ): Boolean {
        val disposable = Disposer.newDisposable()
        try {
            val project = openSingleProjectWithoutResolve(repositoryRoot, disposable)
            project ?: return false

            val future = ApplicationManager.getApplication().executeOnPooledThread<Project> {
                ApplicationManager.getApplication().assertIsNonDispatchThread()
                val context = RepositoryOpenerConfiguratorContext(repositoryRoot)
                project.resolve(configurator(project), context)
                project
            }
            future.get() ?: run {
                logger.error("Can not run resolve the project ${project.name} correctly!")
                return false
            }

            println("The project ${project.name} was open successfåully")
            action(project)
            return true
        } finally {
            Disposer.dispose(disposable)
        }
    }

    fun openMavenOrGradleProjectWithResolve(
        repositoryRoot: Path,
        action: (Project) -> Boolean,
    ): Boolean {
        return openProjectWithResolve(repositoryRoot, action) {
            if (MavenProjectsManager.getInstance(it).isMavenizedProject) {
                logger.info("IDEA detected Maven build system")
                MavenCommandLineInspectionProjectConfigurator()
            } else {
                logger.info("IDEA detected Gradle build system")
                GradleCommandLineProjectConfigurator()
            }
        }
    }

    // TODO: delete it
    fun openProjectWithResolve(
        repositoryRoot: Path,
        action: (Project) -> Boolean,
    ) = openMavenOrGradleProjectWithResolve(repositoryRoot, action)

    private fun waitForInvokeLaterActivities() {
        ApplicationManager.getApplication().invokeAndWait(
            {},
            ModalityState.any()
        )
    }

    private fun openSingleProjectWithoutResolve(repositoryRoot: Path, disposable: Disposable): Project? {
        ApplicationManager.getApplication().assertIsNonDispatchThread()
        val project: Project = ApplicationManager.getApplication().executeOnPooledThread<Project> {
            val project = openProject(repositoryRoot, disposable)
            project ?: error("Can not open project $repositoryRoot")
        }.get() ?: run {
            logger.error("Project is null")
            return null
        }
        logger.info("Project ${project.name} was open without resolve!")
        return project
    }

    fun openSingleProject(repositoryRoot: Path, action: (Project) -> Boolean): Boolean {
        val disposable = Disposer.newDisposable()
        try {
            val project = openSingleProjectWithoutResolve(repositoryRoot, disposable)
            project ?: return false

            action(project)
            return true
        } finally {
            Disposer.dispose(disposable)
        }
    }

    /**
     * Function to close project. The close should be forced to avoid physical changes to data.
     */
    private fun closeSingleProject(project: Project) =
        try {
            ProjectManagerEx.getInstanceEx().forceCloseProject(project)
        } catch (e: AlreadyDisposedException) {
            // TODO: figure out why this happened
            logger.error("Failed to close project", e)
        }
}
