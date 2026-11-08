import kotlin.random.Random

private fun assertSame(a: LongSumSegmentTree, b: SegmentTree<Long>, from: Int, until: Int) {
    check(a.query(from, until) == b.query(from, until))
}

fun main() {
    val empty = longArrayOf().sumSegmentTree()
    check(empty.size == 0 && empty.query(0, 0) == 0L && empty.all() == 0L)
    check(empty.findPrefix { it >= 0 } == null)
    check(runCatching { empty[0] }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { empty.query(0, 1) }.exceptionOrNull() is IllegalArgumentException)

    val singleton = longArrayOf(42).sumSegmentTree()
    check(singleton[0] == 42L && singleton.query(0, 1) == 42L)
    singleton[0] = -11L
    check(singleton.all() == -11L)
    check(longArrayOf(1, 2, 3).sumSegmentTree()[1..2] == 5L)
    check(intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE).sumSegmentTree().all() == 2L * Int.MAX_VALUE)
    check(longArrayOf(Long.MAX_VALUE, 1L).sumSegmentTree().all() == Long.MIN_VALUE)

    check(runCatching { singleton[-1] = 4L }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { singleton.query(1, 0) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { singleton.query(-1, 0) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { singleton.query(0, 2) }.exceptionOrNull() is IllegalArgumentException)

    val p = longArrayOf(2, 3, 5, 7).sumSegmentTree()
    check(p.findPrefix { it >= 1 } == 0)
    check(p.findPrefix { it >= 5 } == 1)
    check(p.findPrefix { it >= 10 } == 2)
    check(p.findPrefix { it >= 18 } == null)
    check(p.findPrefix { it >= 0 } == 0)
    p[1] = 10
    check(p.findPrefix { it >= 10 } == 1)

    val rnd = Random(20091209)
    var verified = 0
    repeat(400) {
        val n = rnd.nextInt(0, 80)
        val initial = LongArray(n) { rnd.nextLong(-10000, 10001) }
        val fast = initial.sumSegmentTree()
        val generic = initial.segmentTree()
        val naive = initial.copyOf()
        repeat(350) {
            if (n > 0 && rnd.nextInt(3) == 0) {
                val i = rnd.nextInt(n)
                val value = rnd.nextLong(-10000, 10001)
                fast[i] = value
                generic[i] = value
                naive[i] = value
                check(fast[i] == generic[i])
            } else {
                val from = rnd.nextInt(n + 1)
                val until = rnd.nextInt(from, n + 1)
                val expected = naive.copyOfRange(from, until).sum()
                check(fast.query(from, until) == expected)
                assertSame(fast, generic, from, until)
                verified++
            }
            check(fast.all() == generic.all())
        }
    }
    println("PASS: LongSumSegmentTree ($verified randomized oracle queries)")
}
