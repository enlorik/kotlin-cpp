/** Result of a single-source shortest path computation. */
data class ShortestPathResult(val dist: LongArray, val prev: IntArray) {

    fun hasPath(t: Int) = dist[t] < GRAPH_INF

    /** Returns the distance to [t], or null if unreachable. */
    fun distTo(t: Int): Long? = if (hasPath(t)) dist[t] else null

    /** Reconstructs the vertex path from the source to [t]; empty if unreachable. */
    fun reconstructPath(t: Int): List<Int> {
        if (!hasPath(t)) return emptyList()
        val path = mutableListOf<Int>()
        var cur = t
        while (cur != -1) { path.add(cur); cur = prev[cur] }
        path.reverse()
        return path
    }
}

/**
 * Result of a minimum spanning tree / forest computation.
 * [totalWeight] is the sum of edge weights; [edges] lists each MST edge as (u, v, w).
 */
data class MSTResult(val totalWeight: Long, val edges: List<Triple<Int, Int, Long>>)

/**
 * Result of strongly-connected-component decomposition.
 * [comp][v] = SCC id of vertex v (0-indexed).
 * SCC ids are in reverse topological order (id 0 = a sink SCC).
 */
data class SCCResult(val count: Int, val comp: IntArray)

/**
 * Result of bridge / articulation-point finding on an undirected graph.
 * Each bridge is stored as the pair (u, v) in DFS-tree order (u is the ancestor).
 */
data class BridgeArtResult(
    val bridges: List<Pair<Int, Int>>,
    val articulationPoints: List<Int>
)
