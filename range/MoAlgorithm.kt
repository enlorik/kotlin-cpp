import kotlin.math.sqrt

/** Half-open range [from, until) for offline Mo's algorithm. */
data class MoQuery(val from: Int, val until: Int)

/**
 * Reorders offline interval queries to minimize changes to a sliding window.
 * Caller maintains state with add(index) and remove(index); snapshot must
 * return a stable (copied or immutable) answer.
 * Queries answered in original order. O((n+q)*sqrt(n)) window moves with
 * standard block ordering; each callback's own cost is additional.
 */
fun <T> moQueries(
    n: Int,
    queries: List<MoQuery>,
    add: (index: Int) -> Unit,
    remove: (index: Int) -> Unit,
    snapshot: () -> T
): List<T> {
    require(n >= 0) { "Size must be nonnegative" }
    queries.forEach { require(it.from >= 0 && it.from <= it.until && it.until <= n) { "Invalid query $it" } }
    val block = maxOf(1, sqrt(n.toDouble()).toInt())
    val order = queries.indices.sortedWith(compareBy<Int>({ queries[it].from / block },
        { i -> if ((queries[i].from / block) % 2 == 0) queries[i].until else -queries[i].until }))
    val result = arrayOfNulls<Any?>(queries.size)
    var left = 0
    var right = 0
    for (id in order) {
        val q = queries[id]
        while (left > q.from) add(--left)
        while (right < q.until) add(right++)
        while (left < q.from) remove(left++)
        while (right > q.until) remove(--right)
        result[id] = snapshot()
    }
    @Suppress("UNCHECKED_CAST")
    return result.map { it as T }
}

/** Convenient Mo's algorithm example: distinct values in each interval. */
fun IntArray.moDistinctCounts(queries: List<MoQuery>): List<Int> {
    val counts = HashMap<Int, Int>()
    var distinct = 0
    return moQueries(size, queries,
        add = { i ->
            val x = this[i]
            val old = counts[x] ?: 0
            if (old == 0) distinct++
            counts[x] = old + 1
        },
        remove = { i ->
            val x = this[i]
            val old = counts.getValue(x)
            if (old == 1) { counts.remove(x); distinct-- }
            else counts[x] = old - 1
        },
        snapshot = { distinct }
    )
}
