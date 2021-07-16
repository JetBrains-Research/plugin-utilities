package org.jetbrains.research.pluginUtilities

import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.assertDirectoriesEqual
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Files

private const val ANDROID_SDK_PATH = "path/to/my/android/sdk"

@RunWith(Parameterized::class)
class AndroidSdkPreprocessingTest :
    ParametrizedBaseTest(getResourcesRootPath(::AndroidSdkPreprocessingTest, "java_mock_projects")) {

    val preprocessor = Preprocessor(listOf(AndroidSdkPreprocessing(ANDROID_SDK_PATH)))

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFolder: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::AndroidSdkPreprocessingTest,
            resourcesRootName = "java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = Extension.EMPTY
        )
    }

    @Test
    fun `should add local properties with path to sdk`() {
        val tempOutputDirectory = Files.createTempDirectory("preprocessed").toFile()
        preprocessor.preprocess(inFolder!!, tempOutputDirectory)
        assertDirectoriesEqual(tempOutputDirectory, outFolder!!)
    }
}
