import java.util.LinkedList

// Bellman-Ford shortest paths from src
// edges: list of Triple(u, v, weight)
// Returns LongArray of distances, or null if a negative cycle is reachable from src
fun bellmanFord(n: Int, edges: List<Triple<Int, Int, Long>>, src: Int): LongArray? {
    val dist = LongArray(n) { Long.MAX_VALUE }
    dist[src] = 0L
    repeat(n - 1) {
        for ((u, v, w) in edges) {
            if (dist[u] != Long.MAX_VALUE && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w
            }
        }
    }
    // Detect negative cycle
    for ((u, v, w) in edges) {
        if (dist[u] != Long.MAX_VALUE && dist[u] + w < dist[v]) return null
    }
    return dist
}

// SPFA (Shortest Path Faster Algorithm) — Bellman-Ford with queue optimisation
// g: adjacency list of (neighbor, weight) pairs
// Returns LongArray of distances, or null if a negative cycle is detected
fun spfa(g: Array<List<Pair<Int, Long>>>, src: Int): LongArray? {
    val n = g.size
    val dist = LongArray(n) { Long.MAX_VALUE }
    val inQueue = BooleanArray(n)
    val relaxCount = IntArray(n)
    dist[src] = 0L
    val q = LinkedList<Int>()
    q.add(src)
    inQueue[src] = true
    while (q.isNotEmpty()) {
        val u = q.poll()
        inQueue[u] = false
        for ((v, w) in g[u]) {
            if (dist[u] != Long.MAX_VALUE && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w
                if (!inQueue[v]) {
                    q.add(v)
                    inQueue[v] = true
                    relaxCount[v]++
                    if (relaxCount[v] >= n) return null  // negative cycle
                }
            }
        }
    }
    return dist
}
