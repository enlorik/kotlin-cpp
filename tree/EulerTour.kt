// Euler tour (DFS timestamps) for trees.

// Returns (tin, tout) where:
//   tin[v]  = DFS entry time of v  (0-indexed, assigned in preorder)
//   tout[v] = DFS exit time of v   (= last tin value inside v's subtree, inclusive)
//
// Subtree of v = all u with tin[v] <= tin[u] <= tout[v].
// Uses the bitwise-complement trick: pushing x.inv() (<0) as an exit marker avoids
// a separate "visited" array and keeps the stack a plain IntArray.
fun eulerTour(g: Array<IntArray>, root: Int = 0): Pair<IntArray, IntArray> {
    val n = g.size
    val tin = IntArray(n)
    val tout = IntArray(n)
    var timer = 0
    val parent = IntArray(n) { -1 }
    val stk = IntArray(2 * n)
    var top = 0
    stk[top++] = root
    while (top > 0) {
        val x = stk[--top]
        if (x < 0) {
            // Exit marker: record last timer value for this vertex's subtree
            tout[x.inv()] = timer - 1
            continue
        }
        tin[x] = timer++
        stk[top++] = x.inv()   // push exit marker (processed after all descendants)
        for (u in g[x]) if (u != parent[x]) { parent[u] = x; stk[top++] = u }
    }
    return tin to tout
}

// Returns the DFS preorder sequence of vertex indices.
// Useful for flattening subtree queries: subtree of v occupies a contiguous
// slice of the returned array (use together with eulerTour for bounds).
fun dfsOrder(g: Array<IntArray>, root: Int = 0): IntArray {
    val n = g.size
    val order = IntArray(n)
    var cnt = 0
    val vis = BooleanArray(n)
    val stk = IntArray(n)
    var top = 0
    stk[top++] = root
    vis[root] = true
    while (top > 0) {
        val v = stk[--top]
        order[cnt++] = v
        for (u in g[v]) if (!vis[u]) { vis[u] = true; stk[top++] = u }
    }
    return order
}
