package org.mm0.kt

fun String.fails(write: TestWriter.() -> Unit) = fails({ write() }, { write() })
fun String.pass(write: TestWriter.() -> Unit) = pass({ write() }, { write() })

fun String.pass(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
    MM0TestFileWriter("$passFolder/$this.mm0", true).use { it.mm0() }
    MMUTestFileWriter("$passFolder/$this.mmu", true).use { it.mmu() }
}
fun String.fails(mm0: MM0TestFileWriter.() -> Unit, mmu: MMUTestFileWriter.() -> Unit) {
    MM0TestFileWriter("$failFolder/$this.mm0", false).use { it.mm0() }
    MMUTestFileWriter("$failFolder/$this.mmu", false).use { it.mmu() }
}

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

