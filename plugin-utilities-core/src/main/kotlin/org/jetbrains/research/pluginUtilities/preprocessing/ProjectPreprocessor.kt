package org.jetbrains.research.pluginUtilities.preprocessing

import com.intellij.openapi.project.Project

interface ProjectPreprocessor {
    fun preprocess(project: Project): Project?
}
