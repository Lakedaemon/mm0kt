package org.mm0.kt

class ContextImpl(private val assertions: STree<M.Computer.Assertion>?, private val comp: STree<M.Computer>?, private val human: STree<M.Human>?, override val delimiters: STrie, private val directCoercions: STree<STree<List<M.Human.Coercion>>>?) : Context {
    override fun operator(constant: CharSequence): M.Human.Operator? = human.find(constant) as? M.Human.Operator
    override fun notation(constant: CharSequence): M.Human.Notation? = human.find(constant) as? M.Human.Notation
    override fun directCoercion(actual: CharSequence, expected: CharSequence): List<M.Human.Coercion>? = directCoercions.find(actual).find(expected)
    override fun term(id: CharSequence): M.Computer.Term? = comp.find(id) as? M.Computer.Term
    override fun definition(id: CharSequence): M.Computer.Definition? = comp.find(id) as? M.Computer.Definition
    override fun assertion(id: CharSequence): M.Computer.Assertion? = assertions.find(id)
    override fun toString() = "delimiters=$delimiters\nhuman=$human\ncomp=$comp\nassertions=$assertions"
    override fun typeFor(id: CharSequence): Type? = comp.find(id)?.let {
        return when (it) {
            is M.Computer.Term -> it.type
            is M.Computer.Definition -> it.type
            else -> null
        }
    } ?: (human.find(id) as? M.Human.Notation)?.type
}