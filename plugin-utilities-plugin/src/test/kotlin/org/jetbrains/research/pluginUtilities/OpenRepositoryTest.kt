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

private const val ANDROID_SDK_PATH = "/Users/Egor.Porsev/android_sdk/sdk"

@RunWith(Parameterized::class)
class OpenRepositoryTest :
    ParametrizedBaseTest(getResourcesRootPath(::OpenRepositoryTest, "java_mock_projects")) {

    private val preprocessor = Preprocessor(listOf(AndroidSdkPreprocessing(ANDROID_SDK_PATH)))
    private val repositoryOpener = RepositoryOpener(preprocessor, javaBuildSystems)

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::OpenRepositoryTest,
            resourcesRootName = "java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = null
        )
    }

    @Test
    fun `should open repository successfully`() {
        repositoryOpener.assertRepositoryOpens(inFolder!!)
    }
}
