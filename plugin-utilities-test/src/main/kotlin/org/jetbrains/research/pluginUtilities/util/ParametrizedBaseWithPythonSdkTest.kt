package org.jetbrains.research.pluginUtilities.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.junit.Ignore

/*
 * Base class for parameterized tests with Python SDK.
 * Sometimes we need to use some information that is available only if SDK connected,
 * for example, some information from PyBuiltinCache.
 */
@Ignore
open class ParametrizedBaseWithPythonSdkTest(testDataRoot: String) : ParametrizedBaseTest(testDataRoot) {
    private lateinit var sdk: Sdk

    override fun setUp() {
        super.setUp()
        setupSdk()
    }

    override fun tearDown() {
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
        super.tearDown()
    }

    private fun setupSdk() {
        val project = myFixture.project
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(testDataPath).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }
}
