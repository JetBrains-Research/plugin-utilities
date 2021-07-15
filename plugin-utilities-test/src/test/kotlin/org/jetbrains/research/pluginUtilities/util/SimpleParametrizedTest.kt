package org.jetbrains.research.pluginUtilities.util

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SimpleParametrizedTestTest : ParametrizedBaseTest(getResourcesRootPath(::SimpleParametrizedTestTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(::SimpleParametrizedTestTest)
    }

    @Test
    fun simpleTest() {
        val inPsiFile = getPsiFile(inFile!!, myFixture)
        val outPsiFile = getPsiFile(outFile!!, myFixture)
        Assert.assertEquals(inPsiFile.text, outPsiFile.text)
    }
}
