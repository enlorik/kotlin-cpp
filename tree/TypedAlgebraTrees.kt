/**
 * Trees need only an associative operation with a two-sided identity.
 * These overloads delegate to existing battle-tested implementations.
 */

// Ordinary segment trees accept any Monoid, including noncommutative ones.
fun IntArray.segmentTree(op: Monoid<Int>): SegmentTree<Int> = segmentTree(op.asAggregateOp())
fun LongArray.segmentTree(op: Monoid<Long>): SegmentTree<Long> = segmentTree(op.asAggregateOp())
fun <T> List<T>.segmentTree(op: Monoid<T>): SegmentTree<T> = segmentTree(op.asAggregateOp())
fun <T> Array<out T>.segmentTree(op: Monoid<T>): SegmentTree<T> = segmentTree(op.asAggregateOp())

// Historical versions also only need a Monoid.
fun IntArray.persistentSegmentTree(op: Monoid<Int>): PersistentAggregateTree<Int> =
    persistentSegmentTree(op.asAggregateOp())
fun LongArray.persistentSegmentTree(op: Monoid<Long>): PersistentAggregateTree<Long> =
    persistentSegmentTree(op.asAggregateOp())
fun <T> List<T>.persistentSegmentTree(op: Monoid<T>): PersistentAggregateTree<T> =
    persistentSegmentTree(op.asAggregateOp())
fun <T> Array<out T>.persistentSegmentTree(op: Monoid<T>): PersistentAggregateTree<T> =
    persistentSegmentTree(op.asAggregateOp())

/**
 * Lazy updates require both a Monoid and a compatible MonoidAction.
 * An arbitrary lambda that merely changes individual values is not enough:
 * apply must transform the aggregate of the entire interval.
 */
fun <T, U> List<T>.lazySegmentTree(
    op: Monoid<T>,
    action: MonoidAction<T, U>
): LazyAggregateTree<T, U> =
    lazySegmentTree(
        op.asAggregateOp(),
        RangeAction(
            apply = { aggregate, update, length -> action.apply(aggregate, update, length) },
            compose = { old, newer -> action.compose(old, newer) }
        )
    )

fun <U> IntArray.lazySegmentTree(
    op: Monoid<Int>,
    action: MonoidAction<Int, U>
): LazyAggregateTree<Int, U> = asList().lazySegmentTree(op, action)

fun <U> LongArray.lazySegmentTree(
    op: Monoid<Long>,
    action: MonoidAction<Long, U>
): LazyAggregateTree<Long, U> = asList().lazySegmentTree(op, action)

fun <T, U> Array<out T>.lazySegmentTree(
    op: Monoid<T>,
    action: MonoidAction<T, U>
): LazyAggregateTree<T, U> = asList().lazySegmentTree(op, action)

/**
 * All typed monoids support range assignments, because x repeated n times
 * has a well-defined aggregate. The default repeat uses O(log n) combines.
 */
fun <T> List<T>.lazySegmentTree(
    op: Monoid<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = lazySegmentTree(op.asAggregateOp(), repeatValue)

fun IntArray.lazySegmentTree(
    op: Monoid<Int>,
    repeatValue: ((value: Int, count: Int) -> Int)? = null
): AssignmentLazyTree<Int> = asList().lazySegmentTree(op, repeatValue)

fun LongArray.lazySegmentTree(
    op: Monoid<Long>,
    repeatValue: ((value: Long, count: Int) -> Long)? = null
): AssignmentLazyTree<Long> = asList().lazySegmentTree(op, repeatValue)

fun <T> Array<out T>.lazySegmentTree(
    op: Monoid<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = asList().lazySegmentTree(op, repeatValue)
