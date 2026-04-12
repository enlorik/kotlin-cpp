// Find all bridges in an undirected graph (iterative)
// Returns list of Pair(u, v) for each bridge edge
// Note: for multigraphs with parallel edges, a parallel edge is never a bridge.
// This implementation is correct for simple graphs; for multigraphs use edge-indexed adjacency.
fun bridges(g: Array<IntArray>): List<Pair<Int, Int>> {
    val n = g.size
    val disc = IntArray(n) { -1 }
    val low = IntArray(n)
    val result = mutableListOf<Pair<Int, Int>>()
    var timer = 0

    // frame: [node, parentNode, adjIndex]
    val stack = ArrayDeque<IntArray>()

    for (start in 0 until n) {
        if (disc[start] != -1) continue
        disc[start] = timer; low[start] = timer++
        stack.addLast(intArrayOf(start, -1, 0))

        while (stack.isNotEmpty()) {
            val frame = stack.last()
            val u = frame[0]; val parent = frame[1]; val i = frame[2]
            if (i < g[u].size) {
                frame[2]++
                val v = g[u][i]
                if (disc[v] == -1) {
                    disc[v] = timer; low[v] = timer++
                    stack.addLast(intArrayOf(v, u, 0))
                } else if (v != parent) {
                    if (disc[v] < low[u]) low[u] = disc[v]
                }
            } else {
                stack.removeLast()
                if (stack.isNotEmpty()) {
                    val pframe = stack.last()
                    val p = pframe[0]
                    if (low[u] < low[p]) low[p] = low[u]
                    if (low[u] > disc[p]) result.add(p to u)
                }
            }
        }
    }
    return result
}

// Find all articulation points in an undirected graph (iterative)
// Returns sorted list of articulation point vertex indices
fun articulationPoints(g: Array<IntArray>): List<Int> {
    val n = g.size
    val disc = IntArray(n) { -1 }
    val low = IntArray(n)
    val isAP = BooleanArray(n)
    var timer = 0

    // frame: [node, parentNode, adjIndex, childCount]
    val stack = ArrayDeque<IntArray>()

    for (start in 0 until n) {
        if (disc[start] != -1) continue
        disc[start] = timer; low[start] = timer++
        stack.addLast(intArrayOf(start, -1, 0, 0))

        while (stack.isNotEmpty()) {
            val frame = stack.last()
            val u = frame[0]; val parent = frame[1]; val i = frame[2]
            if (i < g[u].size) {
                frame[2]++
                val v = g[u][i]
                if (disc[v] == -1) {
                    frame[3]++          // count DFS-tree children of u
                    disc[v] = timer; low[v] = timer++
                    stack.addLast(intArrayOf(v, u, 0, 0))
                } else if (v != parent) {
                    if (disc[v] < low[u]) low[u] = disc[v]
                }
            } else {
                stack.removeLast()
                if (stack.isNotEmpty()) {
                    val pframe = stack.last()
                    val p = pframe[0]
                    if (low[u] < low[p]) low[p] = low[u]
                    // Non-root AP condition
                    if (pframe[1] != -1 && low[u] >= disc[p]) isAP[p] = true
                }
                // Root AP condition: root is AP iff it has >1 DFS-tree children
                if (parent == -1 && frame[3] > 1) isAP[u] = true
            }
        }
    }
    return isAP.indices.filter { isAP[it] }
}
