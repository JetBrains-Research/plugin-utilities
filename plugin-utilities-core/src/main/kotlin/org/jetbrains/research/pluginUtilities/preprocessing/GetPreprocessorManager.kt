package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.preprocessing.android.AndroidSdkPreprocessor
import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor

const val IDEA_FOLDER_NAME = ".idea"

/**
 * Creates a PreprocessorManager for Kotlin and Java projects.
 * Combines [AndroidSdkPreprocessor] and [DeleteFilesPreprocessor],
 * adding `local.properties` files and deleting `.idea` folders.
 * @param androidSdkHome Absolute path to Android SDK home.
 * If it is null then [AndroidSdkPreprocessor] will not be used
 */
fun getKotlinJavaPreprocessorManager(androidSdkHome: String?) = PreprocessorManager(
    listOfNotNull(
        androidSdkHome?.let { AndroidSdkPreprocessor(it) },
        DeleteFilesPreprocessor(listOf(IDEA_FOLDER_NAME))
    )
)
