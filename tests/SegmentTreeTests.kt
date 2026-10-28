import kotlin.random.Random

fun main() {
    val a = intArrayOf(2, 4, 6, 8, 10)
    val t = a.segmentTree()
    check(t.query(1, 4) == 18)
    t.set(2, 100)
    check(t.query(1, 4) == 112)
    check(t.all() == 124 && t[2] == 100 && t.query(0, 0) == 0)
    check(t.findPrefix { it >= 7 } == 2)
    check(t.findPrefix { it >= 125 } == null)
    check(t.findPrefix { it >= 0 } == 0)
    val empty = intArrayOf().segmentTree()
    check(empty.all() == 0 && empty.query(0, 0) == 0 && empty.findPrefix { true } == null)
    val text = listOf("a", "bc", "d", "e").segmentTree("") { x, y -> x + y }
    check(text.query(1, 3) == "bcd")
    text.set(2, "XYZ")
    check(text.query(0, 4) == "abcXYZe")
    check(text.findPrefix { it.length >= 5 } == 2)
    val huge = longArrayOf(1L, 2L, 3L).segmentTree()
    check(huge.findPrefix { it >= 6L } == 2)
    val min = intArrayOf(3, 7, 2).segmentTree(IntAggregations.MIN)
    check(min.query(0, 3) == 2 && min.query(0, 2) == 3)
    val shared = intArrayOf(3, 4, 5).segmentTree(IntAggregations.SUM)
    check(shared.query(1, 3) == 9)
    var failed = false
    try { t.query(0, 6) } catch (_: IllegalArgumentException) { failed = true }
    check(failed)
    failed = false
    try { t.set(-1, 0) } catch (_: IllegalArgumentException) { failed = true }
    check(failed)
    val random = Random(44012)
    repeat(5000) {
        val n = random.nextInt(0, 70)
        val arr = IntArray(n) { random.nextInt(0, 20) }
        val seg = arr.segmentTree()
        repeat(50) {
            if (n > 0 && random.nextBoolean()) {
                val index = random.nextInt(n)
                val value = random.nextInt(0, 20)
                arr[index] = value
                seg.set(index, value)
            } else {
                val left = random.nextInt(n + 1)
                val right = random.nextInt(left, n + 1)
                check(seg.query(left, right) == arr.slice(left until right).sum())
                val target = random.nextInt(0, arr.sum() + 5)
                val expected = if (n == 0) null
                    else (0 until n).firstOrNull { p -> arr.slice(0..p).sum() >= target }
                check(seg.findPrefix { it >= target } == expected)
            }
        }
    }
    println("PASS: 250000 random segment tree operations and prefix searches")
}
