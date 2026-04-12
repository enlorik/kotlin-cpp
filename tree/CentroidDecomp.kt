// Centroid decomposition of an undirected tree.
// centParent[v]: parent of v in the centroid decomposition tree (-1 for the global centroid)
// depth[v]:      depth of v in the centroid decomposition tree
// forEachAncestor: iterate v and all its ancestors up the centroid tree
//
// Typical usage: for each vertex v, iterate its centroid ancestors to answer
// distance queries or path-counting problems in O(log n) per query.
class CentroidDecomp(private val g: Array<IntArray>) {
    private val n = g.size
    val centParent = IntArray(n) { -1 }
    val depth = IntArray(n)
    private val sz = IntArray(n)
    private val removed = BooleanArray(n)

    // Reusable stacks to avoid per-call allocation inside calcSz
    private val vStk = IntArray(n)
    private val pStk = IntArray(n)
    private val ord = IntArray(n)
    private val pord = IntArray(n)

    init {
        calcSz(0, -1)
        decompose(findCentroid(0, -1, sz[0]), -1, 0)
    }

    // Compute subtree sizes for the active (non-removed) component rooted at `root`.
    private fun calcSz(root: Int, initPar: Int) {
        var top = 0; var cnt = 0
        vStk[top] = root; pStk[top] = initPar; top++
        while (top > 0) {
            top--
            val v = vStk[top]; val par = pStk[top]
            ord[cnt] = v; pord[cnt] = par; cnt++
            for (u in g[v]) if (u != par && !removed[u]) {
                vStk[top] = u; pStk[top] = v; top++
            }
        }
        // Post-order pass: reverse of the pre-order traversal above
        for (i in cnt - 1 downTo 0) {
            sz[ord[i]] = 1
            for (u in g[ord[i]]) if (u != pord[i] && !removed[u]) sz[ord[i]] += sz[u]
        }
    }

    // Walk from `start` toward the centroid of its active component (size = treeSize).
    private fun findCentroid(start: Int, initPar: Int, treeSize: Int): Int {
        var v = start; var par = initPar
        loop@ while (true) {
            for (u in g[v]) if (u != par && !removed[u] && sz[u] > treeSize / 2) {
                par = v; v = u; continue@loop
            }
            return v
        }
    }

    // Recursion depth here is O(log n) — the height of the centroid decomposition tree.
    private fun decompose(c: Int, par: Int, d: Int) {
        centParent[c] = par; depth[c] = d; removed[c] = true
        for (u in g[c]) if (!removed[u]) {
            calcSz(u, -1)
            decompose(findCentroid(u, -1, sz[u]), c, d + 1)
        }
    }

    // Calls action(v), action(centParent[v]), action(centParent[centParent[v]]), ...
    // until the root of the centroid tree is reached. O(log n) iterations.
    fun forEachAncestor(v: Int, action: (Int) -> Unit) {
        var cur = v
        while (cur != -1) { action(cur); cur = centParent[cur] }
    }
}
