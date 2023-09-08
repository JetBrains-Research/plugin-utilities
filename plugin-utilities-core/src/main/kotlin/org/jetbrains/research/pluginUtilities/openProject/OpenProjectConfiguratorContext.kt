package org.jetbrains.research.pluginUtilities.openProject

import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import java.util.function.Predicate

class OpenProjectConfiguratorContext(
    private val projectRoot: Path,
    private val indicator: ProgressIndicatorBase = ProgressIndicatorBase(),
    private val filesFilter: Predicate<Path> = Predicate { true },
    private val virtualFilesFilter: Predicate<VirtualFile> = Predicate { true }
) : CommandLineInspectionProjectConfigurator.ConfiguratorContext {
    override fun getProgressIndicator() = indicator
    override fun getLogger() = ProjectOpenerListener
    override fun getProjectPath() = projectRoot
    override fun getFilesFilter(): Predicate<Path> = filesFilter
    override fun getVirtualFilesFilter(): Predicate<VirtualFile> = virtualFilesFilter
}
