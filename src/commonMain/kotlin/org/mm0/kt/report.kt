package org.mm0.kt


/** those functions avoid having to have data classes all over the places,
 * which would needlessly drastically increase the method counts,
 * which is a huge bother on Android (as there is a limit of 65536 methods,
 * that is painfull to cross) */

fun MM0.report(): String = when (this) {
    is MM0.MM0Sort -> with(sort) { "sort $id isFree=$isFree isProvable = ${isProvable}isPure=$isPure isStrict=$isStrict" }
    is MM0.MM0Term -> "term $id binders=${humanBinders.joinToString() { it.report() }} arrows=${arrows.joinToString(" > ") { it.report() }}"
    is MM0.MM0Definition -> "def $id binders=${humanBinders.reportH()} type=${type.report()} formula=$formula"
    is MM0.MM0Assert.MM0Axiom -> "axiom $id ${formulaTypeBinders.joinToString(" ") { it.report() }} : ${arrows.joinToString(" > ") { it.report() }}"
    is MM0.MM0Assert.MM0Theorem -> "theorem $id ${formulaTypeBinders.joinToString(" ") { it.report() }} : ${arrows.joinToString(" > ") { it.report() }}"
    else -> "unsupported yet $this"
}

fun MMU.report(): String = when (this) {
    is MMU.MMUSort -> with(sort) { "sort $id isFree=$isFree isProvable = ${isProvable}isPure=$isPure isStrict=$isStrict" }
    is MMU.MMUTerm -> with(term) { "term $id binders=${binders.report()} type=${type.report()}" }
    is MMU.MMUDefinition -> with(definition) { "definition $id binders=${binders.report()} type=${type.report()} moreDummies=${moreDummiesForDef.report()} formula=$tree" }
    is MMU.MMUAxiom -> with(axiom) { "axiom $id ${binders.joinToString(" ") { it.report() }} : ${hypotheses.joinToString(" > ") { it.report() }} ${if (hypotheses.isEmpty()) "" else " > "} $conclusion" }
    is MMU.MMUTheorem -> with(theorem) { "theorem $id ${binders.joinToString(" ") { it.report() }} : ${hypotheses.joinToString(" > ") { it.report() }} ${if (hypotheses.isEmpty()) "" else " > "} $conclusion\n\n$proof" }
    is MMU.MMUInput -> with(input) { "input $type" }
    is MMU.MMUOutput -> with(output) { "input $type" }
    else -> "unsupported yet $this"
}


fun FormulaOrType.report() = when (this) {
    is FormulaOrType.Formula -> "$ $formula $"
    is FormulaOrType.Type -> type.report()
}

fun FormulaTypeBinder.report() = when (this) {
    is FormulaTypeBinder.Formula -> "${if (isBound) "{" else "("}${names.joinToString(" ")} : $ $formula $${if (isBound) "}" else ")"}"
    is FormulaTypeBinder.Type -> "${if (isBound) "{" else "("}${names.joinToString(" ")} : ${type.report()}${if (isBound) "}" else ")"}"
}

fun Type.report() = if (dependencies.isEmpty()) sort else dependencies.joinToString(" ", prefix = "$sort ")

fun List<Binder>.report() = joinToString(" ") { it.report() }
fun Binder.report() = "${if (isBound) "{" else "("}$name : ${type.report()}${if (isBound) "}" else ")"}"
fun List<HumanBinder>.reportH() = joinToString(" ") { it.report() }
fun HumanBinder.report() = "${if (isBound) "{" else "("}${names.joinToString(" ")} : ${type.report()}${if (isBound) "}" else ")"}"
fun NamedHypothesis.report() = "$name = $formula"