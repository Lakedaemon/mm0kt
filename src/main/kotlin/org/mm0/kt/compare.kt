package org.mm0.kt

internal fun compare(a: CharSequence, b: CharSequence): Int {
    val min = Integer.min(a.length, b.length)
    for (k in 0 until min) {
        val comp = a[k].compareTo(b[k])
        if (comp != 0) return comp
    }
    val c = a.length - b.length
    return when {
        c < 0 -> -1
        c == 0 -> 0
        else -> 1
    }
}