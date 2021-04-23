package org.mm0.kt

import java.io.Closeable
import java.io.File
import java.io.FileWriter

class MM0TestFileWriter(path: String) : TestWriterBoth, Closeable {

    private val fW = FileWriter(File(path))
    private fun write(string: String) {
        fW.write(string)
    }

    override fun close() {
        fW.close()
    }

    override fun comment(vararg strings: String) = strings.forEach { this w "--" w it w "\n" }
    override fun both(vararg both: String) = this w DELIMITER s "$" s both.joinToString(" ") s "$" ww ";\n"
    override fun leftRight(vararg left: String, right: List<String>) = this w DELIMITER s "$" s left.joinToString(" ") s "$" s right.joinToString(" ", prefix = " $ ", postfix = " $ ") ww ";\n"
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) = this w PURE.ifs(isPure) w STRICT.ifs(isStrict) w PROVABLE.ifs(isProvable) w FREE.ifs(isFree) w SORT s id ww ";\n"
    override fun coercion(id: String, coerced: String, coercedInto: String) = this w COERCION s id w ":" s coerced s ">" s coercedInto ww ";\n"
    override fun term(id: String, arrows: String, vararg humanBinders: String) = this w TERM s id s humanBinders.joinToString(" ") w ":" w arrows ww ";\n"
    override fun op(id: String, constant: String, precedence: Int, opType: String) = this w opType s id w ":" s "$" s constant s "$" s PREC s (if (precedence == Int.MAX_VALUE) MAX else precedence.toString()) ww ";\n"
    override fun def(id: String, type: String, formula: String?, vararg humanBinders: String, tree: String, isLocal: Boolean) :Unit = if (isLocal) Unit else (this w DEFINITION s id s humanBinders.joinToString(" ") w ":" s type).let{ if (formula == null)  ww(";\n") else this s "=" s "$" s formula s "$" ww ";\n"}
    override fun axiom(id: String, arrows: String, vararg formulaTypeBinders: String) = this w AXIOM s id s formulaTypeBinders.joinToString(" ") w ":" s arrows ww ";\n"
    override fun theorem(id: String, arrows: String, vararg formulaTypeBinders: String, proof:String, isLocal:Boolean) = if (isLocal) Unit else this w THEOREM s id s formulaTypeBinders.joinToString(" ") w ":" s arrows ww ";\n"


    override fun raw(string: String) = write(string)
    override fun mm0(string: String) = write(string)
    override fun mmu(string: String) {}

    private infix fun MM0TestFileWriter.w(s: String): MM0TestFileWriter = apply { write(s) }
    private infix fun MM0TestFileWriter.s(s: String): MM0TestFileWriter = apply {
        write(" ")
        write(s)
    }
    private fun String.ifs(boolean:Boolean) :String = if (boolean) "$this " else ""

    private infix fun MM0TestFileWriter.ww(end: String) = write(end)
}
