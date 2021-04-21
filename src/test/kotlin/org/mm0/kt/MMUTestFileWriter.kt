package org.mm0.kt

import java.io.Closeable
import java.io.File
import java.io.FileWriter
import kotlin.random.Random

class MMUTestFileWriter(path: String, val shouldBeWeird:Boolean) : TestWriterBoth, Closeable {
    private val random = Random(6666)
    private val whiteSpaces = listOf("\u000A", "\u000B", "\u000C", " ", "\t")

    private val fW = FileWriter(File(path))
    fun write(string: String) {
        fW.write(string)
    }

    override fun close() {
        fW.close()
    }

    private infix fun MMUTestFileWriter.w(s: CharSequence): MMUTestFileWriter = apply { fW.append(s) }
    private infix fun MMUTestFileWriter.s(s: CharSequence): MMUTestFileWriter = apply {
        fW.append(if (shouldBeWeird) space() else " ")
        fW.append(s)
    }

    private fun space() = (0..random.nextInt().toPositive() % 3).joinToString("") { whiteSpaces[random.nextInt().toPositive() % whiteSpaces.size] }

    private infix fun StringBuilder.sp(cs : CharSequence) = append(cs)

    private infix fun MMUTestFileWriter.sr(end: CharSequence) {
        this s if (!shouldBeWeird || random.nextBoolean()) end else ""

    }
    override fun both(vararg both: String) {}
    override fun leftRight(vararg left: String, right: List<String>) {}
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) = this w "($SORT" s id s (if (isPure) " $PURE" else "") s (if (isStrict) " $STRICT" else "") s (if (isProvable) " $PROVABLE" else "") s (if (isFree) " $FREE" else "") s ")" sr "\n"
    override fun coercion(id: String, coerced: String, coercedInto: String) {}
    override fun term(id: String, type: String, vararg binders: String) = this w "(" w TERM s id s "(" w binders.joinToString(" "){"($it)"} w ")" s "(" w type w ")" w ")" sr "\n"
    override fun op(id: String, constant: String, precedence: Int, opType: String) {}
    override fun def(id: String, type: String, tree: String, vararg binders: String, moreDummies: String) :Unit =this s "(" w DEFINITION s id s "(" s binders.joinToString(" ")  s ")" s "(" s type s ")" s "(" s moreDummies s ")" s tree s ")" sr "\n"

    override fun raw(string: String) = write(string)
    override fun mm0(string: String) {}
    override fun mmu(string: String) = write(string)
}