package org.jetbrains.research.pluginUtilities.openProject

import com.jetbrains.python.inspections.PythonPluginCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.MavenCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.GradleCommandLineProjectConfigurator
import org.jetbrains.research.pluginUtilities.preprocessing.python.PythonProjectPreprocessor
import java.nio.file.Path


fun getKotlinJavaProjectOpener() = ProjectOpener(
    projectPreprocessorProvider = null,
    projectConfiguratorProvider = {
        if (MavenProjectsManager.getInstance(it).isMavenizedProject) {
            MavenCommandLineInspectionProjectConfigurator()
        } else {
            GradleCommandLineProjectConfigurator()
        }
    }
)

fun getPythonProjectOpener(venvRoot: Path? = null) = ProjectOpener(
    projectPreprocessorProvider = { PythonProjectPreprocessor(venvRoot) },
    projectConfiguratorProvider = { PythonPluginCommandLineInspectionProjectConfigurator() }
)
