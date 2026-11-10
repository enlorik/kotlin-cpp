fun main() {
    val matrix = intArrayOf(-4, 7, 2, -4, 9).waveletMatrix()
    check(matrix.kthSmallest(0, 5, 2) == 2)
    check(matrix.rank(-4, 0, 5) == 2)
    check(matrix.countLess(0, 5, 3) == 3)

    val lines = (-10L..10L).liChao()
    lines.addLine(2, 1)
    lines.addLine(-1, 7)
    check(lines.query(0) == 1L)
    lines.addSegment(-10, -1, 0, -5)
    check(lines.query(-3) == -5L)

    val edges = listOf(0 to 1, 1 to 2)
    val sums = longArrayOf(1, 2, 3).heavyLightSums(edges)
    check(sums.pathSum(0, 2) == 6L)
    sums.set(1, 10)
    check(sums.pathSum(2, 0) == 14L)

    val strings = listOf("a", "b", "c").heavyLightTree(
        edges, monoid("") { a: String, b: String -> a + b }
    )
    check(strings.pathQuery(2, 0) == "cba")
    println("standalone advanced algorithms OK")
}
