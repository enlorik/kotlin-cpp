fun main() {
    val graph = SparseGraph(5)
    graph.addEdge(0, 1)
    graph.addEdge(1, 2)
    graph.addEdge(1, 3)
    graph.addEdge(3, 4)
    val sums = graph.heavyLightSums(longArrayOf(2, 3, 5, 7, 11))
    check(sums.pathSum(2, 4) == 5L + 3L + 7L + 11L)
    check(sums.subtreeSum(3) == 18L)
    check(sums.lca(2, 4) == 1)

    val concat = monoid("") { a: String, b: String -> a + b }
    val words = graph.heavyLight(listOf("a", "b", "c", "d", "e"), concat)
    check(words.pathQuery(4, 2) == "edbc")

    val directed = SparseGraph(2, directed = true)
    directed.addEdge(0, 1)
    check(runCatching {
        directed.heavyLightSums(longArrayOf(1, 2))
    }.exceptionOrNull() is IllegalArgumentException)

    val disconnected = SparseGraph(3)
    disconnected.addEdge(0, 1)
    check(runCatching {
        disconnected.heavyLightSums(longArrayOf(1, 2, 3))
    }.exceptionOrNull() is IllegalArgumentException)

    println("PASS: SparseGraph heavy-light monoid bridge")
}
