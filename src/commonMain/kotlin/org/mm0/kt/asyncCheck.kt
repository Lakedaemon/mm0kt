package org.mm0.kt

val simpleCanonizer = Canonizer()


/** check a sequence of MM0 statements and MMU directives
 * and stores the checked immutable statement/directive
 * in a Math structure.
 *
 * Ideally, the Math structure could be based on a RedBlackTree,
 * so that we can quickly access the Math environment to a point in the s/d sequence
 *
 * trees are only slightly smaller than hashmaps but offers the possibility to time travel,
 * which is desirable for maths
 *
 * statement/directives can be checked really fast, except for theorems which can have huge proofs.
 * So, it would be nice to check them in coroutines, for an immutable Math context
 * That way, we could proof check them in parallel, on different machines,
 * and get a report for each proof (passes or not, set of depending theorems)
 * that we would agregate later/lazily/reactively...
 *
 *
 * */


/** we take a sequence of MM0 and a sequence of MMU,
 * we check them, with regards to an existing Math Context
 * and we spit an enhanced Math Context,
 * as well as a sequence of (checked) MM0/MMU
 * */
fun simpleCheck(context: Context, theorem: M.Computer.Assertion.Theorem, binders: List<Binder>, proof: CharSequence): Unit = SimpleChecker(context, theorem, binders, proof, simpleCanonizer).check()