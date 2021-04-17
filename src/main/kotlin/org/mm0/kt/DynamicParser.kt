package org.mm0.kt


/**
 * CharSequence should be a slice of CharSequence (instead of string)
 *
 *
 *
 *
 *
 * MathParser should be able to parse CharSequence (CharSequence ?)  (not only String)
 * MathParser should either spit string tokens or tokens with information concerning range
 *
 * CharSequence of a Char Range should fold into a charRange
 * CharSequence have control over HashCode (String/CharSequence don't)
 * */


/** toToken takes a position-limit range and a string replacement and returns a token
 * if string == "", the token.string will represent the string of chars between pos and limit (possibly canonised)
 * if string != "", the token.string will be the string provided but pos and limit will point to the associated charSequence in the parsed string
 *
 * wrap allow a token to transmit pos and limit to a provided string
 * string allow a string to be associated to the token (and to be able to have MM0Tree<String>
 * */

class DynamicParser(private val context: Context, val canonizer: Canonizer = simpleCanonizer) {

    private val toToken = fun(charSequence: CharSequence, position: Int, limit: Int): String = canonizer.toImmutable(charSequence, position, limit)

    fun parse(mm0MathString: CharSequence, types: Map<CharSequence, Type>): StringTree {
        val toks = tokens(mm0MathString, context.delimiters, toToken).toList()

        /** Now parse the tokens */
        val tokens = TokenProvider(toks)
        val first = tokens.parseSingle(types)
        return parseExpression(tokens, first, 0, types)
    }


    /** grow a computed left hand side parse tree with a parse of the tokens to the right
     *
     * tokens is a reference to the (String) Token sequence, with a position
     * position increases (sometimes recursively) through the algorithm, so we don't need to copy it
     *
     * tokens initialy points to the last token of lhs
     * */

    private fun parseExpression(tokens: TokenProvider, alreadyParsed: StringTree, minPrecedence: Int, variables: Map<CharSequence, Type>): StringTree {
        //  debug("parseExpression($tokens, ${alreadyParsed.prettyPrint()}, $minPrecedence, $variables)")
        var leftTree = alreadyParsed
        while (tokens.hasToken && isBinaryOperatorWithPrecedenceGreaterThan(tokens, minPrecedence)) {
            val token = context.operator(tokens.current) ?: error("a binary operator should be in mm0Tokens : ${tokens.current}")
            val opToken = tokens.token
            val opPrecedence = token.precedence//op.operatorPrecedence(variables)
            tokens.moveRight(1)
            var rightTree = tokens.parseSingle(variables)
            while (tokens.hasToken && isBinaryOperatorInfixRightWithSamePrecedenceOrIsBinaryOperatorWithGreaterPrecedenceThan(tokens, opPrecedence)) {
                rightTree = parseExpression(tokens, rightTree, context.operator(tokens.current)?.precedence ?: throw Exception("shouldn't happened"), variables)
            }
            leftTree = variables.opResultTree(leftTree, token.id, opToken, rightTree)
            //variables.checkOperatorType(leftTree.id, token.id.string, rightTree.id)
            //leftTree = MM0Tree(token.id.string, listOf(leftTree, rightTree))
        }
        return leftTree
    }


    private fun M.Human.Operator.isBinaryOperator(): Boolean = operatorType != PREFIX && 2 == (context.term(id)?.binders?.size ?: context.definition(id)?.binders?.size)


    private fun isBinaryOperatorWithPrecedenceGreaterThan(tokens: TokenProvider, minPrecedence: Int) = true == context.operator(tokens.current)?.let { it.isBinaryOperator() && it.precedence >= minPrecedence }

    private fun isBinaryOperatorInfixRightWithSamePrecedenceOrIsBinaryOperatorWithGreaterPrecedenceThan(tokens: TokenProvider, minPrecedence: Int) = true == context.operator(tokens.current)?.let { it.isBinaryOperator() && (it.precedence > minPrecedence || (it.precedence == minPrecedence && it.operatorType == INFIXR)) }

    /** parse one Tree out of tokens and updates position to the next token */
    private fun TokenProvider.parseSingle(variables: Map<CharSequence, Type>): StringTree {
        // debug("$this.parseSingle($variables)")
        if (!hasToken) throw Exception("no token, this shouldn't happen")
        if (current in variables) {
            val result = StringTree(canonizer.toImmutable(token), listOf())
            moveRight(1)
            return result
        }

        // to do improve prefixOperator shouldn't be called for delimiters
        val currentToken = token
        val prefixOperator = current.prefixOperator()
        val notation = context.notation(current)
        return when {
            current == "(" -> {
                var pos = position + 1
                var depth = 1
                while (depth > 0 && pos < list.size) when (list[pos++]) {
                    "(" -> ++depth
                    ")" -> --depth
                }
                if (depth > 0) throw Exception("unbalanced parentheses in $this")
                val tokens2 = TokenProvider(list.subList(position + 1, pos - 1))
                val result = parseExpression(tokens2, tokens2.parseSingle(variables), 0, variables)
                moveRight(pos - position)
                result
            }
            prefixOperator != null -> {
                moveRight(1)
                //debug("prefixOperator=$prefixOperator")
                val children: List<StringTree> = (1..prefixOperator.typedList.size).map {
                    val child = parseExpression(this, parseSingle(variables), prefixOperator.precedenceLevel, variables)

                    /** check types */
                    val coercions = checkTypes(child.id.type(variables).sort, prefixOperator.typedList[it - 1].sort) ?: error("$it for prefix ${prefixOperator.id} expected type ${prefixOperator.typedList[it - 1]} was actual ${child.id.type(variables)} for\n${child/*.prettyPrint()*/}\nprefixOperator.typedList = ${prefixOperator.typedList}\ntypeList[${it - 1}]=${prefixOperator.typedList[it - 1]}\n$prefixOperator")
                    coercions.foldRight(child) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }
                }
                StringTree(prefixOperator.id, children)
            }
            notation != null -> {
                val newToken = notation.id
                moveRight(1)
                var prec = notation.precedence
                val types = notation.humanBinders.flatMap { it.names.map { s -> Binder(it.isBound, s, it.type) } }//  .typeBinders.map { it.type }
                var idCount = 0
                StringTree(newToken, notation.notationLiterals.mapNotNull {
                    when (it) {
                        is NotationLiteral.ID -> {
                            val child = parseExpression(this, parseSingle(variables), prec, variables)
                            val coercions = checkTypes(child.id.type(variables).sort, types[idCount].type.sort /* .type    .first()*/) ?: throw Exception("actual type for term $idCount of notation ${notation.id} is ${child.id.type(variables).sort} but was expected to be ${types[idCount].type.sort}")
                            //if (!checkTypes(child.id.type(variables).first(), types[idCount].type.first())) throw Exception("actual type for term $idCount of notation ${notation.id} is ${child.id.type(variables).first()} but was expected to be ${types[idCount].type.first()}")
                            idCount++
                            coercions.foldRight(child) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }
                        }
                        is NotationLiteral.Constant -> {
                            if (!current.charsEquals(it.constant)) throw Exception("we should have $current == ${it.constant}")
                            prec = it.precedence
                            moveRight(1)
                            null
                        }
                    }
                })
            }
            // TODO shouldn't happen
            else -> {
                val result = StringTree(canonizer.toImmutable(token), listOf())
                moveRight(1)
                result
            }
        }
    }

    private fun checkTypes(actual: String, expected: String): List<M.Human.Coercion>? = if (actual == expected) listOf() else context.directCoercion(actual, expected)


    private data class PrefixOperator(val id: String, val precedenceLevel: Int, val typedList: List<Type>)

    private fun String.prefixOperator(): PrefixOperator? {
        val token = context.operator(this)
        if (token == null) {
            val term = context.term(this)
            //debug("term for $this=$term")
            if (term != null) return PrefixOperator(term.id, Int.MAX_VALUE, term.binders.map { it.type }/*trueTypeList*/)
            val def = context.definition(this) ?: return null
            val types = def.binders.map { it.type }

            return PrefixOperator(def.id, Int.MAX_VALUE, types)
        }
        // might be a term or a def, we should keep a mapping to save time
        val term = context.term(token.id)
        if (term != null) return PrefixOperator(term.id, token.precedence, term.binders.map { it.type })

        val def = context.definition(token.id) ?: return null
        //println("def[${def.id}] binders=${def.binders.joinToString(", "){it.name+":"+it.type.sort}}")
        return PrefixOperator(def.id, token.precedence, def.binders.map { it.type })
    }

    private fun Map<CharSequence, Type>.opResultTree(left: StringTree, op: String, opToken: String, right: StringTree): StringTree {
        val leftType = left.id.type(this)
        val rightType = right.id.type(this)
        val term = context.term(op)
        if (term != null) {
            val binders = term.binders
            val trueBinderTypes = if (binders.isNotEmpty()) binders.map { it.type } else listOf(term.type)//binders.flatMap { it.identifiers.map { s -> Pair(s, it.type) } }
            val leftCoercions = checkTypes(leftType.sort, trueBinderTypes[0].sort) ?: throw Exception("actual left type $leftType for $op does not coerce to ${trueBinderTypes[0].sort}")
            val rightCoercions = checkTypes(rightType.sort, trueBinderTypes[1].sort) ?: throw Exception("actual right type $rightType for $op does not coerce to ${trueBinderTypes[1].sort}")
            return StringTree(term.id, listOf(leftCoercions.foldRight(left) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }, rightCoercions.foldRight(right) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }))
        }
        val def = context.definition(op)
        if (def != null) {
            val trueBinderTypes = def.binders.map { it.type }//def.idTypes.map{it.second}
            //val trueBinderTypes = if (binders.isNotEmpty()) binders.typedList().map { it.type } else term.arrow.dropLast(1)//binders.flatMap { it.identifiers.map { s -> Pair(s, it.type) } }
            val leftCoercions = checkTypes(leftType.sort, trueBinderTypes[0].sort) ?: throw Exception("actual left type $leftType for $op does not coerce to ${trueBinderTypes[0].sort}")
            val rightCoercions = checkTypes(rightType.sort, trueBinderTypes[1].sort) ?: throw Exception("actual right type $rightType for $op does not coerce to ${trueBinderTypes[1].sort}")
            return StringTree(def.id, listOf(leftCoercions.foldRight(left) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }, rightCoercions.foldRight(right) { coercion, tree -> StringTree(coercion.id, listOf(tree)) }))
        }
        throw Exception("should not happen : $op")
    }


    private fun String.type(variables: Map<CharSequence, Type>): Type {
        val type = variables[this]
        if (type != null) return type
        val notation = context.notation(this)
        if (notation != null) return notation.type
        val term = context.term(this)
        if (term != null) return term.type
        val def = context.definition(this)
        if (def != null) return def.type
        error("cannot find the type of $this with variables=$variables")
    }


    private inner class TokenProvider(val list: List<String>) {
        var position: Int = 0
        val hasToken: Boolean get() = position < list.size

        val token: String get() = list[position]

        var current: String = if (list.isNotEmpty()) list[position] else ""

        fun moveRight(delta: Int) {
            position += delta
            if (position < list.size) current = list[position]
        }

        override fun toString(): String = list.subList(position, list.size).toString()
    }
}

/** string is a mm0 MathString*/
fun tokens(charSequence: CharSequence, delimiters: STrie, toTokenString: (CharSequence, Int, Int) -> String): Sequence<String> = sequence {
    val size = charSequence.length
    var position = 0
    var tokenStart = -1
    var tokenLimit = -1
    while (position < size) {
        // find the start of the token
        val char = charSequence[position]
        /** if we have white space, we eventually yield and advance after the whitespace */
        if (char.isWhitespace()) {
            if (tokenStart >= 0) {
                yield(toTokenString(charSequence, tokenStart, position))
                tokenStart = -1
            }
            position++
            continue
        }
        /** if we have a delimiter, we yield and advance after the delimiter */
        // TODO we should use a true trie... for delimiters
        val delim = delimiters.keyStarting(charSequence, position)
        if (delim != null) {
            // we found a delimiter
            if (tokenStart >= 0) yield(toTokenString(charSequence, tokenStart, position))
            yield(delim)
            position += delim.length
            tokenStart = -1
            continue
        }

        /** eventually set the token start */
        if (tokenStart < 0) tokenStart = position
        /** agglomerate the token */
        tokenLimit = 1 + position++
    }
    if (tokenStart >= 0) yield(toTokenString(charSequence, tokenStart, tokenLimit))
}

private fun CharSequence.charPrefixAt(other: CharSequence, position: Int = 0): Boolean = other.length - position >= length && (0 until length).all { this[it] == other[position + it] }
