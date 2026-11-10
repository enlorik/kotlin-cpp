/**
 * Bridge from the existing SparseGraph API to rooted tree aggregation.
 * Edge weights are deliberately ignored: HLD values live on vertices.
 * Use only a connected, undirected tree with n-1 edges.
 */
private fun SparseGraph.treeEdgePairs(): List<Pair<Int, Int>> {
    require(!directed) { "Heavy-Light requires an undirected tree" }
    val edges = ArrayList<Pair<Int, Int>>()
    for (u in 0 until n) {
        for (edge in g[u]) {
            require(edge.to != u) { "Tree cannot contain self loops" }
            if (u < edge.to) edges.add(u to edge.to)
        }
    }
    require(edges.size == n - 1) { "Expected n-1 edges for a tree" }
    return edges
}

fun <T> SparseGraph.heavyLight(
    values: List<T>,
    operation: Monoid<T>,
    root: Int = 0
): HeavyLightIndex<T> {
    require(values.size == n)
    return HeavyLightIndex(values, treeEdgePairs(), operation, root)
}

fun SparseGraph.heavyLightSums(
    values: LongArray,
    root: Int = 0
): HeavyLightLongSums {
    require(values.size == n)
    return HeavyLightLongSums(values, treeEdgePairs(), root)
}

fun SparseGraph.heavyLightSums(
    values: IntArray,
    root: Int = 0
): HeavyLightLongSums =
    heavyLightSums(LongArray(values.size) { values[it].toLong() }, root)
