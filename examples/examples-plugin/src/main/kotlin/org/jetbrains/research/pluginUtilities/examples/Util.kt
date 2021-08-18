package org.jetbrains.research.pluginUtilities.examples

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

fun getSubdirectories(path: Path): List<Path> {
    return Files.walk(path, 1)
        .filter { Files.isDirectory(it) && !it.equals(path) }
        .toList()
}
