/** Make custom lazy actions ergonomic on the same array types as other trees. */
fun <U> IntArray.lazySegmentTree(
    op: AggregateOp<Int>, action: RangeAction<Int, U>
): LazyAggregateTree<Int, U> = asList().lazySegmentTree(op, action)

fun <U> LongArray.lazySegmentTree(
    op: AggregateOp<Long>, action: RangeAction<Long, U>
): LazyAggregateTree<Long, U> = asList().lazySegmentTree(op, action)

fun <T, U> Array<out T>.lazySegmentTree(
    op: AggregateOp<T>, action: RangeAction<T, U>
): LazyAggregateTree<T, U> = asList().lazySegmentTree(op, action)
