package org.mm0.kt

fun CharSequence.charsEquals(other: CharSequence): Boolean {
    if (length != other.length) return false
    for (a in 0 until length) if (this[a] != other[a]) return false
    return true
}

fun CharSequence.charsHashCode(): Int {
    var hash = 0
    for (char in this) hash = hash * 31 + char.hashCode()
    return hash
}