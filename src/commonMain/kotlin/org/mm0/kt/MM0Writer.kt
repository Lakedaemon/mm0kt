package org.mm0.kt

import org.mm0.kt.M.Computer.*
import org.mm0.kt.M.Human.*
import java.io.Closeable

open class MM0Writer:Closeable {
    open fun write(string: String) {}
    override fun close() {}

    fun add(delimiters: Delimiters) = with(delimiters) { write("$DELIMITER $ ${(left + both).joinToString(" ")} $${if (right.isEmpty()) "" else right.joinToString(" ", prefix = " $ ", postfix = " $ ")};\n") }
    fun add(sort: Sort) = with(sort) { write("${if (isPure) "$PURE " else ""}${if (isStrict) "$STRICT " else ""}${if (isProvable) "$PROVABLE " else ""}${if (isFree) "$FREE " else ""}$SORT $id;\n") }
    fun add(coercion: Coercion) = with(coercion) { write("$COERCION $id : $coerced > $coercedInto;\n") }
    fun add(term: Term) = with(term) { write("$TERM $id ${binders.human().mm0()}} : ${type.mm0()};\n") }
    fun add(operator: Operator) = with(operator) { write("$operatorType $id: $$constant$ $PREC ${precedence.toStringPrecedence()};\n") }

    fun add(notation: Notation) = with(notation) { write("$NOTATION $id ${humanBinders.joinToString(" ") { it.mm0() }} : ${type.mm0()} = ($$constant$:${precedence.toStringPrecedence()}) ${notationLiterals.joinToString(" ") { it.mm0() }};\n") }


    private fun Int.toStringPrecedence() = if (this == Int.MAX_VALUE) MAX else toString()

    // Beware, grouping might change the order of binders
    fun add(definition: Definition, toTree: StringTree.() -> String) = with(definition) { write("$DEFINITION $id ${binders.human().mm0()} : ${type.mm0()} = $ ${tree.toTree()} $;\n") }

    fun addAxiom(axiom: Assertion.Axiom, toTree: StringTree.() -> String) = with(axiom) { write("$AXIOM $id ${binders.human().mm0()} ${hypotheses.joinToString(" ") {"(${it.name} : ${it.formula.toTree()}"}} : ${conclusion.toTree()};\n") }
    fun addTheorem(theorem: Assertion.Theorem, toTree: StringTree.() -> String) = with(theorem) { write("$THEOREM $id ${binders.human().mm0()} ${hypotheses.joinToString(" ") { "(${it.name} : ${it.formula.toTree()}" }} : ${conclusion.toTree()};\n") }


   // fun add(inout: Inout2) = with(inout) { write("${if (isInput) INPUT else OUTPUT} $kind : ${io.joinToString(" ") { it.mm0() }};\n") }

    private val names = mutableListOf<String>()
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

    // TODO might require enhancing
    //private fun IO.mm0() = if (isFormula) "$ $formula $" else id

    private fun Iterable<CharSequence>.mm0() = joinToString(" ").let { if (it.isEmpty()) "" else " $it" }

    private fun Type.mm0(): String = dependencies.joinToString(" ").let { if (it.isEmpty()) sort else "$sort $it" }

    private fun NotationLiteral.mm0() = when (this) {
        is NotationLiteral.Constant -> "($$constant$:$precedence)"
        is NotationLiteral.ID -> id
    }

}