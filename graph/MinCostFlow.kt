import java.util.LinkedList

// Min-cost max-flow using SPFA (Bellman-Ford with queue) for shortest path.
// Handles negative-cost edges (but no negative cycles on residual graph).
// Usage: addEdge, then minCostFlow(s, t) or minCostFlow(s, t, limit).
class MCMF(val n: Int) {
    private data class Edge(val to: Int, var cap: Long, val cost: Long, val rev: Int)

    private val graph = Array(n) { mutableListOf<Edge>() }

    fun addEdge(u: Int, v: Int, cap: Long, cost: Long) {
        graph[u].add(Edge(v,  cap, cost,  graph[v].size))
        graph[v].add(Edge(u, 0L, -cost, graph[u].size - 1))
    }

    // Returns Pair(totalFlow, totalCost)
    // Sends at most maxFlow units from s to t.
    fun minCostFlow(s: Int, t: Int, maxFlow: Long = Long.MAX_VALUE): Pair<Long, Long> {
        var flow = 0L
        var totalCost = 0L

        while (flow < maxFlow) {
            // SPFA to find minimum-cost augmenting path
            val dist = LongArray(n) { Long.MAX_VALUE }
            val prevNode = IntArray(n) { -1 }
            val prevEdge = IntArray(n) { -1 }
            val inQueue = BooleanArray(n)
            dist[s] = 0L
            val q = LinkedList<Int>()
            q.add(s); inQueue[s] = true

            while (q.isNotEmpty()) {
                val u = q.poll()
                inQueue[u] = false
                for ((idx, e) in graph[u].withIndex()) {
                    if (e.cap > 0 && dist[u] != Long.MAX_VALUE && dist[u] + e.cost < dist[e.to]) {
                        dist[e.to] = dist[u] + e.cost
                        prevNode[e.to] = u
                        prevEdge[e.to] = idx
                        if (!inQueue[e.to]) { q.add(e.to); inQueue[e.to] = true }
                    }
                }
            }

            if (dist[t] == Long.MAX_VALUE) break  // no augmenting path

            // Find bottleneck capacity along the shortest path
            var push = maxFlow - flow
            var cur = t
            while (cur != s) {
                push = minOf(push, graph[prevNode[cur]][prevEdge[cur]].cap)
                cur = prevNode[cur]
            }

            // Augment along the path
            cur = t
            while (cur != s) {
                val e = graph[prevNode[cur]][prevEdge[cur]]
                e.cap -= push
                graph[cur][e.rev].cap += push
                cur = prevNode[cur]
            }

            flow += push
            totalCost += push * dist[t]
        }

        return flow to totalCost
    }
}
