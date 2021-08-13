package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.preprocessing.android.AndroidSdkPreprocessor
import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor

fun getKotlinJavaPreprocessorManager(androidSdkAbsolutePath: String) = PreprocessorManager(
    listOf(
        AndroidSdkPreprocessor(androidSdkAbsolutePath),
        DeleteFilesPreprocessor(listOf(".idea"))
    )
)
