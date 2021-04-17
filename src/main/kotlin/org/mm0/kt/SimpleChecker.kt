package org.mm0.kt

class SimpleChecker(private val context: Context, theorem: M.Computer.Assertion.Theorem, binders: List<Binder>, proof: CharSequence, private val canonizer: Canonizer) : ProofChecker(theorem, binders, proof) {

    private val consumable: Consumable = StringConsumable(proof.toString())
    private val hypotheses: MutableMap<CharSequence, StringTree> = theorem.hypotheses.associate { Pair(it.name as CharSequence, it.formula) }.toMutableMap()
    private val types: Map<CharSequence, Type> = (theorem.binders + binders).associate { Pair(it.name as CharSequence, it.type) }


    private var savedDirectivePos: Int = 0
    private var savedElementPos: Int = 0
    private fun parserError(message: String): Nothing {
        val (directive, element, after) = consumable.charSequences(listOf(Pair(savedDirectivePos, consumable.position()), Pair(savedElementPos, consumable.position()), Pair(consumable.position(), consumable.position() + 20)))
        error("$message while checking ${theorem.id}\n$directive∎$element∎$after")
    }


    override fun check() {
        val proofResult = consumable.proofResultOf()
        if (theorem.conclusion != proofResult) parserError("invalid proof\nwe expected ${theorem.conclusion}\nand we got $proofResult")
    }

    private fun skip(c: Char) {
        if (!consumable.skipIf(c)) {
            savedElementPos = consumable.position()
            parserError("cannot skip $c")
        }
    }


    private fun id(): CharSequence {
        savedElementPos = consumable.position()
        val resId = consumable.consumeId(false) ?: parserError("bad identifier")
        skipWhite()
        return resId
    }

    private fun canonicId(): String = canonizer.toImmutable(id())


    private fun Consumable.sTree(): StringTree {
        consumeId(false)?.let { s -> skipWhite(); return StringTree(canonizer.toImmutable(s), listOf()) }
        skip('(')
        val id = canonicId()
        val children = mutableListOf<StringTree>()
        while (!skipIf(')')) children.add(sTree())
        return StringTree(id, children)
    }

    private fun skipWhite() = consumable.consume()
    private fun unificationFor(binders: List<Binder>, arguments: List<StringTree>, types: Map<CharSequence, Type>): Map<CharSequence, StringTree> = mutableMapOf<CharSequence, StringTree>().apply {
        if (arguments.size != binders.size) parserError("mismatched arguments\narguments=$arguments\nbinders=${binders.joinToString(" ") { it.report() }}")
        for ((index, arg) in arguments.withIndex()) {
            val binder = binders[index]
            // check arg type
            val argType = types[arg.id] ?: context.typeFor(arg.id) ?: parserError("cannot associate a type tp ${arg.id}")
            // TODO what to do about dependencies ?
            if (binder.type.sort != argType.sort) parserError("mismatched arguments : ${binder.type}!=$argType")
            this[binder.name] = arg
        }
    }

    private fun StringTree.unify(map: Map<CharSequence, StringTree>): StringTree = map[id] ?: StringTree(id, children.map { it.unify(map) })


    private fun Consumable.proofResultOf(): StringTree {
        if (skipIf("?")) parserError("missing proof ?")
        if (!skipIf('(')) return hypotheses[id()] ?: error("missing hypothesis")
        if (skipIf(LET)) {
            hypotheses[canonicId()] = proofResultOf()
            return proofResultOf().apply { skip(')') }
        }
        if (skipIf(CONVERSION)) {
            val exp = sTree()
            val conversionResult = conversionResultOf()
            val proofResult = proofResultOf()
            skip(')')
            if (conversionResult.second != proofResult || exp != conversionResult.first) parserError("we expected proofResult=$proofResult\n and we got conversionB=${conversionResult.second}\nwe expected exp=$exp\n and got expConversionA= ${conversionResult.first}")
            return conversionResult.first
        }
        val id = id()
        if (id in types) return StringTree(canonizer.toImmutable(id()), listOf())
        /** assertion */
        val arguments = mutableListOf<StringTree>()
        if (skipIf('(')) while (!skipIf(')')) arguments.add(sTree())
        else while (look() != ')') arguments.add(StringTree(canonicId(), listOf()))
        val assertion = context.assertion(id) ?: parserError("missing assertion for $id")
        val unification = unificationFor(assertion.binders, arguments, types)
        for (hypothesis in assertion.hypotheses) {
            val hypResult = hypothesis.formula.unify(unification)
            val hypProofResult = proofResultOf()
            if (hypResult != hypProofResult) parserError("hypothesis $hypothesis not proven\nWe expected $hypResult\nWe got $hypProofResult")
        }
        skip(')')
        return assertion.conclusion.unify(unification)
    }

    private fun Consumable.conversionResultOf(): Pair<StringTree, StringTree> = when {
        !skipIf('(') -> id().let { id -> if (id in types) StringTree(canonizer.toImmutable(id), listOf()).let { Pair(it, it) } else parserError("$id not in variables : $types") }
        skipIf(SYM) -> conversionResultOf().let { Pair(it.second, it.first) }.apply { skip(')') }
        skipIf(UNFOLDCONV) -> {
            val defId = canonicId()
            skip('(')
            val expressions = mutableListOf<StringTree>()
            while (!skipIf(')')) expressions.add(sTree())

            val dummies = mutableListOf<String>()
            skip('(')
            while (!skipIf(')')) dummies.add(canonicId())
            val conversionAB = conversionResultOf()
            skip(')')

            /** checking that def.formula.unifiy(args)==conversionA */
            val def = context.definition(defId) ?: error("missing definition")
            val unification = unificationFor(def.binders, expressions, types) + dummies.mapIndexed { index, s -> Pair(def.moreDummiesForDef[index].name, StringTree(s, listOf())) }
            val defResult = def.tree.unify(unification)
            if (defResult != conversionAB.first) parserError("Bad conversion\nWe expected $defResult\nWe got ${conversionAB.first}")
            Pair(StringTree(defId, expressions), conversionAB.second)
        }
        else -> {
            /** substitution */
            // termOrId
            val termOrDefId = canonizer.toImmutable(id())
            // fetch proofs
            val proofs = mutableListOf<Pair<StringTree, StringTree>>()
            while (!skipIf(')')) proofs.add(conversionResultOf())
            Pair(StringTree(termOrDefId, proofs.map { it.first }), StringTree(termOrDefId, proofs.map { it.second }))
        }
    }
}




