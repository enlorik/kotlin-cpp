/**
 * Smoke checks for consistent aggregation vocab and ergonomic segment-tree API.
 * Everything here uses only the two small shared/tree modules.
 */
fun main() {
    val counts = intArrayOf(2, 4, 6, 8, 10)
    val sum = counts.segmentTree(IntAggregations.SUM)
    check(sum[2] == 6)
    check(sum[1..3] == 18)
    check(sum.query(1..3) == 18)
    check(sum.query(1, 4) == 18)
    sum[2] = 100
    check(sum[2] == 100)
    check(sum[1..3] == 112)
    check(sum.findPrefix { it >= 110 } == 3)

    val lowest = counts.segmentTree(IntAggregations.MIN)
    check(lowest.query(0..4) == 2)
    lowest[0] = 12
    check(lowest.query(0..4) == 4)

    val bits = intArrayOf(1, 2, 4).segmentTree(IntAggregations.OR)
    check(bits[0..2] == 7)

    val longs = longArrayOf(Long.MAX_VALUE, -7, 2).segmentTree(LongAggregations.MIN)
    check(longs[0..2] == -7L)
    longs[1] = 6L
    check(longs.all() == 2L)

    val items = arrayOf("aa", "b", "ccc").segmentTree(AggregateOp("", { a: String, b: String -> a + b }))
    check(items[0..1] == "aab")
    items[1] = "zzz"
    check(items.query(1..2) == "zzzccc")

    // The previous public GraphAggregateOp name remains source-compatible.
    val legacy = GraphAggregateOp(0, { x: Int, y: Int -> x + y })
    check(legacy.initial == 0 && legacy.combine(5, 8) == 13)

    // Check empty and illegal range semantics for inclusive Kotlin ranges.
    val empty = intArrayOf().segmentTree(IntAggregations.SUM)
    check(empty.all() == 0 && empty.query(0, 0) == 0)
    var rejected = false
    try { empty.query(0..0) } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    println("PASS: shared aggregation, no-graph tree API, inclusive ranges and bracket assignment")
}
