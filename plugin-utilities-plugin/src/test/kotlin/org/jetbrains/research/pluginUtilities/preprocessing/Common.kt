package org.jetbrains.research.pluginUtilities.preprocessing

val JAVA_MOCK_PROJECTS_PATH = System.getenv("JAVA_MOCK_PROJECTS")
    ?: error("JAVA_MOCK_PROJECTS environment variable is not set. It must point to a folder with java mock projects")
