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
class DeleteFilesPreprocessorTest :
    ParametrizedBaseTest(getResourcesRootPath(::AndroidSdkPreprocessingTest, "../java_mock_projects")) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    private val preprocessor = PreprocessorManager(listOf(DeleteFilesPreprocessor(badFiles)))

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::AndroidSdkPreprocessingTest,
            resourcesRootName = "../java_mock_projects",
            inExtension = Extension.EMPTY,
            outExtension = null
        )

        val badFiles = listOf("bad_directory", "bad_file.txt")
    }

    /**
     * Asserts that there are no directories named [badFiles] in any part of the [directory] file tree.
     */
    private fun assertAllBadFilesDeleted(directory: File) {
        val badFile = directory.listFiles()?.find { it.name in badFiles }
        if (badFile != null) {
            throw AssertionError("Directory ${directory.path} has a bad file ${badFile.name}")
        } else {
            directory.subdirectories.forEach {
                assertAllBadFilesDeleted(it)
            }
        }
    }

    @Test
    fun `should delete bad files`() {
        val tempOutputDirectory = Files.createTempDirectory("preprocessed").toFile()
        preprocessor.preprocessRepository(inFolder!!, tempOutputDirectory)
        assertAllBadFilesDeleted(tempOutputDirectory)
    }
}
