package org.mm0.kt

import org.mm0.kt.Context.Diff

fun CharSequence.toEnhancedFormula(diff: Diff, types: Map<CharSequence, Type>, wrapIfNotSingle: Boolean = false, parser:DynamicParser = DynamicParser(diff)): String = parser.parse(this, types).toEnhancedFormula(diff, wrapIfNotSingle = wrapIfNotSingle)
fun StringTree.toEnhancedFormula(diff: Diff, wrapIfNotSingle: Boolean = false): String = StringBuilder().transform(diff, this,wrapIfNotSingle && children.isNotEmpty()).toString()


fun CharSequence.toMmuTree(parser:DynamicParser, types: Map<CharSequence, Type>): String = parser.parse(this, types).toMmuTree()
fun StringTree.toMmuTree(): String = StringBuilder().noTransform(this).toString()

/** this is some simple experimental code, that somehow works (but shouldn't if there are many notations/defs, attached to an id,
 * beautifying mmu trees into math formula probably requires human intelligence...)
 *
 * this should be ok for pass/fail tests though
 * */
private fun StringBuilder.transform(diff: Diff, tree: StringTree, needsOuterParenthesis: Boolean = false): StringBuilder = apply {
    val id = tree.id
    val immutableNotation = diff.newNotations[id]
    if (immutableNotation != null) {
        // use a notation
        val term = diff.term(id) ?: error("missing term for $id")
        val binders = term.binders
        append(immutableNotation.constant).append(" ")
        for (notationLiteral in immutableNotation.notationLiterals) when (notationLiteral) {
            is NotationLiteral.Constant -> append(notationLiteral.constant).append(" ")
            is NotationLiteral.ID -> {
                val string = notationLiteral.id
                // this is a var, get it's index
                val index = binders.indexOfFirst { it.name == string }
                // if (index < 0) println(binders.toString() + "\n[" + string + "]")
                val child = tree.children[index]
                transform(diff, child, child.children.isNotEmpty())
                append(" ")
            }
        }
        return@apply
    }
    val sn = diff.newOperators[id]
    if (sn != null) {
        val precedence = sn.precedence
        // upgrade to new notation
        when (sn.operatorType) {
            PREFIX -> {
                // wn a  ~a
                if (needsOuterParenthesis) append("(")
                if (tree.children.isEmpty()) append(sn.constant) else {
                    append(sn.constant)
                    diff.delimiters.find(sn.constant) ?: append(" ")
                    for (child in tree.children) {
                        //val a = tree.children[0]
                        val sna = diff.newOperators[child.id]
                        transform(diff, child, child.children.isNotEmpty() && (sna == null || sna.precedence <= precedence))
                        append(" ")
                    }
                }
                if (needsOuterParenthesis) append(")")
                return@apply
            }
            INFIXL -> {
                // wi a b should give a -> b
                if (needsOuterParenthesis) append("(")
                val a = tree.children[0]
                val b = tree.children[1]
                val sna = diff.newOperators[a.id]
                transform(diff, a, a.children.isNotEmpty() && (sna == null || sna.precedence < precedence))
                append(" ")
                append(sn.constant)
                append(" ")
                val snb = diff.newOperators[b.id]
                transform(diff, tree.children[1], b.children.isNotEmpty() && (snb == null || snb.precedence <= precedence))
                if (needsOuterParenthesis) append(")")
                return@apply
            }
            INFIXR -> {
                // wi a b should give a -> b
                if (needsOuterParenthesis) append("(")
                val a = tree.children[0]
                val b = tree.children[1]
                val sna = diff.newOperators[a.id]
                transform(diff, a, a.children.isNotEmpty() && (sna == null || sna.precedence <= precedence))
                append(" ")
                append(sn.constant)
                append(" ")
                val snb = diff.newOperators[b.id]
                transform(diff, tree.children[1], b.children.isNotEmpty() && (snb == null || snb.precedence < precedence))
                if (needsOuterParenthesis) append(")")
                return@apply
            }
        }
    }


    val needsParenthesis = needsOuterParenthesis || tree.children.isNotEmpty()
    if (needsParenthesis) append("(")
    append(tree.id)
    for (child in tree.children) {
        append(" ")
        transform(diff, child, child.children.isNotEmpty())
    }
    if (needsParenthesis) append(")")
}

private fun StringBuilder.noTransform(tree: StringTree): StringBuilder = apply {
    val needsParenthesis = tree.children.isNotEmpty()
    if (needsParenthesis) append("(")
    append(tree.id)
    for (child in tree.children) {
        append(" ")
        noTransform(child)
    }
    if (needsParenthesis) append(")")
}