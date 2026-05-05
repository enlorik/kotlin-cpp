/**
 * Adjacency-matrix graph representation.
 * Best for dense graphs (n ≤ ~500) and algorithms that need O(1) edge-weight lookup.
 *
 * Edge weights default to GRAPH_INF (absent). Self-loop costs are initialised to 0.
 * Multiple calls to [addEdge] for the same pair keep the minimum weight.
 */
class DenseGraph(val n: Int, val directed: Boolean = false) {

    /** g[i][j] = weight of edge i→j, or GRAPH_INF if absent. */
    val g: Array<LongArray> = Array(n) { LongArray(n) { GRAPH_INF } }

    init { for (i in 0 until n) g[i][i] = 0L }

    fun addEdge(u: Int, v: Int, w: Long = 1L) {
        g[u][v] = minOf(g[u][v], w)
        if (!directed) g[v][u] = minOf(g[v][u], w)
    }

    // ── All-pairs shortest paths ─────────────────────────────────────────────

    /**
     * Floyd-Warshall all-pairs shortest paths. O(n³).
     * Returns a new n×n distance matrix; does NOT modify [g].
     * Call [hasNegativeCycle] on the result to detect negative cycles.
     */
    fun floydWarshall(): Array<LongArray> {
        val d = Array(n) { g[it].copyOf() }
        for (k in 0 until n) {
            val dk = d[k]
            for (i in 0 until n) {
                val di = d[i]
                if (di[k] >= GRAPH_INF) continue
                for (j in 0 until n) {
                    if (dk[j] < GRAPH_INF && di[k] + dk[j] < di[j])
                        di[j] = di[k] + dk[j]
                }
            }
        }
        return d
    }

    /** Returns true if the Floyd-Warshall distance matrix contains a negative cycle. */
    fun hasNegativeCycle(d: Array<LongArray>) = (0 until n).any { d[it][it] < 0L }

    // ── Transitive closure ───────────────────────────────────────────────────

    /**
     * Computes the transitive closure using Boolean matrix closure. O(n³).
     * tc[i][j] = true iff vertex j is reachable from vertex i.
     */
    fun transitiveClosure(): Array<BooleanArray> {
        val tc = Array(n) { i -> BooleanArray(n) { j -> g[i][j] < GRAPH_INF } }
        for (k in 0 until n)
            for (i in 0 until n) if (tc[i][k])
                for (j in 0 until n) if (tc[k][j]) tc[i][j] = true
        return tc
    }
}
