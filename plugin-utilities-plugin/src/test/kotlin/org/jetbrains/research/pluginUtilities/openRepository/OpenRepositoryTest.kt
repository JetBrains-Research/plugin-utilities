package org.jetbrains.research.pluginUtilities.openRepository

import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.preprocessing.AndroidSdkPreprocessing
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessing
import org.jetbrains.research.pluginUtilities.preprocessing.Preprocessor
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

private const val ANDROID_SDK_PATH = "/Users/Egor.Porsev/android_sdk/sdk"

@RunWith(Parameterized::class)
class OpenRepositoryTest :
    ParametrizedBaseTest(getResourcesRootPath(::OpenRepositoryTest, "../java_mock_projects")) {

    private val buildSystems = listOf(BuildSystem.Maven, BuildSystem.Gradle)
    private val preprocessing = listOf<Preprocessing>()

    private val preprocessor = Preprocessor(preprocessing)
    private val repositoryOpener = RepositoryOpener(preprocessor, buildSystems)

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::OpenRepositoryTest,
            resourcesRootName = "../java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = null
        ).drop(3)
    }

    @Test
    fun `should open repository successfully`() {
        repositoryOpener.assertRepositoryOpens(inFolder!!)
    }
}
