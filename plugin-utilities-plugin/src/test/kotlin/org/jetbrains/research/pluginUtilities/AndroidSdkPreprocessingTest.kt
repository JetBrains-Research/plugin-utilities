package org.jetbrains.research.pluginUtilities

import org.jetbrains.research.pluginUtilities.util.assertDirectoriesEqual
import org.junit.Test
import java.io.File
import java.nio.file.Files

const val ANDROID_SDK_PATH = "path/to/my/android/sdk"

internal class AndroidSdkPreprocessingTest {
    val preprocessor = Preprocessor(listOf(AndroidSdkPreprocessing(ANDROID_SDK_PATH)))

    private fun runPreprocessing(projectName: String) {
        val resources = File("src/test/resources/org/jetbrains/research/pluginUtilities/util/java_mock_projects")
        val repoDirectory = resources.resolve(projectName)
        val expectedOutputDirectory = resources.resolve("preprocessed_$projectName")
        assertCorrectPreprocessing(repoDirectory, expectedOutputDirectory)
    }

    private fun assertCorrectPreprocessing(repoDirectory: File, expectedOutputDirectory: File) {
        val tempOutputDirectory = Files.createTempDirectory("preprocessed").toFile()
        preprocessor.preprocess(repoDirectory, tempOutputDirectory)
        assertDirectoriesEqual(tempOutputDirectory, expectedOutputDirectory)
    }

    @Test
    fun `should add local properties to root of SimpleAndroid`() {
        runPreprocessing("SimpleAndroid")
    }

    @Test
    fun `should add local properties to subfolder of EmptyRootSimpleAndroid`() {
        runPreprocessing("EmptyRootSimpleAndroid")
    }
}
