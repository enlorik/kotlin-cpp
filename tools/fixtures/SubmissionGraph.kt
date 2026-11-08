fun main() {
    val graph = SparseGraph(3)
    graph.addEdge(0, 1)
    graph.addEdge(1, 2)
    val paths = graph.shortestPaths(0)
    check(paths.algorithm == ShortestPathAlgorithm.BFS)
    check(paths.dist[2] == 2L)
    check(FOUR.dr.size == 4)
    println("submission graph OK")
}
