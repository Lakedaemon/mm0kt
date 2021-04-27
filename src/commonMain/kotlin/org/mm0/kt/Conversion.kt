package org.mm0.kt

/** a convertible is a special proof that returns dual results (resA, resB) that represents the same stuff
 * with some definitions folded/unfolded
 *
 * Is is only used to prove that a MathTree A is exchangeable for a MathTree B
 *
 * */
sealed class Conversion {


    /** Terminal : returns a convertible for holders (name, name)
     *
     * it says that var ==[conv]== var
     * */
    class Holder(val name: String) : Conversion()

    // returns convertibilityResultOf(tree) = (first, second)
    /** returns a convertible proof that swaps (tree.resultA(), tree.resultB()) into (tree.resultB(), tree.resultA())
     *
     * if you have treeA ==[conv]== treeB
     * then you get treeB ==[conv]== treeA
     * */
    class Sym(val tree: Conversion) : Conversion()

    /** folds/unfolds a definition/term in a list of math expressions
     *
     * the def/term unified with the holders and dummies (*) should be equal to convertible.resultA()
     * if it holds, it returns a Pair(MM0Tree.Full(id, expressions) (*), convertible.resultB())
     *
     * id : the id of the definition to unfold/fold
     * holders : the list of arguments of the term/definition
     * dummies : list of dummy holders (they cannot be folded/unfolded)
     * convertible : Convertible
     *
     *   if you have defFormula(args) ==[conv]== tree
     *   then you get (defId, args) ==[conv]== tree
     *
     *
     * */
    class Unfold(val id: String, val holders: List<StringTree>, val dummies: List<String>, val conversion: Conversion) : Conversion()

    // returns                 Pair(
    //                    MM0Tree.Full(termOrDefId, proofs.map { it.first }),
    //                    MM0Tree.Full(termOrDefId, proofs.map { it.second })
    //                )

    /** (Terminal without args) applies a term or a def to folded/unfolded versions
     *
     *  Basically, it says that if you have arg(i)==[conv]==arg'
     *  then you get f(arg(i)) ==[conv]== f(arg'(i)
     *
     * */
    class Substitution(val termOrDefId: String, val conversions: List<Conversion>) : Conversion()
}