package org.mm0.kt

import java.io.File

//fun String.fails(write: TestWriter.() -> Unit) = fails({ write() }, { write() })
//fun String.pass(write: TestWriter.() -> Unit) = pass({ write() }, { write() })



val names = mutableMapOf<String, Int>()
fun unique(name: String): String {
    val int = 1 + names.getOrDefault(name, 0)
    names[name] = int
    return if (int == 1) name else "$name$int"
}

fun failBoth(subdir:String, action:Folder.()->Unit) = Folder(subdir, false).action()
fun passBoth(subdir:String, action:Folder.()->Unit) = Folder(subdir, true).action()

class Folder(subDir:String, private val shouldPass:Boolean) {
    val folder = "${if(shouldPass) passFolder else failFolder}/$subDir"
    init{ File(folder).mkdirs() }
    fun String.test(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
        val uniqueName = unique(this)
        MM0TestFileWriter("$folder/$uniqueName.mm0", shouldPass).use { it.mm0() }
        MMUTestFileWriter("$folder/$uniqueName.mmu", shouldPass).use { it.mmu() }
    }
    fun String.test(write: TestWriter.() -> Unit) = test({ write() }, { write() })
}

/*fun String.pass(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
    MM0TestFileWriter("$passFolder/${unique(this)}.mm0", true).use { it.mm0() }
    MMUTestFileWriter("$passFolder/${unique(this)}.mmu", true).use { it.mmu() }
}
fun String.fails(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
    val uniqueName = unique(this)
    MM0TestFileWriter("$failFolder/$uniqueName.mm0", false).use { it.mm0() }
    MMUTestFileWriter("$failFolder/$uniqueName.mmu", false).use { it.mmu() }
}*/


const val failFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/fail"
const val passFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/pass"

fun Int.toPositive() = if (this < 0) -this else this

internal fun String.toBinder(): Binder = with(StringConsumable(this)) {
    consume()
    val id = consumeId(false)?.toString() ?: error("bad id [$this@toBinder]")
    consume()
    val sort = consumeId(false)?.toString() ?: error("bad sort [$this@toBinder]")
    consume()
    if (!consumeIf('(')) return Binder(true, id, Type(sort, listOf()))
    consume()
    val dependencies = mutableListOf<String>()
    while (!consumeIf(')')) {
        dependencies.add(consumeId(false).toString() ?: error("bad dependency"))
        consume()
    }
    return Binder(false, id, Type(sort, dependencies))
}

internal fun String.toType(): Type= with(StringConsumable(this)) {
    consume()
    val sort = consumeId(false)?.toString() ?: error("bad sort [$this@toBinder]")
    consume()
    if (!consumeIf('(')) return Type(sort, listOf())
    consume()
    val dependencies = mutableListOf<String>()
    while (!consumeIf(')')) {
        dependencies.add(consumeId(false).toString() ?: error("bad dependency"))
        consume()
    }
    return Type(sort, dependencies)
}

/*private fun StringBuilder.toTreeString(tree: StringTree): StringBuilder {
    if (tree.children.isEmpty()) append(tree.id) else {
        append("( ").append(tree.id).append(" ")
        for (child in tree.children) toTreeString(child)
        append(")")
    }
    return this
}

private fun StringTree.toTreeString(): String = StringBuilder().toTreeString(this).toString()
*/

