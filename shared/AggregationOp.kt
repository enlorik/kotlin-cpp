/**
 * Associative aggregation with a two-sided identity.
 *
 * The operation is shared by grids, graphs, and segment trees.
 * idempotent affects the graph nearest-source fast path only:
 * it means combine(x, x) == x (e.g. MIN, MAX, OR, AND).
 */
class AggregateOp<T>(
    val initial: T,
    val combine: (T, T) -> T,
    val idempotent: Boolean = false
)

/** Backwards-compatible name for existing graph and grid APIs. */
typealias GraphAggregateOp<T> = AggregateOp<T>

/** Aggregations for Int-valued grids, graphs, and segment trees. */
object IntAggregations {
    val OR = AggregateOp(0, { a: Int, b: Int -> a or b }, idempotent = true)
    val AND = AggregateOp(-1, { a: Int, b: Int -> a and b }, idempotent = true)
    val MIN = AggregateOp(Int.MAX_VALUE, { a: Int, b: Int -> minOf(a, b) }, idempotent = true)
    val MAX = AggregateOp(Int.MIN_VALUE, { a: Int, b: Int -> maxOf(a, b) }, idempotent = true)
    val SUM = AggregateOp(0, { a: Int, b: Int -> a + b })
    val XOR = AggregateOp(0, { a: Int, b: Int -> a xor b })
}

/** Corresponding operations for Long-valued ranges and graphs. */
object LongAggregations {
    val OR = AggregateOp(0L, { a: Long, b: Long -> a or b }, idempotent = true)
    val AND = AggregateOp(-1L, { a: Long, b: Long -> a and b }, idempotent = true)
    val MIN = AggregateOp(Long.MAX_VALUE, { a: Long, b: Long -> minOf(a, b) }, idempotent = true)
    val MAX = AggregateOp(Long.MIN_VALUE, { a: Long, b: Long -> maxOf(a, b) }, idempotent = true)
    val SUM = AggregateOp(0L, { a: Long, b: Long -> a + b })
    val XOR = AggregateOp(0L, { a: Long, b: Long -> a xor b })
}
