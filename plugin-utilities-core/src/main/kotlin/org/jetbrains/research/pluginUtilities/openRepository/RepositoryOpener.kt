package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.serviceContainer.AlreadyDisposedException
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.slf4j.LoggerFactory
import java.io.File

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

    private fun openSingleProject(projectRoot: File): Project {
        logger.info("Opening project ${projectRoot.name}")
        var resultProject: Project? = null

        try {
            ApplicationManager.getApplication().invokeAndWait {
                val project = ProjectUtil.openOrImport(projectRoot.toPath())

                if (MavenProjectsManager.getInstance(project).isMavenizedProject) {
                    logger.info("IDEA detected Maven build system")
                    MavenProjectsManager.getInstance(project).scheduleImportAndResolve()
                    MavenProjectsManager.getInstance(project).importProjects()
                } else {
                    logger.info("IDEA detected Gradle build system")
                    ExternalSystemUtil.refreshProject(
                        projectRoot.path,
                        ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
                    )
                }
                resultProject = project
            }
        } catch (e: ProcessCanceledException) {
            throw ProjectOpeningException("Process was canceled", e)
        }

        return resultProject?.also {
            logger.info("Project ${it.name} opened")
        } ?: throw ProjectOpeningException(
            "Project was null for an unknown reason. " +
                "`openOrImport` may have returned null"
        )
    }

    /**
     * Function to close project. The close should be forced to avoid physical changes to data.
     */
    private fun closeSingleProject(project: Project) = ApplicationManager.getApplication().invokeAndWait {
        try {
            ProjectManagerEx.getInstanceEx().forceCloseProject(project)
        } catch (e: Exception) {
            // TODO: figure out why this happened
            logger.error("Failed to close project", e)
        }
    }
}

class ProjectOpeningException(message: String, cause: Exception?) : Exception(message, cause) {
    constructor(msg: String) : this(msg, null)
}
