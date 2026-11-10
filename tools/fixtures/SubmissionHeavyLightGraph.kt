fun main() {
    val graph = SparseGraph(3)
    graph.addEdge(0, 1)
    graph.addEdge(1, 2)
    check(graph.heavyLightSums(longArrayOf(1, 2, 3)).pathSum(0, 2) == 6L)
    println("standalone graph heavy-light OK")
}
