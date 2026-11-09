/**
 * An immediately executing range-query DSL.
 *
 * Unlike deferred rangeProgram, query returns its answer *now*, so the result
 * can control subsequent updates, branches and loops in a contest solution.
 * Point assignments use a mutable segment tree. General lazy updates require
 * an explicit lawful action, not a guessed update interpretation.
 *
 * All ranges are 0-based and half-open. Inputs are snapshotted by the tree.
 */
@RangeProgramDsl
class OnlineRangeScope<T> internal constructor(private val index: RangeIndex<T>) {
    val size: Int get() = index.size
    val strategy: RangeIndexStrategy get() = index.strategy

    fun query(from: Int, until: Int): T = index.query(from, until)

    /** Kotlin's inclusive range, e.g. query(1..3) includes index 3. */
    fun query(indices: IntRange): T {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    fun set(index: Int, value: T) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        this.index.set(index, value)
    }

    fun all(): T = index.all()
}

/**
 * Online DSL for general associative aggregates with a lawful lazy action.
 * update(from, until, change) applies immediately, before the next query.
 */
@RangeProgramDsl
class OnlineLazyRangeScope<T, U> internal constructor(
    private val index: LazyAggregateTree<T, U>
) {
    val size: Int get() = index.size

    fun query(from: Int, until: Int): T = index.query(from, until)

    fun query(indices: IntRange): T {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    fun update(from: Int, until: Int, change: U) {
        index.update(from, until, change)
    }

    fun all(): T = index.all()
}

/** Point-assignment online program. Unlike the recorder, block is run once immediately. */
fun <T, R> List<T>.onlineRangeProgram(
    operation: AggregateOp<T>,
    block: OnlineRangeScope<T>.() -> R
): R = OnlineRangeScope(rangeIndex(operation, mutable = true)).block()

fun <T, R> List<T>.onlineRangeProgram(
    operation: Monoid<T>,
    block: OnlineRangeScope<T>.() -> R
): R = onlineRangeProgram(operation.asAggregateOp(), block)

fun <T, R> Array<out T>.onlineRangeProgram(
    operation: AggregateOp<T>,
    block: OnlineRangeScope<T>.() -> R
): R = asList().onlineRangeProgram(operation, block)

fun <T, R> Array<out T>.onlineRangeProgram(
    operation: Monoid<T>,
    block: OnlineRangeScope<T>.() -> R
): R = asList().onlineRangeProgram(operation, block)

fun <R> IntArray.onlineRangeProgram(
    operation: AggregateOp<Int>,
    block: OnlineRangeScope<Int>.() -> R
): R = asList().onlineRangeProgram(operation, block)

fun <R> IntArray.onlineRangeProgram(
    operation: Monoid<Int>,
    block: OnlineRangeScope<Int>.() -> R
): R = asList().onlineRangeProgram(operation, block)

fun <R> LongArray.onlineRangeProgram(
    operation: AggregateOp<Long>,
    block: OnlineRangeScope<Long>.() -> R
): R = asList().onlineRangeProgram(operation, block)

fun <R> LongArray.onlineRangeProgram(
    operation: Monoid<Long>,
    block: OnlineRangeScope<Long>.() -> R
): R = asList().onlineRangeProgram(operation, block)

/** Lazy-range-update online program, requiring caller-provided lawful action. */
fun <T, U, R> List<T>.onlineRangeProgram(
    operation: AggregateOp<T>,
    action: RangeAction<T, U>,
    block: OnlineLazyRangeScope<T, U>.() -> R
): R = OnlineLazyRangeScope(rangeIndex(operation, action)).block()

fun <T, U, R> List<T>.onlineRangeProgram(
    operation: Monoid<T>,
    action: MonoidAction<T, U>,
    block: OnlineLazyRangeScope<T, U>.() -> R
): R = OnlineLazyRangeScope(rangeIndex(operation, action)).block()

fun <T, U, R> Array<out T>.onlineRangeProgram(
    operation: AggregateOp<T>,
    action: RangeAction<T, U>,
    block: OnlineLazyRangeScope<T, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)

fun <T, U, R> Array<out T>.onlineRangeProgram(
    operation: Monoid<T>,
    action: MonoidAction<T, U>,
    block: OnlineLazyRangeScope<T, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)

fun <U, R> IntArray.onlineRangeProgram(
    operation: AggregateOp<Int>,
    action: RangeAction<Int, U>,
    block: OnlineLazyRangeScope<Int, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)

fun <U, R> IntArray.onlineRangeProgram(
    operation: Monoid<Int>,
    action: MonoidAction<Int, U>,
    block: OnlineLazyRangeScope<Int, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)

fun <U, R> LongArray.onlineRangeProgram(
    operation: AggregateOp<Long>,
    action: RangeAction<Long, U>,
    block: OnlineLazyRangeScope<Long, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)

fun <U, R> LongArray.onlineRangeProgram(
    operation: Monoid<Long>,
    action: MonoidAction<Long, U>,
    block: OnlineLazyRangeScope<Long, U>.() -> R
): R = asList().onlineRangeProgram(operation, action, block)
