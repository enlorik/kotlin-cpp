/**
 * Network flow graph supporting Dinic's max-flow algorithm.
 * Complexity: O(V² · E) in general; O(E · √V) for unit-capacity graphs.
 *
 * Note: [dfsBlock] is recursive. For very deep level graphs run main() in a
 * thread with a larger stack:  Thread(null, ::solve, "main", 1 shl 26).start()
 */
class FlowGraph(val n: Int) {

    data class FlowEdge(val to: Int, var cap: Long, val rev: Int)

    val g: Array<ArrayList<FlowEdge>> = Array(n) { ArrayList() }

    /** Adds a directed edge u→v with capacity [cap] and the required reverse edge. */
    fun addEdge(u: Int, v: Int, cap: Long) {
        g[u].add(FlowEdge(v, cap,          g[v].size))
        g[v].add(FlowEdge(u, 0L,  g[u].size - 1))
    }

    // ── Dinic's algorithm ────────────────────────────────────────────────────

    private fun bfsLevel(s: Int, t: Int, level: IntArray): Boolean {
        level.fill(-1); level[s] = 0
        val q = ArrayDeque<Int>(); q.addLast(s)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (e in g[v]) if (e.cap > 0 && level[e.to] < 0) {
                level[e.to] = level[v] + 1; q.addLast(e.to)
            }
        }
        return level[t] >= 0
    }

    private fun dfsBlock(v: Int, t: Int, pushed: Long, level: IntArray, iter: IntArray): Long {
        if (v == t) return pushed
        while (iter[v] < g[v].size) {
            val e = g[v][iter[v]]
            if (e.cap > 0 && level[e.to] == level[v] + 1) {
                val d = dfsBlock(e.to, t, minOf(pushed, e.cap), level, iter)
                if (d > 0L) { e.cap -= d; g[e.to][e.rev].cap += d; return d }
            }
            iter[v]++
        }
        return 0L
    }

    /** Returns the value of the maximum flow from [s] to [t]. */
    fun maxFlow(s: Int, t: Int): Long {
        var flow = 0L
        val level = IntArray(n)
        while (bfsLevel(s, t, level)) {
            val iter = IntArray(n)
            var f: Long
            do { f = dfsBlock(s, t, GRAPH_INF, level, iter); flow += f } while (f > 0L)
        }
        return flow
    }

    // ── Min-cut helpers (call after maxFlow) ─────────────────────────────────

    /**
     * Returns a boolean array where [result][v] == true iff vertex v is on the
     * source side of the min cut (reachable from [s] in the residual graph).
     */
    fun minCutSide(s: Int): BooleanArray {
        val vis = BooleanArray(n)
        val q = ArrayDeque<Int>(); q.addLast(s); vis[s] = true
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (e in g[v]) if (e.cap > 0 && !vis[e.to]) { vis[e.to] = true; q.addLast(e.to) }
        }
        return vis
    }

    /**
     * Returns cut edges as triples (u, v, originalCapacity) where u is on the
     * source side and v is on the sink side.
     */
    fun minCutEdges(s: Int): List<Triple<Int, Int, Long>> {
        val side = minCutSide(s)
        val result = mutableListOf<Triple<Int, Int, Long>>()
        for (u in 0 until n) if (side[u]) for (e in g[u]) if (!side[e.to]) {
            val origCap = e.cap + g[e.to][e.rev].cap
            if (origCap > 0L) result.add(Triple(u, e.to, origCap))
        }
        return result
    }
}

// ── Min-cost max-flow ────────────────────────────────────────────────────────

/**
 * Min-cost max-flow network using SPFA (Bellman-Ford with queue) augmentation.
 * Handles negative-cost edges. Complexity: O(V · E · flow) in the worst case.
 */
class MCFlowGraph(val n: Int) {

    data class MCFlowEdge(val to: Int, var cap: Long, val cost: Long, val rev: Int)

    val g: Array<ArrayList<MCFlowEdge>> = Array(n) { ArrayList() }

    /** Adds directed edge u→v with capacity [cap] and per-unit cost [cost]. */
    fun addEdge(u: Int, v: Int, cap: Long, cost: Long) {
        g[u].add(MCFlowEdge(v, cap,  cost, g[v].size))
        g[v].add(MCFlowEdge(u, 0L, -cost, g[u].size - 1))
    }

    /**
     * Sends at most [maxFlow] units along cheapest paths from [s] to [t].
     * Returns a (flow, totalCost) pair.
     */
    fun minCostFlow(s: Int, t: Int, maxFlow: Long = GRAPH_INF): Pair<Long, Long> {
        var flow = 0L; var totalCost = 0L
        val dist     = LongArray(n)
        val prev     = IntArray(n)
        val prevEdge = IntArray(n)
        val inQueue  = BooleanArray(n)

        while (flow < maxFlow) {
            // SPFA shortest path in residual graph
            dist.fill(GRAPH_INF); dist[s] = 0L
            inQueue[s] = true
            val q = ArrayDeque<Int>(); q.addLast(s)
            while (q.isNotEmpty()) {
                val v = q.removeFirst(); inQueue[v] = false
                for ((i, e) in g[v].withIndex()) {
                    if (e.cap > 0 && dist[v] + e.cost < dist[e.to]) {
                        dist[e.to] = dist[v] + e.cost
                        prev[e.to] = v; prevEdge[e.to] = i
                        if (!inQueue[e.to]) { q.addLast(e.to); inQueue[e.to] = true }
                    }
                }
            }
            if (dist[t] == GRAPH_INF) break

            // Bottleneck along shortest path
            var push = maxFlow - flow
            var cur = t
            while (cur != s) { push = minOf(push, g[prev[cur]][prevEdge[cur]].cap); cur = prev[cur] }

            // Augment
            cur = t
            while (cur != s) {
                val e = g[prev[cur]][prevEdge[cur]]
                e.cap -= push; g[e.to][e.rev].cap += push
                cur = prev[cur]
            }
            flow += push; totalCost += push * dist[t]
        }
        return Pair(flow, totalCost)
    }

    /** Sends maximum possible flow along cheapest paths. Returns (flow, cost). */
    fun minCostMaxFlow(s: Int, t: Int) = minCostFlow(s, t)
}
