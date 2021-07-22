package org.jetbrains.research.pluginUtilities.preprocessing

import java.io.File

/**
 * Object that preprocesses a given repository by mutating its file tree.
 * For example, it can add new files to the repository, remove or change the existing ones.
 * @property name The name of the preprocessor that is used for debugging and logging
 */
interface Preprocessor {
    val name: String
    fun preprocess(repoDirectory: File)
}
