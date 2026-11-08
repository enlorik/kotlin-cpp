/**
 * Choose a single-source shortest-path algorithm from the graph's current edge
 * weights. Selection scans the adjacency lists in O(V + E).
 *
 * All unit weights -> BFS; {0,1} weights -> 0-1 BFS; other nonnegative ->
 * Dijkstra; negative weights -> Bellman-Ford (with negative-cycle rejection).
 */
enum class ShortestPathAlgorithm {
    BFS, ZERO_ONE_BFS, DIJKSTRA, BELLMAN_FORD
}

/** The selected algorithm plus the usual ShortestPathResult conveniences. */
data class AutoShortestPaths(
    val algorithm: ShortestPathAlgorithm,
    val result: ShortestPathResult
) {
    val dist: LongArray get() = result.dist
    val prev: IntArray get() = result.prev
    fun hasPath(target: Int): Boolean = result.hasPath(target)
    fun distTo(target: Int): Long? = result.distTo(target)
    fun reconstructPath(target: Int): List<Int> = result.reconstructPath(target)
}

/**
 * Scan current edge weights and delegate to the correct existing algorithm.
 * Throws if a negative cycle reachable from source makes distances undefined.
 * Overflow limitations are the same as in the underlying graph algorithms.
 */
fun SparseGraph.shortestPaths(source: Int): AutoShortestPaths {
    require(source in 0 until n) { "Source $source out of bounds for $n vertices" }

    var allUnit = true
    var allZeroOne = true
    var hasNegative = false
    for (edges in g) for (edge in edges) {
        if (edge.w != 1L) allUnit = false
        if (edge.w != 0L && edge.w != 1L) allZeroOne = false
        if (edge.w < 0L) hasNegative = true
    }

    return when {
        allUnit -> AutoShortestPaths(ShortestPathAlgorithm.BFS, bfs(source))
        allZeroOne -> AutoShortestPaths(ShortestPathAlgorithm.ZERO_ONE_BFS, zeroOneBfs(source))
        !hasNegative -> AutoShortestPaths(ShortestPathAlgorithm.DIJKSTRA, dijkstra(source))
        else -> {
            val (paths, negativeCycle) = bellmanFord(source)
            check(!negativeCycle) {
                "A negative-weight cycle is reachable from source $source; shortest paths are undefined"
            }
            AutoShortestPaths(ShortestPathAlgorithm.BELLMAN_FORD, paths)
        }
    }
}
