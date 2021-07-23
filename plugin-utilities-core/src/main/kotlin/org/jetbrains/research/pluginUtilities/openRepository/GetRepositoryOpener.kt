package org.jetbrains.research.pluginUtilities.openRepository

import org.jetbrains.research.pluginUtilities.BuildSystem

fun getKotlinJavaRepositoryOpener() = RepositoryOpener(
    listOf(
        BuildSystem.Maven,
        BuildSystem.Gradle,
        BuildSystem.GradleKotlinDsl
    )
)
