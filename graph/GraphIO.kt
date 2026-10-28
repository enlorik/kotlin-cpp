/** Read m edges using the shared FastInput scanner; labels are 1-based by default. */
fun FastInput.readGraph(
    n: Int,
    m: Int,
    directed: Boolean = false,
    oneIndexed: Boolean = true,
    weighted: Boolean = false
): SparseGraph {
    require(n >= 0 && m >= 0) { "Graph sizes must be nonnegative" }
    val graph = SparseGraph(n, directed)
    repeat(m) {
        val rawU = nextInt()
        val rawV = nextInt()
        val u = if (oneIndexed) rawU - 1 else rawU
        val v = if (oneIndexed) rawV - 1 else rawV
        require(u in 0 until n && v in 0 until n) { "Edge endpoint out of bounds" }
        val weight = if (weighted) nextLong() else 1L
        graph.addEdge(u, v, weight)
    }
    return graph
}

/** Read n, m graph edges from standard input (after the header). */
fun readGraph(
    n: Int,
    m: Int,
    directed: Boolean = false,
    oneIndexed: Boolean = true,
    weighted: Boolean = false
): SparseGraph {
    require(n >= 0 && m >= 0) { "Graph sizes must be nonnegative" }
    val graph = SparseGraph(n, directed)
    repeat(m) {
        val (rawU, rawV) = readInts()
        val u = if (oneIndexed) rawU - 1 else rawU
        val v = if (oneIndexed) rawV - 1 else rawV
        require(u in 0 until n && v in 0 until n) { "Edge endpoint out of bounds" }
        graph.addEdge(u, v, if (weighted) readLong() else 1L)
    }
    return graph
}
