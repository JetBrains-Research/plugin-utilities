package org.jetbrains.research.ml.pluginUtilities

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
import java.io.File

inline fun openRepository(repoDirectory: File, acceptedBuildSystems: List<BuildSystem>, action: (Project) -> Unit) {
    val projectRoots = repoDirectory.collectBuildSystemRoots(acceptedBuildSystems)
    for (projectRoot in projectRoots) {
        val project = try {
            openSingleProject(projectRoot)
        } catch (e: Exception) {
            println("Failed to open project $projectRoot: $e")
            continue
        }
        action(project)
        closeSingleProject(project)
    }
}

fun openSingleProject(projectRoot: File): Project {
    println("Opening project ${projectRoot.name}")
    var resultProject: Project? = null

    ApplicationManager.getApplication().invokeAndWait {
        val project = ProjectUtil.openOrImport(projectRoot.toPath())

        if (MavenProjectsManager.getInstance(project).isMavenizedProject) {
            println("It is a Maven project")
            MavenProjectsManager.getInstance(project).scheduleImportAndResolve()
            MavenProjectsManager.getInstance(project).importProjects()
        } else {
            println("It is a Gradle project")
            ExternalSystemUtil.refreshProject(
                projectRoot.path,
                ImportSpecBuilder(project, GradleConstants.SYSTEM_ID)
            )
        }
        resultProject = project
    }

    return resultProject?.also { println("Project ${it.name} opened") } ?: error("Project was null for some unknown reason")
}

/**
 * Function to close project. The close should be forced to avoid physical changes to data.
 */
fun closeSingleProject(project: Project) =
    try {
        ProjectManagerEx.getInstanceEx().forceCloseProject(project)
    } catch (e: AlreadyDisposedException) {
        // TODO: figure out why this happened
        println(e.message)
    }