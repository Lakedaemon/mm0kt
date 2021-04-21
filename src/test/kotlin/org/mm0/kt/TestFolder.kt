package org.mm0.kt

import java.io.File

sealed class TestFolder(subDir:String, protected val shouldPass:Boolean) {
    class MM0(subDir:String, shouldPass:Boolean) : TestFolder(subDir, shouldPass) {
        fun String.test(write: TestWriter.() -> Unit) {
            val uniqueName = unique(this)
            MM0TestFileWriter("$folder/$uniqueName.mm0").use { it.write() }
        }
    }
    class MMU(subDir:String, shouldPass:Boolean) : TestFolder(subDir, shouldPass) {
        fun String.test(write: TestWriter.() -> Unit) {
            val uniqueName = unique(this)
            MMUTestFileWriter("$folder/$uniqueName.mmu").use { it.write() }
        }
    }
    class Both(subDir:String, shouldPass:Boolean) : TestFolder(subDir, shouldPass) {
        fun String.test(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
            val uniqueName = unique(this)
            MM0TestFileWriter("$folder/$uniqueName.mm0").use { it.mm0() }
            MMUTestFileWriter("$folder/$uniqueName.mmu").use { it.mmu() }
        }
        fun String.test(write: TestWriterBoth.() -> Unit) = test({ write() }, { write() })
    }

    val folder = "${if(shouldPass) passFolder else failFolder}/$subDir"
    private val names = mutableMapOf<String, Int>()
    protected fun unique(name: String): String {
        val int = 1 + names.getOrDefault(name, 0)
        names[name] = int
        return if (int == 1) name else "$name$int"
    }

    init{ File(folder).mkdirs() }
}