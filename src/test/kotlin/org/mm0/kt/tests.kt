package org.mm0.kt


fun failMM0(subdir:String, action:TestFolder.MM0.()->Unit) = TestFolder.MM0(subdir, false).action()
fun passMM0(subdir:String, action:TestFolder.MM0.()->Unit) = TestFolder.MM0(subdir, true).action()

fun failMMU(subdir:String, action:TestFolder.MMU.()->Unit) = TestFolder.MMU(subdir, false).action()
fun passMMU(subdir:String, action:TestFolder.MMU.()->Unit) = TestFolder.MMU(subdir, true).action()

fun failBoth(subdir:String, action:TestFolder.Both.()->Unit) = TestFolder.Both(subdir, false).action()
fun passBoth(subdir:String, action:TestFolder.Both.()->Unit) = TestFolder.Both(subdir, true).action()

const val failFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/fail"
const val passFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/pass"

fun Int.toPositive() = if (this < 0) -this else this

internal fun String.toBinder(): Binder = with(StringConsumable(this)) {
    consume()
    val id = consumeId(false)?.toString() ?: error("bad id [${this@toBinder}]")
    consume()
    val sort = consumeId(false)?.toString() ?: error("bad sort [${this@toBinder}]")
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
    val sort = consumeId(false)?.toString() ?: error("bad sort [${this@toType}]")
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