package org.jetbrains.research.pluginUtilities.util

import java.io.File
import java.nio.file.Files
import kotlin.streams.asSequence

enum class Extension(val value: String) {
    KT("kt"),
    KTS("kts"),
    PY("py"),
    TXT("txt"),
    EMPTY("")
}

// .asSequence() needed because without it Kotlin compiler caused a java.lang.NoSuchMethodError for me -_-
val File.subdirectories: List<File>
    get() = Files.walk(this.toPath(), 1).filter { Files.isDirectory(it) && it != this.toPath() }.map { it.toFile() }
        .asSequence()
        .toList()
