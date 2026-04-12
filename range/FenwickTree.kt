class FenwickTree<T>(n: Int, val id: T, val combine: (T, T) -> T, val uncombine: (T, T) -> T) {
    private val n = n
    private val tree: Array<Any?>

    init {
        tree = Array(n + 1) { id }
    }

    fun update(i: Int, v: T) {
        var x = i
        while (x <= n) {
            @Suppress("UNCHECKED_CAST")
            tree[x] = combine(tree[x] as T, v)
            x += x and (-x)
        }
    }

    fun prefix(i: Int): T {
        var x = i
        var res = id
        while (x > 0) {
            @Suppress("UNCHECKED_CAST")
            res = combine(res, tree[x] as T)
            x -= x and (-x)
        }
        return res
    }

    fun query(l: Int, r: Int): T = uncombine(prefix(r), prefix(l - 1))

    // Find smallest index with prefix >= k; requires a sum-like tree with non-negative increments
    fun kth(k: T): Int {
        var pos = 0
        var cur = id
        var bitMask = Integer.highestOneBit(n)
        while (bitMask > 0) {
            val next = pos + bitMask
            if (next <= n) {
                @Suppress("UNCHECKED_CAST")
                val nxt = combine(cur, tree[next] as T)
                // Use uncombine to check: if nxt < k then move right
                // We rely on the caller using Long/Int so comparison is possible via uncombine trick.
                // Generic version: attempt to move right if combined value is still "less than" k.
                // For a generic tree we store the result and let callers cast appropriately.
                if (uncombine(k, nxt) != id) {
                    cur = nxt
                    pos = next
                }
            }
            bitMask = bitMask shr 1
        }
        return pos + 1
    }
}

fun <T> fw(n: Int, id: T, combine: (T, T) -> T, uncombine: (T, T) -> T): FenwickTree<T> =
    FenwickTree(n, id, combine, uncombine)

fun fw(n: Int): FenwickTree<Long> =
    FenwickTree(n, 0L, { a, b -> a + b }, { a, b -> a - b })
