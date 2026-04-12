class LazySegTree<T, L>(
    n: Int,
    val valueId: T,
    val tagId: L,
    val merge: (T, T) -> T,
    val apply: (T, L, Int) -> T,
    val compose: (L, L) -> L
) {
    private val size: Int
    private val tree: Array<Any?>
    private val lazy: Array<Any?>
    private val len: IntArray

    init {
        var s = 1
        while (s < n) s = s shl 1
        size = s
        tree = Array(2 * size) { valueId }
        lazy = Array(2 * size) { tagId }
        len = IntArray(2 * size)
        for (i in 1 until size) len[i] = size shr Integer.numberOfTrailingZeros(i)
        for (i in size until 2 * size) len[i] = 1
    }

    @Suppress("UNCHECKED_CAST")
    private fun push(i: Int) {
        val tag = lazy[i] as L
        if (tag == tagId) return
        applyNode(2 * i, tag)
        applyNode(2 * i + 1, tag)
        lazy[i] = tagId
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyNode(i: Int, tag: L) {
        tree[i] = apply(tree[i] as T, tag, len[i])
        lazy[i] = compose(lazy[i] as L, tag)
    }

    @Suppress("UNCHECKED_CAST")
    private fun pull(i: Int) {
        tree[i] = merge(tree[2 * i] as T, tree[2 * i + 1] as T)
    }

    fun update(l: Int, r: Int, tag: L) {
        update(1, 0, size, l, r, tag)
    }

    @Suppress("UNCHECKED_CAST")
    private fun update(node: Int, lo: Int, hi: Int, l: Int, r: Int, tag: L) {
        if (r <= lo || hi <= l) return
        if (l <= lo && hi <= r) { applyNode(node, tag); return }
        push(node)
        val mid = (lo + hi) / 2
        update(2 * node, lo, mid, l, r, tag)
        update(2 * node + 1, mid, hi, l, r, tag)
        pull(node)
    }

    fun query(l: Int, r: Int): T = query(1, 0, size, l, r)

    @Suppress("UNCHECKED_CAST")
    private fun query(node: Int, lo: Int, hi: Int, l: Int, r: Int): T {
        if (r <= lo || hi <= l) return valueId
        if (l <= lo && hi <= r) return tree[node] as T
        push(node)
        val mid = (lo + hi) / 2
        return merge(query(2 * node, lo, mid, l, r), query(2 * node + 1, mid, hi, l, r))
    }
}

fun <T, L> lazy(
    n: Int, valueId: T, tagId: L,
    merge: (T, T) -> T,
    apply: (T, L, Int) -> T,
    compose: (L, L) -> L
): LazySegTree<T, L> = LazySegTree(n, valueId, tagId, merge, apply, compose)
