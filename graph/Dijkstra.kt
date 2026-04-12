import java.util.PriorityQueue

// Dijkstra's algorithm on adjacency list of (neighbor, weight) pairs
// Returns LongArray of distances (Long.MAX_VALUE if unreachable)
fun dij(g: Array<List<Pair<Int, Long>>>, src: Int): LongArray {
    val n = g.size
    val dist = LongArray(n) { Long.MAX_VALUE }
    dist[src] = 0L
    val pq = PriorityQueue<Pair<Long, Int>>(compareBy { it.first })
    pq.add(0L to src)
    while (pq.isNotEmpty()) {
        val (d, u) = pq.poll()
        if (d > dist[u]) continue
        for ((v, w) in g[u]) {
            val nd = d + w
            if (nd < dist[v]) {
                dist[v] = nd
                pq.add(nd to v)
            }
        }
    }
    return dist
}

// Dijkstra on flat IntArray adjacency: g[u] = [v1, w1, v2, w2, ...]
// Returns LongArray of distances (Long.MAX_VALUE if unreachable)
fun dij(g: Array<IntArray>, src: Int): LongArray {
    val n = g.size
    val dist = LongArray(n) { Long.MAX_VALUE }
    dist[src] = 0L
    val pq = PriorityQueue<Pair<Long, Int>>(compareBy { it.first })
    pq.add(0L to src)
    while (pq.isNotEmpty()) {
        val (d, u) = pq.poll()
        if (d > dist[u]) continue
        val arr = g[u]
        var i = 0
        while (i + 1 < arr.size) {
            val v = arr[i]
            val w = arr[i + 1].toLong()
            val nd = d + w
            if (nd < dist[v]) {
                dist[v] = nd
                pq.add(nd to v)
            }
            i += 2
        }
    }
    return dist
}

// Dijkstra with path reconstruction
// Returns list of vertices on shortest path from src to dst, empty if unreachable
fun dijPath(g: Array<List<Pair<Int, Long>>>, src: Int, dst: Int): List<Int> {
    val n = g.size
    val dist = LongArray(n) { Long.MAX_VALUE }
    val prev = IntArray(n) { -1 }
    dist[src] = 0L
    val pq = PriorityQueue<Pair<Long, Int>>(compareBy { it.first })
    pq.add(0L to src)
    while (pq.isNotEmpty()) {
        val (d, u) = pq.poll()
        if (d > dist[u]) continue
        for ((v, w) in g[u]) {
            val nd = d + w
            if (nd < dist[v]) {
                dist[v] = nd
                prev[v] = u
                pq.add(nd to v)
            }
        }
    }
    if (dist[dst] == Long.MAX_VALUE) return emptyList()
    val path = mutableListOf<Int>()
    var cur = dst
    while (cur != -1) {
        path.add(cur)
        cur = prev[cur]
    }
    path.reverse()
    return path
}
