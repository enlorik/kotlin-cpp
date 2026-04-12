class SegTree<T>(n: Int, val id: T, val merge: (T, T) -> T) {
    private val size: Int
    private val tree: Array<Any?>

    init {
        var s = 1
        while (s < n) s = s shl 1
        size = s
        tree = arrayOfNulls(2 * size)
        tree.fill(id)
    }

    fun update(i: Int, v: T) {
        var pos = i + size
        tree[pos] = v
        pos = pos shr 1
        while (pos >= 1) {
            @Suppress("UNCHECKED_CAST")
            tree[pos] = merge(tree[pos * 2] as T, tree[pos * 2 + 1] as T)
            pos = pos shr 1
        }
    }

    fun query(l: Int, r: Int): T {
        @Suppress("UNCHECKED_CAST")
        var resL = id
        @Suppress("UNCHECKED_CAST")
        var resR = id
        var lo = l + size
        var hi = r + size
        while (lo < hi) {
            if (lo and 1 == 1) { resL = merge(resL, tree[lo] as T); lo++ }
            if (hi and 1 == 1) { hi--; resR = merge(tree[hi] as T, resR) }
            lo = lo shr 1
            hi = hi shr 1
        }
        return merge(resL, resR)
    }
}

fun <T> st(n: Int, id: T, merge: (T, T) -> T): SegTree<T> = SegTree(n, id, merge)
