// Simple assert-style tests for the graph library.
// Run with: kotlinc graph/*.kt tests/GraphTests.kt -include-runtime -d out.jar && java -jar out.jar

fun check(cond: Boolean, msg: String = "assertion failed") {
    if (!cond) throw AssertionError(msg)
}

// ── SparseGraph tests ─────────────────────────────────────────────────────────

fun testBfs() {
    val g = SparseGraph(6, directed = true)
    g.addEdge(0, 1); g.addEdge(0, 2); g.addEdge(1, 3); g.addEdge(2, 4); g.addEdge(4, 5)
    val r = g.bfs(0)
    check(r.dist[0] == 0L)
    check(r.dist[3] == 2L)
    check(r.dist[5] == 3L)
    check(r.reconstructPath(5) == listOf(0, 2, 4, 5))
}

fun testMultiBfs() {
    val g = SparseGraph(5, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(3, 4)
    val r = g.multiBfs(listOf(0, 3))
    check(r.dist[2] == 2L)
    check(r.dist[4] == 1L)
}

fun testDijkstra() {
    val g = SparseGraph(5, directed = true)
    g.addEdge(0, 1, 10L); g.addEdge(0, 2, 3L); g.addEdge(2, 1, 4L); g.addEdge(1, 3, 2L)
    g.addEdge(2, 3, 8L); g.addEdge(3, 4, 5L)
    val r = g.dijkstra(0)
    check(r.dist[1] == 7L)
    check(r.dist[4] == 14L)
}

fun testBellmanFord() {
    val g = SparseGraph(4, directed = true)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, -3L); g.addEdge(2, 3, 2L); g.addEdge(0, 3, 10L)
    val (r, neg) = g.bellmanFord(0)
    check(!neg)
    check(r.dist[3] == 0L)
}

fun testBellmanFordNegCycle() {
    val g = SparseGraph(3, directed = true)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, -4L); g.addEdge(2, 0, 1L)
    val (_, neg) = g.bellmanFord(0)
    check(neg)
}

fun testSpfa() {
    val g = SparseGraph(4, directed = true)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, -3L); g.addEdge(2, 3, 2L)
    val (r, neg) = g.spfa(0)
    check(!neg)
    check(r.dist[3] == 0L)
}

fun testTopoSort() {
    val g = SparseGraph(6, directed = true)
    g.addEdge(5, 2); g.addEdge(5, 0); g.addEdge(4, 0); g.addEdge(4, 1); g.addEdge(2, 3); g.addEdge(3, 1)
    val order = g.topoSort()!!
    val pos = IntArray(6)
    for ((i, v) in order.withIndex()) pos[v] = i
    check(pos[5] < pos[2] && pos[5] < pos[0])
    check(pos[4] < pos[0] && pos[4] < pos[1])
    check(pos[2] < pos[3] && pos[3] < pos[1])
}

fun testTopoSortCycle() {
    val g = SparseGraph(3, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0)
    check(g.topoSort() == null)
}

fun testScc() {
    val g = SparseGraph(8, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0)
    g.addEdge(1, 3); g.addEdge(3, 4); g.addEdge(4, 3)
    g.addEdge(5, 6); g.addEdge(6, 7); g.addEdge(7, 5)
    val r = g.scc()
    check(r.comp[0] == r.comp[1] && r.comp[1] == r.comp[2])
    check(r.comp[3] == r.comp[4])
    check(r.comp[5] == r.comp[6] && r.comp[6] == r.comp[7])
    check(r.count == 3)   // {0,1,2}, {3,4}, {5,6,7}
}

fun testTarjanSCC() {
    val g = SparseGraph(5, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0)
    g.addEdge(2, 3); g.addEdge(3, 4)
    val r = g.tarjanSCC()
    check(r.comp[0] == r.comp[1] && r.comp[1] == r.comp[2])
    check(r.comp[3] != r.comp[0])
    check(r.comp[4] != r.comp[3])
    check(r.count == 3)
}

fun testBridgesAndArticulationPoints() {
    // Graph: 0-1-2-3 with extra edge 1-3; bridge is 0-1
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(1, 3)
    val r = g.bridgesAndArticulationPoints()
    check(r.bridges.contains(0 to 1) || r.bridges.contains(1 to 0))
    check(1 in r.articulationPoints)
    check(0 !in r.articulationPoints)
}

fun testPrimMST() {
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, 2L); g.addEdge(2, 3, 3L); g.addEdge(0, 3, 6L); g.addEdge(1, 3, 5L)
    val mst = g.primMST()!!
    check(mst.totalWeight == 6L)
    check(mst.edges.size == 3)
}

fun testKruskalMST() {
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, 2L); g.addEdge(2, 3, 3L); g.addEdge(0, 3, 6L); g.addEdge(1, 3, 5L)
    val mst = g.kruskalMST()!!
    check(mst.totalWeight == 6L)
    check(mst.edges.size == 3)
}

fun testConnectedComponents() {
    val g = SparseGraph(6, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(3, 4)
    val (cnt, comp) = g.connectedComponents()
    check(cnt == 3)
    check(comp[0] == comp[1] && comp[1] == comp[2])
    check(comp[3] == comp[4])
    check(comp[5] != comp[0] && comp[5] != comp[3])
}

fun testConnectedComponentsRejectionDirected() {
    val g = SparseGraph(3, directed = true)
    g.addEdge(0, 1)
    var threw = false
    try { g.connectedComponents() } catch (e: IllegalStateException) { threw = true }
    check(threw, "connectedComponents() should throw on directed graph")
}

fun testWeakConnectedComponents() {
    val g = SparseGraph(4, directed = true)
    g.addEdge(0, 1); g.addEdge(2, 3)
    val (cnt, comp) = g.weakConnectedComponents()
    check(cnt == 2)
    check(comp[0] == comp[1])
    check(comp[2] == comp[3])
    check(comp[0] != comp[2])
}

fun testIsBipartite() {
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 0)
    val (bip, _) = g.isBipartite()
    check(bip)

    val g2 = SparseGraph(3, directed = false)
    g2.addEdge(0, 1); g2.addEdge(1, 2); g2.addEdge(2, 0)
    val (bip2, _) = g2.isBipartite()
    check(!bip2)
}

fun testTreeDiameter() {
    // Path graph 0-1-2-3-4
    val g = SparseGraph(5, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3); g.addEdge(3, 4)
    val (d, _, _) = g.treeDiameter()
    check(d == 4L)
}

fun testWeightedTreeDiameter() {
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, 5L); g.addEdge(2, 3, 2L)
    val (d, _, _) = g.weightedTreeDiameter()
    check(d == 8L)
}

fun testZeroOneBfs() {
    val g = SparseGraph(5, directed = true)
    g.addEdge(0, 1, 0L); g.addEdge(1, 2, 1L); g.addEdge(0, 2, 1L)
    g.addEdge(2, 3, 0L); g.addEdge(3, 4, 1L)
    val r = g.zeroOneBfs(0)
    check(r.dist[2] == 1L)
    check(r.dist[4] == 2L)
}

fun testNegativeCycle() {
    val g = SparseGraph(3, directed = true)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, -4L); g.addEdge(2, 0, 1L)
    val cycle = g.negativeCycle()
    check(cycle != null)
    check(cycle!!.isNotEmpty())

    val g2 = SparseGraph(3, directed = true)
    g2.addEdge(0, 1, 1L); g2.addEdge(1, 2, 2L)
    check(g2.negativeCycle() == null)
}

fun testSecondBestMST() {
    // 4-node graph; MST weight = 6, second-best = 7
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1, 1L); g.addEdge(0, 2, 2L); g.addEdge(0, 3, 3L)
    g.addEdge(1, 2, 4L); g.addEdge(2, 3, 5L)
    val second = g.secondBestMST()
    check(second != null)
    val mstW = g.kruskalMST()!!.totalWeight
    check(second!! > mstW)
}

// ── DenseGraph tests ──────────────────────────────────────────────────────────

fun testFloydWarshall() {
    val g = DenseGraph(4, directed = true)
    g.addEdge(0, 1, 3L); g.addEdge(1, 2, 2L); g.addEdge(0, 2, 8L); g.addEdge(2, 3, 1L)
    val d = g.floydWarshall()
    check(d[0][2] == 5L)
    check(d[0][3] == 6L)
    check(!g.hasNegativeCycle(d))
}

fun testHasNegativeCycle() {
    val g = DenseGraph(3, directed = true)
    g.addEdge(0, 1, 1L); g.addEdge(1, 2, -4L); g.addEdge(2, 0, 1L)
    val d = g.floydWarshall()
    check(g.hasNegativeCycle(d))
}

fun testTransitiveClosure() {
    val g = DenseGraph(4, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(3, 0)
    val tc = g.transitiveClosure()
    check(tc[0][2])    // 0 → 1 → 2
    check(tc[3][2])    // 3 → 0 → 1 → 2
    check(!tc[0][3])   // 0 cannot reach 3
}

// ── FlowGraph tests ───────────────────────────────────────────────────────────

fun testMaxFlow() {
    // Standard max-flow example
    val g = FlowGraph(6)
    g.addEdge(0, 1, 16L); g.addEdge(0, 2, 13L)
    g.addEdge(1, 2, 10L); g.addEdge(1, 3, 12L)
    g.addEdge(2, 1,  4L); g.addEdge(2, 4, 14L)
    g.addEdge(3, 2,  9L); g.addEdge(3, 5, 20L)
    g.addEdge(4, 3,  7L); g.addEdge(4, 5,  4L)
    val flow = g.maxFlow(0, 5)
    check(flow == 23L)
}

fun testMinCutSide() {
    val g = FlowGraph(4)
    g.addEdge(0, 1, 3L); g.addEdge(0, 2, 2L); g.addEdge(1, 3, 2L); g.addEdge(2, 3, 3L)
    g.maxFlow(0, 3)
    val side = g.minCutSide(0)
    check(side[0])
    check(!side[3])
}

// ── MCFlowGraph tests ─────────────────────────────────────────────────────────

fun testMinCostFlow() {
    val g = MCFlowGraph(4)
    g.addEdge(0, 1, 2L, 1L); g.addEdge(0, 2, 2L, 2L)
    g.addEdge(1, 3, 2L, 1L); g.addEdge(2, 3, 2L, 1L)
    val (flow, cost) = g.minCostFlow(0, 3, 3L)
    // 2 units via 0→1→3 at cost 2 each = 4; 1 unit via 0→2→3 at cost 3 = 3; total = 7
    check(flow == 3L)
    check(cost == 7L)
}

// ── BipartiteGraph tests ──────────────────────────────────────────────────────

fun testMaxMatching() {
    val g = BipartiteGraph(3, 3)
    g.addEdge(0, 0); g.addEdge(0, 1); g.addEdge(1, 1); g.addEdge(2, 2)
    val matchL = g.maxMatching()
    check(g.matchingSize(matchL) == 3)
}

fun testMinVertexCover() {
    val g = BipartiteGraph(3, 3)
    g.addEdge(0, 0); g.addEdge(0, 1); g.addEdge(1, 1); g.addEdge(2, 2)
    val matchL = g.maxMatching()
    val (lc, rc) = g.minVertexCover(matchL)
    check(lc.size + rc.size == g.matchingSize(matchL))
}

// ── Hungarian algorithm tests ─────────────────────────────────────────────────

fun testHungarianMinCost() {
    val cost = arrayOf(
        longArrayOf(4L, 1L, 3L),
        longArrayOf(2L, 0L, 5L),
        longArrayOf(3L, 2L, 2L)
    )
    val (minCost, assignment) = hungarianMinCost(cost)
    // Optimal: row0→col1 (1) + row1→col0 (2) + row2→col2 (2) = 5
    check(minCost == 5L)
    var total = 0L
    for (i in assignment.indices) total += cost[i][assignment[i]]
    check(total == minCost)
}

// ── TwoSAT tests ──────────────────────────────────────────────────────────────

fun testTwoSATSatisfiable() {
    // (x0 OR x1) AND (NOT x0 OR x2) AND (NOT x2 OR NOT x1)
    val sat = TwoSAT(3)
    sat.addOr(0, true,  1, true)
    sat.addOr(0, false, 2, true)
    sat.addOr(2, false, 1, false)
    val result = sat.solve()
    check(result != null)
    val a = result!!
    check(a[0] || a[1])
    check(!a[0] || a[2])
    check(!a[2] || !a[1])
}

fun testTwoSATUnsatisfiable() {
    // x0 AND NOT x0
    val sat = TwoSAT(1)
    sat.force(0, true)
    sat.force(0, false)
    check(sat.solve() == null)
}

fun testTwoSATForce() {
    val sat = TwoSAT(2)
    sat.force(0, true)
    sat.force(1, false)
    val result = sat.solve()
    check(result != null)
    check(result!![0] == true)
    check(result[1] == false)
}

fun testTwoSATXor() {
    // x0 XOR x1: exactly one is true
    val sat = TwoSAT(2)
    sat.addXor(0, true, 1, true)
    val result = sat.solve()
    check(result != null)
    check(result!![0] != result[1])
}

// ── Euler path / circuit tests ────────────────────────────────────────────────

fun testEulerCircuitDirected() {
    // Directed: 0→1→2→0
    val g = SparseGraph(3, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0)
    val circuit = g.eulerCircuit(0)
    check(circuit != null)
    check(circuit!!.first() == 0 && circuit.last() == 0)
    check(circuit.size == 4)
}

fun testEulerCircuitUndirected() {
    val g = SparseGraph(3, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 0)
    val circuit = g.eulerCircuit(0)
    check(circuit != null)
    check(circuit!!.first() == circuit.last())
    check(circuit.size == 4)
}

fun testEulerCircuitInvalidOddDegree() {
    val g = SparseGraph(3, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2)   // 0 and 2 have odd degree
    check(g.eulerCircuit(0) == null)
}

fun testEulerPathDirected() {
    // 0→1→2→3, path from 0 to 3
    val g = SparseGraph(4, directed = true)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3)
    val path = g.eulerPath()
    check(path != null)
    check(path!!.size == 4)
    check(path.first() == 0 && path.last() == 3)
}

fun testEulerPathUndirected() {
    val g = SparseGraph(4, directed = false)
    g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3)
    val path = g.eulerPath()
    check(path != null)
    check(path!!.size == 4)
}

fun testEulerPathValidAndInvalid() {
    // Graph with 4 odd-degree vertices (degrees: 0=2, 1=3, 2=3, 3=2, 4=2 for a different topology)
    // First: a graph with exactly 2 odd-degree vertices → valid Euler path
    val g2 = SparseGraph(5, directed = false)
    g2.addEdge(0, 1); g2.addEdge(0, 2); g2.addEdge(1, 3); g2.addEdge(2, 3); g2.addEdge(3, 4)
    // degrees: 0=2, 1=2, 2=2, 3=3, 4=1 → odd: {3, 4} → Euler path exists
    val path2 = g2.eulerPath()
    check(path2 != null)
    check(path2!!.size == 6)

    // Graph with 4 odd-degree vertices → no Euler path or circuit
    val g3 = SparseGraph(4, directed = false)
    g3.addEdge(0, 1); g3.addEdge(0, 2); g3.addEdge(1, 2); g3.addEdge(2, 3)
    // degrees: 0=2, 1=2, 2=3, 3=1 → odd: {2, 3} → path exists, so use a case with 4 odd vertices
    val g4 = SparseGraph(4, directed = false)
    g4.addEdge(0, 1); g4.addEdge(0, 2); g4.addEdge(1, 3); g4.addEdge(2, 3); g4.addEdge(0, 3); g4.addEdge(1, 2)
    // degrees: all degree 3 → 4 odd-degree vertices → neither path nor circuit
    check(g4.eulerPath() == null)
}

// ── LCA tests ─────────────────────────────────────────────────────────────────

fun testLCA() {
    // Tree: 0-1, 0-2, 1-3, 1-4
    val g = SparseGraph(5, directed = false)
    g.addEdge(0, 1); g.addEdge(0, 2); g.addEdge(1, 3); g.addEdge(1, 4)
    g.buildLCA(0)
    check(g.lca(3, 4) == 1)
    check(g.lca(3, 2) == 0)
    check(g.lca(0, 4) == 0)
}

// ── Main ──────────────────────────────────────────────────────────────────────

fun main() {
    val tests = listOf(
        "BFS"                       to ::testBfs,
        "multiBFS"                  to ::testMultiBfs,
        "Dijkstra"                  to ::testDijkstra,
        "Bellman-Ford"              to ::testBellmanFord,
        "Bellman-Ford neg cycle"    to ::testBellmanFordNegCycle,
        "SPFA"                      to ::testSpfa,
        "topoSort"                  to ::testTopoSort,
        "topoSort cycle"            to ::testTopoSortCycle,
        "SCC (Kosaraju)"            to ::testScc,
        "SCC (Tarjan)"              to ::testTarjanSCC,
        "bridges+APs"               to ::testBridgesAndArticulationPoints,
        "Prim MST"                  to ::testPrimMST,
        "Kruskal MST"               to ::testKruskalMST,
        "connectedComponents"       to ::testConnectedComponents,
        "connectedComponents reject directed" to ::testConnectedComponentsRejectionDirected,
        "weakConnectedComponents"   to ::testWeakConnectedComponents,
        "isBipartite"               to ::testIsBipartite,
        "treeDiameter"              to ::testTreeDiameter,
        "weightedTreeDiameter"      to ::testWeightedTreeDiameter,
        "0-1 BFS"                   to ::testZeroOneBfs,
        "negativeCycle"             to ::testNegativeCycle,
        "secondBestMST"             to ::testSecondBestMST,
        "Floyd-Warshall"            to ::testFloydWarshall,
        "hasNegativeCycle (dense)"  to ::testHasNegativeCycle,
        "transitiveClosure"         to ::testTransitiveClosure,
        "maxFlow (Dinic)"           to ::testMaxFlow,
        "minCutSide"                to ::testMinCutSide,
        "minCostFlow"               to ::testMinCostFlow,
        "maxMatching"               to ::testMaxMatching,
        "minVertexCover"            to ::testMinVertexCover,
        "hungarianMinCost"          to ::testHungarianMinCost,
        "2-SAT satisfiable"         to ::testTwoSATSatisfiable,
        "2-SAT unsatisfiable"       to ::testTwoSATUnsatisfiable,
        "2-SAT force"               to ::testTwoSATForce,
        "2-SAT XOR"                 to ::testTwoSATXor,
        "Euler circuit (directed)"  to ::testEulerCircuitDirected,
        "Euler circuit (undirected)" to ::testEulerCircuitUndirected,
        "Euler circuit invalid"     to ::testEulerCircuitInvalidOddDegree,
        "Euler path (directed)"     to ::testEulerPathDirected,
        "Euler path (undirected)"   to ::testEulerPathUndirected,
        "Euler path valid+invalid"  to ::testEulerPathValidAndInvalid,
        "LCA"                       to ::testLCA,
    )

    var passed = 0; var failed = 0
    for ((name, fn) in tests) {
        try {
            fn()
            println("  PASS  $name")
            passed++
        } catch (e: AssertionError) {
            println("  FAIL  $name: ${e.message}")
            failed++
        } catch (e: Exception) {
            println("  ERROR $name: $e")
            failed++
        }
    }
    println("\n$passed passed, $failed failed out of ${tests.size} tests")
    if (failed > 0) throw RuntimeException("$failed test(s) failed")
}
