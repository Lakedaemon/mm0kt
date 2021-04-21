package org.mm0.kt

import java.io.Closeable
import java.io.File
import java.io.FileWriter
import kotlin.random.Random

class MM0TestFileWriter(path: String, val shouldBeWeird:Boolean) : TestWriterBoth, Closeable {
    private val random = Random(666)
    private val whiteSpaces = listOf("\u000A", "\u000B", "\u000C", " ", "\t")

    private val fW = FileWriter(File(path))
    private fun write(string: String) {
        fW.write(string)
    }

    override fun close() {
        fW.close()
    }

    private infix fun MM0TestFileWriter.w(s: String): MM0TestFileWriter = apply { write(s) }
    private infix fun MM0TestFileWriter.s(s: String): MM0TestFileWriter = apply {
        if (!shouldBeWeird) fW.write(" ") else for (a in 0..(random.nextInt().toPositive() % 3)) write(whiteSpaces[random.nextInt().toPositive() % whiteSpaces.size])
        write(s)
    }

    private infix fun MM0TestFileWriter.sr(end: String) {
        this s if (!shouldBeWeird || random.nextBoolean()) end else ""
    }

    override fun both(vararg both: String) = this w DELIMITER s "$" s both.joinToString(" ") s "$" s ";" sr "\n"
    override fun leftRight(vararg left: String, right: List<String>) = this w DELIMITER s "$" s left.joinToString(" ") s "$" s right.joinToString(" ", prefix = " $ ", postfix = " $ ") s ";" sr "\n"
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) = this w (if (isPure) "$PURE " else "") s (if (isStrict) "$STRICT " else "") s (if (isProvable) "$PROVABLE " else "") s (if (isFree) "$FREE " else "") s SORT s id s ";" sr "\n"
    override fun coercion(id: String, coerced: String, coercedInto: String) = this w COERCION s id s ":" s coerced s ">" s coercedInto s ";" sr "\n"
    override fun term(id: String, type: String, vararg binders: String) = this w TERM s id s binders.map{ it.toBinder()}.mm0() s ":" s type.toType().mm0() s ";" sr "\n"
    override fun op(id: String, constant: String, precedence: Int, opType: String) =this w opType s id s ":" s "$" s constant s "$" s PREC s  (if (precedence == Int.MAX_VALUE) MAX else precedence.toString()) s ";" sr "\n"
    override fun def(id: String, type: String, tree: String, vararg binders: String, moreDummies: String) :Unit = this s DEFINITION s id s binders.map{ it.toBinder()}.mm0() s ":" s type.toType().mm0() s "=" s "$" s tree s "$" s ";" sr "\n"

    override fun raw(string: String) = write(string)
    override fun mm0(string: String) = write(string)
    override fun mmu(string: String) {}

    /*  private val names = mutableListOf<String>()
                    private fun List<Binder>.human(): List<HumanBinder> {
                        if (isEmpty()) return listOf()

                        /** group binders if possible without changing their order */
                        val humanBinders = mutableListOf<HumanBinder>()
                        var lastBinder = this[0]
                        names.clear()
                        names.add(lastBinder.name)
                        asSequence().drop(1).forEach { binder ->
                            if (lastBinder.isBound == binder.isBound && lastBinder.type == binder.type) names.add(binder.name) else {
                                humanBinders.add(HumanBinder(lastBinder.isBound, names.toList(), lastBinder.type))
                                lastBinder = binder
                                names.clear()
                                names.add(binder.name)
                            }
                        }
                        humanBinders.add(HumanBinder(lastBinder.isBound, names.toList(), lastBinder.type))
                        return humanBinders
                    }

                    private fun List<HumanBinder>.mm0() = joinToString(" ") { it.mm0() }
                    private fun HumanBinder.mm0() = "${if (isBound) "{" else "("}${names.joinToString(" ")} : ${type.mm0()}${if (isBound) "}" else ")"}"
                */
    private fun List<Binder>.mm0() = joinToString(" ") { it.mm0() }
    private fun Binder.mm0() = "${if (isBound) "{" else "("}$name : ${type.mm0()}${if (isBound) "}" else ")"}"


    private fun Type.mm0(): String = dependencies.joinToString(" ").let { if (it.isEmpty()) sort else "$sort $it" }
}