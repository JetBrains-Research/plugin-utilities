package org.jetbrains.research.pluginUtilities.util

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.io.File

enum class Type {
    Input, Output
}

class TestFileFormat(private val prefix: String, private val extension: Extension, val type: Type) {
    data class TestFile(val file: File, val type: Type, val number: Number)

    fun check(file: File): TestFile? {
        val number = "(?<=${prefix}_)\\d+(?=(_.*)?\\.${extension.value})".toRegex().find(file.name)?.value?.toInt()
        return number?.let { TestFile(file, type, number) }
    }

    fun match(testFile: TestFile): Boolean {
        return testFile.type == type
    }
}

object FileTestUtil {

    /**
     * We assume the format of the test files will be:
     *
     * inPrefix_i_anySuffix.inExtension
     * outPrefix_i_anySuffix.outExtension,
     *
     * where:
     * inPrefix and outPrefix are set in [inFormat] and [outFormat] together with extensions,
     * i is a number; two corresponding input and output files should have the same number,
     * suffixes can by any symbols not necessary the same for the corresponding files.
     */
    fun getInAndOutFilesMap(
        folder: String,
        inFormat: TestFileFormat = TestFileFormat("in", Extension.KT, Type.Input),
        outFormat: TestFileFormat? = null
    ): Map<File, File?> {
        val (files, folders) = File(folder).listFiles().orEmpty().partition { it.isFile }

        // Process files in the given folder
        val inAndOutFilesGrouped = files.mapNotNull { inFormat.check(it) ?: outFormat?.check(it) }.groupBy { it.number }
        val inAndOutFilesMap = inAndOutFilesGrouped.map { (number, fileInfoList) ->
            val (f1, f2) = if (outFormat == null) {
                require(fileInfoList.size == 1) { "There are less or more than 1 test files with number $number" }
                Pair(fileInfoList.first(), null)
            } else {
                require(fileInfoList.size == 2) { "There are less or more than 2 test files with number $number" }
                fileInfoList.sortedBy { it.type }.zipWithNext().first()
            }
            require(inFormat.match(f1)) { "The input file does not match the input format" }
            outFormat?.let {
                require(f2 != null && outFormat.match(f2)) { "The output file does not match the output format" }
            }
            f1.file to f2?.file
        }.sortedBy { it.first.name }.toMap()

        outFormat?.let {
            require(inAndOutFilesMap.values.mapNotNull { it }.size == inAndOutFilesMap.values.size) { "Output tests" }
        }

        // Process all other nested files
        return folders.sortedBy { it.name }.map { getInAndOutFilesMap(it.absolutePath, inFormat, outFormat) }
            .fold(inAndOutFilesMap, { a, e -> a.plus(e) })
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
    val files1 = dir1.listFiles()?.sortedBy { it.name } ?: error("$dir1 is not a directory")
    val files2 = dir2.listFiles()?.sortedBy { it.name } ?: error("$dir2 is not a directory")

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
