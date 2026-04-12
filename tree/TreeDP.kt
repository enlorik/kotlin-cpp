// Generic tree DP framework.

// Bottom-up DP on a rooted tree.
//
// init(v)                      — initial state for vertex v (before processing children)
// merge(state, childDP, child) — fold child's DP result into the running state for v
// finalize(v, state)           — optional post-processing after all children of v
//
// Example — subtree size:
//   treeDP(g, init = { 1 }, merge = { s, c, _ -> s + c })
//
// Returns dp[] where dp[v] holds the result for the subtree rooted at v.
// Access result as: dp[v] as T
@Suppress("UNCHECKED_CAST")
fun <T> treeDP(
    g: Array<IntArray>,
    root: Int = 0,
    init: (v: Int) -> T,
    merge: (parentState: T, childResult: T, child: Int) -> T,
    finalize: (v: Int, state: T) -> T = { _, s -> s }
): Array<Any?> {
    val n = g.size
    val dp = arrayOfNulls<Any>(n)
    val parent = IntArray(n) { -1 }
    val order = IntArray(n)
    var head = 0; var tail = 0
    order[tail++] = root
    val vis = BooleanArray(n); vis[root] = true
    while (head < tail) {
        val v = order[head++]
        for (u in g[v]) if (!vis[u]) { vis[u] = true; parent[u] = v; order[tail++] = u }
    }
    for (i in tail - 1 downTo 0) {
        val v = order[i]
        var state = init(v)
        for (u in g[v]) if (u != parent[v]) state = merge(state, dp[u] as T, u)
        dp[v] = finalize(v, state)
    }
    return dp
}

// Re-rooting DP: computes the DP answer for every possible root in O(n · max_degree).
// For trees with bounded degree (e.g. binary trees) this is O(n).
//
// init(v)                      — initial state for vertex v
// merge(state, childDP, child) — same semantics as treeDP; IMPORTANT: during the
//                                top-down pass the parent is passed as a virtual child,
//                                so merge will be called with child = parent[v].
//                                Ensure your merge function handles this correctly.
// finalize(v, state)           — same semantics as treeDP
//
// Returns ans[] where ans[v] is the DP answer when v is the root.
// Access result as: ans[v] as T
@Suppress("UNCHECKED_CAST")
fun <T> rerootDP(
    g: Array<IntArray>,
    init: (v: Int) -> T,
    merge: (T, T, Int) -> T,
    finalize: (Int, T) -> T = { _, s -> s }
): Array<Any?> {
    val n = g.size
    val down = arrayOfNulls<Any>(n)   // down[v] = treeDP answer for subtree of v
    val up = arrayOfNulls<Any>(n)     // up[v]   = finalized contribution from parent side
    val ans = arrayOfNulls<Any>(n)

    val parent = IntArray(n) { -1 }
    val order = IntArray(n)
    var head = 0; var tail = 0
    order[tail++] = 0
    val vis = BooleanArray(n); vis[0] = true
    while (head < tail) {
        val v = order[head++]
        for (u in g[v]) if (!vis[u]) { vis[u] = true; parent[u] = v; order[tail++] = u }
    }

    // Bottom-up pass: same as treeDP rooted at 0
    for (i in tail - 1 downTo 0) {
        val v = order[i]
        var st = init(v)
        for (u in g[v]) if (u != parent[v]) st = merge(st, down[u] as T, u)
        down[v] = finalize(v, st)
    }

    // Top-down pass: for each vertex v compute ans[v] and set up[child] for each child.
    // pref[j] = base merged with children[0..j-1], where base = init(v) + up[v] if parent exists.
    // For child j: state-without-j = pref[j] then re-merge children[j+1..m-1].
    // Complexity: O(deg(v)^2) per vertex, O(n * max_degree) total.
    for (i in 0 until tail) {
        val v = order[i]
        val children = g[v].filter { it != parent[v] }
        val m = children.size

        val base: T = if (parent[v] == -1) init(v) else merge(init(v), up[v] as T, parent[v])
        if (m == 0) {
            ans[v] = finalize(v, base)
            continue
        }

        // Build prefix array
        val pref = arrayOfNulls<Any>(m + 1)
        pref[0] = base
        for (j in 0 until m) pref[j + 1] = merge(pref[j] as T, down[children[j]] as T, children[j])
        ans[v] = finalize(v, pref[m] as T)

        // For each child c_j: up[c_j] = finalize(v, pref[j] extended with children[j+1..m-1])
        for (j in 0 until m) {
            var st = pref[j] as T
            for (k in j + 1 until m) st = merge(st, down[children[k]] as T, children[k])
            up[children[j]] = finalize(v, st)
        }
    }
    return ans
}
