/**
 * Share grid/graph IntAggregations reducers with the range segment tree.
 * This adapter is optional; tree/SegmentTree.kt is standalone.
 */
fun IntArray.segmentTree(op: GraphAggregateOp<Int>): SegmentTree<Int> =
    SegmentTree(asList(), op.initial, op.combine)

fun <T> List<T>.segmentTree(op: GraphAggregateOp<T>): SegmentTree<T> =
    SegmentTree(this, op.initial, op.combine)
