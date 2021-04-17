package org.mm0.kt

data class STrie(val isBlack: Boolean, val left: STrie?, val key: String, val right: STrie?) {

    /** with this, we can have arbitrary keys, including CharSequence */

    fun insert(newKey:String): STrie {
        val result = insertInto(this, newKey)
        return if (result.isBlack) result else result.copy(true)
    }

    private fun insertInto(trie: STrie?, newKey: String): STrie = when {
        trie == null -> copy(true, null, newKey, null)
        newKey < trie.key -> balance(trie.copy(left = insertInto(trie.left, newKey)))
        newKey > trie.key -> balance(trie.copy(right = insertInto(trie.right, newKey)))
        else -> trie
    }

    private fun balance(trie: STrie): STrie = with(trie) {
        if (!isBlack) return this
        if (left != null && !this.left.isBlack) {
            if (left.left != null && !this.left.left.isBlack) return buildBalancedTree(leftLeft = left.left.left, leftKey = left.left.key, leftRight = left.left.right, midKey = left.key, rightLeft = left.right, rightKey = key, rightRight = right)
            if (left.right != null && !left.right.isBlack) return buildBalancedTree(leftLeft = left.left, leftKey = left.key, leftRight = left.right.left, midKey = left.right.key, rightLeft = left.right.right, rightKey = key, rightRight = right)
        }
        if (right != null && !right.isBlack) {
            if (right.left != null && !right.left.isBlack) return buildBalancedTree(leftLeft = left, leftKey = key, leftRight = right.left.left, midKey = right.left.key, rightLeft = right.left.right, rightKey = right.key, rightRight = right.right)
            if (right.right != null && !right.right.isBlack) return buildBalancedTree(leftLeft = left, leftKey = key, leftRight = right.left, midKey = right.key, rightLeft = right.right.left, rightKey = right.right.key, rightRight = right.right.right)
        }
        return this
    }

    private fun buildBalancedTree(leftLeft: STrie?, leftKey: String, leftRight: STrie?, midKey: String, rightLeft: STrie?, rightKey: String, rightRight: STrie?) = copy(isBlack = false, left = copy(true, left=leftLeft, key=leftKey, right=leftRight), key = midKey, right = copy(true, left=rightLeft, key=rightKey, right=rightRight))
}