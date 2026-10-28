import kotlin.random.Random
import kotlin.math.abs

fun main() {
    check(firstTrueFrom(0) { it >= 15 } == 15)
    check(firstTrueFrom(10) { it >= 1 } == 10)
    check(firstTrueFrom(0) { false } == null)
    check(firstTrueFrom(Int.MAX_VALUE) { true } == Int.MAX_VALUE)
    check(firstTrueFrom(Int.MAX_VALUE) { false } == null)
    check(firstTrueFrom(Int.MIN_VALUE) { it >= Int.MAX_VALUE } == Int.MAX_VALUE)
    check(firstTrueFrom(Int.MIN_VALUE) { it >= 0 } == 0)
    check(firstTrueFrom(0L) { it >= 15L } == 15L)
    check(firstTrueFrom(0L) { false } == null)
    check(firstTrueFrom(Long.MIN_VALUE) { it >= 0L } == 0L)
    check(firstTrueFrom(Long.MIN_VALUE) { it >= Long.MAX_VALUE } == Long.MAX_VALUE)
    check(firstTrueFrom(Long.MIN_VALUE) { false } == null)
    check(firstTrueFrom(Long.MAX_VALUE) { true } == Long.MAX_VALUE)
    check(firstTrueFrom(Long.MAX_VALUE) { false } == null)
    var calls = 0
    check(firstTrueFrom(0L) { calls++; it >= 1_000_000_000_000_000_000L } == 1_000_000_000_000_000_000L)
    check(calls <= 130)

    check(abs(bisect(1.0, 2.0) { it * it >= 2.0 } - kotlin.math.sqrt(2.0)) < 1e-14)
    check(bisect(1.0, 2.0) { true } == 1.0)
    check(bisect(0.0, 2.0) { it >= 2.0 } == 2.0)
    check(bisect(-Double.MAX_VALUE, Double.MAX_VALUE) { it >= 0.0 } == 0.0)
    check(abs(bisectRoot(1.0, 2.0) { it * it - 2.0 } - kotlin.math.sqrt(2.0)) < 1e-14)
    check(abs(bisectRoot(-2.0, -1.0) { it * it - 2.0 } + kotlin.math.sqrt(2.0)) < 1e-14)
    check(bisectRoot(0.0, 1.0) { it } == 0.0)
    var rejected = false
    try { bisect(0.0, 1.0) { false } } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)
    rejected = false
    try { bisectRoot(0.0, 1.0) { it + 1.0 } } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)
    rejected = false
    try { bisect(Double.NaN, 1.0) { true } } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    val random = Random(72818)
    repeat(6000) {
        val n = random.nextInt(0, 100)
        val xs = IntArray(n) { random.nextInt(-100, 101) }.sortedArray()
        val threshold = random.nextInt(-110, 111)
        val expected = xs.indexOfFirst { it >= threshold }.let { if (it < 0) n else it }
        check(xs.partitionPoint { it >= threshold } == expected)
        check(xs.map { it.toLong() }.toLongArray().partitionPoint { it >= threshold.toLong() } == expected)
        check(xs.toList().partitionPoint { it >= threshold } == expected)
        check(xs.toTypedArray().partitionPoint { it >= threshold } == expected)
        val start = random.nextInt(n + 1)
        val end = random.nextInt(start, n + 1)
        val expectedRange = (start until end).firstOrNull { xs[it] >= threshold } ?: end
        check(xs.partitionPoint(start, end) { it >= threshold } == expectedRange)
        check(xs.toList().partitionPoint(start, end) { it >= threshold } == expectedRange)
        val left = random.nextInt(-100_000, 100_000)
        val target = random.nextInt(left, 100_001)
        check(firstTrueFrom(left) { it >= target } == target)
        check(firstTrueFrom(left.toLong()) { it >= target.toLong() } == target.toLong())
    }
    println("PASS advanced binary search: 6000 randomized cases plus overflow and bisection")
}
