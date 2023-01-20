package org.jetbrains.research.pluginUtilities.openProject

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Disposer
import java.nio.file.Path


fun ProjectOpener.openAndApply(
    projectRoot: Path,
    resolve: Boolean = false,
    action: (Project) -> Boolean,
): Boolean {
    val disposable = Disposer.newDisposable()
    try {
        val project = this.open(projectRoot, disposable, resolve)
        project?.let {
            action(project)
            return true
        } ?: return false
    } finally {
        Disposer.dispose(disposable)
    }
}

fun ProjectOpener.assertProjectOpensWithResolve(projectRoot: Path) {
    this.openAndApply(projectRoot, true) {
        if (!it.hasResolvedDependencies) {
            throw AssertionError("Project ${it.name} has no resolved dependencies")
        }
        true
    }
}

private val Project.hasResolvedDependencies: Boolean
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
