import java.util.TreeMap

/**
 * Find first-true boundaries for many monotone queries simultaneously.
 * Query q searches [from[q], until[q]), returning until[q] when none true.
 * Midpoints are grouped and visited in ascending order per round, allowing
 * the caller to reuse sweep data. Reset such state via onRoundStart.
 * The evaluator returns one Boolean per query in queryIds order.
 * Excluding evaluation: O(Q log R log Q) time, O(Q) extra space.
 */
fun parallelFirstTrue(
    from: IntArray,
    until: IntArray,
    onRoundStart: (round: Int) -> Unit = {},
    evaluate: (mid: Int, queryIds: IntArray) -> BooleanArray
): IntArray {
    require(from.size == until.size) { "from and until arrays must have the same length" }
    val n = from.size
    val left = from.copyOf()
    val right = until.copyOf()
    for (q in 0 until n) require(left[q] <= right[q]) {
        "Expected from[$q] <= until[$q]"
    }
    var round = 0
    while (true) {
        val groups = TreeMap<Int, MutableList<Int>>()
        for (q in 0 until n) if (left[q] < right[q]) {
            val mid = (left[q].toLong() + (right[q].toLong() - left[q]) / 2).toInt()
            groups.getOrPut(mid) { ArrayList() }.add(q)
        }
        if (groups.isEmpty()) break
        onRoundStart(round++)
        for ((mid, ids) in groups) {
            val queryIds = ids.toIntArray()
            val results = evaluate(mid, queryIds)
            require(results.size == queryIds.size) {
                "Evaluator returned unexpected number of answers at midpoint $mid"
            }
            for (j in queryIds.indices) {
                val q = queryIds[j]
                if (results[j]) right[q] = mid else left[q] = mid + 1
            }
        }
    }
    return left
}

/** Shared interval [from, until) for all query IDs 0 until queryCount. */
fun parallelFirstTrue(
    queryCount: Int,
    from: Int,
    until: Int,
    onRoundStart: (round: Int) -> Unit = {},
    evaluate: (mid: Int, queryIds: IntArray) -> BooleanArray
): IntArray {
    require(queryCount >= 0) { "queryCount must be nonnegative" }
    require(from <= until) { "Expected from <= until" }
    return parallelFirstTrue(IntArray(queryCount) { from }, IntArray(queryCount) { until },
        onRoundStart, evaluate)
}
