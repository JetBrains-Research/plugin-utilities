package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.serviceContainer.AlreadyDisposedException
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

/**
 * Preprocesses repositories and opens projects in them.
 * Repositories are preprocessed with [preprocessor]
 * Each repository may contain several projects, it locates them with [collectBuildSystemRoots] and [acceptedBuildSystems]
 */
class RepositoryOpener(private val preprocessor: Preprocessor, private val acceptedBuildSystems: List<BuildSystem>) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Preprocesses repository and opens all projects inside of it.
     * @param repoDirectory root of the repository
     * @param action the function that is called for each opened project
     * @returns true if and only if all projects were opened successfully.
     */
    fun openRepository(repoDirectory: File, action: (Project) -> Unit): Boolean {
        logger.info("Opening repository $repoDirectory")
        val projectRoots = preprocessRepository(repoDirectory).collectBuildSystemRoots(acceptedBuildSystems)
        logger.info("Found project roots $projectRoots")

        var allProjectsOpenedSuccessfully = true

        for (projectRoot in projectRoots) {
            val project = try {
                openSingleProject(projectRoot)
            } catch (e: Exception) {
                logger.error("Failed to open project ${projectRoot.path}", e)
                allProjectsOpenedSuccessfully = false
                continue
            }
            action(project)
            closeSingleProject(project)
        }
        return allProjectsOpenedSuccessfully
    }

    private fun preprocessRepository(repoDirectory: File): File {
        // The .resolve(..) part is needed so the temp directory name matches the name of the repoDirectory
        // For the purpose of being more user-friendly
        val tempDirectory = Files.createTempDirectory("preprocessed_before_open").toFile().resolve(repoDirectory.name)
        tempDirectory.mkdir()
        preprocessor.preprocess(repoDirectory, tempDirectory)
        return tempDirectory
    }

    // TODO: refactor this mess
    private fun openSingleProject(projectRoot: File): Project {
        logger.info("Opening project ${projectRoot.name}")
        var resultProject: Project? = null

        ApplicationManager.getApplication().invokeAndWait {
            val project = ProjectUtil.openOrImport(projectRoot.toPath())

            if (MavenProjectsManager.getInstance(project).isMavenizedProject) {
                logger.info("It is a Maven project")
                MavenProjectsManager.getInstance(project).scheduleImportAndResolve()
                MavenProjectsManager.getInstance(project).importProjects()
            } else {
                logger.info("It is a Gradle project")
                ExternalSystemUtil.refreshProject(
                    projectRoot.path,
                    ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
                )
            }
            resultProject = project
        }

        return resultProject?.also { logger.info("Project ${it.name} opened") } ?: error("Project was null for some unknown reason")
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
