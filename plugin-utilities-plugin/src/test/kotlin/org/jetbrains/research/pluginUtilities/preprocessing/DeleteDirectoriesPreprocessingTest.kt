package org.jetbrains.research.pluginUtilities.preprocessing

import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.subdirectories
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Files

@RunWith(Parameterized::class)
class DeleteDirectoriesPreprocessingTest :
    ParametrizedBaseTest(getResourcesRootPath(::AndroidSdkPreprocessingTest, "../java_mock_projects")) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    private val preprocessor = PreprocessorManager(listOf(DeleteFilesPreprocessor(badDirectories)))

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::AndroidSdkPreprocessingTest,
            resourcesRootName = "../java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = null
        )

        val badDirectories = listOf("bad_directory")
    }

    /**
     * Asserts that there are no directories named [badDirectories] in any part of the [directory] file tree.
     */
    private fun assertAllBadDirectoriesDeleted(directory: File) {
        val badSubdirectory = directory.subdirectories.find { it.name in badDirectories }
        if (badSubdirectory != null) {
            throw AssertionError("Directory ${directory.path} has a bad subdirectory ${badSubdirectory.name}")
        } else {
            directory.subdirectories.forEach {
                assertAllBadDirectoriesDeleted(it)
            }
        }
    }

    @Test
    fun `should add local properties with path to sdk`() {
        val tempOutputDirectory = Files.createTempDirectory("preprocessed").toFile()
        preprocessor.preprocessRepository(inFolder!!, tempOutputDirectory)
        assertAllBadDirectoriesDeleted(tempOutputDirectory)
    }
}
