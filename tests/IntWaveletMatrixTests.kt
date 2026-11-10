import kotlin.random.Random

private fun assertRejected(body: () -> Unit) {
    check(runCatching(body).exceptionOrNull() is IllegalArgumentException)
}

fun main() {
    val sample = intArrayOf(Int.MAX_VALUE, -4, Int.MIN_VALUE, -4, 0, 9)
    val index = sample.waveletMatrix()
    sample[0] = 123
    check(index.size == 6)
    check((0 until 6).map { index.kthSmallest(0, 6, it) } ==
        listOf(Int.MIN_VALUE, -4, -4, 0, 9, Int.MAX_VALUE))
    check(index.kthLargest(0, 6, 0) == Int.MAX_VALUE)
    check(index.rank(-4, 0, 6) == 2)
    check(index.rank(Int.MIN_VALUE, 0, 6) == 1)
    check(index.countLess(0, 6, 0) == 3)
    check(index.countBetween(0, 6, -4, 10) == 4)
    check(index.predecessor(0, 6, -4) == Int.MIN_VALUE)
    check(index.successor(0, 6, 10) == Int.MAX_VALUE)
    check(index.predecessor(0, 6, Int.MIN_VALUE) == null)
    check(index.successor(0, 6, Int.MAX_VALUE) == Int.MAX_VALUE)
    check(index.countLess(3, 3, 0) == 0)
    check(index.predecessor(2, 2, 0) == null)
    check(index.successor(2, 2, 0) == null)
    check(intArrayOf().waveletMatrix().countLess(0, 0, 99) == 0)
    assertRejected { index.kthSmallest(0, 0, 0) }
    assertRejected { index.kthLargest(0, 6, 6) }
    assertRejected { index.rank(4, -1, 6) }
    assertRejected { index.countLess(4, 7, 0) }

    val rng = Random(2861739)
    var checks = 0
    repeat(250) {
        val n = rng.nextInt(0, 100)
        val a = IntArray(n) {
            when (rng.nextInt(7)) {
                0 -> Int.MIN_VALUE
                1 -> Int.MAX_VALUE
                else -> rng.nextInt(-100, 101)
            }
        }
        val wm = a.waveletMatrix()
        repeat(160) {
            val l = rng.nextInt(n + 1)
            val r = rng.nextInt(l, n + 1)
            val sorted = a.sliceArray(l until r).sorted()
            val x = when (rng.nextInt(6)) {
                0 -> Int.MIN_VALUE
                1 -> Int.MAX_VALUE
                else -> rng.nextInt(-150, 151)
            }
            val y = when (rng.nextInt(6)) {
                0 -> Int.MIN_VALUE
                1 -> Int.MAX_VALUE
                else -> rng.nextInt(-150, 151)
            }
            check(wm.rank(x, l, r) == sorted.count { it == x })
            check(wm.countLess(l, r, x) == sorted.count { it < x })
            check(wm.countBetween(l, r, x, y) == sorted.count { it >= x && it < y })
            check(wm.predecessor(l, r, x) == sorted.lastOrNull { it < x })
            check(wm.successor(l, r, x) == sorted.firstOrNull { it >= x })
            if (sorted.isNotEmpty()) {
                val k = rng.nextInt(sorted.size)
                check(wm.kthSmallest(l, r, k) == sorted[k])
                check(wm.kthLargest(l, r, k) == sorted[sorted.size - 1 - k])
            }
            checks++
        }
    }
    println("PASS: signed Int Wavelet Matrix ($checks randomized oracle ranges)")
}
