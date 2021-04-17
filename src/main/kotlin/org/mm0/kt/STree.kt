package org.mm0.kt


/**
 * A persistent (immutable, thread safe), flexible and light Red Black Tree
 * that only supports inserting and search, for now
 *
 * We separate key from value to speed up finding
 * otherwise we would either have to build an entry
 * or to have a sealed class/interface with a (thread-associated) mutable entry and an immutable one
 *
 * To implement lighter sets we would have to duplicate this class
 * but we mostly want maps
 *
 * The empty tree is modelled as a Tree<K,V>? null
 * As this is an abstract class, it needs the derived class to implement a few things to make it work,
 * like a static method to instancing a Tree holding one (key, value) pair
 * */
// TODO use a sealed class to implement color
data class STree<V>(val isBlack: Boolean, val left: STree<V>?, val key: String, val value: V, val right: STree<V>?) {

    /** with this, we can have arbitrary keys, including CharSequence */

    fun insert(newKey: String, newValue: V): STree<V> {
        val result = insertInto(this, newKey, newValue)
        return if (result.isBlack) result else result.copy(true)
    }

    private fun insertInto(tree: STree<V>?, newKey: String, newValue: V): STree<V> = when {
        tree == null -> copy(isBlack = true, left = null, key = newKey, value = newValue, right = null)
        newKey < tree.key -> balance(tree.copy(left = insertInto(tree.left, newKey, newValue)))
        newKey > tree.key -> balance(tree.copy(right = insertInto(tree.right, newKey, newValue)))
        newValue != tree.value -> tree.copy(value = newValue)
        else -> tree
    }

    private fun <V> balance(tree: STree<V>): STree<V> = with(tree) {
        if (!isBlack) return this
        if (left != null && !this.left.isBlack) {
            if (left.left != null && !this.left.left.isBlack) return buildBalancedTree(leftLeft = left.left.left, leftKey = left.left.key, leftValue = left.left.value, leftRight = left.left.right, midKey = left.key, midValue = left.value, rightLeft = left.right, rightKey = key, rightValue = value, rightRight = right)
            if (left.right != null && !left.right.isBlack) return buildBalancedTree(leftLeft = left.left, leftKey = left.key, leftValue = left.value, leftRight = left.right.left, midKey = left.right.key, midValue = left.right.value, rightLeft = left.right.right, rightKey = key, rightValue = value, rightRight = right)
        }
        if (right != null && !right.isBlack) {
            if (right.left != null && !right.left.isBlack) return buildBalancedTree(leftLeft = left, leftKey = key, leftValue = value, leftRight = right.left.left, midKey = right.left.key, midValue = right.left.value, rightLeft = right.left.right, rightKey = right.key, rightValue = right.value, rightRight = right.right)
            if (right.right != null && !right.right.isBlack) return buildBalancedTree(leftLeft = left, leftKey = key, leftValue = value, leftRight = right.left, midKey = right.key, midValue = right.value, rightLeft = right.right.left, rightKey = right.right.key, rightValue = right.right.value, rightRight = right.right.right)
        }
        return this
    }

    private fun buildBalancedTree(leftLeft: STree<V>?, leftKey: String, leftValue: V, leftRight: STree<V>?, midKey: String, midValue: V, rightLeft: STree<V>?, rightKey: String, rightValue: V, rightRight: STree<V>?) = copy(isBlack = false, left = copy(true, leftLeft, leftKey, leftValue, leftRight), key = midKey, value = midValue, right = copy(true, rightLeft, rightKey, rightValue, rightRight))
}

