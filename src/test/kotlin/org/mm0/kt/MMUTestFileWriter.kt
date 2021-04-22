package org.mm0.kt

import java.io.Closeable
import java.io.File
import java.io.FileWriter

class MMUTestFileWriter(path: String) : TestWriterBoth, Closeable {
    private val contextBuilder = ContextBuilderImpl()
    private fun parser() :DynamicParser {
        for (m in registeringQueue) contextBuilder.register(m)
        registeringQueue.clear()
        return DynamicParser(contextBuilder.current())
    }

    // lazily registering thing
    private val registeringQueue = mutableListOf<M>()

    private val fW = FileWriter(File(path))
    private fun write(string: String) {
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

    private infix fun MMUTestFileWriter.ww(end: CharSequence) {
        this w end
    }

    private fun register(m: M) {
        registeringQueue.add(m)
    }
    override fun comment(vararg strings: String) {}
    override fun both(vararg both: String) = register(M.Human.Delimiters(listOf(), both.toList(), listOf()))
    override fun leftRight(vararg left: String, right: List<String>) = register(M.Human.Delimiters(left.toList(), listOf(), right))
    override fun sort(id: String, isPure: Boolean, isStrict: Boolean, isProvable: Boolean, isFree: Boolean) {
        register(M.Computer.Sort(id, isPure = isPure, isStrict = isStrict, isProvable = isProvable, isFree = isFree))
        this w "($SORT" s id s (if (isPure) " $PURE" else "") s (if (isStrict) " $STRICT" else "") s (if (isProvable) " $PROVABLE" else "") s (if (isFree) " $FREE" else "") ww ")\n"
    }

    override fun coercion(id: String, coerced: String, coercedInto: String) = register(M.Human.Coercion(id, coerced, coercedInto))
    override fun op(id: String, constant: String, precedence: Int, opType: String) = register(M.Human.Operator(id, constant = constant, precedence = precedence, operatorType = opType))

    override fun term(id: String, arrows: String, vararg humanBinders: String) {
        val trueHumanBinders = humanBinders.map { it.toHumanBinder() }

        val termArrows = arrows.termArrows()
        val binders = if (termArrows.size > 1) termArrows.dropLast(1).map { Binder(false, underscoreCS, it) } else trueHumanBinders.toBindersWithoutAdditionnalDummies()
        val type = termArrows.last()
        register(M.Computer.Term(id, binders, type))
        this w "(" w TERM s id s "(" w binders.joinToString(" ") { it.mmu() } w ")" s "(" w type.mmu() ww "))\n"
    }

    override fun def(id: String, type: String, formula: String?, vararg humanBinders: String, tree: String, isLocal: Boolean) {
        val trueHumanBinders = humanBinders.map { it.toHumanBinder() }
        val binders = trueHumanBinders.toBindersWithoutAdditionnalDummies()
        val additionalDummies = trueHumanBinders.flatMap { hb -> hb.names.mapNotNull { if (it.startsWith('.')) Binder(true, it.substring(1, it.length), hb.type) else null } }


        val types = (binders + additionalDummies).associate { Pair(it.name as CharSequence, it.type) }
        val trueTree = if (formula != null && tree == formula) parser().parse(formula, types) else StringConsumable(tree).tree()

        val trueType = type.toType2()
        register(M.Computer.Definition(id, isLocal, binders, trueType, additionalDummies, trueTree))
        this w "(" w DEFINITION s id s "(" w binders.joinToString(" ") w ")" s "(" w trueType.mmu() w ")" s "(" w additionalDummies.joinToString(" ") { "(${it.name} ${it.type.sort})" } w ")" s trueTree.toMmuTree() ww ")\n"
    }

    private fun Consumable.tree(): StringTree {
        if (!consumeIf('(')) return StringTree(consumeId(false)?.toString()?:error("cannot parse $this"), listOf())
        val id = consumeId(false)?.toString()?:error("cannot parse $this")
        val list = mutableListOf<StringTree>()
        while (!skipIf(')')) {
            list.add(tree())
            consume()
        }
        return StringTree(id, list)
    }

    private fun String.arrows(): List<FormulaOrType> = with(StringConsumable(this)) {
        val formulaOrTypes = mutableListOf<FormulaOrType>()
        consume()
        while (!isConsumed()) {
            val formula = consumeFormula()
            formulaOrTypes.add(if (formula != null) FormulaOrType.Formula(formula) else {
                val sort = consumeId(false)?.toString() ?: error("cannot parse ${this@arrows}")
                consume()
                val dependencies = mutableListOf<String>()
                while (!isConsumed() && !consumeIf('>')) dependencies.add(consumeId(false)?.toString() ?: error("cannot parse ${this@arrows}"))
                FormulaOrType.Type(Type(sort, dependencies))
            })
            consume()
        }
        return formulaOrTypes
    }

    private fun String.formulaTypeBinder(): FormulaTypeBinder = with(StringConsumable(this)) {
        consume()
        val isBound = consumeIf('{')
        val end = if (isBound) '}' else {
            consumeIf('(')
            ')'
        }
        consume()
        val names = mutableListOf<String>()
        while (!consumeIf(':')) {
            names.add((consumeId(true)?.toString() ?: error("error parsing ${this@formulaTypeBinder}")))
            consume()
        }
        val formula = consumeFormula()
        if (formula != null) {
            consume()
            if (!consumeIf(end)) error("error parsing ${this@formulaTypeBinder}")
            return FormulaTypeBinder.Formula(isBound, names, formula)
        }
        val sort = consumeId(false)?.toString() ?: error("cannot parse ${this@formulaTypeBinder}")
        consume()
        val dependencies = mutableListOf<String>()
        while (!consumeIf(end)) {
            dependencies.add(consumeId(false)?.toString() ?: error("cannot parse ${this@formulaTypeBinder}"))
            consume()
        }
        return FormulaTypeBinder.Type(isBound, names, Type(sort, dependencies))
    }


    override fun axiom(id: String, arrows: String, vararg formulaTypeBinders: String) {
        val parser = parser()

        val hypotheses = mutableListOf<NamedHypothesis>()

        val trueArrows = arrows.arrows()
        val trueFormulaTypeBinders = formulaTypeBinders.map { it.formulaTypeBinder() }
        val binders = trueFormulaTypeBinders.flatMap { ftb ->
            ftb.names.mapNotNull { s ->
                if (ftb is FormulaTypeBinder.Type && !s.startsWith('.')) Binder(ftb.isBound, s, ftb.type) else null
            }
        }
        val types = binders.associate { Pair(it.name as CharSequence, it.type) }
        trueFormulaTypeBinders.filterIsInstance<FormulaTypeBinder.Formula>().forEach { ftb ->
            val tree = parser.parse(ftb.formula, types)
            ftb.names.forEach { s -> hypotheses.add(NamedHypothesis(s, tree)) }
        }
        // what to do with arrows that are type ?
        trueArrows.dropLast(1).forEachIndexed { index, formulaOrType -> if (formulaOrType is FormulaOrType.Formula) hypotheses.add(NamedHypothesis(underscoreCS+index, parser.parse(formulaOrType.formula, types))) }


        // compute vars out of arrows and or binders
        //val types = ax.binders.associate { Pair(it.name as CharSequence, it.type) }

        /*for (arrow in trueArrows.dropLast(1)) when (arrow)
        {
            is FormulaOrType.Formula -> hypotheses.add(NamedHypothesis(underscoreCS, dynamicParser.parse(arrow.formula, types)))
            is FormulaOrType.Type -> {
            println("meh" + mm0.report())
            TODO()
        }
        }

        val formula = trueArrows.last()
        if (formula !is FormulaOrType.Formula || dynamicParser.parse(formula.formula, types) != ax.conclusion) return false.apply
        { println("form false $formula\nparsed=${dynamicParser.parse((formula as? FormulaOrType.Formula)?.formula ?: "gah", types)}") }

        mm0.formulaTypeBinders.forEach
        {
            when (it) {
                is FormulaTypeBinder.Formula -> {
                    if (it.names.size != 1) return false
                    hypotheses.add(NamedHypothesis(it.names.first(), dynamicParser.parse(it.formula, types)))
                }
                is FormulaTypeBinder.Type -> binders.addAll(it.names.map { s -> Binder(it.isBound, s, it.type) })
            }
        }*/


        this w "(" w AXIOM s id s "(" w binders.joinToString(" ") { it.mmu() } w ") (" w hypotheses.joinToString(" ") { it.formula.toMmuTree() } w ")" s parser.parse((trueArrows.last() as FormulaOrType.Formula).formula, types).toMmuTree() ww ")\n"
    }

/*override fun axiom(id: String, conclusion: String, vararg formulaTypeBinders: String, hypotheses: List<String>) {
    this w "(" w AXIOM s id s "(" w formulaTypeBinders.joinToString(" ") w ")" s "(" w hypotheses.joinToString(" ") w ")" s conclusion ww ")\n"
}*/

    override fun raw(string: String) = write(string)
    override fun mm0(string: String) {}
    override fun mmu(string: String) = write(string)

    private fun Binder.mmu() = "($name ${type.sort}${if (isBound) "" else " (${type.dependencies.joinToString(" ")})"})"
    private fun Type.mmu() = "$sort (${dependencies.joinToString(" ")})"

}