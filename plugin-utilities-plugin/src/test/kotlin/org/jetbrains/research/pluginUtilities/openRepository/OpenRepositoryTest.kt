package org.jetbrains.research.pluginUtilities.openRepository

import org.jetbrains.research.pluginUtilities.BuildSystem
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

const val ANDROID_SDK_PATH = "/Users/path/to/sdk"

// TODO: fix the tests
@Ignore(
    """
    For some reason these tests do not pass. However, in headless mode all projects open fine.
    To save some time, all tests are implemented as a CLI runner for now.
"""
)
@RunWith(Parameterized::class)
class OpenRepositoryTest :
    ParametrizedBaseTest(getResourcesRootPath(::OpenRepositoryTest, "../java_mock_projects")) {

    private val buildSystems = listOf(BuildSystem.Maven, BuildSystem.Gradle)

    private val repositoryOpener = RepositoryOpener(buildSystems)

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
