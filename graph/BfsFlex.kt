/**
 * BFS with customizable filtering and visitation hooks.
 * All allowed edges count as one step, regardless of Edge.w.
 */
data class BfsFlexResult(val dist: LongArray, val prev: IntArray, val order: IntArray) {
    fun hasPath(v: Int): Boolean = dist[v] < GRAPH_INF
    fun reconstructPath(v: Int): List<Int> = ShortestPathResult(dist, prev).reconstructPath(v)
}

/**
 * O(V + E) if callbacks take O(1) time.
 * canGo is called only on edges to undiscovered vertices.
 * onDiscover runs after dist/prev are assigned; onPop runs when a vertex is dequeued.
 * If target is supplied, the search stops when target is popped, so distances may be partial.
 */
inline fun SparseGraph.bfsFlex(
    src: Int,
    target: Int = -1,
    canGo: (u: Int, e: Edge) -> Boolean = { _, _ -> true },
    onDiscover: (u: Int, v: Int, e: Edge) -> Unit = { _, _, _ -> },
    onPop: (u: Int) -> Unit = {}
): BfsFlexResult {
    require(src in 0 until n) { "src out of bounds" }
    require(target == -1 || target in 0 until n) { "target out of bounds" }

    val dist = LongArray(n) { GRAPH_INF }
    val prev = IntArray(n) { -1 }
    val order = IntArray(n)
    var count = 0
    val q = ArrayDeque<Int>()

    dist[src] = 0L
    q.add(src)

    while (q.isNotEmpty()) {
        val u = q.removeFirst()
        order[count++] = u
        onPop(u)
        if (u == target) break

        for (e in g[u]) {
            val v = e.to
            if (dist[v] != GRAPH_INF || !canGo(u, e)) continue

            dist[v] = dist[u] + 1L
            prev[v] = u
            onDiscover(u, v, e)
            q.add(v)
        }
    }
    return BfsFlexResult(dist, prev, order.copyOf(count))
}
