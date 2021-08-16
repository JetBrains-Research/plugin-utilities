package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest.Companion.getResourcesRootPath

val JAVA_MOCK_PROJECTS_PATH = System.getenv("JAVA_MOCK_PROJECTS")
    ?: getResourcesRootPath(::AndroidSdkPreprocessingTest)
