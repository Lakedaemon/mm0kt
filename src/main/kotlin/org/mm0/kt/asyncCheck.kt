package org.mm0.kt

import org.mm0.kt.MM0.*
import org.mm0.kt.MMU.*
import org.mm0.kt.M.Computer.*

val simpleCanonizer = Canonizer()


/** check a sequence of MM0 statements and MMU directives
 * and stores the checked immutable statement/directive
 * in a Math structure.
 *
 * Ideally, the Math structure could be based on a RedBlackTree,
 * so that we can quickly access the Math environment to a point in the s/d sequence
 *
 * trees are only slightly smaller than hashmaps but offers the possibility to time travel,
 * which is desirable for maths
 *
 * statement/directives can be checked really fast, except for theorems which can have huge proofs.
 * So, it would be nice to check them in coroutines, for an immutable Math context
 * That way, we could proof check them in parallel, on different machines,
 * and get a report for each proof (passes or not, set of depending theorems)
 * that we would agregate later/lazily/reactively...
 *
 *
 * */


/** we take a sequence of MM0 and a sequence of MMU,
 * we check them, with regards to an existing Math Context
 * and we spit an enhanced Math Context,
 * as well as a sequence of (checked) MM0/MMU
 * */
fun simpleCheck(context: Context, theorem: Assertion.Theorem, binders: List<Binder>, proof: CharSequence): Unit = SimpleChecker(context, theorem, binders, proof, simpleCanonizer).check()


fun ContextBuilder.asyncCheck(mm0s: Sequence<MM0>, mmus: Sequence<MMU>, checker: (Context, Assertion.Theorem, List<Binder>, CharSequence) -> Unit) {
    val it0 = mm0s.iterator()
    for (mmu in mmus) {
        //println("$mmu")
        when (mmu) {
            is MMUSort -> registerUntil(it0) { mm0 -> if (isMatch(mmu.sort, mm0)) register(mmu.sort) else error("sort ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUTerm -> registerUntil(it0) { mm0 -> if (isMatch(mmu.term, mm0)) register(mmu.term) else error("term ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUInput -> registerUntil(it0) { mm0 -> if (isMatch(mmu.input, mm0)) Unit else error("input ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUOutput -> registerUntil(it0) { mm0 -> if (isMatch(mmu.output, mm0)) Unit else error("output ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUDefinition -> if (mmu.definition.isLocal) register(mmu.definition) else registerUntil(it0) { mm0 -> if (current().isMatch(mmu.definition, mm0)) register(mmu.definition) else error("definition ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUAxiom -> registerUntil(it0) { mm0 -> if (isMatch(mmu.axiom, mm0)) register(mmu.axiom) else error("axiom ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMUTheorem -> {
                checker(current(), mmu.theorem, mmu.binders, mmu.proof)
                if (!mmu.theorem.isLocal) registerUntil(it0) { mm0 -> if (!isMatch(mmu.theorem, mm0)) error("theorem ${mmu.report()} cannot be matched to ${mm0.report()}") }
                register(mmu.theorem)
            }
        }
    }
    /** consume the remaining mm0 statements, if any */
    registerUntil(it0) { mm0 ->
        error("no corresponding mmu directive for $mm0")
    }
}


private fun ContextBuilder.registerUntil(it0: Iterator<MM0>, check: ContextBuilder.(mm0: MM0) -> Unit) {
    while (it0.hasNext()) {
        val mm0 = it0.next()
        //println(mm0.toString())
        when (mm0) {
            is MM0Delimiters -> register(mm0.delimiters)
            is MM0Coercion -> register(mm0.coercion)
            is MM0Operator -> register(mm0.operator)
            is MM0Notation -> register(mm0.notation)
            else -> {
                check(mm0)
                return
            }
        }
    }
}

private fun isMatch(a: Sort, mm0: MM0): Boolean {
    if (mm0 !is MM0Sort) return false
    val b = mm0.sort
    return a.id == b.id && a.isProvable == b.isProvable && a.isFree == b.isFree && a.isPure == b.isPure && a.isStrict == b.isStrict
}

private fun isMatch(term: Term, mm0: MM0): Boolean {
    if (mm0 !is MM0Term || term.id != mm0.id) return false
    val arrows = mm0.arrows
    if (term.type != arrows.lastOrNull()) return false
    /** there may be binders */
    if (arrows.size == 1) return mm0.humanBinders.isHumanValidMatch(term.binders)
    /** there are arrows (with more that the ending type)**/
    if (mm0.humanBinders.isNotEmpty() || term.binders.size != arrows.size - 1) return false
    return (0 until arrows.size - 1).all { arrows[it] == term.binders[it].type }
}

/** match public definition */
private fun Context.isMatch(def: Definition, mm0: MM0): Boolean {
    if (mm0 !is MM0Definition || def.id != mm0.id) return false
    if (def.type != mm0.type) return false
    if (mm0.humanBinders.asSequence().flatMap{ hb-> hb.names.mapNotNull { if (it.startsWith('.')) Binder(true, it.substring(1), hb.type) else null  } }.toList() != def.moreDummiesForDef) return false.apply{println("6667")}
    if (!mm0.humanBinders.isHumanValidMatch(def.binders)) return false
    val formula = mm0.formula
    if (formula != null) {
        val parser = DynamicParser(this)
        val types = (def.binders + def.moreDummiesForDef) .associate { Pair(it.name as CharSequence, it.type) }
        val tree = parser.parse(formula, types)
        if (tree != def.tree) error("different formula : \ncharSequence=$formula\nmm0=$tree\nmmu=${def.tree}\ncontext=$this")
    }
    return true
}

private fun isMatch(input: Input, mm0: MM0): Boolean = mm0 is MM0InOut && mm0.isInput && mm0.type == input.type
private fun isMatch(output: Output, mm0: MM0): Boolean = mm0 is MM0InOut && !mm0.isInput && mm0.type == output.type


/** match public axiom */
private fun ContextBuilder.isMatch(ax: Assertion, mm0: MM0): Boolean {
    if (mm0 !is MM0Assert || ax.id != mm0.id) return false
    val dynamicParser = DynamicParser(current(), canonizer)

    val hypotheses = mutableListOf<NamedHypothesis>()
    val binders = mutableListOf<Binder>()
    val arrows = mm0.arrows

    // compute vars out of arrows and or binders
    val types = ax.binders.associate { Pair(it.name as CharSequence, it.type) }

    for (arrow in arrows.dropLast(1)) when (arrow) {
        is FormulaOrType.Formula -> hypotheses.add(NamedHypothesis(underscoreCS, dynamicParser.parse(arrow.formula, types)))
        is FormulaOrType.Type -> {
            println("meh" + mm0.report())
            TODO()
        }
    }

    val formula = arrows.last()
    if (formula !is FormulaOrType.Formula || dynamicParser.parse(formula.formula, types) != ax.conclusion) return false.apply{println("form false $formula\nparsed=${dynamicParser.parse((formula as? FormulaOrType.Formula)?.formula?:"gah", types)}")}

    mm0.formulaTypeBinders.forEach {
        when (it) {
            is FormulaTypeBinder.Formula -> {
                if (it.names.size != 1) return false
                hypotheses.add(NamedHypothesis(it.names.first(), dynamicParser.parse(it.formula, types)))
            }
            is FormulaTypeBinder.Type -> binders.addAll(it.names.map { s -> Binder(it.isBound, s, it.type) })
        }
    }
    return ax.binders.isValidMatch(binders) && ax.hypotheses.hypothesesMatch(hypotheses)
}

private fun List<NamedHypothesis>.hypothesesMatch(hypotheses: List<NamedHypothesis>): Boolean = foldRightIndexed(true) {index, nh, acc -> acc && hypotheses[index].formula == nh.formula}

/** check if exported humanBinder match with binders, and if those are valid*/
private fun List<HumanBinder>.isHumanValidMatch(binders: List<Binder>): Boolean = asSequence().flatMap { it.names.mapNotNull { s -> if (s.startsWith('.')) null else Binder(it.isBound, s, it.type) } }.toList().isValidMatch(binders)
private fun List<Binder>.isValidMatch(binders: List<Binder>): Boolean {
    val sorted = binders.sortedBy { it.name }
    var lastName = ""
    for (b in sorted) {
        if (b.name == lastName) error("duplicate variable names in ${sorted.joinToString(" ") { it.report() }}")
        lastName = b.name
    }
    return this== binders
}