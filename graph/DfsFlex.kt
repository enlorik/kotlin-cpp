/** Result of iterative depth-first search of a SparseGraph. */
data class DfsFlexResult(
    /** DFS tree depth, or -1 when unreachable; NOT a shortest-path distance. */
    val depth: IntArray,
    /** DFS tree parent; -1 for the root and unreachable vertices. */
    val prev: IntArray,
    val enterOrder: IntArray,
    val exitOrder: IntArray
) {
    /** Alias for discovery order. */
    val order: IntArray get() = enterOrder
    fun hasPath(v: Int): Boolean = depth[v] != -1
    fun reconstructPath(v: Int): List<Int> {
        if (!hasPath(v)) return emptyList()
        val path = ArrayList<Int>()
        var u = v
        while (u != -1) {
            path.add(u)
            u = prev[u]
        }
        path.reverse()
        return path
    }
}

/**
 * Iterative DFS with custom edge filtering, discovery, finish, and tree-edge hooks.
 * Each vertex is entered at most once, in graph adjacency-list order.
 * canGo is called only on edges to not-yet-visited vertices.
 * onStep runs after depth/prev are set and before onEnter for the child.
 * onExit runs when ALL eligible outgoing edges of a vertex are explored.
 * target stops when first entered, without onExit for active frames.
 * O(V+E) time for O(1) callbacks; O(V) extra space.
 */
inline fun SparseGraph.dfsFlex(
    src: Int,
    target: Int = -1,
    canGo: (u: Int, e: Edge) -> Boolean = { _, _ -> true },
    onEnter: (u: Int) -> Unit = {},
    onExit: (u: Int) -> Unit = {},
    onStep: (u: Int, v: Int, e: Edge) -> Unit = { _, _, _ -> }
): DfsFlexResult {
    require(src in 0 until n) { "src out of bounds" }
    require(target == -1 || target in 0 until n) { "target out of bounds" }

    val depth = IntArray(n) { -1 }
    val prev = IntArray(n) { -1 }
    val enter = IntArray(n)
    val exit = IntArray(n)
    val stackVertex = IntArray(n)
    val stackNext = IntArray(n)
    var top = 0
    var entered = 1
    var exited = 0
    depth[src] = 0
    stackVertex[0] = src
    enter[0] = src
    onEnter(src)
    if (src != target) {
        while (top >= 0) {
            val u = stackVertex[top]
            val i = stackNext[top]
            if (i == g[u].size) {
                onExit(u)
                exit[exited++] = u
                top--
                continue
            }
            stackNext[top] = i + 1
            val e = g[u][i]
            val v = e.to
            if (depth[v] != -1 || !canGo(u, e)) continue
            depth[v] = depth[u] + 1
            prev[v] = u
            onStep(u, v, e)
            enter[entered++] = v
            top++
            stackVertex[top] = v
            stackNext[top] = 0
            onEnter(v)
            if (v == target) break
        }
    }
    return DfsFlexResult(depth, prev, enter.copyOf(entered), exit.copyOf(exited))
}
