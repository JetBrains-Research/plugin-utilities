package org.jetbrains.research.pluginUtilities.openRepository

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import java.io.File

fun RepositoryOpener.assertRepositoryOpens(repositoryDirectory: File) {
    val allProjectsWereOpenedSuccessfully = openRepository(repositoryDirectory) {
        if (!it.hasResolvedDependencies) {
            throw AssertionError("Project ${it.name} has no resolved dependencies")
        }
    }
    if (!allProjectsWereOpenedSuccessfully) {
        throw AssertionError("Some projects in the repository were not opened successfully")
    }
}

val Project.hasResolvedDependencies: Boolean
    get() = countDependencies(this) > 0

private fun countDependencies(project: Project): Int = project.modules.sumOf { countLibraries(it) }

private val Project.modules: Array<Module>
    get() = ModuleManager.getInstance(this).modules

private fun countLibraries(module: Module): Int {
    var nLibraries = 0
    ModuleRootManager.getInstance(module).orderEntries().forEachLibrary {
        nLibraries++
        true
    }
    return nLibraries
}
