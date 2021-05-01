package org.mm0.kt

/** Contexts are supposed to be used in a single thread */
interface ContextBuilder {
    val canonizer: Canonizer
    fun register(m: M)
    fun current(): Context

    fun asyncCheck(mm0s: Sequence<MM0>, mmus: Sequence<MMU>, checker: (Context, M.Computer.Assertion.Theorem, List<Binder>, CharSequence) -> Unit): Sequence<Matched> = sequence {
        val it0 = mm0s.iterator()
        for (mmu in mmus) when (mmu) {
            is MMU.MMUSort -> registerUntil(it0) { mm0 ->
                if (!isMatch(mmu.sort, mm0)) error("sort ${mmu.report()} cannot be matched to ${mm0.report()}")
                yield(Matched.Sort(mm0 as MM0.MM0Sort, mmu.sort))
                register(mmu.sort)
            }
            is MMU.MMUTerm -> registerUntil(it0) { mm0 ->
                if (!isMatch(mmu.term, mm0)) error("term ${mmu.report()} cannot be matched to ${mm0.report()}")
                yield(Matched.Term(mm0 as MM0.MM0Term, mmu.term))
                register(mmu.term)
            }
            is MMU.MMUInput -> registerUntil(it0) { mm0 -> if (isMatch(mmu.input, mm0)) yield(Matched.Input(mm0 as MM0.MM0InOut, mmu)) else error("input ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMU.MMUOutput -> registerUntil(it0) { mm0 -> if (isMatch(mmu.output, mm0)) yield(Matched.Output(mm0 as MM0.MM0InOut, mmu)) else error("output ${mmu.report()} cannot be matched to ${mm0.report()}") }
            is MMU.MMUDefinition -> if (mmu.definition.isLocal) {
                yield(Matched.LocalDefinition(mmu.definition))
                register(mmu.definition)
            } else registerUntil(it0) { mm0 ->
                if (!current().isMatch(mmu.definition, mm0)) error("definition ${mmu.report()} cannot be matched to ${mm0.report()}")
                yield(Matched.Definition(mm0 as MM0.MM0Definition, mmu.definition))
                register(mmu.definition)
            }
            is MMU.MMUAxiom -> registerUntil(it0) { mm0 ->
                if (!isMatch(mmu.axiom, mm0)) error("axiom ${mmu.report()} cannot be matched to ${mm0.report()}")
                yield(Matched.Axiom(mm0 as MM0.MM0Assert.MM0Axiom, mmu))
                register(mmu.axiom)
            }
            is MMU.MMUTheorem -> {
                checker(current(), mmu.theorem, mmu.binders, mmu.proof)
                if (mmu.theorem.isLocal) yield(Matched.LocalTheorem(mmu)) else registerUntil(it0) { mm0 ->
                    if (!isMatch(mmu.theorem, mm0)) error("theorem ${mmu.report()} cannot be matched to ${mm0.report()}")
                    yield(Matched.Theorem(mm0 as MM0.MM0Assert.MM0Theorem, mmu))
                }
                register(mmu.theorem)
            }
        }

        /** consume the remaining mm0 statements, if any */
        registerUntil(it0) { mm0 -> error("no corresponding mmu directive for $mm0") }
    }

    private suspend fun SequenceScope<Matched>.registerUntil(it0: Iterator<MM0>, check: suspend SequenceScope<Matched>.(mm0: MM0) -> Unit) {
        while (it0.hasNext()) when (val mm0 = it0.next()) {
            is MM0.MM0Delimiters -> {
                yield(Matched.Delimiters(mm0))
                register(mm0.delimiters)
            }
            is MM0.MM0Coercion -> {
                yield(Matched.Coercion(mm0))
                register(mm0.coercion)
            }
            is MM0.MM0Operator -> {
                yield(Matched.Operator(mm0))
                register(mm0.operator)
            }
            is MM0.MM0Notation -> {
                yield(Matched.Notation(mm0))
                register(mm0.notation)
            }
            else -> {
                check(mm0)
                return
            }
        }

    }

    private fun isMatch(a: M.Computer.Sort, mm0: MM0): Boolean {
        if (mm0 !is MM0.MM0Sort) return false
        val b = mm0.sort
        return a.id == b.id && a.isProvable == b.isProvable && a.isFree == b.isFree && a.isPure == b.isPure && a.isStrict == b.isStrict
    }

    private fun isMatch(term: M.Computer.Term, mm0: MM0): Boolean {
        if (mm0 !is MM0.MM0Term || term.id != mm0.id) return false
        val arrows = mm0.arrows
        if (term.type != arrows.lastOrNull()) return false
        /** there may be binders */
        if (arrows.size == 1) return mm0.humanBinders.isHumanValidMatch(term.binders)
        /** there are arrows (with more that the ending type)**/
        if (mm0.humanBinders.isNotEmpty() || term.binders.size != arrows.size - 1) return false
        return (0 until arrows.size - 1).all { arrows[it] == term.binders[it].type }
    }

    /** match public definition */
    private fun Context.isMatch(def: M.Computer.Definition, mm0: MM0): Boolean {
        if (mm0 !is MM0.MM0Definition || def.id != mm0.id) return false
        if (def.type != mm0.type) return false
        if (!mm0.humanBinders.isHumanValidMatch(def.binders)) return false
        val formula = mm0.formula
        if (formula != null) {
            /** check additional dummies for non abstract definition */
            if (mm0.humanBinders.asSequence().flatMap { hb -> hb.names.mapNotNull { if (it.startsWith('.')) Pair(it.substring(1), hb.type.sort) else null } }.toSet() != def.moreDummiesForDef.map { Pair(it.name, it.type.sort) }.toSet()) return false
            val parser = DynamicParser(this)
            val types = (def.binders + def.moreDummiesForDef).associate { Pair(it.name as CharSequence, it.type) }
            val tree = parser.parse(formula, types)
            if (tree != def.tree) error("different formula : \ncharSequence=$formula\nmm0=$tree\nmmu=${def.tree}\ncontext=$this")
        }
        return true
    }

    private fun isMatch(input: M.Computer.Input, mm0: MM0): Boolean = mm0 is MM0.MM0InOut && mm0.isInput && mm0.type == input.type
    private fun isMatch(output: M.Computer.Output, mm0: MM0): Boolean = mm0 is MM0.MM0InOut && !mm0.isInput && mm0.type == output.type


    /** match public axiom */
    private fun isMatch(ax: M.Computer.Assertion, mm0: MM0): Boolean {
        if (mm0 !is MM0.MM0Assert || ax.id != mm0.id) return false
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
        if (formula !is FormulaOrType.Formula || dynamicParser.parse(formula.formula, types) != ax.conclusion) return false.apply { println("form false $formula\nparsed=${dynamicParser.parse((formula as? FormulaOrType.Formula)?.formula ?: "gah", types)}") }

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

    private fun List<NamedHypothesis>.hypothesesMatch(hypotheses: List<NamedHypothesis>): Boolean = foldRightIndexed(true) { index, nh, acc -> acc && hypotheses[index].formula == nh.formula }

    /** check if exported humanBinder match with binders, and if those are valid*/
    private fun List<HumanBinder>.toBindersWithoutAdditionnalDummies(): List<Binder> = asSequence().flatMap { it.names.mapNotNull { s -> if (s.startsWith('.')) null else Binder(it.isBound, s, it.type) } }.toList()

    private fun List<HumanBinder>.isHumanValidMatch(binders: List<Binder>): Boolean = toBindersWithoutAdditionnalDummies().isValidMatch(binders)
    private fun List<Binder>.isValidMatch(binders: List<Binder>): Boolean {
        val sorted = binders.sortedBy { it.name }
        var lastName = ""
        for (b in sorted) {
            if (b.name == lastName) error("duplicate variable names in ${sorted.joinToString(" ") { it.report() }}")
            lastName = b.name
        }
        return this == binders
    }
}