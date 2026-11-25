/** Validate and root an undirected SparseGraph; edge weights are ignored.
 * Rejects cycles, self-loops, parallel edges, disconnected/asymmetric adjacency.
 */
fun SparseGraph.rootedTree(root: Int = 0): RootedTreeToolkit {
    require(!directed && root in 0 until n)
    val parent = IntArray(n) { -2 }
    parent[root] = -1
    val queue = IntArray(n)
    queue[0] = root
    var used = 1; var i = 0
    while (i < used) {
        val v = queue[i++]
        var reverseEdges = 0
        for (e in g[v]) {
            val u = e.to
            require(u in 0 until n && u != v)
            if (u == parent[v]) { reverseEdges++; continue }
            require(parent[u] == -2) { "Expected an undirected tree without parallel edges" }
            parent[u] = v
            queue[used++] = u
        }
        require(reverseEdges == if (v == root) 0 else 1) { "Asymmetric adjacency" }
    }
    require(used == n) { "Tree must be connected" }
    return RootedTreeToolkit(parent)
}
fun SparseGraph.virtualTreeIndex(root: Int = 0): VirtualTreeIndex =
    VirtualTreeIndex(rootedTree(root).parents())

/** Monoid rerooting on a validated tree. Neighbor order is induced by
 * edges emitted in ascending child vertex ID, not graph insertion order.
 * Edge weights are ignored by topology; caller may use them in lift.
 */
fun <T> SparseGraph.rerootingTree(
    monoid: Monoid<T>,
    finish: (Int,T) -> T,
    lift: (T,Int,Int) -> T,
    root: Int = 0
): List<T> {
    val p = rootedTree(root).parents()
    val edges = p.indices.filter { p[it] >= 0 }.map { p[it] to it }
    return rerootingDp(n,edges,monoid,finish,lift)
}
