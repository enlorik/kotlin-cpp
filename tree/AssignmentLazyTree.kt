/**
 * Compute value combined with itself `count` times in O(log count)
 * operations (binary exponentiation in an arbitrary associative monoid).
 * Works for noncommutative operations because every factor is `value`.
 */
private fun <T> monoidPower(value: T, count: Int, op: AggregateOp<T>): T {
    require(count >= 0) { "Repeat count must be nonnegative" }
    var remaining = count
    var power = value
    var result = op.initial
    while (remaining > 0) {
        if ((remaining and 1) != 0) result = op.combine(result, power)
        remaining = remaining ushr 1
        if (remaining > 0) power = op.combine(power, power)
    }
    return result
}

/**
 * Lazy segment tree supporting range ASSIGN for any lawful monoid.
 *
 * Assigning `x` to an entire segment replaces its aggregate with
 * x combined with itself `segmentLength` times.
 *
 * With the default logarithmic monoid power: O(log² n) assignment,
 * O(log n) range queries, O(n) construction and memory.
 * If `repeatValue(x, length)` is an O(1) lawful implementation
 * (e.g. SUM -> x * length), assignment improves to O(log n).
 *
 * All ranges are half-open; works with noncommutative aggregates.
 */
class AssignmentLazyTree<T> internal constructor(
    values: List<T>,
    val op: AggregateOp<T>,
    repeatValue: (value: T, count: Int) -> T
) {
    private val backing = LazyAggregateTree(
        values, op,
        RangeAction<T, T>(
            apply = { _, value, length -> repeatValue(value, length) },
            compose = { _, newer -> newer } // Last assignment wins.
        )
    )

    val size: Int get() = backing.size

    /** Replace every element of [from, until) with the given value. */
    fun set(from: Int, until: Int, value: T) = backing.update(from, until, value)

    /** Alias to match the generic LazyAggregateTree vocabulary. */
    fun update(from: Int, until: Int, value: T) = set(from, until, value)

    fun query(from: Int, until: Int): T = backing.query(from, until)

    fun all(): T = backing.all()
}

/**
 * Assign whole intervals under a custom associative operation.
 * A more general update requires an explicit RangeAction.
 */
fun <T> List<T>.lazySegmentTree(
    op: AggregateOp<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = AssignmentLazyTree(
    this, op, repeatValue ?: { value, count -> monoidPower(value, count, op) }
)

fun IntArray.lazySegmentTree(
    op: AggregateOp<Int>,
    repeatValue: ((value: Int, count: Int) -> Int)? = null
): AssignmentLazyTree<Int> = asList().lazySegmentTree(op, repeatValue)

fun LongArray.lazySegmentTree(
    op: AggregateOp<Long>,
    repeatValue: ((value: Long, count: Int) -> Long)? = null
): AssignmentLazyTree<Long> = asList().lazySegmentTree(op, repeatValue)

fun <T> Array<out T>.lazySegmentTree(
    op: AggregateOp<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = asList().lazySegmentTree(op, repeatValue)
