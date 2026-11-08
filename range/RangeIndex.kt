/**
 * The chosen implementation for rangeIndex(op, mutable=...).
 * STATIC_IDEMPOTENT: sparse table, STATIC_GENERAL: disjoint sparse table,
 * POINT_UPDATES: segment tree.
 */
enum class RangeIndexStrategy { STATIC_IDEMPOTENT, STATIC_GENERAL, POINT_UPDATES }

/**
 * A convenient facade that selects a lawful data structure for the requested
 * usage. Static query O(1) after O(n log n) build; mutable point update and
 * query O(log n) after O(n) build. Selection never guesses associativity:
 * callers must provide an actual monoid and correctly declare idempotency.
 */
class RangeIndex<T>(values: List<T>, op: AggregateOp<T>, mutable: Boolean) {
    val size: Int = values.size
    val strategy: RangeIndexStrategy = when {
        mutable -> RangeIndexStrategy.POINT_UPDATES
        op.idempotent -> RangeIndexStrategy.STATIC_IDEMPOTENT
        else -> RangeIndexStrategy.STATIC_GENERAL
    }
    private val segment: SegmentTree<T>? = if (mutable) SegmentTree(values, op.initial, op.combine) else null
    private val sparse: SparseTable<T>? = if (strategy == RangeIndexStrategy.STATIC_IDEMPOTENT) SparseTable(values, op) else null
    private val disjoint: DisjointSparseTable<T>? = if (strategy == RangeIndexStrategy.STATIC_GENERAL) DisjointSparseTable(values, op) else null

    fun query(from: Int, until: Int): T = when (strategy) {
        RangeIndexStrategy.POINT_UPDATES -> segment!!.query(from, until)
        RangeIndexStrategy.STATIC_IDEMPOTENT -> sparse!!.query(from, until)
        RangeIndexStrategy.STATIC_GENERAL -> disjoint!!.query(from, until)
    }

    /** Only valid if mutable=true. */
    fun set(index: Int, value: T) {
        check(strategy == RangeIndexStrategy.POINT_UPDATES) {
            "rangeIndex was built as static; construct with mutable=true for point updates"
        }
        segment!![index] = value
    }

    fun all(): T = query(0, size)
}

fun <T> List<T>.rangeIndex(op: AggregateOp<T>, mutable: Boolean = false): RangeIndex<T> =
    RangeIndex(this, op, mutable)
fun <T> Array<out T>.rangeIndex(op: AggregateOp<T>, mutable: Boolean = false): RangeIndex<T> =
    asList().rangeIndex(op, mutable)
fun IntArray.rangeIndex(op: AggregateOp<Int>, mutable: Boolean = false): RangeIndex<Int> =
    asList().rangeIndex(op, mutable)
fun LongArray.rangeIndex(op: AggregateOp<Long>, mutable: Boolean = false): RangeIndex<Long> =
    asList().rangeIndex(op, mutable)

/** With range updates, choose a generic lazy action instead of a static index. */
fun <T, U> List<T>.rangeIndex(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    LazyAggregateTree(this, op, action)
fun <T, U> Array<out T>.rangeIndex(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    asList().rangeIndex(op, action)
fun <U> IntArray.rangeIndex(op: AggregateOp<Int>, action: RangeAction<Int, U>): LazyAggregateTree<Int, U> =
    asList().rangeIndex(op, action)
fun <U> LongArray.rangeIndex(op: AggregateOp<Long>, action: RangeAction<Long, U>): LazyAggregateTree<Long, U> =
    asList().rangeIndex(op, action)

/** Typed monoids preserve their declared idempotency when selecting an index. */
fun <T> List<T>.rangeIndex(op: Monoid<T>, mutable: Boolean = false): RangeIndex<T> =
    rangeIndex(op.asAggregateOp(), mutable)
fun <T> Array<out T>.rangeIndex(op: Monoid<T>, mutable: Boolean = false): RangeIndex<T> =
    asList().rangeIndex(op, mutable)
fun IntArray.rangeIndex(op: Monoid<Int>, mutable: Boolean = false): RangeIndex<Int> =
    asList().rangeIndex(op, mutable)
fun LongArray.rangeIndex(op: Monoid<Long>, mutable: Boolean = false): RangeIndex<Long> =
    asList().rangeIndex(op, mutable)

/** Typed range-update actions select a LazyAggregateTree with the full action law. */
fun <T, U> List<T>.rangeIndex(op: Monoid<T>, action: MonoidAction<T, U>): LazyAggregateTree<T, U> =
    lazySegmentTree(op, action)
fun <T, U> Array<out T>.rangeIndex(op: Monoid<T>, action: MonoidAction<T, U>): LazyAggregateTree<T, U> =
    asList().rangeIndex(op, action)
fun <U> IntArray.rangeIndex(op: Monoid<Int>, action: MonoidAction<Int, U>): LazyAggregateTree<Int, U> =
    asList().rangeIndex(op, action)
fun <U> LongArray.rangeIndex(op: Monoid<Long>, action: MonoidAction<Long, U>): LazyAggregateTree<Long, U> =
    asList().rangeIndex(op, action)
