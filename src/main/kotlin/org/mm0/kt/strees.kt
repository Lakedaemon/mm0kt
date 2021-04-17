package org.mm0.kt

fun <V> STree<V>?.find(keyToFind: CharSequence): V? = if (this == null) null else when {
    compare(keyToFind, key) < 0 -> left.find(keyToFind)
    compare(keyToFind, key) > 0 -> right.find(keyToFind)
    else -> value
}

/** in order efficient walk */
fun <V> STree<V>?.forEach(action:(String, V)->Unit) :Unit = if (this == null) Unit else {
    left.forEach(action)
    action(key, value)
    right.forEach(action)
}





fun <V> STree<V>?.put(key: String, value: V): STree<V> = this?.insert(key, value) ?: STree(true, null, key, value, null)



/** use forEach for performance reasons (no allocations, no suspend routines) */
fun <V> STree<V>?.keys(): Sequence<String> = sequence { giveKey(this@keys) }
fun <V> STree<V>?.values(): Sequence<V> = sequence { giveValue(this@values) }
fun <V> STree<V>?.keyValues(): Sequence<Pair<String, V>> = sequence { giveKeyValue(this@keyValues) }

private suspend fun <V> SequenceScope<String>.giveKey(tree: STree<V>?): Unit = if (tree == null) Unit else {
    giveKey(tree.left)
    yield(tree.key)
    giveKey(tree.right)
}

private suspend fun <V> SequenceScope<V>.giveValue(tree: STree<V>?): Unit = if (tree == null) Unit else {
    giveValue(tree.left)
    yield(tree.value)
    giveValue(tree.right)
}

private suspend fun <V> SequenceScope<Pair<String, V>>.giveKeyValue(tree: STree<V>?): Unit = if (tree == null) Unit else {
    giveKeyValue(tree.left)
    yield(Pair(tree.key, tree.value))
    giveKeyValue(tree.right)
}