package org.jetbrains.research.pluginUtilities.preprocessing.python

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.io.exists
import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.configuration.PyProjectVirtualEnvConfiguration
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.research.pluginUtilities.preprocessing.ProjectPreprocessor
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths


class PythonProjectPreprocessor(private val venvRoot: Path? = null) : ProjectPreprocessor {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PYTHON_VENV_FOLDER_NAME = ".venv"
    }

    /**
     * Setting up a virtual environment.
     *
     * If the [path to the virtual environment][venvRoot] is defined,
     * then try to set up it, otherwise try to find a local virtual environment
     * in the root of the [project] in the ".venv" folder and set up it.
     */
    override fun preprocess(project: Project): Project? {
        val localVenv = project.basePath?.let { Paths.get(it, PYTHON_VENV_FOLDER_NAME) }

        logger.info("Trying to use a global venv.")
        if (tryToSetupVenv(project, venvRoot)) {
            logger.info("The analysis will run with the global venv ($venvRoot).")
            return project
        }

        logger.info("Trying to use a local venv.")
        if (tryToSetupVenv(project, localVenv)) {
            logger.info("The analysis will run with the local venv ($localVenv).")
            return project
        }

        logger.warn("The analysis will run without the SDK.")
        return null
    }

    /**
     * Trying to set up a virtual environment.
     *
     * If the [virtual environment path][venvPath] exists,
     * set up the virtual environment and return true,
     * otherwise log a warning and return false.
     */
    private fun tryToSetupVenv(project: Project, venvPath: Path?): Boolean {
        if (venvPath != null && venvPath.exists()) {
            ApplicationManager.getApplication().invokeAndWait {
                setSdkToProject(project, venvPath)
            }
            return true
        }

        logger.warn("The path to venv was not found or does not exist.")
        return false
    }

    @Throws(IllegalStateException::class)
    private fun setSdkToProject(project: Project, venvRoot: Path) {
        val baseSdk = createBaseSdk(project)
        val sdk = createVirtualEnvSdk(project, baseSdk, venvRoot)

        val projectManager = ProjectRootManager.getInstance(project)
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }

    private fun createBaseSdk(project: Project): Sdk {
        val myInterpreterList = PyConfigurableInterpreterList.getInstance(project)
        val myProjectSdksModel = myInterpreterList.model
        val pySdkType = PythonSdkType.getInstance()

        return myProjectSdksModel.createSdk(pySdkType, getPythonPath())
    }

    private fun getPythonPath(): String {
        val python = "python3"
        val pythonBin = if (SystemUtils.IS_OS_WINDOWS) listOf("where", python) else listOf("which", python)
        val builder = ProcessBuilder(pythonBin)
        builder.redirectErrorStream(true)
        val p = builder.start()

        return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n")
    }

    private fun createVirtualEnvSdk(project: Project, baseSdk: Sdk, venvRoot: Path): Sdk {
        var sdk: Sdk? = null
        ApplicationManager.getApplication().invokeAndWait {
            sdk = PyProjectVirtualEnvConfiguration.createVirtualEnvSynchronously(
                baseSdk = baseSdk,
                existingSdks = listOf(baseSdk),
                venvRoot = venvRoot.toString(),
                projectBasePath = project.basePath,
                project = project,
                module = null
            )
        }

        return sdk ?: error("Internal error: SDK for ${project.name} project was not created")
    }
}
