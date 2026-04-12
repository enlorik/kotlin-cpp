import java.util.LinkedList

// BFS shortest path on unweighted graph
// g: adjacency list (0-indexed)
// Returns IntArray of distances (-1 if unreachable)
fun bfs(g: Array<IntArray>, src: Int): IntArray {
    val n = g.size
    val dist = IntArray(n) { -1 }
    dist[src] = 0
    val q = LinkedList<Int>()
    q.add(src)
    while (q.isNotEmpty()) {
        val u = q.poll()
        for (v in g[u]) {
            if (dist[v] == -1) {
                dist[v] = dist[u] + 1
                q.add(v)
            }
        }
    }
    return dist
}

// BFS path finding from src to dst
// Returns path as list of vertices, empty if no path exists
fun bfs(g: Array<IntArray>, src: Int, dst: Int): List<Int> {
    val n = g.size
    val prev = IntArray(n) { -1 }
    val dist = IntArray(n) { -1 }
    dist[src] = 0
    val q = LinkedList<Int>()
    q.add(src)
    outer@ while (q.isNotEmpty()) {
        val u = q.poll()
        for (v in g[u]) {
            if (dist[v] == -1) {
                dist[v] = dist[u] + 1
                prev[v] = u
                if (v == dst) break@outer
                q.add(v)
            }
        }
    }
    if (dist[dst] == -1) return emptyList()
    val path = mutableListOf<Int>()
    var cur = dst
    while (cur != -1) {
        path.add(cur)
        cur = prev[cur]
    }
    path.reverse()
    return path
}
