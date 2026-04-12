// Heavy-Light Decomposition
// Allows path queries using a segment tree on pos[] array
// g: adjacency list of rooted tree (0-indexed)
// root: root vertex
class HLD(g: Array<IntArray>, root: Int = 0) {
    private val n = g.size
    val pos = IntArray(n)       // HLD position of vertex in linearised array
    val head = IntArray(n)      // chain head for vertex
    val depth = IntArray(n)
    val parent = IntArray(n) { -1 }
    val sz = IntArray(n) { 1 }
    private var timer = 0

    init {
        // BFS to compute parent, depth, and subtree sizes
        val bfsOrder = IntArray(n)
        var bfsHead = 0; var bfsTail = 0
        bfsOrder[bfsTail++] = root
        val vis = BooleanArray(n)
        vis[root] = true
        while (bfsHead < bfsTail) {
            val v = bfsOrder[bfsHead++]
            for (u in g[v]) if (!vis[u]) {
                vis[u] = true
                parent[u] = v
                depth[u] = depth[v] + 1
                bfsOrder[bfsTail++] = u
            }
        }
        for (i in bfsTail - 1 downTo 1) sz[parent[bfsOrder[i]]] += sz[bfsOrder[i]]

        // Iterative DFS assigning HLD positions.
        // Heavy child is pushed last so it is popped first, giving it pos[v]+1
        // and ensuring heavy chains occupy contiguous ranges.
        head[root] = root
        val stk = IntArray(n)
        var top = 0
        stk[top++] = root
        while (top > 0) {
            val v = stk[--top]
            pos[v] = timer++
            var heavy = -1; var maxSz = 0
            for (u in g[v]) if (u != parent[v] && sz[u] > maxSz) { maxSz = sz[u]; heavy = u }
            // Push light children first (processed later), heavy child last (processed next)
            for (u in g[v]) if (u != parent[v] && u != heavy) { head[u] = u; stk[top++] = u }
            if (heavy != -1) { head[heavy] = head[v]; stk[top++] = heavy }
        }
    }

    // LCA of u and v via chain climbing
    fun pathLCA(u: Int, v: Int): Int {
        var a = u; var b = v
        while (head[a] != head[b]) {
            if (depth[head[a]] < depth[head[b]]) { val t = a; a = b; b = t }
            a = parent[head[a]]
        }
        return if (depth[a] <= depth[b]) a else b
    }

    // Decompose path u->v into half-open [l, r) intervals on the HLD array.
    // Apply a segment-tree query/update to each returned range and combine results.
    fun pathRanges(u: Int, v: Int): List<Pair<Int, Int>> {
        val res = mutableListOf<Pair<Int, Int>>()
        var a = u; var b = v
        while (head[a] != head[b]) {
            if (depth[head[a]] < depth[head[b]]) { val t = a; a = b; b = t }
            res.add(pos[head[a]] to pos[a] + 1)
            a = parent[head[a]]
        }
        val lo = if (depth[a] <= depth[b]) pos[a] else pos[b]
        val hi = if (depth[a] <= depth[b]) pos[b] else pos[a]
        res.add(lo to hi + 1)
        return res
    }
}
