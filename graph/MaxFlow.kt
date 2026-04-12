import java.util.LinkedList

// Dinic's max-flow algorithm
// Vertices are 0-indexed; call addEdge then maxFlow.
class Dinic(val n: Int) {
    // Edge: to, remaining capacity, index of reverse edge in graph[to]
    private data class Edge(val to: Int, var cap: Long, val rev: Int)

    private val graph = Array(n) { mutableListOf<Edge>() }
    private val level = IntArray(n)
    private val iter  = IntArray(n)

    fun addEdge(u: Int, v: Int, cap: Long) {
        graph[u].add(Edge(v, cap,          graph[v].size))
        graph[v].add(Edge(u, 0L,           graph[u].size - 1))
    }

    // BFS to build level graph; returns true if sink is reachable
    private fun bfs(s: Int, t: Int): Boolean {
        level.fill(-1)
        level[s] = 0
        val q = LinkedList<Int>()
        q.add(s)
        while (q.isNotEmpty()) {
            val u = q.poll()
            for (e in graph[u]) {
                if (e.cap > 0 && level[e.to] < 0) {
                    level[e.to] = level[u] + 1
                    q.add(e.to)
                }
            }
        }
        return level[t] >= 0
    }

    // DFS blocking flow (recursive; depth bounded by O(V) per BFS phase)
    private fun dfsRec(u: Int, t: Int, f: Long): Long {
        if (u == t) return f
        while (iter[u] < graph[u].size) {
            val e = graph[u][iter[u]]
            if (e.cap > 0 && level[u] < level[e.to]) {
                val d = dfsRec(e.to, t, minOf(f, e.cap))
                if (d > 0) {
                    e.cap -= d
                    graph[e.to][e.rev].cap += d
                    return d
                }
            }
            iter[u]++
        }
        return 0L
    }

    // Compute max flow from s to t
    fun maxFlow(s: Int, t: Int): Long {
        var flow = 0L
        while (bfs(s, t)) {
            iter.fill(0)
            while (true) {
                val f = dfsRec(s, t, Long.MAX_VALUE)
                if (f == 0L) break
                flow += f
            }
        }
        return flow
    }
}
