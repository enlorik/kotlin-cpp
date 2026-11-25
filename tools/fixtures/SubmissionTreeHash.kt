fun main() {
    val s = "banana".rollingHash()
    check(s.equalVerified(1,4,s,3,6))
    val g = SparseGraph(3).apply { addEdge(0,1); addEdge(1,2) }
    val tree = g.rootedTree()
    check(tree.diameter().length == 2)
    check(tree.centroidParents().contentEquals(intArrayOf(1,-1,1)))
    check(tree.connectedKnapsack(longArrayOf(2,3,4),2) == listOf(0L,2L,5L))
    check(g.virtualTreeIndex().lca(1,2) == 1)
    check(g.rerootingTree(monoid(0){a,b->a+b},{_,x->x+1},{x,_,_->x}) == listOf(3,3,3))
}
