fun main() {
    val graph = SparseGraph(3)
    graph.addEdge(0, 1)
    graph.addEdge(1, 2)
    check(graph.bfs(0).dist[2] == 2L)
    check(FOUR.dr.size == 4)
    println("submission graph OK")
}
