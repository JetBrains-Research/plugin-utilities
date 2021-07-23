package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.collectBuildSystemRoots
import org.jetbrains.research.pluginUtilities.preprocessing.android.AndroidSdkPreprocessor
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Files
import java.util.Properties

private const val ANDROID_SDK_PATH = "path/to/my/android/sdk"

@RunWith(Parameterized::class)
class AndroidSdkPreprocessingTest :
    ParametrizedBaseTest(getResourcesRootPath(::AndroidSdkPreprocessingTest, "../java_mock_projects")) {

    val preprocessor = PreprocessorManager(listOf(AndroidSdkPreprocessor(ANDROID_SDK_PATH)))

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::AndroidSdkPreprocessingTest,
            resourcesRootName = "../java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = null
        )
    }

    /**
     * Checks if all directories with java build systems have a local.properties file
     * with the sdk.dir=[ANDROID_SDK_PATH] set
     */
    private fun assertLocalProperties(repoDirectory: File) {
        repoDirectory.collectBuildSystemRoots(AndroidSdkPreprocessor.acceptedBuildSystems).forEach { projectRoot ->
            val localPropertiesFile = projectRoot.resolve(AndroidSdkPreprocessor.LOCAL_PROPERTIES_FILE_NAME)
            assert(localPropertiesFile.exists()) {
                "${AndroidSdkPreprocessor.LOCAL_PROPERTIES_FILE_NAME} file does not exist"
            }
            val properties = Properties()
            properties.load(localPropertiesFile.inputStream())
            assert(properties[AndroidSdkPreprocessor.SDK_PROPERTY_NAME] == ANDROID_SDK_PATH) {
                "${AndroidSdkPreprocessor.SDK_PROPERTY_NAME} property is not set correctly"
            }
        }
    }

    @Test
    fun `should add local properties with path to sdk`() {
        val tempOutputDirectory = Files.createTempDirectory("preprocessed").toFile()
        preprocessor.preprocessRepository(inFolder!!, tempOutputDirectory)
        assertLocalProperties(tempOutputDirectory)
    }
}
