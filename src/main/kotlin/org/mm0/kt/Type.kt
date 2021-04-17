package org.mm0.kt

class Type(val sort: String, val dependencies: List<String>) {
    override fun equals(other: Any?): Boolean = other is Type && sort == other.sort && dependencies==other.dependencies
    override fun hashCode(): Int = 31 * sort.hashCode() + dependencies.hashCode()
}

