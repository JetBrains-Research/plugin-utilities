package org.jetbrains.research.pluginUtilities.util.simpleTests.files

import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SimpleParametrizedFilesTest : ParametrizedBaseTest(getResourcesRootPath(::SimpleParametrizedFilesTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::SimpleParametrizedFilesTest)
    }

    @Test
    fun simpleTest() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        Assert.assertEquals(outPsiFile.text, inPsiFile.text)
    }
}
