/**
 * Opt-in convenience adapters for the reusable aggregation operations.
 * Requires tree/SegmentTree.kt + shared/AggregationOp.kt, NOT graph code.
 */
fun IntArray.segmentTree(op: AggregateOp<Int>): SegmentTree<Int> =
    SegmentTree(asList(), op.initial, op.combine)

fun LongArray.segmentTree(op: AggregateOp<Long>): SegmentTree<Long> =
    SegmentTree(asList(), op.initial, op.combine)

fun <T> List<T>.segmentTree(op: AggregateOp<T>): SegmentTree<T> =
    SegmentTree(this, op.initial, op.combine)

fun <T> Array<out T>.segmentTree(op: AggregateOp<T>): SegmentTree<T> =
    SegmentTree(asList(), op.initial, op.combine)
