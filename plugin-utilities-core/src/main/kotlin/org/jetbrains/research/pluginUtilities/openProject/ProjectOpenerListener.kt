package org.jetbrains.research.pluginUtilities.openProject

import com.intellij.conversion.ConversionListener
import com.intellij.ide.CommandLineInspectionProgressReporter
import org.slf4j.LoggerFactory
import java.nio.file.Path

object ProjectOpenerListener : ConversionListener, CommandLineInspectionProgressReporter {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun reportError(message: String) {
        logger.warn("PROGRESS: $message")
    }

    override fun reportMessage(minVerboseLevel: Int, message: String) {
        logger.info("PROGRESS: $message")
    }

    override fun error(message: String) {
        logger.warn("PROGRESS: $message")
    }

    override fun conversionNeeded() {
        logger.info("PROGRESS: Project conversion is needed")
    }

    override fun successfullyConverted(backupDir: Path) {
        logger.info("PROGRESS: Project was successfully converted")
    }

    override fun cannotWriteToFiles(readonlyFiles: List<Path>) {
        logger.info("PROGRESS: Project conversion failed for:\n" + readonlyFiles.joinToString("\n"))
    }
}
