// Run: kotlinc graph/*.kt tests/DfsFlexTests.kt -include-runtime -d dfsflex-tests.jar
//      java -jar dfsflex-tests.jar
private fun dfCheck(ok: Boolean, msg: String) { if (!ok) error(msg) }
private inline fun dfThrows(block: () -> Unit) {
    var thrown = false
    try { block() } catch (_: IllegalArgumentException) { thrown = true }
    dfCheck(thrown, "Expected IllegalArgumentException")
}

fun main() {
    val g = SparseGraph(7, directed = true)
    g.addEdge(0, 1, 2)
    g.addEdge(0, 2, 10)
    g.addEdge(1, 3, 3)
    g.addEdge(3, 4, 1)
    g.addEdge(4, 1, 1) // cycle
    g.addEdge(2, 5, 1)
    val entered = mutableListOf<Int>()
    val exited = mutableListOf<Int>()
    val accum = LongArray(7)
    val result = g.dfsFlex(
        src = 0,
        canGo = { _, edge -> edge.w < 10 },
        onEnter = { entered.add(it) },
        onExit = { exited.add(it) },
        onStep = { u, v, e -> accum[v] = accum[u] + e.w }
    )
    dfCheck(entered == listOf(0, 1, 3, 4), "pre-order")
    dfCheck(exited == listOf(4, 3, 1, 0), "post-order")
    dfCheck(result.enterOrder.contentEquals(entered.toIntArray()), "enter array")
    dfCheck(result.exitOrder.contentEquals(exited.toIntArray()), "exit array")
    dfCheck(result.order.contentEquals(result.enterOrder), "order alias")
    dfCheck(result.depth[4] == 3, "DFS depth")
    dfCheck(result.depth[2] == -1 && !result.hasPath(5) && !result.hasPath(6), "filter / unreachable")
    dfCheck(result.reconstructPath(4) == listOf(0, 1, 3, 4), "path")
    dfCheck(result.reconstructPath(2).isEmpty(), "unreachable path")
    dfCheck(accum[4] == 6L, "weighted side calculation must not affect depth")
    dfCheck(result.prev[4] == 3, "parent")

    val all = g.dfsFlex(0)
    dfCheck(all.hasPath(5) && all.depth[5] == 2, "nonfiltered graph")
    dfCheck(all.enterOrder.size == 6 && all.exitOrder.size == 6, "all reachable visited")

    val enteredEarly = mutableListOf<Int>()
    val exitedEarly = mutableListOf<Int>()
    val early = g.dfsFlex(0, target = 3,
        onEnter = { enteredEarly.add(it) }, onExit = { exitedEarly.add(it) })
    dfCheck(enteredEarly == listOf(0, 1, 3), "target entered")
    dfCheck(exitedEarly.isEmpty() && early.exitOrder.isEmpty(), "no exits for active target path")
    dfCheck(!early.hasPath(2) && early.reconstructPath(3) == listOf(0, 1, 3), "partial output")
    val rootTarget = g.dfsFlex(0, target = 0)
    dfCheck(rootTarget.enterOrder.contentEquals(intArrayOf(0)) && rootTarget.exitOrder.isEmpty(), "target root")
    dfThrows { g.dfsFlex(-1) }
    dfThrows { g.dfsFlex(0, target = 100) }

    val single = SparseGraph(1).dfsFlex(0)
    dfCheck(single.enterOrder.contentEquals(intArrayOf(0)) && single.exitOrder.contentEquals(intArrayOf(0)), "single node")

    // Reference reachability across 150 deterministic directed graphs.
    for (n in 1..25) for (pattern in 0..5) {
        val graph = SparseGraph(n, directed = true)
        for (u in 0 until n) for (v in 0 until n) {
            if ((u * 29 + v * 31 + pattern * 7) % 9 == 0) graph.addEdge(u, v)
        }
        val dfs = graph.dfsFlex(0)
        val reachable = BooleanArray(n)
        val queue = ArrayDeque<Int>()
        queue.add(0); reachable[0] = true
        while (queue.isNotEmpty()) {
            val u = queue.removeFirst()
            for (edge in graph.g[u]) if (!reachable[edge.to]) {
                reachable[edge.to] = true
                queue.add(edge.to)
            }
        }
        for (v in 0 until n) dfCheck(dfs.hasPath(v) == reachable[v], "reachability mismatch, n=$n p=$pattern v=$v")
    }

    val deepGraph = SparseGraph(100_000, directed = true)
    for (i in 0 until 99_999) deepGraph.addEdge(i, i + 1)
    val deep = deepGraph.dfsFlex(0)
    dfCheck(deep.depth[99_999] == 99_999 && deep.exitOrder.size == 100_000, "deep chain")

    println("PASS: graph DFS hooks, filtering, order, paths, target, cycles, 150 references, 100k chain")
}
