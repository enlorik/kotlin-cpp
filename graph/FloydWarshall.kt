// Floyd-Warshall all-pairs shortest paths
// Modifies dist in-place; use Long.MAX_VALUE/2 (not Long.MAX_VALUE) to represent no edge
// After the call dist[i][j] is the shortest path weight from i to j,
// or Long.MAX_VALUE/2 if unreachable.
fun floydWarshall(dist: Array<LongArray>) {
    val n = dist.size
    val INF = Long.MAX_VALUE / 2
    for (k in 0 until n) {
        for (i in 0 until n) {
            if (dist[i][k] >= INF) continue
            for (j in 0 until n) {
                if (dist[k][j] >= INF) continue
                val nd = dist[i][k] + dist[k][j]
                if (nd < dist[i][j]) dist[i][j] = nd
            }
        }
    }
}

// Build initial distance matrix from a directed edge list
// dist[i][i] = 0, dist[i][j] = Long.MAX_VALUE/2 if no edge i->j
// For an undirected graph, add each edge in both directions.
fun buildDist(n: Int, edges: List<Triple<Int, Int, Long>>): Array<LongArray> {
    val INF = Long.MAX_VALUE / 2
    val dist = Array(n) { i -> LongArray(n) { j -> if (i == j) 0L else INF } }
    for ((u, v, w) in edges) {
        if (w < dist[u][v]) dist[u][v] = w
    }
    return dist
}
