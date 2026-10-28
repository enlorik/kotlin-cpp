// Run: kotlinc graph/*.kt tests/DijkstraFlexTests.kt -include-runtime -d graph-dijkstra-tests.jar
//      java -jar graph-dijkstra-tests.jar
private fun djAssert(ok: Boolean, message: String) { if (!ok) error(message) }
private inline fun djExpectFailure(block: () -> Unit) {
    var failed = false
    try { block() } catch (_: IllegalArgumentException) { failed = true }
    djAssert(failed, "Expected IllegalArgumentException")
}

fun main() {
    val g = SparseGraph(6, directed = true)
    g.addEdge(0, 1, 10); g.addEdge(0, 2, 2); g.addEdge(2, 1, 1)
    g.addEdge(1, 3, 1); g.addEdge(2, 3, 12); g.addEdge(3, 4, 0)
    val relaxes = mutableListOf<Triple<Int,Int,Long>>()
    val finalized = mutableListOf<Pair<Int,Long>>()
    val result = g.dijkstraFlex(0,
        onRelax = { u, v, _, d -> relaxes.add(Triple(u,v,d)) },
        onFinalize = { u, d -> finalized.add(u to d) })
    djAssert(result.dist.contentEquals(longArrayOf(0,3,2,4,4,GRAPH_INF)), "weighted shortest distances")
    djAssert(result.prev[1] == 2 && result.reconstructPath(4) == listOf(0,2,1,3,4), "parent and path")
    djAssert(!result.hasPath(5) && result.distance(5) == null && result.reconstructPath(5).isEmpty(), "unreachable")
    djAssert(relaxes.count { it.second == 1 } == 2, "onRelax fires on repeated improvement")
    djAssert(finalized.size == 5 && finalized.map { it.second } == finalized.map { it.second }.sorted(), "onFinalize sorted")
    djAssert(result.order.toList() == finalized.map { it.first }, "finalization order")
    djAssert(result.isFinalized(4) && !result.isFinalized(5), "settled flags")

    val filtered = g.dijkstraFlex(0, canGo = { _, e -> e.w < 10L })
    djAssert(filtered.dist[4] == 4L && filtered.dist[1] == 3L, "edge filter")
    val forbidden = g.dijkstraFlex(0, canGo = { _, e -> e.to != 2 })
    djAssert(forbidden.dist[4] == 11L && forbidden.dist[2] == GRAPH_INF, "forbidden destination")
    val doubled = g.dijkstraFlex(0, cost = { _, e -> e.w * 2 })
    djAssert(doubled.dist[4] == 8L && doubled.dist[1] == 6L, "custom weight callback")
    val early = g.dijkstraFlex(0, target = 2)
    djAssert(early.dist[2] == 2L && early.isFinalized(2) && early.order.last() == 2, "target finalized")
    djAssert(early.hasPath(1) && !early.isFinalized(1), "other nodes may have tentative distances")
    djAssert(early.reconstructPath(2) == listOf(0, 2), "early path")
    val atRoot = g.dijkstraFlex(0, target = 0)
    djAssert(atRoot.order.contentEquals(intArrayOf(0)) && atRoot.dist[1] == GRAPH_INF, "target root")
    djAssert(SparseGraph(1).dijkstraFlex(0).order.contentEquals(intArrayOf(0)), "isolated root")
    djExpectFailure { g.dijkstraFlex(-1) }
    djExpectFailure { g.dijkstraFlex(0, target = 6) }
    djExpectFailure { SparseGraph(2, directed = true).also { it.addEdge(0,1,-2) }.dijkstraFlex(0) }
    djExpectFailure { g.dijkstraFlex(0, cost = { _, _ -> -1L }) }
    djExpectFailure { g.dijkstraFlex(0, cost = { _, _ -> GRAPH_INF }) }
    val huge = SparseGraph(3, directed = true)
    huge.addEdge(0,1,GRAPH_INF-2); huge.addEdge(1,2,3)
    djAssert(!huge.dijkstraFlex(0).hasPath(2), "overflow protection")

    // 150 directed graphs checked against independent Bellman-Ford-style relaxation.
    for (n in 1..25) for (seed in 0..5) {
        val graph = SparseGraph(n, directed = true)
        for (u in 0 until n) for (v in 0 until n)
            if ((u*17 + v*29 + seed*11) % 7 == 0)
                graph.addEdge(u, v, ((u*31 + v*13 + seed*7) % 12).toLong())
        val actual = graph.dijkstraFlex(0)
        val reference = LongArray(n) { GRAPH_INF }
        reference[0] = 0
        repeat(n) {
            for (u in 0 until n) for (edge in graph.g[u])
                if (reference[u] != GRAPH_INF)
                    reference[edge.to] = minOf(reference[edge.to], reference[u] + edge.w)
        }
        for (v in 0 until n)
            djAssert(actual.dist[v] == reference[v], "oracle n=$n seed=$seed v=$v")
    }

    val chain = SparseGraph(100_000, directed = true)
    for (i in 0 until 99_999) chain.addEdge(i, i+1, 1)
    djAssert(chain.dijkstraFlex(0).dist[99_999] == 99_999L, "100k-node path")
    println("PASS: graph Dijkstra hooks, weighted paths, filtering, validation, 150 references and 100k chain")
}
