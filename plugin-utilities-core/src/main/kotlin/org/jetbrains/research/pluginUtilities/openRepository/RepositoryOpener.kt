package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.conversion.ConversionListener
import com.intellij.ide.CommandLineInspectionProgressReporter
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.serviceContainer.AlreadyDisposedException
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.GradleCommandLineProjectConfigurator
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.function.Predicate

/**
 * Locates projects in repositories and opens them.
 * Each repository may contain several projects, it locates them with [collectBuildSystemRoots] and [acceptedBuildSystems]
 */
class RepositoryOpener(private val acceptedBuildSystems: List<BuildSystem>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Opens all projects inside of the repository.
     * @param repoDirectory root of the repository
     * @param action the function that is called for each opened project
     * @returns true if and only if all projects were opened successfully.
     */
    fun openRepository(repoDirectory: File, action: (Project) -> Unit): Boolean {
        logger.info("Opening repository $repoDirectory")
        val buildSystemRoots = repoDirectory.collectBuildSystemRoots(acceptedBuildSystems)
        val projectRoots = buildSystemRoots.ifEmpty {
            logger.info("No folders with build systems found! Opening the root of the repository")
            listOf(repoDirectory)
        }
        logger.info("Opening project roots: $projectRoots")

        var allProjectsOpenedSuccessfully = true

        for (projectRoot in projectRoots) {
            val project = try {
                openSingleProject(projectRoot)
            } catch (e: Exception) {
                logger.error("Failed to open project ${projectRoot.path}", e)
                allProjectsOpenedSuccessfully = false
                continue
            }
            try {
                action(project)
            } finally {
                closeSingleProject(project)
            }
        }
        return allProjectsOpenedSuccessfully
    }

    private val listener = object : ConversionListener, CommandLineInspectionProgressReporter {
        override fun reportError(message: String) {
            logger.warn("PROGRESS: $message")
        }

        override fun reportMessage(minVerboseLevel: Int, message: String) {
            logger.info("PROGRESS: $message")
        }

        override fun error(message: String) {
            logger.warn("PROGRESS: $message")
        }

        override fun conversionNeeded() {
            logger.info("PROGRESS: Project conversion is needed")
        }

        override fun successfullyConverted(backupDir: Path) {
            logger.info("PROGRESS: Project was successfully converted")
        }

        override fun cannotWriteToFiles(readonlyFiles: List<Path>) {
            logger.info("PROGRESS: Project conversion failed for:\n" + readonlyFiles.joinToString("\n"))
        }
    }

    private fun openSingleProject(projectRoot: File): Project {
        logger.info("Opening project ${projectRoot.name}")
        var resultProject: Project? = null

        try {
            ApplicationManager.getApplication().invokeAndWait {
                val project = ProjectManagerEx.getInstanceEx().openProject(
                    Path.of(projectRoot.path),
                    OpenProjectTask(isNewProject = true, runConfigurators = true, forceOpenInNewFrame = true)
                ) ?: throw ProjectOpeningException(
                    "`openProject` returned null"
                )
                resultProject = project
            }
        } catch (e: ProcessCanceledException) {
            throw ProjectOpeningException("Process was canceled", e)
        }

        require(resultProject != null) { "Project was null for an unknown reason." }
        logger.info("Project ${resultProject!!.name} opened")

        val future = ApplicationManager.getApplication().executeOnPooledThread {
            if (MavenProjectsManager.getInstance(resultProject!!).isMavenizedProject) {
                logger.info("IDEA detected Maven build system")
                MavenProjectsManager.getInstance(resultProject!!).scheduleImportAndResolve()
                MavenProjectsManager.getInstance(resultProject!!).importProjects()
            } else {
                logger.info("IDEA detected Gradle build system")
//                    ExternalSystemUtil.refreshProject(
//                        projectRoot.path,
//                        ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
//                    )
                ApplicationManager.getApplication().assertIsNonDispatchThread()
                val indicator = ProgressIndicatorBase()
                val context = object : CommandLineInspectionProjectConfigurator.ConfiguratorContext {
                    override fun getProgressIndicator() = indicator
                    override fun getLogger() = listener
                    override fun getProjectPath() = Path.of(projectRoot.path)
                    override fun getFilesFilter(): Predicate<Path> = Predicate { true }
                    override fun getVirtualFilesFilter(): Predicate<VirtualFile> = Predicate { true }
                }
                GradleCommandLineProjectConfigurator().configureProject(resultProject!!, context)
            }
        }
        future.get()
        return resultProject?.also {
            logger.info("Project ${it.name} opened")
        } ?: throw ProjectOpeningException(
            "Project was null for an unknown reason."
        )
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

class ProjectOpeningException(message: String, cause: Exception?) : Exception(message, cause) {
    constructor(msg: String) : this(msg, null)
}
