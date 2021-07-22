package org.jetbrains.research.pluginUtilities.util.simpleTests.folders

import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SimpleParametrizedFoldersTest : ParametrizedBaseTest(getResourcesRootPath(::SimpleParametrizedFoldersTest)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFolder: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            ::SimpleParametrizedFoldersTest,
            inExtension = Extension.EMPTY,
            outExtension = null
        )
    }

    @Test
    fun simpleTest() {
        val countFiles = inFolder?.listFiles()?.size ?: error("Input folders do not exist")
        Assert.assertEquals(1, countFiles)
    }
}
