/**
 * An abelian group: associative and commutative combine, two-sided identity,
 * and inverse(x) such that combine(x, inverse(x)) == identity.
 * The caller must uphold these algebraic laws for custom groups.
 */
class AbelianGroup<T>(val op: AggregateOp<T>, val inverse: (T) -> T)

/**
 * Fenwick tree for a commutative invertible aggregation, such as SUM or XOR.
 * Construction O(n), prefix/range/point operations O(log n), memory O(n).
 * All ranges are half-open; a new group does not alter the older sum-only
 * FenwickTree or its Long-valued default API.
 */
class GroupFenwickTree<T>(values: List<T>, val group: AbelianGroup<T>) {
    val size: Int = values.size
    private val a = values.toMutableList()
    private val bit = MutableList(size + 1) { group.op.initial }

    init {
        for (i in a.indices) {
            val p = i + 1
            bit[p] = group.op.combine(bit[p], a[i])
            val parent = p + (p and -p)
            if (parent <= size) bit[parent] = group.op.combine(bit[parent], bit[p])
        }
    }

    operator fun get(index: Int): T {
        require(index in 0 until size) { "Index $index out of bounds" }
        return a[index]
    }

    /** Combine one delta with the current value; XOR deltas toggle bits. */
    fun apply(index: Int, delta: T) {
        require(index in 0 until size) { "Index $index out of bounds" }
        a[index] = group.op.combine(a[index], delta)
        var i = index + 1
        while (i <= size) {
            bit[i] = group.op.combine(bit[i], delta)
            i += i and -i
        }
    }

    /** Sum-style convenience; for XOR, use apply(index, mask). */
    fun add(index: Int, delta: T) = apply(index, delta)

    fun set(index: Int, value: T) {
        val old = get(index)
        apply(index, group.op.combine(group.inverse(old), value))
    }

    fun prefix(until: Int): T {
        require(until in 0..size) { "Prefix endpoint out of bounds" }
        var i = until
        var result = group.op.initial
        while (i > 0) {
            result = group.op.combine(result, bit[i])
            i -= i and -i
        }
        return result
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        return group.op.combine(group.inverse(prefix(from)), prefix(until))
    }

    fun all(): T = prefix(size)
}

/** Built-in SUM and XOR are abelian groups; MIN/MAX/OR/AND are not. */
fun IntArray.fenwickTree(op: AggregateOp<Int>): GroupFenwickTree<Int> {
    val inverse: (Int) -> Int = when (op) {
        IntAggregations.SUM -> { x -> -x }
        IntAggregations.XOR -> { x -> x }
        else -> throw IllegalArgumentException("Fenwick range queries require invertible operations (SUM or XOR); use a segment tree for MIN/MAX/OR/AND")
    }
    return GroupFenwickTree(asList(), AbelianGroup(op, inverse))
}

fun LongArray.fenwickTree(op: AggregateOp<Long>): GroupFenwickTree<Long> {
    val inverse: (Long) -> Long = when (op) {
        LongAggregations.SUM -> { x -> -x }
        LongAggregations.XOR -> { x -> x }
        else -> throw IllegalArgumentException("Fenwick range queries require invertible operations (SUM or XOR); use a segment tree for MIN/MAX/OR/AND")
    }
    return GroupFenwickTree(asList(), AbelianGroup(op, inverse))
}

fun <T> List<T>.fenwickTree(group: AbelianGroup<T>): GroupFenwickTree<T> = GroupFenwickTree(this, group)
fun IntArray.fenwickTree(group: AbelianGroup<Int>): GroupFenwickTree<Int> = GroupFenwickTree(asList(), group)
fun LongArray.fenwickTree(group: AbelianGroup<Long>): GroupFenwickTree<Long> = GroupFenwickTree(asList(), group)
