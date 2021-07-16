package org.jetbrains.research.pluginUtilities.util

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.io.File

enum class Type {
    Input, Output
}

class TestDataFormat(private val prefix: String, private val extension: Extension, val type: Type) {
    data class TestFileOrFolder(val fileOrFolder: File, val type: Type, val number: Number)

    fun check(fileOrFolder: File): TestFileOrFolder? {
        val regex = if (extension == Extension.EMPTY) {
            "(?<=${prefix}_)\\d+(?=(_.*)?)".toRegex()
        } else {
            "(?<=${prefix}_)\\d+(?=(_.*)?\\.${extension.value})".toRegex()
        }
        val number = regex.find(fileOrFolder.name)?.value?.toInt()
        return number?.let { TestFileOrFolder(fileOrFolder, type, number) }
    }

    fun match(testFileOrFolder: TestFileOrFolder): Boolean {
        return testFileOrFolder.type == type
    }
}

object FileTestUtil {

    /**
     * We assume the format of the test files will be:
     *
     * inPrefix_i_anySuffix[.inExtension]?
     * outPrefix_i_anySuffix[.outExtension?,
     *
     * where:
     * inPrefix and outPrefix are set in [inFormat] and [outFormat] together with extensions,
     * i is a number; two corresponding input and output files should have the same number,
     * suffixes can by any symbols not necessary the same for the corresponding files,
     * extensions listed in [Extension] are optional(empty) only for directories.
     */
    fun getInAndOutDataMap(
        folder: String,
        inFormat: TestDataFormat = TestDataFormat("in", Extension.KT, Type.Input),
        outFormat: TestDataFormat? = null
    ): Map<File, File?> {
        val filesOrFolders = File(folder).listFiles().orEmpty()
        // Partition files ot folders in the given folder: which match in/out format and which will be processed recursively
        val inAndOutFormats = mutableListOf<TestDataFormat.TestFileOrFolder>()
        val toProcessFiles = mutableListOf<File>()

        filesOrFolders.forEach { fileOrFolder ->
            val dataFormat = inFormat.check(fileOrFolder) ?: outFormat?.check(fileOrFolder)
            dataFormat?.let { inAndOutFormats.add(it) } ?: toProcessFiles.add(fileOrFolder)
        }

        val inAndOutFilesMap = inAndOutFormats
            .groupBy { it.number }
            .map { (number, fileOrFolderInfoList) ->
                val (f1, f2) = if (outFormat == null) {
                    require(fileOrFolderInfoList.size == 1) { "There are less or more than 1 test files or folders with number $number" }
                    Pair(fileOrFolderInfoList.first(), null)
                } else {
                    require(fileOrFolderInfoList.size == 2) { "There are less or more than 2 test files or folders with number $number" }
                    fileOrFolderInfoList.sortedBy { it.type }.zipWithNext().first()
                }
                require(inFormat.match(f1)) { "The input file does not match the input format" }
                outFormat?.let {
                    require(f2 != null && outFormat.match(f2)) { "The output file or folder does not match the output format" }
                }
                f1.fileOrFolder to f2?.fileOrFolder
            }.sortedBy { it.first.name }.toMap()

        outFormat?.let {
            require(inAndOutFilesMap.values.mapNotNull { it }.size == inAndOutFilesMap.values.size) { "Output tests" }
        }

        return toProcessFiles.filter { it.isDirectory }.sortedBy { it.name }
            .map { getInAndOutDataMap(it.absolutePath, inFormat, outFormat) }
            .fold(inAndOutFilesMap) { a, e -> a.plus(e) }
    }
}

fun getPsiFile(file: File, fixture: CodeInsightTestFixture): PsiFile {
    return fixture.configureByFile(file.path)
}

/**
 * Checks if 2 directories have the same contents
 * @throws AssertionError if directories do not have the same contents
 */
fun assertDirectoriesEqual(dir1: File, dir2: File) {
    val files1 = dir1.listFiles()?.sorted() ?: error("$dir1 is not a directory")
    val files2 = dir2.listFiles()?.sorted() ?: error("$dir2 is not a directory")

    for ((file1, file2) in files1.zip(files2)) {
        if (file1.isFile && file2.isFile) {
            assertFilesEqual(file1, file2)
        } else if (file1.isDirectory && file2.isDirectory) {
            assertDirectoriesEqual(file1, file2)
        } else {
            throw AssertionError("$file1 and $file2 are files of different types")
        }
    }
}

/**
 * Checks if 2 regular files have equal contents
 * @throws AssertionError if given regular files do not have equal contents
 */
fun assertFilesEqual(file1: File, file2: File) {
    if (!file1.readBytes().contentEquals(file2.readBytes())) {
        throw AssertionError("Files $file1 and $file2 do not have equal contents")
    }
}
