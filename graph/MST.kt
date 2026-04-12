import java.util.PriorityQueue

// --- DSU helpers (file-private) ---

private fun makeUF(n: Int) = IntArray(n) { it }

private fun find(uf: IntArray, x: Int): Int {
    var r = x
    while (uf[r] != r) r = uf[r]
    var i = x
    while (i != r) { val nx = uf[i]; uf[i] = r; i = nx }
    return r
}

private fun union(uf: IntArray, a: Int, b: Int): Boolean {
    val ra = find(uf, a); val rb = find(uf, b)
    if (ra == rb) return false
    uf[ra] = rb
    return true
}

// Kruskal's MST
// edges: list of Triple(u, v, weight)
// Returns list of edges in the MST, or null if the graph is not connected
fun kruskal(n: Int, edges: List<Triple<Int, Int, Long>>): List<Triple<Int, Int, Long>>? {
    val sorted = edges.sortedBy { it.third }
    val uf = makeUF(n)
    val mst = mutableListOf<Triple<Int, Int, Long>>()
    for (e in sorted) {
        if (union(uf, e.first, e.second)) {
            mst.add(e)
            if (mst.size == n - 1) return mst
        }
    }
    return if (mst.size == n - 1) mst else null
}

// Prim's MST for dense graphs
// g: adjacency matrix; g[u][v] = Long.MAX_VALUE/2 if no edge
// Returns total MST weight (sum of chosen edge weights)
fun prim(g: Array<LongArray>): Long {
    val n = g.size
    val INF = Long.MAX_VALUE / 2
    val inMST = BooleanArray(n)
    val key = LongArray(n) { INF }
    key[0] = 0L
    val pq = PriorityQueue<Pair<Long, Int>>(compareBy { it.first })
    pq.add(0L to 0)
    var total = 0L
    while (pq.isNotEmpty()) {
        val (d, u) = pq.poll()
        if (inMST[u]) continue
        inMST[u] = true
        total += d
        for (v in 0 until n) {
            if (!inMST[v] && g[u][v] < INF && g[u][v] < key[v]) {
                key[v] = g[u][v]
                pq.add(key[v] to v)
            }
        }
    }
    return total
}
