package org.mm0.kt


import org.mm0.kt.M.Computer.*
import org.mm0.kt.M.Computer.Assertion.*

open class MMUWriter {
    open fun write(string: String) {}
    open fun close() {}

    fun mmu(sort: Sort) = with(sort) { write("($SORT $id ${if (isPure) " $PURE" else ""}${if (isStrict) " $STRICT" else ""}${if (isProvable) " $PROVABLE" else ""}${if (isFree) " $FREE" else ""})\n") }
    fun mmu(term: Term) = with(term) { write("($TERM $id (${binders.joinToString(" ") { it.mmu() }}) (${type.sort} (${type.dependencies.joinToString(" ")})))\n") }
    fun mmu(def: Definition, toTree: StringTree.() -> String) = with(def) { write("($DEFINITION $id (${binders.joinToString(" ") { it.mmu() }}) (${type.mmu()}) (${moreDummiesForDef.joinToString(" ") { "(${it.name} ${it.type.sort})" }}) ${tree.toTree()})\n") }
    fun mmu(axiom: Axiom, toTree: StringTree.() -> String) = write("($AXIOM ${axiom.id} (${axiom.binders.joinToString(" ") { bd -> "(${bd.mmu()})" }}) (${axiom.hypotheses.joinToString(" ") { h -> h.formula.toTree() }}) ${axiom.conclusion.toTree()})\n")
    fun mmu(theorem: Theorem, toTree: StringTree.() -> String, dummiesForProof: List<Pair<String, Type>>, proofString: String) = with(theorem) { write("(${if (isLocal) "" else "$PUBLIC "}$THEOREM $id (${binders.joinToString(" ") { bd -> "(${bd.mmu()})" }}) (${theorem.hypotheses.joinToString(" ") { h -> "(${h.name} ${h.formula})" }}) ${theorem.conclusion.toTree()} (${dummiesForProof.joinToString(" ") { "(${it.first} ${it.second.sort})" }}) $proofString)\n") }
    fun mmuOutput() = write("($OUTPUT $STRING)\n")
    fun mmuInput() = write("($INPUT $STRING)\n")

    private fun Binder.mmu() = "$name ${type.sort}${if (isBound) "" else " (${type.dependencies.joinToString(" ")})"}"
    private fun Type.mmu() = "$sort (${dependencies.joinToString(" ")})"
}
