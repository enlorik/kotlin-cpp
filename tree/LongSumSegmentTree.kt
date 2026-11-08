/**
 * Unboxed Long sum segment tree for memory-sensitive contest solutions.
 *
 * Point assignments and half-open interval sums take O(log n); construction
 * is O(n), memory is O(n). Sum uses Long arithmetic with JVM wraparound on
 * overflow, like the existing generic Long-sum segment tree.
 *
 * Unlike SegmentTree<T>, this only supports SUM. For MIN, MAX, custom
 * operations, or noncommutative operations, use the generic segment tree.
 */
class LongSumSegmentTree(values: LongArray) {
    val size: Int = values.size
    private val base: Int
    private val sums: LongArray

    init {
        require(size <= (1 shl 29)) { "Too many elements for an array-backed segment tree" }
        var power = 1
        while (power < size) power = power shl 1
        base = power
        sums = LongArray(base * 2)
        values.copyInto(sums, destinationOffset = base)
        for (i in base - 1 downTo 1) sums[i] = sums[i * 2] + sums[i * 2 + 1]
    }

    operator fun get(index: Int): Long {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        return sums[base + index]
    }

    /** Replaces one element and recomputes its ancestors in O(log n). */
    operator fun set(index: Int, value: Long) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        var node = base + index
        sums[node] = value
        node /= 2
        while (node > 0) {
            sums[node] = sums[node * 2] + sums[node * 2 + 1]
            node /= 2
        }
    }

    /** Sums [from, until), returning 0 for an empty range. */
    fun query(from: Int, until: Int): Long {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid half-open range [$from,$until) for size $size"
        }
        var left = from + base
        var right = until + base
        var result = 0L
        while (left < right) {
            if ((left and 1) != 0) result += sums[left++]
            if ((right and 1) != 0) result += sums[--right]
            left /= 2
            right /= 2
        }
        return result
    }

    /** Kotlin inclusive IntRange; tree[1..3] sums indices 1, 2, 3. */
    fun query(indices: IntRange): Long {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    operator fun get(indices: IntRange): Long = query(indices)
    fun all(): Long = sums[1]

    /**
     * First index whose inclusive prefix sum satisfies a monotone predicate,
     * or null if none does. For example, cumulative sums of nonnegative values.
     * The predicate must transition only false -> true as the prefix grows.
     */
    fun findPrefix(predicate: (Long) -> Boolean): Int? {
        if (size == 0) return null
        if (predicate(0L)) return 0
        if (!predicate(sums[1])) return null
        var node = 1
        var before = 0L
        while (node < base) {
            val next = before + sums[node * 2]
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

/** Fast sum-only tree backed by unboxed LongArray nodes. */
fun LongArray.sumSegmentTree(): LongSumSegmentTree = LongSumSegmentTree(this)

/** Int inputs widened to Long so totals do not overflow at 32 bits. */
fun IntArray.sumSegmentTree(): LongSumSegmentTree =
    LongSumSegmentTree(LongArray(size) { this[it].toLong() })
