/** A compressed rooted tree: vertices are original IDs in DFS order.
 * parent[i] is an index in vertices (-1 at root); edgeLength[i] is original distance.
 */
data class VirtualTree(val vertices: IntArray, val parent: IntArray, val edgeLength: IntArray)

/** Rooted tree index from a parent array with exactly one -1 root.
 * O(n log n) preprocessing and space; iterative DFS (no recursion depth limit).
 */
class VirtualTreeIndex(parents: IntArray) {
    val size = parents.size
    private val depth = IntArray(size)
    private val tin = IntArray(size)
    private val end = IntArray(size)
    private val up: Array<IntArray>
    init {
        require(size > 0)
        val root = parents.indexOf(-1)
        require(root >= 0 && parents.count { it == -1 } == 1)
        val children = Array(size) { ArrayList<Int>() }
        for (v in 0 until size) if (v != root) {
            require(parents[v] in 0 until size && parents[v] != v)
            children[parents[v]].add(v)
        }
        val stack = IntArray(size)
        val order = IntArray(size)
        var top = 0; var count = 0
        stack[top++] = root
        while (top > 0) {
            val v = stack[--top]
            tin[v] = count; order[count++] = v
            for (u in children[v]) { depth[u] = depth[v]+1; stack[top++] = u }
        }
        require(count == size) { "Parents must describe one rooted tree" }
        for (i in size-1 downTo 0) {
            val v = order[i]
            end[v] = tin[v]+1
            for (u in children[v]) end[v] = maxOf(end[v],end[u])
        }
        up = Array(32-Integer.numberOfLeadingZeros(size)) { IntArray(size) }
        for (v in 0 until size) up[0][v] = if (v == root) root else parents[v]
        for (k in 1 until up.size) for (v in 0 until size)
            up[k][v] = up[k-1][up[k-1][v]]
    }
    private fun checkVertex(v: Int) { require(v in 0 until size) }
    private fun ancestor(a: Int,b: Int): Boolean = tin[a] <= tin[b] && tin[b] < end[a]
    fun isAncestor(a: Int,b: Int): Boolean {
        checkVertex(a); checkVertex(b); return ancestor(a,b)
    }
    fun lca(a: Int,b: Int): Int {
        checkVertex(a); checkVertex(b)
        if (ancestor(a,b)) return a
        if (ancestor(b,a)) return b
        var v = a
        for (k in up.indices.reversed()) if (!ancestor(up[k][v],b)) v = up[k][v]
        return up[0][v]
    }
    fun distance(a: Int,b: Int): Int {
        val c = lca(a,b)
        return (depth[a]-depth[c])+(depth[b]-depth[c])
    }
    /** O(k log k + k log n) for k terminals; duplicates ignored; <= 2k-1 nodes.
     * Empty input returns an empty tree. Root is the LCA of all terminals.
     */
    fun build(terminals: IntArray): VirtualTree {
        terminals.forEach { checkVertex(it) }
        val selected = terminals.distinct().sortedBy { tin[it] }
        if (selected.isEmpty()) return VirtualTree(intArrayOf(),intArrayOf(),intArrayOf())
        val nodes = ArrayList<Int>(selected)
        for (i in 1 until selected.size) nodes.add(lca(selected[i-1],selected[i]))
        val vertices = nodes.distinct().sortedBy { tin[it] }.toIntArray()
        val parents = IntArray(vertices.size) { -1 }
        val lengths = IntArray(vertices.size)
        val stack = IntArray(vertices.size)
        var top = 0
        for (i in vertices.indices) {
            while (top > 0 && !ancestor(vertices[stack[top-1]],vertices[i])) top--
            if (top > 0) {
                parents[i] = stack[top-1]
                lengths[i] = depth[vertices[i]]-depth[vertices[parents[i]]]
            }
            stack[top++] = i
        }
        return VirtualTree(vertices,parents,lengths)
    }
}
