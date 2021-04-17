package org.mm0.kt

fun STrie?.keyStarting(keyToFind: CharSequence, position: Int): String? = if (this == null) null else when {
    compareKeyStarting(keyToFind, position, key) < 0 -> left.keyStarting(keyToFind, position)
    compareKeyStarting(keyToFind, position, key) > 0 -> right.keyStarting(keyToFind, position)
    else -> key
}

/** returns 0 if key is equal to a.substring(position, position+key.length)
 * even if there is stuff after in the charSequence */
private fun compareKeyStarting(a: CharSequence, position: Int, key: String): Int {
    val min = Integer.min(a.length - position, key.length)
    for (k in 0 until min) {
        val comp = a[position + k].compareTo(key[k])
        if (comp != 0) return comp
    }
    return if (a.length - position >= key.length) 0 else -1
}