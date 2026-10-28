import kotlin.random.Random

private fun verify(ok: Boolean, message: String) { check(ok) { message } }
private inline fun rejects(block: () -> Unit) {
    var didThrow = false
    try { block() } catch (_: IllegalArgumentException) { didThrow = true }
    verify(didThrow, "Invalid argument should be rejected")
}

fun main() {
    val a = intArrayOf(2, 4, 6, 8, 10, 12, 14)
    verify(firstTrue(a.size) { a[it] >= 9 } == 4, "firstTrue(n)")
    verify(firstTrue(a.size) { a[it] > 14 } == a.size, "no true")
    verify(firstTrue(0) { true } == 0, "empty predicate")
    verify(firstTrue(0, 7) { false } == 7, "all false")
    verify(firstTrue(0, 7) { true } == 0, "all true")
    verify(lastTrue(7) { a[it] < 10 } == 3, "lastTrue(n)")
    verify(lastTrue(7) { false } == -1, "lastTrue none")
    verify(lastTrue(7) { true } == 6, "lastTrue all")
    verify(lastTrue(0) { true } == -1, "lastTrue empty")

    verify(firstTrue(Int.MIN_VALUE, Int.MAX_VALUE) { it >= -123456789 } == -123456789,
        "Int full-range midpoint")
    verify(firstTrue(Int.MIN_VALUE, Int.MAX_VALUE) { false } == Int.MAX_VALUE,
        "Int full range no success")
    verify(firstTrue(Int.MIN_VALUE, Int.MAX_VALUE) { true } == Int.MIN_VALUE,
        "Int full range all success")
    verify(lastTrue(Int.MIN_VALUE, Int.MAX_VALUE) { it < -123456789 } == -123456790,
        "Int full range last true")
    verify(lastTrue(Int.MIN_VALUE, Int.MAX_VALUE) { false } == null,
        "Int.MIN_VALUE null sentinel avoids overflow")
    verify(lastTrue(Int.MIN_VALUE, Int.MAX_VALUE) { true } == Int.MAX_VALUE-1,
        "Int all true with excluded upper bound")
    verify(lastTrue(Int.MIN_VALUE, Int.MIN_VALUE) { true } == null, "empty Int interval")

    verify(firstTrue(Long.MIN_VALUE, Long.MAX_VALUE) { it >= 0L } == 0L,
        "Long full signed range no overflow")
    verify(firstTrue(Long.MIN_VALUE, Long.MAX_VALUE) { it >= Long.MAX_VALUE - 1 } == Long.MAX_VALUE - 1,
        "Long near exclusive end")
    verify(firstTrue(Long.MIN_VALUE, Long.MAX_VALUE) { false } == Long.MAX_VALUE,
        "Long no true after overflow midpoint")
    verify(firstTrue(Long.MIN_VALUE, Long.MAX_VALUE) { true } == Long.MIN_VALUE,
        "Long first true at minimum")
    verify(firstTrue(0L, Long.MAX_VALUE) { it >= 5_000_000_000_000_000_000L } == 5_000_000_000_000_000_000L,
        "long binary search on answer")
    verify(lastTrue(Long.MIN_VALUE, Long.MAX_VALUE) { it < 0 } == -1L,
        "Long last true -1")
    verify(lastTrue(Long.MIN_VALUE, Long.MAX_VALUE) { false } == null,
        "Long MIN no true")
    verify(lastTrue(Long.MIN_VALUE, Long.MAX_VALUE) { true } == Long.MAX_VALUE - 1,
        "Long upper bound exclusive")
    verify(firstTrue(0L) { it >= 9L } == 0L, "empty long count")
    verify(lastTrue(0L) { true } == -1L, "empty long last true")
    verify(lastTrue(100L) { it < 67L } == 66L, "long convenience lastTrue")

    verify(a.lowerBound(9) == 4, "lowerBound sample")
    verify(a.upperBound(10) == 5, "upperBound sample")
    verify(a.lowerBound(2) == 0, "first element")
    verify(a.upperBound(14) == 7, "past last element")
    verify(a.lowerBound(9, 2, 4) == 4, "subrange no match sentinel")
    verify(a.lowerBound(8, 2, 6) == 3, "subrange position")
    verify(a.upperBound(8, 2, 6) == 4, "subrange upperBound")
    verify(a.lowerBound(10, 4, 4) == 4, "empty subrange")
    verify(intArrayOf().lowerBound(42) == 0, "empty array")
    verify(intArrayOf(3, 3, 3).lowerBound(3) == 0, "duplicate lowerBound")
    verify(intArrayOf(3, 3, 3).upperBound(3) == 3, "duplicate upperBound")

    verify(longArrayOf(Long.MIN_VALUE, -1L, -1L, Long.MAX_VALUE).lowerBound(-1L) == 1,
        "LongArray lowerBound")
    verify(longArrayOf(Long.MIN_VALUE, -1L, -1L, Long.MAX_VALUE).upperBound(-1L) == 3,
        "LongArray upperBound")
    verify(listOf("a", "bb", "bb", "ccc").lowerBound("bb") == 1,
        "list of strings lowerBound")
    verify(listOf("a", "bb", "bb", "ccc").upperBound("bb") == 3,
        "list of strings upperBound")
    verify(arrayOf(1, 2, 2, 3).lowerBound(2) == 1, "object array lowerBound")
    verify(arrayOf(1, 2, 2, 3).upperBound(2) == 3, "object array upperBound")

    val descending = listOf(9, 7, 7, 4, 2)
    val descendingOrder = compareByDescending<Int> { it }
    verify(descending.lowerBound(7, descendingOrder) == 1, "comparator lowerBound")
    verify(descending.upperBound(7, descendingOrder) == 3, "comparator upperBound")
    verify(descending.lowerBound(6, descendingOrder) == 3, "comparator insertion position")

    rejects { firstTrue(-1) { true } }
    rejects { lastTrue(-1) { true } }
    rejects { firstTrue(2, 1) { true } }
    rejects { lastTrue(2L, 1L) { true } }
    rejects { a.lowerBound(3, -1, 4) }
    rejects { a.upperBound(3, 4, 8) }
    rejects { a.lowerBound(3, 6, 5) }
    rejects { a.toList().lowerBound(3, 6, 5) }

    val random = Random(90413)
    var checks = 0
    repeat(3000) {
        val n = random.nextInt(0, 120)
        val list = IntArray(n) { random.nextInt(-50, 51) }.sortedArray()
        val longs = LongArray(n) { list[it].toLong() }
        val from = random.nextInt(n + 1)
        val until = random.nextInt(from, n + 1)
        for (target in -55..55 step 5) {
            val lower = (from until until).firstOrNull { list[it] >= target } ?: until
            val upper = (from until until).firstOrNull { list[it] > target } ?: until
            verify(list.lowerBound(target, from, until) == lower, "random Int lower")
            verify(list.upperBound(target, from, until) == upper, "random Int upper")
            verify(longs.lowerBound(target.toLong(), from, until) == lower, "random Long lower")
            verify(longs.upperBound(target.toLong(), from, until) == upper, "random Long upper")
            verify(list.asList().lowerBound(target, from, until) == lower, "random list lower")
            verify(list.asList().upperBound(target, from, until) == upper, "random list upper")
            verify(firstTrue(from, until) { list[it] >= target } == lower, "random firstTrue")
            verify(lastTrue(from, until) { list[it] < target } ==
                (if (lower == from) null else lower - 1), "random lastTrue")
            checks++
        }
    }

    // FirstTrue must always finish within logarithmic predicate calls.
    var calls = 0
    val pos = firstTrue(0L, Long.MAX_VALUE) {
        calls++
        it >= 4_000_000_000_000_000_000L
    }
    verify(pos == 4_000_000_000_000_000_000L && calls <= 64,
        "binary search must be logarithmic, calls=$calls")

    println("PASS: $checks randomized search cases + overflow, duplicates, empty ranges, and comparator tests")
}
