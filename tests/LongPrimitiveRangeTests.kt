import kotlin.random.Random

private fun longOpFold(values: LongArray, lo: Int, hi: Int, op: LongBuiltInMonoid): Long {
    var acc = op.identity
    for (i in lo until hi) acc = op.combine(acc, values[i])
    return acc
}

fun main() {
    LongMinMonoid.checkLaws(listOf(-9L, 0L, 3L, 20L))
    LongMaxMonoid.checkLaws(listOf(-9L, 0L, 3L, 20L))
    LongGcdMonoid.checkLaws(listOf(0L, 1L, 4L, 12L, 21L))
    LongXorMonoid.checkLaws(listOf(-3L, 0L, 1L, 7L, Long.MIN_VALUE))

    val operations: List<LongBuiltInMonoid> =
        listOf(LongSumMonoid, LongMinMonoid, LongMaxMonoid, LongGcdMonoid, LongXorMonoid)

    check(longArrayOf(Long.MAX_VALUE, 1L).rangeIndex(LongSumMonoid).query(0, 2) ==
        Long.MIN_VALUE)
    val source = longArrayOf(4L, 9L, 2L)
    val index = source.rangeIndex(LongMinMonoid)
    source[0] = -100L
    check(index.query(0, 3) == 2L)
    check(index.strategy == LongPrimitiveStrategy.STATIC_SPARSE)
    check(longArrayOf(1, 2).rangeIndex(LongXorMonoid).strategy ==
        LongPrimitiveStrategy.STATIC_PREFIX)
    check(longArrayOf(1, 2).rangeIndex(LongSumMonoid).strategy ==
        LongPrimitiveStrategy.STATIC_PREFIX)
    check(intArrayOf(2, 3, 4).longRangeIndex(LongMaxMonoid).query(0, 3) == 4L)

    check(runCatching { longArrayOf(-1L).rangeIndex(LongGcdMonoid) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        longArrayOf(1L).rangeIndex(LongGcdMonoid, mutable = true).set(0, -1L)
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        longArrayOf(1L).rangeIndex(LongMaxMonoid).set(0, 2L)
    }.exceptionOrNull() is IllegalStateException)
    check(runCatching {
        longArrayOf(1L).rangeIndex(LongMinMonoid).query(0, 2)
    }.exceptionOrNull() is IllegalArgumentException)

    // Explicit built-in online receiver: each query may decide the next point update.
    check(longArrayOf(2L, 4L, 6L).onlineLongPoints(LongXorMonoid) {
        val previous = query(0, 3)  // 2 xor 4 xor 6 = 0
        set(1, previous)
        query(0, 3)                // 2 xor 0 xor 6 = 4
    } == 4L)

    // Noncommutative monoids must preserve order and use the general index.
    val concat = monoid("") { a: String, b: String -> a + b }
    check(listOf("ab", "c", "def").rangeIndex(concat).query(0, 3) == "abcdef")
    val words = listOf("a", "b", "c").rangeIndex(concat, mutable = true)
    words.set(1, "XYZ")
    check(words.query(0, 3) == "aXYZc")

    val rnd = Random(1447101)
    var checks = 0
    for (op in operations) repeat(160) { round ->
        val n = rnd.nextInt(0, 64)
        val initial = LongArray(n) {
            if (op === LongGcdMonoid) rnd.nextLong(0, 201)
            else rnd.nextLong(-1000, 1001)
        }
        val static = initial.rangeIndex(op)
        val mutable = initial.rangeIndex(op, mutable = true)
        val erased: Monoid<Long> = op
        val generic = initial.rangeIndex(erased, mutable = true)
        val expected = initial.copyOf()
        check(mutable.strategy == LongPrimitiveStrategy.POINT_SEGMENT)
        check(static.strategy ==
            if (op === LongSumMonoid || op === LongXorMonoid)
                LongPrimitiveStrategy.STATIC_PREFIX
            else LongPrimitiveStrategy.STATIC_SPARSE)
        repeat(120) {
            if (n > 0 && rnd.nextInt(3) == 0) {
                val at = rnd.nextInt(n)
                val value = if (op === LongGcdMonoid) rnd.nextLong(0, 501)
                            else rnd.nextLong(-500, 501)
                mutable.set(at, value)
                generic.set(at, value)
                expected[at] = value
            } else {
                val lo = rnd.nextInt(n + 1)
                val hi = rnd.nextInt(lo, n + 1)
                val expectedValue = longOpFold(expected, lo, hi, op)
                check(mutable.query(lo, hi) == expectedValue) {
                    "Mutable ${op.reduction} mismatch round=$round [$lo,$hi)"
                }
                check(generic.query(lo, hi) == expectedValue)
                val staticValue = longOpFold(initial, lo, hi, op)
                check(static.query(lo, hi) == staticValue) {
                    "Static ${op.reduction} mismatch round=$round [$lo,$hi)"
                }
                checks++
            }
            check(mutable.all() == longOpFold(expected, 0, n, op))
        }
    }
    println("PASS: primitive Long static/mutable monoid backends ($checks oracle queries)")
}
