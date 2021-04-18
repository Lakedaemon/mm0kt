package org.mm0.kt

val failTests = listOf(
    "empty id".fail(sort("")),
    "empty id".fail(sort("\t")),
    "no break space is not ascii".fail(sort(" s")),
    /** identifier ::= [a-zA-Z_][a-zA-Z0-9_]**/
    "bad id for sorts".fail(sort("0")),
    "bad id for sorts".fail(sort("sot-")),
    "duplicated ids for sorts".fail(sort("s", false), sort("s", true)),
    "term typed with a pure sort".fail(sort("s", isPure = true), term("a", "s")),
)