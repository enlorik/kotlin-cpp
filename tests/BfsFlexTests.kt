// Standalone tests for the optional hook-based BFS API.
// Run: kotlinc graph/*.kt tests/BfsFlexTests.kt -include-runtime -d bfsflex-tests.jar
//      java -jar bfsflex-tests.jar

private fun assertFlex(ok: Boolean, msg: String) {
    if (!ok) throw AssertionError(msg)
}

private fun sampleGraph(): SparseGraph {
    val g = SparseGraph(6, directed = true)
    g.addEdge(0, 1, 2)
    g.addEdge(0, 2, 10)
    g.addEdge(1, 3, 3)
    g.addEdge(2, 4, 1)
    g.addEdge(3, 5, 1)
    return g
}

fun main() {
    val g = sampleGraph()

    // Defaults must exactly match the existing BFS in distances and parent pointers.
    val simple = g.bfs(0)
    val flex = g.bfsFlex(0)
    assertFlex(flex.dist.contentEquals(simple.dist), "distances differ from bfs")
    assertFlex(flex.prev.contentEquals(simple.prev), "parents differ from bfs")
    assertFlex(flex.order.contentEquals(intArrayOf(0, 1, 2, 3, 4, 5)), "unexpected pop order")
    assertFlex(flex.reconstructPath(5) == listOf(0, 1, 3, 5), "path reconstruction failed")

    val extra = LongArray(6)
    val children = Array(6) { mutableListOf<Int>() }
    val popped = mutableListOf<Int>()

    // Custom filtering and side calculations without rewriting the traversal.
    val filtered = g.bfsFlex(
        src = 0,
        canGo = { _, edge -> edge.w < 10L },
        onDiscover = { u, v, edge ->
            extra[v] = extra[u] + edge.w
            children[u].add(v)
        },
        onPop = { popped.add(it) }
    )
    assertFlex(!filtered.hasPath(2) && !filtered.hasPath(4), "blocked edges should be unreachable")
    assertFlex(filtered.dist[5] == 3L, "BFS must still count edges, not weights")
    assertFlex(extra[5] == 6L, "onDiscover did not fill side array")
    assertFlex(children[0] == listOf(1) && children[1] == listOf(3), "children hook incorrect")
    assertFlex(popped == listOf(0, 1, 3, 5), "onPop order incorrect")

    // Early stopping happens on POP. Nodes discovered but not popped may have finite distances.
    val early = g.bfsFlex(0, target = 1)
    assertFlex(early.order.contentEquals(intArrayOf(0, 1)), "target did not stop at pop")
    assertFlex(early.dist[2] == 1L, "already discovered nodes should retain distances")
    assertFlex(!early.hasPath(3), "nodes beyond target should remain undiscovered")

    val atSource = g.bfsFlex(0, target = 0)
    assertFlex(atSource.order.contentEquals(intArrayOf(0)), "target=source must stop immediately")
    assertFlex(atSource.reconstructPath(0) == listOf(0), "source-only path incorrect")

    val isolated = SparseGraph(2).bfsFlex(0)
    assertFlex(!isolated.hasPath(1), "unreachable vertex should not have a path")
    assertFlex(isolated.reconstructPath(1).isEmpty(), "unreachable path must be empty")

    println("PASS: bfsFlex defaults, hooks, filtering, order, paths and early stop")
}
