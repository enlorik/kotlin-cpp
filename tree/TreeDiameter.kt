// Tree diameter (longest path) and related functions.
// All functions assume a connected, undirected tree.

// Returns Triple(diameter length, endpoint u, endpoint v).
// Uses two BFS: first from any vertex to find one endpoint, then from that endpoint.
fun treeDiameter(g: Array<IntArray>): Triple<Int, Int, Int> {
    fun bfs(src: Int): IntArray {
        val dist = IntArray(g.size) { -1 }
        dist[src] = 0
        val q = IntArray(g.size); var h = 0; var t = 0
        q[t++] = src
        while (h < t) {
            val v = q[h++]
            for (u in g[v]) if (dist[u] == -1) { dist[u] = dist[v] + 1; q[t++] = u }
        }
        return dist
    }
    val d1 = bfs(0)
    val u = d1.indices.maxByOrNull { d1[it] }!!
    val d2 = bfs(u)
    val v = d2.indices.maxByOrNull { d2[it] }!!
    return Triple(d2[v], u, v)
}

// Weighted version — edge weights are Long.
// Returns Triple(diameter length, endpoint u, endpoint v).
fun treeDiameterW(g: Array<List<Pair<Int, Long>>>): Triple<Long, Int, Int> {
    fun bfs(src: Int): LongArray {
        val dist = LongArray(g.size) { -1L }
        dist[src] = 0L
        val q = IntArray(g.size); var h = 0; var t = 0
        q[t++] = src
        while (h < t) {
            val v = q[h++]
            for ((u, w) in g[v]) if (dist[u] == -1L) { dist[u] = dist[v] + w; q[t++] = u }
        }
        return dist
    }
    val d1 = bfs(0)
    val u = d1.indices.maxByOrNull { d1[it] }!!
    val d2 = bfs(u)
    val v = d2.indices.maxByOrNull { d2[it] }!!
    return Triple(d2[v], u, v)
}

// Eccentricity of each vertex: the maximum distance to any other vertex.
// Uses the theorem that for any vertex v in a tree, the farthest vertex from v
// is always one of the two diameter endpoints, so two BFS suffice.
fun eccentricities(g: Array<IntArray>): IntArray {
    fun bfs(src: Int): IntArray {
        val dist = IntArray(g.size) { -1 }
        dist[src] = 0
        val q = IntArray(g.size); var h = 0; var t = 0
        q[t++] = src
        while (h < t) {
            val v = q[h++]
            for (u in g[v]) if (dist[u] == -1) { dist[u] = dist[v] + 1; q[t++] = u }
        }
        return dist
    }
    val (_, ep1, ep2) = treeDiameter(g)
    val d1 = bfs(ep1)
    val d2 = bfs(ep2)
    return IntArray(g.size) { maxOf(d1[it], d2[it]) }
}
