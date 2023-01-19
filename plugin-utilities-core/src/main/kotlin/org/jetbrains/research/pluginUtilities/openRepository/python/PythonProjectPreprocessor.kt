package org.jetbrains.research.pluginUtilities.openRepository.python

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.configuration.PyProjectVirtualEnvConfiguration
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.research.pluginUtilities.openRepository.ProjectPreprocessor
import java.io.BufferedReader
import java.io.InputStreamReader


class PythonProjectPreprocessor(private val venvRoot: String) : ProjectPreprocessor {

    override fun preprocess(project: Project) {
        setSdkToProject(project, venvRoot)
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

    private fun createVirtualEnvSdk(project: Project, baseSdk: Sdk, venvRoot: String): Sdk {
        var sdk: Sdk? = null
        ApplicationManager.getApplication().invokeAndWait {
            sdk = PyProjectVirtualEnvConfiguration.createVirtualEnvSynchronously(
                baseSdk = baseSdk,
                existingSdks = listOf(baseSdk),
                venvRoot = venvRoot,
                projectBasePath = project.basePath,
                project = project,
                module = null
            )
        }

        return sdk ?: error("Internal error: SDK for ${project.name} project was not created")
    }

    @Throws(IllegalStateException::class)
    private fun setSdkToProject(project: Project, venvRoot: String) {
        val baseSdk = createBaseSdk(project)
        val sdk = createVirtualEnvSdk(project, baseSdk, venvRoot)

        val projectManager = ProjectRootManager.getInstance(project)
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }
}
