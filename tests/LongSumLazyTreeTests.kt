import kotlin.random.Random

private fun expectOutOfBounds(body: () -> Unit) {
    check(runCatching(body).exceptionOrNull() is IllegalArgumentException)
}

fun main() {
    LongSumMonoid.checkLaws(listOf(-3L, -1L, 0L, 1L, 7L))
    LongAddAction.checkActionLaws(
        LongSumMonoid, listOf(-3L, 0L, 7L), listOf(-4L, 0L, 5L)
    )
    LongAssignAction.checkActionLaws(
        LongSumMonoid, listOf(-3L, 0L, 7L), listOf(-4L, 0L, 5L)
    )

    val empty = longArrayOf().longSumLazyTree()
    check(empty.all() == 0L && empty.query(0, 0) == 0L)
    empty.add(0, 0, 10L)
    empty.assign(0, 0, -2L)
    expectOutOfBounds { empty.add(0, 1, 7L) }
    expectOutOfBounds { empty.assign(-1, 0, 7L) }
    expectOutOfBounds { empty.query(1, 0) }

    val source = longArrayOf(1, 2, 3, 4, 5)
    val tree = source.longSumLazyTree()
    source[0] = 100
    check(tree.all() == 15L)
    tree.assign(0, 5, 0L)
    tree.add(1, 4, 5L)
    tree.assign(2, 3, -8L)
    tree.add(0, 3, 2L)
    check(tree.query(0, 5) == 8L)
    check(tree.query(2, 3) == -6L)

    val typedFast = longArrayOf(2, 4, 6).rangeIndex(LongSumMonoid, LongAddAction)
    typedFast.update(0, 2, 3L)
    check(typedFast.query(0, 3) == 18L)
    val typedAssign = longArrayOf(2, 4, 6).rangeIndex(LongSumMonoid, LongAssignAction)
    typedAssign.update(0, 2, 10L)
    check(typedAssign.query(0, 3) == 26L)

    val result = longArrayOf(2, 4, 6).onlineRangeProgram(LongSumMonoid, LongAddAction) {
        val before = query(0, 3)
        update(0, 2, before)
        all()
    }
    check(result == 36L)
    val repaint = longArrayOf(2, 4, 6).onlineRangeProgram(LongSumMonoid, LongAssignAction) {
        update(0, 2, query(0, 3))
        query(0, 3)
    }
    check(repaint == 30L)

    val rnd = Random(20261109)
    var oracleQueries = 0
    repeat(320) { iteration ->
        val n = rnd.nextInt(0, 65)
        val initial = LongArray(n) { rnd.nextLong(-1000L, 1001L) }
        val naive = initial.copyOf()
        val fast = initial.longSumLazyTree()
        repeat(260) {
            val l = rnd.nextInt(n + 1)
            val r = rnd.nextInt(l, n + 1)
            when (rnd.nextInt(4)) {
                0 -> {
                    val delta = rnd.nextLong(-1000L, 1001L)
                    fast.add(l, r, delta)
                    for (j in l until r) naive[j] += delta
                }
                1 -> {
                    val value = rnd.nextLong(-1000L, 1001L)
                    fast.assign(l, r, value)
                    naive.fill(value, l, r)
                }
                else -> {
                    val expected = (l until r).sumOf { naive[it] }
                    check(fast.query(l, r) == expected) {
                        "Bad range sum in iteration $iteration: [$l,$r)"
                    }
                    oracleQueries++
                }
            }
            check(fast.all() == naive.sum())
        }
        check(initial.indices.all { initial[it] in -1000L..1000L })
    }
    println("PASS: primitive Long-sum lazy algebra ($oracleQueries randomized oracle queries)")
}
