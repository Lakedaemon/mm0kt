package org.mm0.kt

fun parsingDualFails() {
    // ascii
    "next line is not ascii".fails { sort("\u0085 s\u0085") }
    "no-break-space is not ascii".fails { sort("\u00A0 s\u00A0") }

    // identifier ::= [a-zA-Z_][a-zA-Z0-9_]
    "empty id".fails { sort("") }
    "empty id".fails { sort("\t") }
    "bad id".fails { sort("0") }
    "bad id".fails { sort("sot-") }

    // int
    // overflow int
    "negative int".fails { op("plus", precedence = -1, constant = "+") }

    // formula

    // delimiters
    "empty delimiters".fails { both() }
    "empty delimiters".fails { leftRight() }

    // term
    "term typed with a dummy".fails {
        sort("s")
        term("a", "s")
    }
}

fun matchingFails() {

}

fun registeringFails() {
    // sort
    "duplicated ids for sorts".fails {
        sort("s", false)
        sort("s", true)
    }

    // pure means that this sort does not have any term formers. It is an uninterpreted domain which may have variables but has no constant symbols, binary operators, or anything else targeting this sort. If a sort has this modifier, it is illegal to declare a term with this sort as the target.
    "term typed with a pure sort".fails {
        sort("s", isPure = true)
        term("a", "s ()")
    }

    // strict is the "opposite" of pure: it says that the sort does not have any variable binding operators. It is illegal to have a bound variable or dummy variable of this sort, and it cannot appear as a dependency in another variable. For example, if x: set and ph: wff x then set must not be declared strict. (pure and strict are not mutually exclusive, although a sort with both properties is not very useful.)
    "dummy variable with a pure sort".fails {
        sort("s", isStrict = true)
        term("a", "s ()", "x s ()")
    }

    "dependency with a pure sort".fails {
        sort("s", isPure = true)
        term("a", "s (x)", "x s ()")
    }
    // provable means that the sort is a thing that can be "proven". All formulas appearing in axioms and definitions (between $) must have a provable sort.*/
    "formula without provable sort".fails {
        sort("s")
        term("a", "s")
        def("b", "s ()", "a")
    }
    // free means that dummy variables may not be dropped in definitions unless they appear in binding syntax constructors.
    // how to test that ? What does it mean ?


    // coercion
    "duplicated ids for coercion".fails {
        sort("s")
        sort("t")
        sort("u")
        coercion("c", "s", "t")
        coercion("c", "s", "u")
    }
    "idempotent coercion".fails {
        sort("s")
        coercion("c", "s", "s")
    }

    "not unique coercion path".fails {
        sort("s")
        sort("t")
        sort("u")
        coercion("c1", "s", "t")
        coercion("c2", "t", "u")
        coercion("c3", "s", "u")
    }

    "duplicated id for terms".fails {
        sort("s")
        sort("t")
        term("a", "s ()")
        term("a", "t ()")
    }
    "term with non-existent sort".fails { term("a", "s ()") }

    "term shadowing a def".fails {
        sort("s")
        term("a", "s ()")
        def("b", "s ()", "a")
        term("b", "s ()")
    }

    // def
    "def shadowing a term".fails {
        sort("s")
        term("a", "s ()")
        term("b", "s ()")
        def("a", "s ()", "b")
    }
    "recursive def".fails {
        sort("s")
        def("a", "s ()", "a")
    }
}

fun proofCheckingFail() {}

fun fail() {
    parsingDualFails()
    matchingFails()
    registeringFails()
    proofCheckingFail()
}