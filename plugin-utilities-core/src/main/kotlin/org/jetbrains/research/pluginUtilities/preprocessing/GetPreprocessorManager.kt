package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.preprocessing.android.AndroidSdkPreprocessor
import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor

const val IDEA_FOLDER_NAME = ".idea"

/**
 * Creates a PreprocessorManager for Kotlin and Java projects.
 * Combines [AndroidSdkPreprocessor] and [DeleteFilesPreprocessor],
 * adding `local.properties` files and deleting `.idea` folders.
 * @param androidSdkAbsolutePath Absolute path to Android SDK.
 * If it is null then [AndroidSdkPreprocessor] will not be used
 */
fun getKotlinJavaPreprocessorManager(androidSdkAbsolutePath: String?) = PreprocessorManager(
    listOfNotNull(
        androidSdkAbsolutePath?.let { AndroidSdkPreprocessor(it) },
        DeleteFilesPreprocessor(listOf(IDEA_FOLDER_NAME))
    )
)
