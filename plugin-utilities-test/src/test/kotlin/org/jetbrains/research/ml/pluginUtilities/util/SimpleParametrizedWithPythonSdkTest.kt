package org.jetbrains.research.ml.pluginUtilities.util

import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyBuiltinCache
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SimpleParametrizedWithPythonSdkTest :
    ParametrizedBaseWithPythonSdkTest(getResourcesRootPath(::SimpleParametrizedTestTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::SimpleParametrizedTestTest,
            inExtension = Extension.PY,
            outExtension = Extension.PY
        )
    }

    @Test
    fun simpleTest() {
        val inPsiFile = getPsiFile(inFile!!, myFixture) as PyFile
        val outPsiFile = getPsiFile(outFile!!, myFixture) as PyFile
        Assert.assertEquals(inPsiFile.text, outPsiFile.text)
    }

    @Test
    fun checkSDKTest() {
        val inPsiFile = getPsiFile(inFile!!, myFixture) as PyFile
        val builtinsCache = PyBuiltinCache.getInstance(inPsiFile)
        builtinsCache.boolType ?: error("Python SDK was not configured in the tests")
    }
}
