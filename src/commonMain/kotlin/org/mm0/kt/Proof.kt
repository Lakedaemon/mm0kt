package org.mm0.kt

sealed class Proof  {
    object Missing : Proof()
    data class Hypothesis(val hypothesisName: String, val expectedResult: StringTree) : Proof()

    // check if proof has expectedResultForProof. If it does, return expectedResult
    data class Record(val savedExpressionId: String, val savedProofResult: StringTree, val expectedResultForProof: StringTree, val proof: Proof, val secondProof: Proof) : Proof()

    /**
     * allow proofs to work on folded/unfolded parts
     *
     * expectedResult should be equal to convertible.resultA()
     * convertible.resultB() should be equal to proof.result()
     *
     * return expectedResult
     * */
    data class Conv(val expectedResult: StringTree, val conversion: Conversion, val expectedResultForProof: StringTree, val proof: Proof) : Proof()

    data class Holder(val name: String) : Proof()

    sealed class Assert(val expectedResult: StringTree, val id: String, val arguments: List<StringTree>, val hypothesisProofs: List<Proof>) : Proof() {
        /** Theorem Result should be equal to expected result*/
        class Axiom(expectedResult: StringTree, id: String, arguments: List<StringTree>, hypothesisProofs: List<Proof>) : Assert(expectedResult, id, arguments, hypothesisProofs)
        class Theorem(expectedResult: StringTree, id: String, arguments: List<StringTree>, hypothesisProofs: List<Proof>) : Assert(expectedResult, id, arguments, hypothesisProofs)
    }
}