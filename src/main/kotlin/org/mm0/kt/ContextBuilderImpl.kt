package org.mm0.kt

/** Check that a constant is not replaced later by another for the DynamicParser
Check that an id is not replaced later by another */

class ContextBuilderImpl(private var assertions: STree<M.Computer.Assertion>?=null, private var comp: STree<M.Computer>?=null, private var sorts:STree<M.Computer.Sort>?=null, private var human: STree<M.Human>?=null, private var coercions: STree<STree<List<M.Human.Coercion>>>?=null, private var delimiters: STree<Delimiter>?=null, override val canonizer: Canonizer = Canonizer()) : ContextBuilder {
    private val contexts = mutableListOf<ContextImpl>()
    override fun current(): Context = contexts.lastOrNull() ?: error("no context built yet")

    override fun register(m: M) {
        when (m) {
            is M.Human.Delimiters -> {
                for (delimiter in m.left) delimiters = delimiters.put(delimiter, Delimiter.Left(delimiter))
                for (delimiter in m.both) delimiters = delimiters.put(delimiter, Delimiter.Both(delimiter))
                for (delimiter in m.right) delimiters = delimiters.put(delimiter, Delimiter.Right(delimiter))
            }
            is M.Human.Operator -> m.addIfFirst(m.constant)
            is M.Human.Notation -> m.addIfFirst(m.constant)
            is M.Human.Coercion -> {

                //coercions.find(m.id)?.let { error("${m.id} id already used before $this with $it") }

                /** it is an error if there there are other ways to do the m coercion */
                val otherCoercionWay =  coercions.find(m.coerced).find(m.coercedInto)
                if (otherCoercionWay != null) error("There is already a way to coerce ${m.coerced} into ${m.coercedInto} through $otherCoercionWay")

                var nextCoercions = coercions
                val previousTree = coercions.find(m.coerced)

                /** add the m coercion */
                var nextTree = previousTree.put(m.coercedInto, listOf(m))

                /** preppend with m existing coercions starting from coercedInto */
                val lm = listOf(m)
                coercions.find(m.coercedInto).forEach { key, list -> nextTree = nextTree.put(key, lm + list) }

                nextCoercions = nextCoercions.put(m.coerced, nextTree)

                /** we also have to append m to existing coercions ending with coerced */
                coercions.forEach{ key, t-> t.find(m.coerced)?.let { list-> nextCoercions = nextCoercions.put(key, t.put(m.coercedInto, list + m))} }

                /** update directCoercions with the final tree */
                coercions = nextCoercions
            }
            is M.Computer.Term -> m.addIfFirst(m.id)
            is M.Computer.Definition -> m.addIfFirst(m.id)
            is M.Computer.Sort -> m.addSortIfFirst(m.id)
            is M.Computer.Input -> TODO("I have not thought about those in this context yet")
            is M.Computer.Output -> TODO("I have not thought about those in this context yet")
            is M.Computer.Assertion -> m.addAssertionIfFirst(m.id)
        }
        contexts.add(ContextImpl(assertions, comp, sorts, human, delimiters, coercions))
    }

    /** Check that a constant is not replaced later by another for the DynamicParser */
    private fun M.Human.addIfFirst(constant: String) {
        human.find(constant)?.let { error("$constant constant already used before $this") }
        human = human.put(constant, this)
    }

    /** Check that an id is not replaced later by another */
    private fun M.Computer.Assertion.addAssertionIfFirst(id: String) {
        assertions.find(id)?.let { error("$id id already used before $this") }
        assertions = assertions.put(id, this)
    }

    private fun M.Computer.Sort.addSortIfFirst(id: String) {
        sorts.find(id)?.let { error("$id id already used before $this") }
        sorts = sorts.put(id, this)
    }

    /** Check that an id is not replaced later by another */
    private fun M.Computer.addIfFirst(id: String) {
        comp.find(id)?.let { error("$id id already used before $this with $it") }
        comp = comp.put(id, this)
    }
}