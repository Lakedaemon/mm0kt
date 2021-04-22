package org.mm0.kt


fun failMM0(subdir: String, action: TestFolder.MM0.() -> Unit) = TestFolder.MM0(subdir, false).action()
fun passMM0(subdir: String, action: TestFolder.MM0.() -> Unit) = TestFolder.MM0(subdir, true).action()

fun failMMU(subdir: String, action: TestFolder.MMU.() -> Unit) = TestFolder.MMU(subdir, false).action()
fun passMMU(subdir: String, action: TestFolder.MMU.() -> Unit) = TestFolder.MMU(subdir, true).action()

fun failBoth(subdir: String, action: TestFolder.Both.() -> Unit) = TestFolder.Both(subdir, false).action()
fun passBoth(subdir: String, action: TestFolder.Both.() -> Unit) = TestFolder.Both(subdir, true).action()

const val failFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/fail"
const val passFolder = "/home/lakedaemon/IdeaProjects/mm0kt/tests/pass"

fun Int.toPositive() = if (this < 0) -this else this


internal fun String.toHumanBinder(): HumanBinder = with(StringConsumable(this)) {
    consume()
    val isBound = consumeIf('{')
    val end = if (isBound) '}' else {
        consumeIf('(')
        ')'
    }
    consume()
    val names = mutableListOf<String>()
    while (!consumeIf(':')) {
        names.add((consumeId(true)?.toString()?:error("error parsing ${this@toHumanBinder}")))
        consume()
    }
    val sort = consumeId(false)?.toString()?:error("error parsing ${this@toHumanBinder}")
    consume()
    val dependencies = mutableListOf<String>()
    while (!consumeIf(end)) {
        dependencies.add(consumeId(false)?.toString()?:error("error parsing ${this@toHumanBinder}r"))
        consume()
    }
    return HumanBinder(isBound, names, Type(sort, dependencies))
}

internal fun String.termArrows(): List<Type> = with(StringConsumable(this)) {
    consume()
    val arrows = mutableListOf<Type>()
    while (!isConsumed()) {
        val sort = consumeId(false)?.toString()?:error("error parsing ${this@termArrows}")
        consume()
        val dependencies = mutableListOf<String>()
        while (!isConsumed() && !consumeIf('>')) {
            dependencies.add(consumeId(false)?.toString()?:error("error parsing ${this@termArrows}"))
            consume()
        }
        arrows.add(Type(sort, dependencies))
        consume()
    }
    return arrows
}

internal fun String.toType2(): Type = with(StringConsumable(this)) {
    consume()
    val sort = consumeId(false)?.toString()?:error("error parsing ${this@toType2}")
    consume()
    val dependencies = mutableListOf<String>()
    while (!isConsumed()) {
        dependencies.add(consumeId(false)?.toString()?:error("error parsing ${this@toType2}"))
        consume()
    }
    return Type(sort, dependencies)
}


@Deprecated("standardize on mm0")
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

@Deprecated("standardize on mm0")
internal fun String.toType(): Type = with(StringConsumable(this)) {
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