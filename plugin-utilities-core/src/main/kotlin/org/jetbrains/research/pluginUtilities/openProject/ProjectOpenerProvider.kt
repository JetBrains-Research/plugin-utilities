package org.jetbrains.research.pluginUtilities.openProject

import com.jetbrains.python.inspections.PythonPluginCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.MavenCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.GradleCommandLineProjectConfigurator
import org.jetbrains.research.pluginUtilities.preprocessing.python.PythonProjectPreprocessor
import org.slf4j.LoggerFactory
import java.nio.file.Path

object ProjectOpenerProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getKotlinJavaProjectOpener() = ProjectOpener(
        projectPreprocessorProvider = null,
        projectConfiguratorProvider = {
            if (MavenProjectsManager.getInstance(it).isMavenizedProject) {
                logger.info("IDEA detected Maven build system")
                MavenCommandLineInspectionProjectConfigurator()
            } else {
                logger.info("IDEA detected Gradle build system")
                GradleCommandLineProjectConfigurator()
            }
        }
    )

    fun getPythonProjectOpener(venvRoot: Path? = null) = ProjectOpener(
        projectPreprocessorProvider = { PythonProjectPreprocessor(venvRoot) },
        projectConfiguratorProvider = { PythonPluginCommandLineInspectionProjectConfigurator() }
    )

}
