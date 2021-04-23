package org.mm0.kt


import org.mm0.kt.MMU.*
import org.mm0.kt.M.Computer.*
import org.mm0.kt.M.Computer.Assertion.*

/** a flexible and efficient parser for mmu files  :
 *
 * usesMmuVariant = false for the original mmu format released to the public and documented there [https://github.com/digama0/mm0]
 *                = True for the slight modification that enables proofs to be completely checked in place without tree allocation
 *
 * this parser should be able to parse :
 *   a big 400MB string
 *   a stream (a 20MB gzipped stream of a 400MB string)
 *   a memory-mapped text file
 *
 * for memory consumption purposes, it should be possible :
 *   - to validate in place proofs (no memoization)
 *   - to store (un)checked proof in a light lazy structure that fetch it when needed (individually or as a batch)
 *   - to store (un)checked proof as a mmu tree (requires canonisation and lots of allocations, trees may be huge)
 *
 * as all non-proof stuff read by the parser is destined to be used immediately and sored in an accumulating  math structure,
 * they should be :
 *   - Immutable
 *   - Canonized
 *   - as light on memory as possible (primitive, strings...)
 *   - as ready to use as possible (no preprocessing)
 *
 *  the api of the parser should be the same as the api of a mmb reader api (no parsing there)
 *
 *   an mmu file is supposed to be read sequentially, no jump allowed, as a stream/sequence of directives
 *   an mmb file is supposed to return lists
 *
 *   an mmu file is supposed to be read in lock step with a mm0 file
 *
 *   is should be possible to return a list of directives (with lazy proofs) of a mmu
 *
 *   proof-checking should happen in parallel coroutines/threads :
 *   - more performance, yeah !
 *   - validate a proof only when all prerequisites proofs are validated
 *   - provide enhanced/complete/customizable reports about a mm0/mmu pair (not just the first error)
 *
 *   it really helps to have good error reports
 *   - optionaly provide a proofChecking stacktrace
 *
 *   make sure the parser fail on wrong input
 *
 * */


/** design :
 * read stuff as a charStream, with local buffer structure ?  */

/** string must only hold ascii  chars */
fun mmuSequenceOf(string: String, canonizer: Canonizer): Sequence<MMU> = MMUSequence(StringConsumable(string), canonizer)

private class MMUSequence(private val consumable: Consumable, private val canonizer: Canonizer) : Sequence<MMU> {
    init {
        skipWhite()
    }

    override fun iterator(): Iterator<MMU> = MMUIterator()

    private inner class MMUIterator : Iterator<MMU> {
        override fun hasNext(): Boolean = !consumable.isConsumed()

        override fun next(): MMU {
            if (skipIf(COMMENT)) return MMULineComment(M.Human.LineComment.MMU(consumable.consumeLine())).apply { skipWhite() }
            savedDirectivePos = consumable.position()
            skip('(')
            val isLocal = when {
                skipIf(LOCAL) -> true
                skipIf(PUBLIC) -> false
                else -> false
            }
            val mmu = when {
                skipIf(DEFINITION) -> MMUDefinition(Definition(isLocal = isLocal, id = canonicId(), binders = binders(), type = type(), moreDummiesForDef = binders(), tree = tree()))
                skipIf(THEOREM) -> MMUTheorem(Theorem(isLocal = isLocal, id = canonicId(), binders = binders(), hypotheses = namedHypotheses(), conclusion = tree()), binders(), proof())
                skipIf(SORT) -> MMUSort(Sort(canonicId(), isPure = skipIf(PURE), isStrict = skipIf(STRICT), isProvable = skipIf(PROVABLE), isFree = skipIf(FREE)))
                skipIf(TERM) -> MMUTerm(Term(id = canonicId(), binders = binders(), type = type()))
                skipIf(AXIOM) -> MMUAxiom(Axiom(id = canonicId(), binders = binders(), hypotheses = hypotheses(), conclusion = tree()))
                skipIf(INPUT) -> MMUInput(Input(STRING).apply { skip(STRING) })
                skipIf(OUTPUT) -> MMUOutput(Output(STRING).apply { skip(STRING) })
                else -> parserError("unsupported yet")
            }
            skip(')')
            return mmu
        }
    }

    /** high level consume functions */
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

    /** mmu specific */
    private fun canonicId() = canonizer.toImmutable(id())

    private fun id(allowDummy: Boolean = false): CharSequence {
        savedElementPos = consumable.position()
        val resId = consumable.consumeId(allowDummy) ?: parserError("bad identifier")
        skipWhite()
        return resId
    }

    private fun type(): Type {
        skip('(')
        val sort = id()
        skip('(')
        val dependencies = spacedIdsClosedPar()
        skip(')')
        return canonizer.toImmutable(sort, dependencies)
    }

    private fun binder(): Binder {
        skip('(')
        val name = canonizer.toImmutable(id())
        val sort = id()
        val isBound = !skipIf('(')
        val dependencies = if (isBound) listOf() else spacedIdsClosedPar()
        skip(')')
        return Binder(isBound, name, canonizer.toImmutable(sort, dependencies))
    }

    private val bindersList = mutableListOf<Binder>()
    private fun binders(): List<Binder> {
        bindersList.clear()
        skip('(')
        while (!skipIf(')')) {
            //skip('(')
            //val isWrappedInParenthesis = skipIf('(')
            bindersList.add(binder())
            //if (isWrappedInParenthesis) skip(')')
            //skip(')')
        }
        return canonizer.toImmutable(bindersList)
    }


    private val hypotheses = mutableListOf<NamedHypothesis>()
    private fun namedHypotheses(): List<NamedHypothesis> {
        hypotheses.clear()
        skip('(')
        while (!skipIf(')')) {
            skip('(')
            hypotheses.add(NamedHypothesis(canonicId(), tree()))
            skip(')')
        }
        return hypotheses.toList()
    }

    private fun hypotheses(): List<NamedHypothesis> {
        hypotheses.clear()
        skip('(')
        while (!skipIf(')')) hypotheses.add(NamedHypothesis(underscoreCS, tree()))
        return hypotheses.toList()
    }


    private val strings = mutableListOf<String>()
    private fun spacedIdsClosedPar(): List<String> = strings.apply {
        clear()
        while (!skipIf(')')) add(canonicId())
    }

    private var savedDirectivePos: Int = 0
    private var savedElementPos: Int = 0
    private fun parserError(message: String): Nothing {
        val (directive, element, after) = consumable.charSequences(listOf(Pair(savedDirectivePos, consumable.position()), Pair(savedElementPos, consumable.position()), Pair(consumable.position(), consumable.position() + 20)))
        error("$message:\n$directive∎$element∎$after")
    }


    private fun tree(): StringTree {
        if (!skipIf('(')) return StringTree(canonicId(), listOf())
        val id = canonicId()
        val list = mutableListOf<StringTree>()
        while (!skipIf(')')) {
            list.add(tree())
            skipWhite()
        }
        return StringTree(id, list)
    }

    private fun balancedParentheses(): CharSequence {
        val charSequence = consumable.consumeBalancedParenthesis() ?: parserError("unbalanced parentheses")
        skipWhite()
        return charSequence
    }

    private fun proof(): CharSequence = consumable.consumeId(false)?.apply { skipWhite() } ?: balancedParentheses()
}