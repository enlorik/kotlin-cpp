import java.util.PriorityQueue

/**
 * Adjacency-list graph (directed or undirected) for competitive programming.
 *
 * All shortest-path results use GRAPH_INF for unreachable vertices.
 * Algorithms that are only valid on trees / DAGs / undirected graphs are noted in their docs.
 *
 * Iterative DFS is used throughout to avoid JVM stack-overflow on large inputs.
 * Dijkstra / Prim use java.util.PriorityQueue (available on Codeforces).
 */
class SparseGraph(val n: Int, val directed: Boolean = false) {

    val g: Array<ArrayList<Edge>> = Array(n) { ArrayList() }

    fun addEdge(u: Int, v: Int, w: Long = 1L) {
        g[u].add(Edge(v, w))
        if (!directed) g[v].add(Edge(u, w))
    }

    // ── BFS (unweighted / unit-weight shortest paths) ─────────────────────────

    /**
     * Single-source BFS. Treats all edges as weight 1.
     * O(V + E).
     */
    fun bfs(src: Int): ShortestPathResult {
        val dist = LongArray(n) { GRAPH_INF }
        val prev = IntArray(n) { -1 }
        dist[src] = 0L
        val q = ArrayDeque<Int>(); q.addLast(src)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (e in g[v]) if (dist[e.to] == GRAPH_INF) {
                dist[e.to] = dist[v] + 1L; prev[e.to] = v; q.addLast(e.to)
            }
        }
        return ShortestPathResult(dist, prev)
    }

    /**
     * Multi-source BFS from all vertices in [sources] simultaneously.
     * Useful for "distance to nearest source" queries.
     */
    fun multiBfs(sources: Iterable<Int>): ShortestPathResult {
        val dist = LongArray(n) { GRAPH_INF }
        val prev = IntArray(n) { -1 }
        val q = ArrayDeque<Int>()
        for (s in sources) if (dist[s] == GRAPH_INF) { dist[s] = 0L; q.addLast(s) }
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (e in g[v]) if (dist[e.to] == GRAPH_INF) {
                dist[e.to] = dist[v] + 1L; prev[e.to] = v; q.addLast(e.to)
            }
        }
        return ShortestPathResult(dist, prev)
    }

    // ── Dijkstra (non-negative weights) ──────────────────────────────────────

    /**
     * Dijkstra's single-source shortest paths. Requires non-negative edge weights.
     * O((V + E) log V).
     */
    fun dijkstra(src: Int): ShortestPathResult {
        val dist = LongArray(n) { GRAPH_INF }
        val prev = IntArray(n) { -1 }
        dist[src] = 0L
        // PriorityQueue entries: LongArray(dist, vertex)
        val pq = PriorityQueue<LongArray>(compareBy { it[0] })
        pq.add(longArrayOf(0L, src.toLong()))
        while (pq.isNotEmpty()) {
            val top = pq.poll()
            val d = top[0]; val v = top[1].toInt()
            if (d > dist[v]) continue
            for (e in g[v]) {
                val nd = dist[v] + e.w
                if (nd < dist[e.to]) {
                    dist[e.to] = nd; prev[e.to] = v
                    pq.add(longArrayOf(nd, e.to.toLong()))
                }
            }
        }
        return ShortestPathResult(dist, prev)
    }

    /**
     * Multi-source Dijkstra: initialise with distance 0 at every vertex in [sources].
     */
    fun multiDijkstra(sources: Iterable<Int>): ShortestPathResult {
        val dist = LongArray(n) { GRAPH_INF }
        val prev = IntArray(n) { -1 }
        val pq = PriorityQueue<LongArray>(compareBy { it[0] })
        for (s in sources) if (dist[s] == GRAPH_INF) { dist[s] = 0L; pq.add(longArrayOf(0L, s.toLong())) }
        while (pq.isNotEmpty()) {
            val top = pq.poll()
            val d = top[0]; val v = top[1].toInt()
            if (d > dist[v]) continue
            for (e in g[v]) {
                val nd = dist[v] + e.w
                if (nd < dist[e.to]) {
                    dist[e.to] = nd; prev[e.to] = v
                    pq.add(longArrayOf(nd, e.to.toLong()))
                }
            }
        }
        return ShortestPathResult(dist, prev)
    }

    // ── Bellman-Ford (any weights, negative-cycle detection) ──────────────────

    /**
     * Bellman-Ford single-source shortest paths. Handles negative edge weights.
     * Returns (result, hasNegativeCycle). O(V · E).
     */
    fun bellmanFord(src: Int): Pair<ShortestPathResult, Boolean> {
        val dist = LongArray(n) { GRAPH_INF }; dist[src] = 0L
        val prev = IntArray(n) { -1 }

        val edges = mutableListOf<Triple<Int, Int, Long>>()
        for (u in 0 until n) for (e in g[u]) edges.add(Triple(u, e.to, e.w))

        var hasNegCycle = false
        for (iter in 0 until n) {
            var relaxed = false
            for ((u, v, w) in edges) {
                if (dist[u] != GRAPH_INF && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w; prev[v] = u; relaxed = true
                    if (iter == n - 1) hasNegCycle = true
                }
            }
            if (!relaxed) break
        }
        return Pair(ShortestPathResult(dist, prev), hasNegCycle)
    }

    // ── SPFA (Shortest Path Faster Algorithm) ─────────────────────────────────

    /**
     * SPFA — BFS-accelerated Bellman-Ford. Handles negative edge weights.
     * Returns (result, hasNegativeCycle). O(V · E) worst case, fast in practice.
     */
    fun spfa(src: Int): Pair<ShortestPathResult, Boolean> {
        val dist    = LongArray(n) { GRAPH_INF }; dist[src] = 0L
        val prev    = IntArray(n) { -1 }
        val inQueue = BooleanArray(n)
        val cnt     = IntArray(n)   // relax count; >= n implies negative cycle
        val q = ArrayDeque<Int>(); q.addLast(src); inQueue[src] = true
        var hasNegCycle = false

        outer@ while (q.isNotEmpty()) {
            val v = q.removeFirst(); inQueue[v] = false
            for (e in g[v]) {
                if (dist[v] + e.w < dist[e.to]) {
                    dist[e.to] = dist[v] + e.w; prev[e.to] = v
                    cnt[e.to]++
                    if (cnt[e.to] >= n) { hasNegCycle = true; break@outer }
                    if (!inQueue[e.to]) { q.addLast(e.to); inQueue[e.to] = true }
                }
            }
        }
        return Pair(ShortestPathResult(dist, prev), hasNegCycle)
    }

    // ── Topological sort (directed acyclic graphs) ────────────────────────────

    /**
     * Kahn's BFS topological sort for directed graphs.
     * Returns the topological order, or null if the graph contains a cycle.
     * O(V + E).
     */
    fun topoSort(): List<Int>? {
        val inDeg = IntArray(n)
        for (u in 0 until n) for (e in g[u]) inDeg[e.to]++
        val q = ArrayDeque<Int>()
        for (v in 0 until n) if (inDeg[v] == 0) q.addLast(v)
        val order = mutableListOf<Int>()
        while (q.isNotEmpty()) {
            val v = q.removeFirst(); order.add(v)
            for (e in g[v]) { inDeg[e.to]--; if (inDeg[e.to] == 0) q.addLast(e.to) }
        }
        return if (order.size == n) order else null
    }

    // ── Strongly connected components ─────────────────────────────────────────

    /**
     * Kosaraju's SCC decomposition for directed graphs.
     * SCC ids in [SCCResult.comp] are in reverse topological order (0 = a sink SCC).
     * O(V + E).
     */
    fun scc(): SCCResult {
        // Pass 1: iterative DFS on forward graph, record finish order
        val visited     = BooleanArray(n)
        val finishOrder = ArrayDeque<Int>()
        for (start in 0 until n) {
            if (visited[start]) continue
            visited[start] = true
            val stack = ArrayDeque<IntArray>()   // frame = [vertex, edgeIndex]
            stack.addLast(intArrayOf(start, 0))
            while (stack.isNotEmpty()) {
                val frame = stack.last()
                val v = frame[0]; val i = frame[1]
                if (i < g[v].size) {
                    frame[1]++
                    val w = g[v][i].to
                    if (!visited[w]) { visited[w] = true; stack.addLast(intArrayOf(w, 0)) }
                } else {
                    stack.removeLast(); finishOrder.addLast(v)
                }
            }
        }

        // Build reverse graph
        val rg = Array(n) { ArrayList<Int>() }
        for (u in 0 until n) for (e in g[u]) rg[e.to].add(u)

        // Pass 2: iterative DFS on reverse graph in reverse finish order
        val comp = IntArray(n) { -1 }
        var sccCount = 0
        while (finishOrder.isNotEmpty()) {
            val start = finishOrder.removeLast()
            if (comp[start] != -1) continue
            val stack = ArrayDeque<Int>(); stack.addLast(start); comp[start] = sccCount
            while (stack.isNotEmpty()) {
                val v = stack.removeLast()
                for (w in rg[v]) if (comp[w] == -1) { comp[w] = sccCount; stack.addLast(w) }
            }
            sccCount++
        }
        return SCCResult(sccCount, comp)
    }

    /**
     * Tarjan's iterative SCC decomposition for directed graphs.
     * Equivalent to [scc] but uses a single DFS pass. O(V + E).
     */
    fun tarjanSCC(): SCCResult {
        val disc    = IntArray(n) { -1 }
        val low     = IntArray(n)
        val onStack = BooleanArray(n)
        val stk     = ArrayDeque<Int>()   // Tarjan's vertex stack
        val comp    = IntArray(n) { -1 }
        var timer   = 0; var sccCount = 0

        for (start in 0 until n) {
            if (disc[start] != -1) continue
            val dfsStack = ArrayDeque<IntArray>()   // frame = [vertex, edgeIndex]
            disc[start] = timer; low[start] = timer++
            stk.addLast(start); onStack[start] = true
            dfsStack.addLast(intArrayOf(start, 0))

            while (dfsStack.isNotEmpty()) {
                val frame = dfsStack.last()
                val v = frame[0]; val i = frame[1]
                if (i < g[v].size) {
                    frame[1]++
                    val w = g[v][i].to
                    if (disc[w] == -1) {
                        disc[w] = timer; low[w] = timer++
                        stk.addLast(w); onStack[w] = true
                        dfsStack.addLast(intArrayOf(w, 0))
                    } else if (onStack[w]) {
                        low[v] = minOf(low[v], disc[w])
                    }
                } else {
                    dfsStack.removeLast()
                    if (dfsStack.isNotEmpty()) {
                        val parent = dfsStack.last()[0]
                        low[parent] = minOf(low[parent], low[v])
                    }
                    if (low[v] == disc[v]) {
                        while (true) {
                            val w = stk.removeLast(); onStack[w] = false; comp[w] = sccCount
                            if (w == v) break
                        }
                        sccCount++
                    }
                }
            }
        }
        return SCCResult(sccCount, comp)
    }

    // ── Bridges and articulation points (undirected graphs) ───────────────────

    /**
     * Finds all bridges and articulation points in an undirected graph.
     * Uses iterative DFS with Tarjan's low-link values. O(V + E).
     * For multigraphs (parallel edges) the result may be inaccurate.
     */
    fun bridgesAndArticulationPoints(): BridgeArtResult {
        val disc       = IntArray(n) { -1 }
        val low        = IntArray(n)
        val par        = IntArray(n) { -1 }
        val childCount = IntArray(n)
        val isAP       = BooleanArray(n)
        val bridges    = mutableListOf<Pair<Int, Int>>()
        var timer      = 0

        for (start in 0 until n) {
            if (disc[start] != -1) continue
            disc[start] = timer; low[start] = timer++
            val stack = ArrayDeque<IntArray>()   // frame = [vertex, edgeIndex]
            stack.addLast(intArrayOf(start, 0))

            while (stack.isNotEmpty()) {
                val frame = stack.last()
                val v = frame[0]; val i = frame[1]
                if (i < g[v].size) {
                    frame[1]++
                    val w = g[v][i].to
                    if (disc[w] == -1) {
                        par[w] = v; childCount[v]++
                        disc[w] = timer; low[w] = timer++
                        stack.addLast(intArrayOf(w, 0))
                    } else if (w != par[v]) {
                        low[v] = minOf(low[v], disc[w])
                    }
                } else {
                    stack.removeLast()
                    val p = par[v]
                    if (p != -1) {
                        low[p] = minOf(low[p], low[v])
                        if (par[p] == -1) {
                            // p is a DFS root: AP if it has ≥ 2 DFS-tree children
                            if (childCount[p] > 1) isAP[p] = true
                        } else {
                            if (low[v] >= disc[p]) isAP[p] = true
                        }
                        if (low[v] > disc[p]) bridges.add(p to v)
                    }
                }
            }
        }
        return BridgeArtResult(bridges, (0 until n).filter { isAP[it] })
    }

    // ── Minimum spanning tree ─────────────────────────────────────────────────

    /**
     * Prim's MST for undirected connected graphs. O((V + E) log V).
     * Returns null if the graph is not connected.
     */
    fun primMST(): MSTResult? {
        val key    = LongArray(n) { GRAPH_INF }; key[0] = 0L
        val parent = IntArray(n) { -1 }
        val inMST  = BooleanArray(n)
        val pq = PriorityQueue<LongArray>(compareBy { it[0] })
        pq.add(longArrayOf(0L, 0L))
        var totalWeight = 0L
        val mstEdges = mutableListOf<Triple<Int, Int, Long>>()

        while (pq.isNotEmpty()) {
            val top = pq.poll()
            val w = top[0]; val v = top[1].toInt()
            if (inMST[v]) continue
            inMST[v] = true; totalWeight += w
            if (parent[v] != -1) mstEdges.add(Triple(parent[v], v, w))
            for (e in g[v]) if (!inMST[e.to] && e.w < key[e.to]) {
                key[e.to] = e.w; parent[e.to] = v
                pq.add(longArrayOf(e.w, e.to.toLong()))
            }
        }
        return if (inMST.all { it }) MSTResult(totalWeight, mstEdges) else null
    }

    /**
     * Kruskal's MST for undirected graphs using DSU. O(E log E).
     * Returns null if the graph is not connected (i.e. a spanning forest, not a tree).
     * For a spanning forest, remove the null check and always return the result.
     */
    fun kruskalMST(): MSTResult? {
        val allEdges = mutableListOf<Triple<Int, Int, Long>>()
        for (u in 0 until n) for (e in g[u]) if (u < e.to) allEdges.add(Triple(u, e.to, e.w))
        allEdges.sortBy { it.third }

        val dsu = DSU(n)
        var totalWeight = 0L
        val mstEdges = mutableListOf<Triple<Int, Int, Long>>()
        for ((u, v, w) in allEdges) {
            if (dsu.union(u, v)) {
                totalWeight += w; mstEdges.add(Triple(u, v, w))
                if (mstEdges.size == n - 1) break
            }
        }
        return if (mstEdges.size == n - 1) MSTResult(totalWeight, mstEdges) else null
    }

    // ── Euler circuit (Hierholzer's algorithm) ────────────────────────────────

    /**
     * Finds an Euler circuit starting at [start] using Hierholzer's algorithm.
     * Assumes the circuit exists (all vertices have even degree for undirected,
     * in-degree == out-degree for directed). O(V + E).
     * For an Euler *path* (two odd-degree vertices), pass the odd-degree start vertex.
     */
    fun eulerCircuit(start: Int = 0): List<Int> {
        // Work on a copy of adjacency indices so the original graph is unchanged
        val idx  = IntArray(n)
        val path = mutableListOf<Int>()
        val stack = ArrayDeque<Int>(); stack.addLast(start)
        while (stack.isNotEmpty()) {
            val v = stack.last()
            if (idx[v] < g[v].size) {
                stack.addLast(g[v][idx[v]++].to)
            } else {
                path.add(stack.removeLast())
            }
        }
        path.reverse()
        return path
    }

    // ── Connectivity ──────────────────────────────────────────────────────────

    /**
     * Finds connected components (or weakly connected for directed graphs).
     * Returns (componentCount, comp) where comp[v] = component id of v.
     */
    fun connectedComponents(): Pair<Int, IntArray> {
        val comp = IntArray(n) { -1 }
        var count = 0
        for (start in 0 until n) {
            if (comp[start] != -1) continue
            val stack = ArrayDeque<Int>(); stack.addLast(start); comp[start] = count
            while (stack.isNotEmpty()) {
                val v = stack.removeLast()
                for (e in g[v]) if (comp[e.to] == -1) { comp[e.to] = count; stack.addLast(e.to) }
            }
            count++
        }
        return Pair(count, comp)
    }

    /**
     * 2-colouring / bipartite check.
     * Returns (isBipartite, color) where color[v] ∈ {0, 1} for bipartite graphs.
     * color[v] == -1 for vertices not yet visited when isBipartite == false.
     */
    fun isBipartite(): Pair<Boolean, IntArray> {
        val color = IntArray(n) { -1 }
        for (start in 0 until n) {
            if (color[start] != -1) continue
            color[start] = 0
            val q = ArrayDeque<Int>(); q.addLast(start)
            while (q.isNotEmpty()) {
                val v = q.removeFirst()
                for (e in g[v]) {
                    if (color[e.to] == -1) { color[e.to] = 1 - color[v]; q.addLast(e.to) }
                    else if (color[e.to] == color[v]) return Pair(false, color)
                }
            }
        }
        return Pair(true, color)
    }

    // ── LCA with binary lifting (trees / forests) ─────────────────────────────

    private val LOG = 20
    private var lcaUp:    Array<IntArray>? = null   // lcaUp[v][k] = 2^k-th ancestor of v
    private var lcaDepth: IntArray?        = null

    /**
     * Preprocesses LCA binary lifting from [root]. Call once before [lca] queries.
     * O(V log V) preprocessing. Works on forests (one call per root).
     */
    fun buildLCA(root: Int = 0) {
        if (lcaUp == null) {
            lcaUp    = Array(n) { IntArray(LOG) }
            lcaDepth = IntArray(n) { -1 }
        }
        val up    = lcaUp!!
        val depth = lcaDepth!!
        // BFS to assign parent and depth
        depth[root] = 0; up[root][0] = root
        val q = ArrayDeque<Int>(); q.addLast(root)
        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (e in g[v]) if (depth[e.to] == -1) {
                depth[e.to] = depth[v] + 1; up[e.to][0] = v; q.addLast(e.to)
            }
        }
        // Fill sparse table
        for (k in 1 until LOG) for (v in 0 until n) {
            up[v][k] = if (depth[v] != -1) up[up[v][k - 1]][k - 1] else v
        }
    }

    /**
     * Returns the lowest common ancestor of [u] and [v].
     * Requires [buildLCA] to have been called first.
     */
    fun lca(u: Int, v: Int): Int {
        val up    = lcaUp    ?: error("buildLCA not called")
        val depth = lcaDepth ?: error("buildLCA not called")
        var a = u; var b = v
        if (depth[a] < depth[b]) { val t = a; a = b; b = t }
        var diff = depth[a] - depth[b]
        for (k in 0 until LOG) if (diff ushr k and 1 == 1) a = up[a][k]
        if (a == b) return a
        for (k in LOG - 1 downTo 0) if (up[a][k] != up[b][k]) { a = up[a][k]; b = up[b][k] }
        return up[a][0]
    }

    /**
     * Returns the distance between [u] and [v] in a tree (sum of edge weights).
     * Requires [buildLCA] for LCA queries; uses weighted distances from BFS/Dijkstra.
     * [dist] must be pre-computed from the tree root via [dijkstra] or [bfs].
     */
    fun treeDist(u: Int, v: Int, dist: LongArray): Long {
        val l = lca(u, v)
        return dist[u] + dist[v] - 2L * dist[l]
    }

    // ── Tree utilities ────────────────────────────────────────────────────────

    /**
     * Computes the diameter of an unweighted tree.
     * Returns (diameter, endpointU, endpointV). O(V).
     */
    fun treeDiameter(): Triple<Long, Int, Int> {
        // BFS from vertex 0 to find one endpoint
        val d1 = bfs(0)
        val u = (0 until n).maxByOrNull { d1.dist[it] }!!
        // BFS from that endpoint to find the other
        val d2 = bfs(u)
        val v = (0 until n).maxByOrNull { d2.dist[it] }!!
        return Triple(d2.dist[v], u, v)
    }

    /**
     * Computes the diameter of a weighted tree via two Dijkstra passes.
     * Returns (diameter, endpointU, endpointV). O(V log V).
     */
    fun weightedTreeDiameter(): Triple<Long, Int, Int> {
        val d1 = dijkstra(0)
        val u = (0 until n).maxByOrNull { if (d1.dist[it] < GRAPH_INF) d1.dist[it] else -1L }!!
        val d2 = dijkstra(u)
        val v = (0 until n).maxByOrNull { if (d2.dist[it] < GRAPH_INF) d2.dist[it] else -1L }!!
        return Triple(d2.dist[v], u, v)
    }

    /**
     * Returns the DFS pre-order of vertices starting at [root].
     * Useful for Euler-tour / HLD preprocessing. O(V + E).
     */
    fun dfsOrder(root: Int = 0): List<Int> {
        val order   = mutableListOf<Int>()
        val visited = BooleanArray(n)
        val stack   = ArrayDeque<IntArray>()   // frame = [vertex, edgeIndex]
        stack.addLast(intArrayOf(root, 0)); visited[root] = true
        while (stack.isNotEmpty()) {
            val frame = stack.last()
            val v = frame[0]; val i = frame[1]
            if (i == 0) order.add(v)
            if (i < g[v].size) {
                frame[1]++
                val w = g[v][i].to
                if (!visited[w]) { visited[w] = true; stack.addLast(intArrayOf(w, 0)) }
            } else {
                stack.removeLast()
            }
        }
        return order
    }
}
