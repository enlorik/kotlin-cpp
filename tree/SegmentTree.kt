/**
 * Point-update, range-aggregate segment tree. The operation must be associative
 * and [initial] a two-sided identity. Supports empty input.
 * All ranges are half-open [from, until); query/set/findPrefix are O(log n).
 * findPrefix requires a monotone false->true predicate over prefixes.
 */
class SegmentTree<T>(values: List<T>, val initial: T, val combine: (T, T) -> T) {
    val size: Int = values.size
    private val base: Int
    private val tree: Array<Any?>

    init {
        require(size <= (1 shl 29)) { "Too many elements for an array-backed segment tree" }
        var power = 1
        while (power < size) power = power shl 1
        base = power
        tree = arrayOfNulls(base * 2)
        for (i in tree.indices) tree[i] = initial
        for (i in values.indices) tree[base + i] = values[i]
        for (i in base - 1 downTo 1) tree[i] = combine(item(i * 2), item(i * 2 + 1))
    }

    @Suppress("UNCHECKED_CAST")
    private fun item(index: Int): T = tree[index] as T

    /** Current element at index (0-based). */
    operator fun get(index: Int): T {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        return item(base + index)
    }

    /** Replace one value and recompute ancestors in O(log n). */
    operator fun set(index: Int, value: T) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        var node = base + index
        tree[node] = value
        node /= 2
        while (node > 0) {
            tree[node] = combine(item(node * 2), item(node * 2 + 1))
            node /= 2
        }
    }

    /** Aggregate [from, until) in O(log n), preserving left-to-right order. */
    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid half-open range [$from,$until) for size $size"
        }
        var left = base + from
        var right = base + until
        var leftAcc = initial
        var rightAcc = initial
        while (left < right) {
            if ((left and 1) != 0) leftAcc = combine(leftAcc, item(left++))
            if ((right and 1) != 0) rightAcc = combine(item(--right), rightAcc)
            left /= 2
            right /= 2
        }
        return combine(leftAcc, rightAcc)
    }

    /** Inclusive Kotlin ranges: query(1..3) is equivalent to query(1, 4). */
    fun query(indices: IntRange): T {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    /** Shorthand for an inclusive range query: tree[1..3]. */
    operator fun get(indices: IntRange): T = query(indices)

    /** Aggregate the whole array, or return identity when empty. */
    fun all(): T = item(1)

    /**
     * First index i such that predicate(combine(a[0],...,a[i])) is true.
     * Returns null when no prefix qualifies. O(log n), not O(log^2 n).
     * Predicate must be monotonic as prefixes grow (e.g. nonnegative SUM).
     */
    fun findPrefix(predicate: (T) -> Boolean): Int? {
        if (size == 0) return null
        if (predicate(initial)) return 0
        if (!predicate(item(1))) return null
        var node = 1
        var before = initial
        while (node < base) {
            val next = combine(before, item(node * 2))
            if (predicate(next)) node *= 2
            else {
                before = next
                node = node * 2 + 1
            }
        }
        val index = node - base
        return if (index < size) index else null
    }
}

/** Build a segment tree over arbitrary associative values (generic monoid). */
fun <T> List<T>.segmentTree(initial: T, combine: (T, T) -> T): SegmentTree<T> =
    SegmentTree(this, initial, combine)

/** Sum segment tree by default; pass a different identity/reducer if needed. */
fun IntArray.segmentTree(
    initial: Int = 0,
    combine: (Int, Int) -> Int = { a, b -> a + b }
): SegmentTree<Int> = SegmentTree(asList(), initial, combine)

fun LongArray.segmentTree(
    initial: Long = 0L,
    combine: (Long, Long) -> Long = { a, b -> a + b }
): SegmentTree<Long> = SegmentTree(asList(), initial, combine)
