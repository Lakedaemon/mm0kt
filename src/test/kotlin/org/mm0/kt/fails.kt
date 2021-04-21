package org.mm0.kt

fun parsingFailsForMM0() = failMM0("parsingMM0") {
    "abstract def with declared dummies".test {
        sort("s")
        term("a", "s ()")
        def(tree="a", moreDummies = "x s", isAbstract = true)
    }
}

fun parsingFailsForMMU() = failMMU("parsingMMU") {
    // local def

    // local theorem

}

fun parsingFailsForBoth() = failBoth("parsingBoth") {

    // ascii
    "next line is not ascii".test { sort("s\u0085") }
    "no-break-space is not ascii".test { sort("s\u00A0") }

    // whitespace
    // GOT ME
    "tab is not a valid whitespace".test { sort("s\t") }
    // GOT ME
    "line tabulation is not a valid whitespace".test { sort("s\u000B") }
    // GOT ME
    "form feed is not a valid whitespace".test { sort("s\u000C") }
    // GOT ME
    "carriage return is not a valid whitespace".test { sort("s\u000D") }



    // identifier ::= [a-zA-Z_][a-zA-Z0-9_]
    "empty id".test { sort("") }
    "empty id".test { sort("\t") }
    "bad id".test { sort("0") }
    "bad id".test { sort("sot-") }

    // int
    // overflow int
    "negative int".test { op("plus", precedence = -1, constant = "+") }

    // formula
    "dollar in inner formula".test { both("a $ b") }
    "dollar in inner formula".test { leftRight("a $ b") }
    "dollar in inner formula".test { leftRight(right = listOf("a $ b")) }

    "garbage after last statement".test {
        sort("s")
        raw(" garbage ")
    }

    // delimiters


    // term

}


fun matchingFails() = failBoth("matching"){
//term

    "different binders order".test{
        sort("s")
        sort("t")
        mm0("term a: s > t > s;")
        mmu("(term a ((y t ())(x s ())) (s ())")
    }
}

fun registeringFails() = failBoth("registering") {
    // sort
    "duplicated ids for sorts".test {
        sort("s", false)
        sort("s", true)
    }

    // pure means that this sort does not have any term formers. It is an uninterpreted domain which may have variables but has no constant symbols, binary operators, or anything else targeting this sort. If a sort has this modifier, it is illegal to declare a term with this sort as the target.
    "term typed with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s ()")
    }



    // strict is the "opposite" of pure: it says that the sort does not have any variable binding operators. It is illegal to have a bound variable or dummy variable of this sort, and it cannot appear as a dependency in another variable. For example, if x: set and ph: wff x then set must not be declared strict. (pure and strict are not mutually exclusive, although a sort with both properties is not very useful.)
    "dummy variable with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s ()", "x s")
    }

    "dependency with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s (x)", "x s ()")
    }
    // provable means that the sort is a thing that can be "proven". All formulas appearing in axioms and definitions (between $) must have a provable sort.*/
    "assertion formula without provable sort".test {
        sort("s")
        term("a", "s ()")
        axiom("b", "a", "x s ()")
        mm0("axiom b (x:s): $ a $;")
    }
    // free means that dummy variables may not be dropped in definitions unless they appear in binding syntax constructors.
    // how to test that ? What does it mean ?
    // pure means that this sort does not have any term formers. It is an uninterpreted domain which may have variables but has no constant symbols, binary operators, or anything else targeting this sort. If a sort has this modifier, it is illegal to declare a term with this sort as the target.
    "term typed with a pure sort".test {
        sort("s", isFree = true)
        term("f", "s ()", "x s")
        mmu("(def y (s ()) ()")
        def("y", "s ()", "(f x)", isMMUOnly = true)
        mm0("def y (.x: s): s = \$ f x \$;")
    }

    // coercion
    // GOT ME
    "duplicated ids for coercion".test {
        sort("s")
        sort("t")
        sort("u")
        coercion("c", "s", "t")
        coercion("c", "s", "u")
    }


    "not unique coercion path".test {
        sort("s")
        sort("t")
        sort("u")
        coercion("c1", "s", "t")
        coercion("c2", "t", "u")
        coercion("c3", "s", "u")
    }

    "duplicated id for terms".test {
        sort("s")
        sort("t")
        term("a", "s ()")
        term("a", "t ()")
    }

    "term with non-existent sort".test { term("a", "s ()") }

    "term shadowing a def".test {
        sort("s")
        term("a", "s ()")
        def("b", "s ()", "a")
        term("b", "s ()")
    }

    // def
    "def shadowing a term".test {
        sort("s")
        term("a", "s ()")
        term("b", "s ()")
        def("a", "s ()", "b")
    }
    "recursive def".test {
        sort("s")
        def("a", "s ()", "a")
    }
}

fun proofCheckingFail() {}

fun fail() {
    parsingFailsForMM0()
    parsingFailsForMMU()
    parsingFailsForBoth()
    matchingFails()
    registeringFails()
    proofCheckingFail()
}