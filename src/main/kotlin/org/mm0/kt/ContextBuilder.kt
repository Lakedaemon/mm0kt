package org.mm0.kt

/** Contexts are supposed to be use in a single thread*/
interface ContextBuilder {
    val canonizer:Canonizer
    fun register(m: M)
    fun current():Context
 }

