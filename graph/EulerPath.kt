// Hierholzer's algorithm for Euler path / circuit.
//
// For directed=true (default):
//   g[u] should contain the out-neighbours of u.
//   Euler path  exists iff exactly 0 or 2 vertices have |outdeg - indeg| = 1
//   (one start vertex with outdeg - indeg = 1, one end vertex with indeg - outdeg = 1,
//    all others balanced).
//   Euler circuit exists iff all vertices are balanced (outdeg == indeg).
//
// For directed=false:
//   g[u] should contain ALL neighbours of u (each undirected edge listed from both ends).
//   Euler path  exists iff 0 or 2 vertices have odd degree.
//   Euler circuit exists iff all vertices have even degree.
//   The function will consume (remove) edges from g during traversal.
//
// Returns an IntArray of vertices forming the path/circuit,
// or null if the required Euler path/circuit does not exist.

fun eulerPath(g: Array<MutableList<Int>>, directed: Boolean = true): IntArray? {
    val n = g.size
    val totalEdges: Int
    val start: Int

    if (directed) {
        val outDeg = IntArray(n) { g[it].size }
        val inDeg  = IntArray(n)
        for (u in 0 until n) for (v in g[u]) inDeg[v]++

        val diff = IntArray(n) { outDeg[it] - inDeg[it] }
        if ((0 until n).any { diff[it] < -1 || diff[it] > 1 }) return null
        val srcs = (0 until n).filter { diff[it] == 1 }
        val snks = (0 until n).filter { diff[it] == -1 }
        if (srcs.size != snks.size || srcs.size > 1) return null

        start = when {
            srcs.size == 1 -> srcs[0]
            else -> (0 until n).firstOrNull { outDeg[it] > 0 } ?: 0
        }
        totalEdges = outDeg.sum()
    } else {
        val deg = IntArray(n) { g[it].size }
        val oddV = (0 until n).filter { deg[it] % 2 == 1 }
        if (oddV.size != 0 && oddV.size != 2) return null
        start = when (oddV.size) {
            2    -> oddV[0]
            else -> (0 until n).firstOrNull { deg[it] > 0 } ?: 0
        }
        totalEdges = deg.sum() / 2
    }

    return hierholzer(g, directed, start, totalEdges)
}

fun eulerCircuit(g: Array<MutableList<Int>>, directed: Boolean = true): IntArray? {
    val n = g.size
    val totalEdges: Int
    val start: Int

    if (directed) {
        val outDeg = IntArray(n) { g[it].size }
        val inDeg  = IntArray(n)
        for (u in 0 until n) for (v in g[u]) inDeg[v]++
        if ((0 until n).any { outDeg[it] != inDeg[it] }) return null
        start = (0 until n).firstOrNull { outDeg[it] > 0 } ?: return intArrayOf(0)
        totalEdges = outDeg.sum()
    } else {
        val deg = IntArray(n) { g[it].size }
        if ((0 until n).any { deg[it] % 2 == 1 }) return null
        start = (0 until n).firstOrNull { deg[it] > 0 } ?: return intArrayOf(0)
        totalEdges = deg.sum() / 2
    }

    return hierholzer(g, directed, start, totalEdges)
}

private fun hierholzer(
    g: Array<MutableList<Int>>,
    directed: Boolean,
    start: Int,
    totalEdges: Int
): IntArray? {
    val idx  = IntArray(g.size)   // pointer for directed traversal
    val path = mutableListOf<Int>()
    val stack = ArrayDeque<Int>()
    stack.addLast(start)

    if (directed) {
        while (stack.isNotEmpty()) {
            val u = stack.last()
            if (idx[u] < g[u].size) {
                stack.addLast(g[u][idx[u]++])
            } else {
                path.add(stack.removeLast())
            }
        }
    } else {
        // Undirected: remove edges as they are traversed to avoid reuse.
        // Each undirected edge is stored in both directions; removing from both ends
        // ensures each physical edge is traversed exactly once.
        // O(E * deg) due to lastIndexOf; acceptable for sparse graphs.
        while (stack.isNotEmpty()) {
            val u = stack.last()
            if (g[u].isNotEmpty()) {
                val v = g[u].removeLast()
                val ri = g[v].lastIndexOf(u)
                if (ri >= 0) g[v].removeAt(ri)
                stack.addLast(v)
            } else {
                path.add(stack.removeLast())
            }
        }
    }

    path.reverse()
    return if (path.size == totalEdges + 1) path.toIntArray() else null
}
