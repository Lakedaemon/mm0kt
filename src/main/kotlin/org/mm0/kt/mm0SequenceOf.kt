package org.mm0.kt

import org.mm0.kt.M.Human.*
import org.mm0.kt.M.Computer.*
import org.mm0.kt.MM0.*
import org.mm0.kt.MM0.MM0Assert.*

fun mm0SequenceOf(string:String, canonizer: Canonizer):Sequence<MM0>  = MM0Sequence(StringConsumable(string), canonizer)

/** a flexible and efficient parser for mm0 files  :
 *
 * mm0 files are the human counterparts of mmu files and mostly handles stuff
 * useful for the dynamic parser like :
 *  - simple notations (unary and binary operators) : prefix, infixl, infixr
 *  - notations
 *  - coercions
 *  - binders for humans (grouped names with the same sort)
 *
 *  it mostly enhances mathematical syntax for the human mind
 */
private class MM0Sequence(private val consumable: Consumable, private val canonizer: Canonizer) : Sequence<MM0> {
    init {skipWhite()}

    override fun iterator(): Iterator<MM0> = MM0Iterator()
    private inner class MM0Iterator : Iterator<MM0> {
        override fun hasNext(): Boolean = !consumable.isConsumed()
        override fun next(): MM0 {
            savedDirectivePos = consumable.position()
            /** notations */
            val mm0 = when {
                skipIf(THEOREM) -> MM0Theorem(id = canonicId(), formulaTypeBinders = formulaTypeBinders(), arrows = assertArrows())
                skipIf(DEFINITION) -> MM0Definition(id = canonicId(), humanBinders = humanBinders(), type = canonizer.toImmutable(canonicId(), idsBefore('=', ';')), formula = if (skipIf('=')) formula() else null)
                skipIf(TERM) -> MM0Term(id = canonicId(), humanBinders = humanBinders(), termArrows())
                skipIf(AXIOM) -> MM0Axiom(id = canonicId(), formulaTypeBinders = formulaTypeBinders(), arrows = assertArrows())
                skipIf(PREFIX) -> operator(PREFIX)
                skipIf(INFIXL) -> operator(INFIXL)
                skipIf(INFIXR) -> operator(INFIXR)
                skipIf(NOTATION) -> MM0Notation(Notation(id = canonicId(), humanBinders = humanBinders(), type = canonizer.toImmutable(canonicId(), idsBeforeSkipping('=')).then('('), constant = formula().then(':'), precedence = (if (skipIf(MAX)) Int.MAX_VALUE else int()).then(')'), notationLiterals = notationLiterals()))
                skipIf(PURE) -> sort(1)
                skipIf(STRICT) -> sort(2)
                skipIf(PROVABLE) -> sort(3)
                skipIf(FREE) -> sort(4)
                skipIf(SORT) -> sort(5)
                skipIf(COERCION) -> MM0Coercion(Coercion(id = canonicId().then(':'), coerced = canonicId().then('>'), coercedInto = canonicId()))
                skipIf(DELIMITER) -> MM0Delimiters(delimiters())
                skipIf(INPUT) -> inout(true)
                skipIf(OUTPUT) -> inout(false)
                else -> parserError("this should not happen")
            }
            skip(';')
            return mm0
        }
    }



    private fun idsBefore(charA: Char, charB: Char) = deps.apply {
        clear()
        while (look() != charA && look() != charB) add(canonicId())
    }

    private fun idsBeforeSkipping(char: Char) = deps.apply {
        clear()
        while (!skipIf(char)) add(canonicId())
    }

    private fun <T> T.then(text: String): T = apply { skip(text) }
    private fun <T> T.then(char: Char): T = apply { skip(char) }

    /** high level mm0 */
    /** high level consume functions */
    private fun look(): Char = consumable.look()

    private fun skipIf(text: String): Boolean {
        if (!consumable.consumeIf(text)) return false
        consumable.consume()
        return true
    }

    private fun skipIf(char: Char): Boolean {
        if (!consumable.consumeIf(char)) return false
        consumable.consume()
        return true
    }

    private fun skip(c: Char) {
        if (!skipIf(c)) {
            savedElementPos = consumable.position()
            parserError("cannot skip $c")
        }
    }

    private fun skip(text: String) {
        if (!skipIf(text)) {
            savedElementPos = consumable.position()
            parserError("cannot skip $text")
        }
    }

    private fun skipWhite() = consumable.consume()

    private fun canonicId() = canonizer.toImmutable(id())
    private fun canonicIdummy() = canonizer.toImmutable(id(true))

    private fun id(allowDummy: Boolean = false): CharSequence {
        savedElementPos = consumable.position()
        val resId = consumable.consumeId(allowDummy) ?: parserError("bad identifier")
        skipWhite()
        return resId
    }

    /** do not trim, the dynamicParser doesn't need it and it is a waste of time and energy */
    private fun formula(): String {
        val f = consumable.consumeFormula() ?: parserError("bad formula")
        skipWhite()
        return f
    }


    /** mm0 */
    private fun delimiters():Delimiters {
        val string = formula()
        if (look() != '$') return Delimiters(listOf(), string.split(' ').distinct().filterNot{it.isEmpty()}, listOf())
        return Delimiters(string.split(' ').distinct().filterNot{it.isEmpty()}, listOf(), formula().split(' ').distinct().filterNot{it.isEmpty()})
    }

    private fun sort(readToken: Int): MM0Sort {
        /** gobble options and sort */
        val isPure = readToken == 1
        val isStrict = readToken == 2 || (readToken < 2 && skipIf(STRICT))
        val isProvable = readToken == 3 || (readToken < 3 && skipIf(PROVABLE))
        val isFree = readToken == 4 || (readToken < 4 && skipIf(FREE))
        if (readToken != 5) skip(SORT)
        return MM0Sort(Sort(isPure = isPure, isStrict = isStrict, isProvable = isProvable, isFree = isFree, id = canonicId()))
    }

    private fun operator(direction: String): MM0Operator = MM0Operator(Operator(id = canonicId().apply { then(':') }, constant = formula().then("prec"), precedence = if (skipIf("max")) Int.MAX_VALUE else int(), operatorType = direction))

    // BEWARE of the mutability of this, should be copied to List
    private val names = mutableListOf<String>()
    private val deps = mutableListOf<String>()
    private fun humanBinder(): HumanBinder {
        names.clear()
        deps.clear()
        val isBound = when {
            skipIf('{') -> true
            skipIf('(') -> false
            else -> parserError("issue with names")
        }

        while (!skipIf(':')) names.add(canonicIdummy())
        val sort = canonicId()
        val end = if (isBound) '}' else ')'
        while (!skipIf(end)) deps.add(canonicId())
        val type = canonizer.toImmutable(sort, deps)
        return HumanBinder(isBound, names.toList(), type)
    }

    private fun humanBinders(): List<HumanBinder> {
        val humanBinders = mutableListOf<HumanBinder>()
        while (!skipIf(':')) humanBinders.add(humanBinder())
        return humanBinders
    }


    private fun termArrows(): List<Type> {
        val arrows = mutableListOf<Type>()
        var sort = canonicId()
        deps.clear()
        while (look() != ';') if (skipIf('>')) {
            arrows.add(canonizer.toImmutable(sort, deps))
            sort = canonicId()
            deps.clear()
        } else deps.add(canonicId())
        arrows.add(canonizer.toImmutable(sort, deps))
        return arrows
    }

    private fun formulaTypeBinder(): FormulaTypeBinder {
        val isBound = when {
            skipIf('{') -> true
            skipIf('(') -> false
            else -> parserError("issue with binder")
        }
        val end = if (isBound) '}' else ')'
        names.clear()
        while (!skipIf(':')) names.add(canonicId())
        if (look() == '$') return FormulaTypeBinder.Formula(isBound = isBound, names = names.toList(), formula = formula())
        val sort = canonicId()
        deps.clear()
        while (!skipIf(end)) deps.add(canonicId())
        return FormulaTypeBinder.Type(isBound = isBound, names = names.toList(), type = canonizer.toImmutable(sort, deps))
    }


    private fun formulaTypeBinders(): List<FormulaTypeBinder> {
        val formulaOrTypeBinders = mutableListOf<FormulaTypeBinder>()
        while (!skipIf(':')) formulaOrTypeBinders.add(formulaTypeBinder())
        return formulaOrTypeBinders
    }

    private fun assertArrows(): List<FormulaOrType> {
        val list = mutableListOf<FormulaOrType>()
        while (look() != ';') {
            if (list.isNotEmpty()) skip('>')
            if (look() == '$') list.add(FormulaOrType.Formula(formula())) else {
                val sort = canonicId()
                deps.clear()
                while (look() != ';' && look() != '>') deps.add(canonicId())
                list.add(FormulaOrType.Type(canonizer.toImmutable(sort, deps)))
            }
        }
        return list
    }

    private fun notationLiterals(): List<NotationLiteral> {
        val list = mutableListOf<NotationLiteral>()
        while (look() != ';') list.add(if (skipIf('(')) NotationLiteral.Constant(formula().apply { then(':') }, (if (skipIf(MAX)) Int.MAX_VALUE else int()).then(')')) else NotationLiteral.ID(canonicId()))
        return list
    }

    private fun inout(isInput: Boolean) = MM0InOut(isInput = isInput, type = canonicId().then(':'), ios = mutableListOf<IO>().apply {
        while (look() != ';') add(if (look() == '$') IO.Formula(formula()) else IO.ID(canonicId()))
    })


    private var savedDirectivePos: Int = 0
    private var savedElementPos: Int = 0
    private fun parserError(message: String): Nothing {
        val (directive, element, after) = consumable.charSequences(listOf(Pair(savedDirectivePos, consumable.position()), Pair(savedElementPos, consumable.position()), Pair(consumable.position(), consumable.position() + 20)))
        error("$message:\n$directive∎$element∎$after")
    }

    private fun int(): Int {
        val int = consumable.consumeInt() ?: parserError("could not consume an int")
        skipWhite()
        return int
    }
}