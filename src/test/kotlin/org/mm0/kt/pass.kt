package org.mm0.kt


fun parsingPassForMM0() = passMM0("parsingMM0") {

}

fun parsingPassForMMU() = passMMU("parsingMMU") {

}

fun parsingPassForBoth() = passBoth("parsingBoth") {
    // mm0 and mmu syntax

    // whitespace
    "space is a valid whitespace".test { sort("s ") }
    "line feed is a valid whitespace".test { sort("s\n") }

    "empty delimiters".test { both() }
    "empty delimiters".test { leftRight() }



    // int
    "big int support".test { op("plus", precedence = 2046, constant = "+") }
    // delimiters
    "duplicate delimiters".test { both("(", "(") }
    "duplicate delimiters".test { leftRight("(", "(") }
    "duplicate delimiters".test { leftRight(right = listOf("(", "(")) }
}

fun matchingPass() = passBoth("matching") {
    // should I do all 16 possibilities ?
    "sort".test { sort("s") }
    "sort".test { sort("s", isPure = true) }
    "sort".test { sort("s", isProvable = true) }
    "sort".test { sort("s", isStrict = true) }
    "sort".test { sort("s", isFree = true) }
    "sort".test { sort("s", isFree = true, isStrict = true) }


    // term
    "same order for binders".test {
        sort("s")
        term("a", "s", "(x:s)","(y:s)")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        term("a", "s", "(x:s)", "(y:t)")
    }
    "same order for binders".test {
        sort("s")
        term("a", "s", "(x y:s)")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        term("a", "s > t > s")
    }
}

fun registeringPass() = passBoth("registering") {
    "reflexive coercion".test {
        sort("s")
        coercion("c", "s", "s")
    }



}

fun proofCheckingPass() = passBoth("proofChecking") {
    "blank file".test { raw(" \n ") }
    "trivial file".test { both("( )") }
}

fun optionalBoth() = passBoth("optionalBoth") {
    // verifiers are only required to support up to prec 2046
    "big int support".test { op("plus", precedence = Int.MAX_VALUE - 1, constant = "+") }

    // Not a fail test, since verifiers are permitted to alpha rename. Maybe "optional success"?
    "different binders order".test {
        sort("s")
        mm0("term a (x y: s):s;")
        mmu("(term a ((y s ())(x s ())) (s ())")
    }
}

fun pass() {
    parsingPassForMM0()
    parsingPassForMMU()
    parsingPassForBoth()
    matchingPass()
    registeringPass()
    proofCheckingPass()
}