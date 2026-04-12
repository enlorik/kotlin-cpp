// LCA using binary lifting
// g: adjacency list of rooted tree (0-indexed)
// root: root vertex
class LCA(g: Array<IntArray>, root: Int = 0) {
    private val LOG = 20
    private val n = g.size
    val depth = IntArray(n)
    private val up = Array(LOG) { IntArray(n) }

    init {
        val parent = IntArray(n) { -1 }
        val queue = IntArray(n)
        var head = 0; var tail = 0
        queue[tail++] = root
        val vis = BooleanArray(n)
        vis[root] = true
        while (head < tail) {
            val v = queue[head++]
            for (u in g[v]) if (!vis[u]) {
                vis[u] = true
                parent[u] = v
                depth[u] = depth[v] + 1
                queue[tail++] = u
            }
        }
        for (v in 0 until n) up[0][v] = if (parent[v] == -1) v else parent[v]
        for (k in 1 until LOG) for (v in 0 until n) up[k][v] = up[k - 1][up[k - 1][v]]
    }

    fun lca(u: Int, v: Int): Int {
        var a = u; var b = v
        if (depth[a] < depth[b]) { val t = a; a = b; b = t }
        var diff = depth[a] - depth[b]
        for (k in 0 until LOG) if (diff ushr k and 1 == 1) a = up[k][a]
        if (a == b) return a
        for (k in LOG - 1 downTo 0) if (up[k][a] != up[k][b]) { a = up[k][a]; b = up[k][b] }
        return up[0][a]
    }

    // Number of edges on path u-v
    fun dist(u: Int, v: Int) = depth[u] + depth[v] - 2 * depth[lca(u, v)]
}

fun lca(g: Array<IntArray>, root: Int = 0) = LCA(g, root)
