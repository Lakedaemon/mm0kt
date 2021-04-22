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

    private infix fun MM0TestFileWriter.w(s: String): MM0TestFileWriter = apply { write(s) }
    private infix fun MM0TestFileWriter.s(s: String): MM0TestFileWriter = apply {
        write(" ")
        write(s)
    }

    private infix fun MM0TestFileWriter.ww(end: String) = write(end)

    override fun comment(vararg strings: String) = strings.forEach { this w "--" w it ww "\n" }

    override fun both(vararg both: String) = this w DELIMITER s "$" s both.joinToString(" ") s "$" ww ";\n"
    override fun leftRight(vararg left: String, right: List<String>) = this w DELIMITER s "$" s left.joinToString(" ") s "$" s right.joinToString(" ", prefix = " $ ", postfix = " $ ") ww ";\n"
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) {
        if (isPure) this w PURE w " "
        if (isStrict) this w STRICT w " "
        if (isProvable) this w PROVABLE w " "
        if (isFree) this w FREE w " "
        this w SORT s id ww ";\n"
    }

    override fun coercion(id: String, coerced: String, coercedInto: String) = this w COERCION s id s ":" s coerced s ">" s coercedInto s ";" ww "\n"
    override fun term(id: String, arrows: String, vararg humanBinders: String) = this w TERM s id s humanBinders.joinToString(" ") w ":" w arrows ww ";\n"


    override fun op(id: String, constant: String, precedence: Int, opType: String) = this w opType s id s ":" s "$" s constant s "$" s PREC s (if (precedence == Int.MAX_VALUE) MAX else precedence.toString()) ww ";\n"

    override fun def(id: String, type: String, formula: String?, vararg humanBinders: String, tree: String, isLocal: Boolean) {
        if (isLocal) return
        this s DEFINITION s id s humanBinders.joinToString(" ") s ":" s type
        if (formula == null)  this ww ";\n" else this s "=" s "$" s formula s "$" ww ";\n"
    }

    /*override fun def(id: String, type: String, tree: String, vararg binders: String, moreDummies: String, isAbstract: Boolean, isMMUOnly: Boolean) {
        if (isMMUOnly) return
        this s DEFINITION s id s binders.map { it.toBinder() }.mm0() s ":" s type.toType().mm0()
        if (isAbstract) this w ";\n" else this s "=" s "$" s tree s "$" ww ";\n"
    }*/
    override fun axiom(id: String, arrows: String, vararg formulaTypeBinders: String) = this w AXIOM s id s formulaTypeBinders.joinToString(" ") s ":" s arrows ww ";\n"


    // override fun axiom(id: String, conclusion: String, vararg formulaTypeBinders: String, hypotheses: List<String>) {}


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
