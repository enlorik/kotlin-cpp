/**
 * Range add, range assign, and range sum in O(log n).
 * Uses half-open ranges [from, until); arithmetic must fit in Long.
 * Unlike a normal SegmentTree, updates may cover entire intervals.
 */
class LazySegmentTree private constructor(values: LongArray) {
    val size: Int = values.size
    private val capacity = maxOf(1, size) * 4
    private val sums = LongArray(capacity)
    private val pendingAdd = LongArray(capacity)
    private val pendingSet = LongArray(capacity)
    private val hasSet = BooleanArray(capacity)

    init {
        require(size <= Int.MAX_VALUE / 4) { "Too many elements" }
        if (size > 0) build(1, 0, size, values)
    }

    private fun build(node: Int, lo: Int, hi: Int, a: LongArray) {
        if (hi - lo == 1) { sums[node] = a[lo]; return }
        val mid = lo + (hi - lo) / 2
        build(node * 2, lo, mid, a)
        build(node * 2 + 1, mid, hi, a)
        sums[node] = sums[node * 2] + sums[node * 2 + 1]
    }

    private fun applySet(node: Int, length: Int, value: Long) {
        sums[node] = value * length
        hasSet[node] = true
        pendingSet[node] = value
        pendingAdd[node] = 0
    }

    private fun applyAdd(node: Int, length: Int, delta: Long) {
        sums[node] += delta * length
        if (hasSet[node]) pendingSet[node] += delta
        else pendingAdd[node] += delta
    }

    private fun push(node: Int, lo: Int, hi: Int) {
        if (hi - lo <= 1) return
        val mid = lo + (hi - lo) / 2
        val left = node * 2
        val right = left + 1
        if (hasSet[node]) {
            applySet(left, mid - lo, pendingSet[node])
            applySet(right, hi - mid, pendingSet[node])
            hasSet[node] = false
        }
        if (pendingAdd[node] != 0L) {
            applyAdd(left, mid - lo, pendingAdd[node])
            applyAdd(right, hi - mid, pendingAdd[node])
            pendingAdd[node] = 0
        }
    }

    private fun validate(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
    }

    fun add(from: Int, until: Int, delta: Long) {
        validate(from, until)
        if (from < until) modify(1, 0, size, from, until, delta, false)
    }

    /** Assign value to every element of [from, until). */
    fun set(from: Int, until: Int, value: Long) {
        validate(from, until)
        if (from < until) modify(1, 0, size, from, until, value, true)
    }

    private fun modify(node: Int, lo: Int, hi: Int, from: Int, until: Int, value: Long, assign: Boolean) {
        if (from <= lo && hi <= until) {
            if (assign) applySet(node, hi - lo, value) else applyAdd(node, hi - lo, value)
            return
        }
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (from < mid) modify(node * 2, lo, mid, from, until, value, assign)
        if (until > mid) modify(node * 2 + 1, mid, hi, from, until, value, assign)
        sums[node] = sums[node * 2] + sums[node * 2 + 1]
    }

    fun query(from: Int, until: Int): Long {
        validate(from, until)
        if (from == until) return 0L
        return queryRec(1, 0, size, from, until)
    }

    private fun queryRec(node: Int, lo: Int, hi: Int, from: Int, until: Int): Long {
        if (from <= lo && hi <= until) return sums[node]
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        var result = 0L
        if (from < mid) result += queryRec(node * 2, lo, mid, from, until)
        if (until > mid) result += queryRec(node * 2 + 1, mid, hi, from, until)
        return result
    }

    fun all(): Long = if (size == 0) 0L else sums[1]

    companion object {
        fun of(values: LongArray): LazySegmentTree = LazySegmentTree(values.copyOf())
        fun zeros(size: Int): LazySegmentTree {
            require(size >= 0 && size <= Int.MAX_VALUE / 4) { "Invalid size" }
            return LazySegmentTree(LongArray(size))
        }
    }
}

fun lazySegmentTree(n: Int): LazySegmentTree = LazySegmentTree.zeros(n)
fun IntArray.lazySegmentTree(): LazySegmentTree = LazySegmentTree.of(LongArray(size) { this[it].toLong() })
fun LongArray.lazySegmentTree(): LazySegmentTree = LazySegmentTree.of(this)
