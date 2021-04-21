package org.mm0.kt

import java.io.Closeable
import java.io.File
import java.io.FileWriter

class MMUTestFileWriter(path: String) : TestWriterBoth, Closeable {
    private val fW = FileWriter(File(path))
    fun write(string: String) {
        fW.write(string)
    }

    override fun close() {
        fW.close()
    }

    private infix fun MMUTestFileWriter.w(s: CharSequence): MMUTestFileWriter = apply { fW.append(s) }
    private infix fun MMUTestFileWriter.s(s: CharSequence): MMUTestFileWriter = apply {
        fW.append(" ")
        fW.append(s)
    }

    private infix fun StringBuilder.sp(cs : CharSequence) = append(cs)
    private infix fun MMUTestFileWriter.ww(end: CharSequence) {
        this w end
    }

    override fun both(vararg both: String) {}
    override fun leftRight(vararg left: String, right: List<String>) {}
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) = this w "($SORT" s id s (if (isPure) " $PURE" else "") s (if (isStrict) " $STRICT" else "") s (if (isProvable) " $PROVABLE" else "") s (if (isFree) " $FREE" else "") ww ")\n"
    override fun coercion(id: String, coerced: String, coercedInto: String) {}
    override fun op(id: String, constant: String, precedence: Int, opType: String) {}

    override fun term(id: String, type: String, vararg binders: String) = this w "(" w TERM s id s "(" w binders.joinToString(" "){"($it)"} w ")" s "(" w type w ")" ww ")\n"
    override fun def(id: String, type: String, tree: String, vararg binders: String, moreDummies: String, isAbstract:Boolean, isMMUOnly:Boolean) :Unit =this w "(" w DEFINITION s id s "(" w binders.joinToString(" ")  w ")" s "(" w type w ")" s "(" w moreDummies w ")" s tree ww ")\n"
    override fun axiom(id:String, conclusion:String, vararg binders:String, hypotheses:List<String>)  = this w "(" w AXIOM s id s "(" w binders.joinToString(" ") w ")" s "(" w hypotheses.joinToString(" ") w ")" s conclusion ww ")\n"
    
    override fun raw(string: String) = write(string)
    override fun mm0(string: String) {}
    override fun mmu(string: String) = write(string)
}