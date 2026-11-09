/**
 * Unboxed Long-sum segment tree with range addition and assignment.
 * Build O(n), add/assign/query O(log n), memory O(n).
 *
 * All intervals are [from, until); arithmetic uses Kotlin Long wraparound.
 * Source is copied by building the tree; later source changes are invisible.
 */
class LongSumLazyTree(values: LongArray) {
    val size = values.size
    private val sums: LongArray
    private val pendingAdd: LongArray
    private val pendingAssign: LongArray
    private val hasAssign: BooleanArray

    init {
        require(size <= (1 shl 28)) { "Too many elements for an array-backed tree" }
        val capacity = maxOf(4, size * 4)
        sums = LongArray(capacity)
        pendingAdd = LongArray(capacity)
        pendingAssign = LongArray(capacity)
        hasAssign = BooleanArray(capacity)
        if (size > 0) build(1, 0, size, values)
    }

    private fun build(node: Int, lo: Int, hi: Int, values: LongArray) {
        if (hi - lo == 1) {
            sums[node] = values[lo]
            return
        }
        val mid = lo + (hi - lo) / 2
        build(node * 2, lo, mid, values)
        build(node * 2 + 1, mid, hi, values)
        pull(node)
    }

    private fun pull(node: Int) {
        sums[node] = sums[node * 2] + sums[node * 2 + 1]
    }

    private fun applyAssign(node: Int, length: Int, value: Long) {
        sums[node] = value * length.toLong()
        hasAssign[node] = true
        pendingAssign[node] = value
        pendingAdd[node] = 0L
    }

    private fun applyAdd(node: Int, length: Int, delta: Long) {
        sums[node] += delta * length.toLong()
        if (hasAssign[node]) pendingAssign[node] += delta
        else pendingAdd[node] += delta
    }

    private fun push(node: Int, lo: Int, hi: Int) {
        if (hi - lo <= 1) return
        val mid = lo + (hi - lo) / 2
        val left = node * 2
        val right = left + 1
        if (hasAssign[node]) {
            val value = pendingAssign[node]
            applyAssign(left, mid - lo, value)
            applyAssign(right, hi - mid, value)
            hasAssign[node] = false
        }
        val delta = pendingAdd[node]
        if (delta != 0L) {
            applyAdd(left, mid - lo, delta)
            applyAdd(right, hi - mid, delta)
            pendingAdd[node] = 0L
        }
    }

    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid range [$from, $until) for size $size"
        }
    }

    fun add(from: Int, until: Int, delta: Long) {
        checkRange(from, until)
        if (from < until) addRec(1, 0, size, from, until, delta)
    }

    private fun addRec(
        node: Int, lo: Int, hi: Int,
        from: Int, until: Int, delta: Long
    ) {
        if (from <= lo && hi <= until) {
            applyAdd(node, hi - lo, delta)
            return
        }
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (from < mid) addRec(node * 2, lo, mid, from, until, delta)
        if (until > mid) addRec(node * 2 + 1, mid, hi, from, until, delta)
        pull(node)
    }

    fun assign(from: Int, until: Int, value: Long) {
        checkRange(from, until)
        if (from < until) assignRec(1, 0, size, from, until, value)
    }

    private fun assignRec(
        node: Int, lo: Int, hi: Int,
        from: Int, until: Int, value: Long
    ) {
        if (from <= lo && hi <= until) {
            applyAssign(node, hi - lo, value)
            return
        }
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (from < mid) assignRec(node * 2, lo, mid, from, until, value)
        if (until > mid) assignRec(node * 2 + 1, mid, hi, from, until, value)
        pull(node)
    }

    fun query(from: Int, until: Int): Long {
        checkRange(from, until)
        return if (from == until) 0L else queryRec(1, 0, size, from, until)
    }

    private fun queryRec(node: Int, lo: Int, hi: Int, from: Int, until: Int): Long {
        if (from <= lo && hi <= until) return sums[node]
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (until <= mid) return queryRec(node * 2, lo, mid, from, until)
        if (from >= mid) return queryRec(node * 2 + 1, mid, hi, from, until)
        return queryRec(node * 2, lo, mid, from, until) +
            queryRec(node * 2 + 1, mid, hi, from, until)
    }

    fun all(): Long = if (size == 0) 0L else sums[1]
}

/** Explicit unboxed alternative to a generic lazy segment tree. */
fun LongArray.longSumLazyTree(): LongSumLazyTree = LongSumLazyTree(this)
fun IntArray.longSumLazyTree(): LongSumLazyTree =
    LongSumLazyTree(LongArray(size) { this[it].toLong() })
