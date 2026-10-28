import java.util.PriorityQueue

/** Dijkstra distances, parents, and the order in which vertices became final. */
class DijkstraFlexResult(
    val dist: LongArray,
    val prev: IntArray,
    val order: IntArray,
    /** True when the shortest distance is proven final. Useful with early target stopping. */
    val settled: BooleanArray
) {
    fun hasPath(v: Int): Boolean = dist[v] < GRAPH_INF
    fun distance(v: Int): Long? = dist[v].takeIf { it < GRAPH_INF }
    fun isFinalized(v: Int): Boolean = settled[v]
    fun reconstructPath(v: Int): List<Int> {
        if (!hasPath(v)) return emptyList()
        val path = ArrayList<Int>()
        var at = v
        while (at != -1) {
            path.add(at)
            at = prev[at]
        }
        path.reverse()
        return path
    }
}

/**
 * Dijkstra with customizable filtering, nonnegative edge costs and callbacks.
 * cost(u, edge) defaults to the existing edge.w.
 * onRelax runs EACH time a strictly better tentative distance is found;
 * onFinalize runs ONCE when a node is popped with its final distance.
 * A target stops when finalized. Other discovered nodes may have tentative distances.
 * Negative or >= GRAPH_INF edge costs are rejected if evaluated.
 * O((V + E) log V) for O(1) callbacks; O(V+E) extra queue space in the worst case.
 */
inline fun SparseGraph.dijkstraFlex(
    src: Int,
    target: Int = -1,
    canGo: (u: Int, e: Edge) -> Boolean = { _, _ -> true },
    cost: (u: Int, e: Edge) -> Long = { _, e -> e.w },
    onRelax: (u: Int, v: Int, e: Edge, newDist: Long) -> Unit = { _, _, _, _ -> },
    onFinalize: (u: Int, dist: Long) -> Unit = { _, _ -> }
): DijkstraFlexResult {
    require(src in 0 until n) { "src out of bounds" }
    require(target == -1 || target in 0 until n) { "target out of bounds" }

    val dist = LongArray(n) { GRAPH_INF }
    val prev = IntArray(n) { -1 }
    val settled = BooleanArray(n)
    val order = IntArray(n)
    var count = 0
    val pq = PriorityQueue<LongArray>(compareBy<LongArray> { it[0] }.thenBy { it[1] })
    dist[src] = 0L
    pq.add(longArrayOf(0L, src.toLong()))

    while (pq.isNotEmpty()) {
        val entry = pq.poll()
        val u = entry[1].toInt()
        val d = entry[0]
        if (settled[u] || d != dist[u]) continue
        settled[u] = true
        order[count++] = u
        onFinalize(u, d)
        if (u == target) break

        for (e in g[u]) {
            val v = e.to
            if (settled[v] || !canGo(u, e)) continue
            val w = cost(u, e)
            require(w >= 0 && w < GRAPH_INF) { "Dijkstra requires edge costs in 0 until GRAPH_INF" }
            if (d > GRAPH_INF - 1 - w) continue // cannot represent this path below INF
            val next = d + w
            if (next < dist[v]) {
                dist[v] = next
                prev[v] = u
                onRelax(u, v, e, next)
                pq.add(longArrayOf(next, v.toLong()))
            }
        }
    }
    return DijkstraFlexResult(dist, prev, order.copyOf(count), settled)
}
