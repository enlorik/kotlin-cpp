import java.util.LinkedList

// Iterative DFS to avoid stack overflow
// Returns vertices in order of first visit
fun dfs(g: Array<IntArray>, src: Int): IntArray {
    val n = g.size
    val visited = BooleanArray(n)
    val order = mutableListOf<Int>()
    val stack = ArrayDeque<Int>()
    stack.addLast(src)
    while (stack.isNotEmpty()) {
        val u = stack.removeLast()
        if (visited[u]) continue
        visited[u] = true
        order.add(u)
        for (i in g[u].indices.reversed()) {
            if (!visited[g[u][i]]) stack.addLast(g[u][i])
        }
    }
    return order.toIntArray()
}

// Topological sort using Kahn's algorithm (BFS-based)
// Returns topo order, or empty IntArray if graph contains a cycle
fun topoSort(g: Array<IntArray>): IntArray {
    val n = g.size
    val inDeg = IntArray(n)
    for (u in 0 until n) for (v in g[u]) inDeg[v]++
    val q = LinkedList<Int>()
    for (i in 0 until n) if (inDeg[i] == 0) q.add(i)
    val result = mutableListOf<Int>()
    while (q.isNotEmpty()) {
        val u = q.poll()
        result.add(u)
        for (v in g[u]) if (--inDeg[v] == 0) q.add(v)
    }
    return if (result.size == n) result.toIntArray() else IntArray(0)
}

// Check if undirected graph is bipartite using BFS
// Returns 2-coloring array (values 0 or 1), or null if not bipartite
fun bipartite(g: Array<IntArray>): IntArray? {
    val n = g.size
    val color = IntArray(n) { -1 }
    val q = LinkedList<Int>()
    for (start in 0 until n) {
        if (color[start] != -1) continue
        color[start] = 0
        q.add(start)
        while (q.isNotEmpty()) {
            val u = q.poll()
            for (v in g[u]) {
                when {
                    color[v] == -1 -> { color[v] = 1 - color[u]; q.add(v) }
                    color[v] == color[u] -> return null
                }
            }
        }
    }
    return color
}
