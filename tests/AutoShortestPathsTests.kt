import kotlin.random.Random

private fun testAlgorithmSelection() {
    val unit = SparseGraph(3, directed = true)
    unit.addEdge(0, 1, 1)
    unit.addEdge(1, 2, 1)
    val a = unit.shortestPaths(0)
    check(a.algorithm == ShortestPathAlgorithm.BFS)
    check(a.distTo(2) == 2L)
    check(a.reconstructPath(2) == listOf(0, 1, 2))

    val binary = SparseGraph(3, directed = true)
    binary.addEdge(0, 1, 0)
    binary.addEdge(1, 2, 1)
    check(binary.shortestPaths(0).algorithm == ShortestPathAlgorithm.ZERO_ONE_BFS)
    check(binary.shortestPaths(0).distTo(2) == 1L)

    val positive = SparseGraph(3, directed = true)
    positive.addEdge(0, 1, 5)
    positive.addEdge(1, 2, 3)
    val c = positive.shortestPaths(0)
    check(c.algorithm == ShortestPathAlgorithm.DIJKSTRA)
    check(c.distTo(2) == 8L)

    val negative = SparseGraph(3, directed = true)
    negative.addEdge(0, 1, -2)
    negative.addEdge(1, 2, 5)
    val d = negative.shortestPaths(0)
    check(d.algorithm == ShortestPathAlgorithm.BELLMAN_FORD)
    check(d.distTo(2) == 3L)

    val empty = SparseGraph(2, directed = true)
    check(empty.shortestPaths(0).algorithm == ShortestPathAlgorithm.BFS)
    check(empty.shortestPaths(0).distTo(1) == null)

    // Selection must reflect new edges added after the previous query.
    unit.addEdge(0, 2, 0)
    check(unit.shortestPaths(0).algorithm == ShortestPathAlgorithm.ZERO_ONE_BFS)

    val cycle = SparseGraph(2, directed = true)
    cycle.addEdge(0, 1, -1)
    cycle.addEdge(1, 0, -1)
    val error = runCatching { cycle.shortestPaths(0) }.exceptionOrNull()
    check(error is IllegalStateException && error.message!!.contains("negative-weight cycle"))
    check(runCatching { unit.shortestPaths(3) }.exceptionOrNull() is IllegalArgumentException)
}

private fun floydOracle(graph: SparseGraph, source: Int): LongArray {
    val d = Array(graph.n) { LongArray(graph.n) { GRAPH_INF } }
    for (i in 0 until graph.n) d[i][i] = 0
    for (u in 0 until graph.n) for (edge in graph.g[u]) {
        if (edge.w < d[u][edge.to]) d[u][edge.to] = edge.w
    }
    for (k in 0 until graph.n) for (u in 0 until graph.n) {
        if (d[u][k] == GRAPH_INF) continue
        for (v in 0 until graph.n) {
            if (d[k][v] == GRAPH_INF) continue
            d[u][v] = minOf(d[u][v], d[u][k] + d[k][v])
        }
    }
    return d[source]
}

private fun testRandomizedAgainstFloydWarshall() {
    val random = Random(20261108)
    repeat(600) { trial ->
        val mode = trial % 4 // unit, zero-one, nonnegative, negative DAG
        val n = random.nextInt(1, 9)
        val graph = SparseGraph(n, directed = true)
        for (u in 0 until n) for (v in 0 until n) {
            if (u == v || (mode == 3 && u >= v)) continue
            if (random.nextInt(100) >= 32) continue
            val w = when (mode) {
                0 -> 1L
                1 -> random.nextInt(0, 2).toLong()
                2 -> random.nextInt(0, 12).toLong()
                else -> random.nextInt(-5, 8).toLong()
            }
            graph.addEdge(u, v, w)
        }
        val source = random.nextInt(n)
        val got = graph.shortestPaths(source)
        val expected = floydOracle(graph, source)
        for (v in 0 until n) {
            check(got.dist[v] == expected[v]) {
                "Mismatch trial=" + trial + " mode=" + mode + " vertex=" + v +
                    ": " + got.dist[v] + " != " + expected[v]
            }
        }
    }
}

fun main() {
    testAlgorithmSelection()
    testRandomizedAgainstFloydWarshall()
    println("PASS: auto shortest paths, 600 randomized Floyd-Warshall oracle graphs")
}
