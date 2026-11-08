fun main() {
    val values = intArrayOf(1, 2, 3, 4)
    val tree = values.sumSegmentTree()
    check(tree.query(0, 4) == 10L)
    tree[2] = 100L
    check(tree[1..3] == 106L)
    check(tree.findPrefix { it >= 3L } == 1)
    println("submission primitive sums OK")
}
