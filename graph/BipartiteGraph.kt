/**
 * Bipartite graph with left vertices [0, n) and right vertices [0, m).
 * Supports Hopcroft-Karp maximum matching and König's minimum vertex cover.
 */
class BipartiteGraph(val n: Int, val m: Int) {

    val adj: Array<ArrayList<Int>> = Array(n) { ArrayList() }

    /** Adds an edge from left vertex [left] to right vertex [right]. */
    fun addEdge(left: Int, right: Int) { adj[left].add(right) }

    // ── Hopcroft-Karp maximum bipartite matching ──────────────────────────────

    /**
     * Computes a maximum bipartite matching in O(E · √V).
     * Returns matchL where matchL[u] = matched right vertex for left u, or -1 if unmatched.
     */
    fun maxMatching(): IntArray {
        val matchL = IntArray(n) { -1 }
        val matchR = IntArray(m) { -1 }
        val dist   = IntArray(n)
        val INF    = Int.MAX_VALUE

        fun bfs(): Boolean {
            dist.fill(INF)
            val q = ArrayDeque<Int>()
            for (u in 0 until n) if (matchL[u] == -1) { dist[u] = 0; q.addLast(u) }
            var found = false
            while (q.isNotEmpty()) {
                val u = q.removeFirst()
                for (v in adj[u]) {
                    val w = matchR[v]
                    if (w == -1) found = true
                    else if (dist[w] == INF) { dist[w] = dist[u] + 1; q.addLast(w) }
                }
            }
            return found
        }

        fun dfs(u: Int): Boolean {
            for (v in adj[u]) {
                val w = matchR[v]
                if (w == -1 || (dist[w] == dist[u] + 1 && dfs(w))) {
                    matchL[u] = v; matchR[v] = u; return true
                }
            }
            dist[u] = INF
            return false
        }

        while (bfs()) for (u in 0 until n) if (matchL[u] == -1) dfs(u)
        return matchL
    }

    /** Returns the number of matched pairs in a given [matchL] array. */
    fun matchingSize(matchL: IntArray) = matchL.count { it != -1 }

    // ── König's minimum vertex cover ──────────────────────────────────────────

    /**
     * Given a maximum matching [matchL], computes a minimum vertex cover via König's theorem.
     * Returns (leftCover, rightCover): the left and right vertex indices in the cover.
     * |leftCover| + |rightCover| == matchingSize(matchL).
     */
    fun minVertexCover(matchL: IntArray): Pair<List<Int>, List<Int>> {
        val matchR = IntArray(m) { -1 }
        for (u in 0 until n) if (matchL[u] != -1) matchR[matchL[u]] = u

        // BFS alternating tree from all unmatched left vertices
        val reachL = BooleanArray(n)
        val reachR = BooleanArray(m)
        val q = ArrayDeque<Int>()
        for (u in 0 until n) if (matchL[u] == -1) { reachL[u] = true; q.addLast(u) }
        while (q.isNotEmpty()) {
            val u = q.removeFirst()
            for (v in adj[u]) if (!reachR[v]) {
                reachR[v] = true
                val w = matchR[v]
                if (w != -1 && !reachL[w]) { reachL[w] = true; q.addLast(w) }
            }
        }
        // Cover = left vertices NOT in alternating tree + right vertices IN alternating tree
        return Pair(
            (0 until n).filter { !reachL[it] },
            (0 until m).filter {  reachR[it] }
        )
    }

    /**
     * Maximum independent set in a bipartite graph (complement of min vertex cover).
     * Returns (leftIndep, rightIndep).
     */
    fun maxIndependentSet(matchL: IntArray): Pair<List<Int>, List<Int>> {
        val (lc, rc) = minVertexCover(matchL)
        val lcSet = lc.toHashSet(); val rcSet = rc.toHashSet()
        return Pair(
            (0 until n).filter { it !in lcSet },
            (0 until m).filter { it !in rcSet }
        )
    }
}

// ── Hungarian algorithm (min-cost perfect matching) ───────────────────────────

/**
 * Hungarian algorithm for minimum-cost perfect matching on an n×n cost matrix.
 * Complexity: O(n³).
 *
 * Returns (minCost, assignment) where assignment[i] = j means left vertex i is
 * matched to right vertex j.
 *
 * For maximum-cost matching, negate all costs before calling.
 */
fun hungarianMinCost(cost: Array<LongArray>): Pair<Long, IntArray> {
    val n = cost.size
    // 1-indexed potentials and matching arrays (index 0 = "dummy" unmatched node)
    val u   = LongArray(n + 1)   // left potentials
    val v   = LongArray(n + 1)   // right potentials
    val p   = IntArray(n + 1)    // p[j] = left vertex matched to right j
    val way = IntArray(n + 1)    // augmenting-path traceback

    for (i in 1..n) {
        p[0] = i
        var j0 = 0
        val minVal = LongArray(n + 1) { GRAPH_INF }
        val used   = BooleanArray(n + 1)
        do {
            used[j0] = true
            val i0 = p[j0]; var delta = GRAPH_INF; var j1 = 0
            for (j in 1..n) if (!used[j]) {
                val cur = cost[i0 - 1][j - 1] - u[i0] - v[j]
                if (cur < minVal[j]) { minVal[j] = cur; way[j] = j0 }
                if (minVal[j] < delta) { delta = minVal[j]; j1 = j }
            }
            for (j in 0..n) {
                if (used[j]) { u[p[j]] += delta; v[j] -= delta }
                else minVal[j] -= delta
            }
            j0 = j1
        } while (p[j0] != 0)
        do { val j1 = way[j0]; p[j0] = p[j1]; j0 = j1 } while (j0 != 0)
    }

    val ans = IntArray(n)
    for (j in 1..n) if (p[j] != 0) ans[p[j] - 1] = j - 1
    return Pair(-v[0], ans)
}
