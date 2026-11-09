import kotlin.random.Random

private fun mustReject(body: () -> Unit) {
    check(runCatching(body).exceptionOrNull() is IllegalArgumentException)
}

fun main() {
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val concat = monoid("") { a: String, b: String -> a + b }
    val legacyMax = aggregateOp(Int.MIN_VALUE, idempotent = true) { a: Int, b: Int -> maxOf(a, b) }
    val addition = action<Int, Int>(
        apply = { total, delta, length -> total + delta * length },
        compose = { old, newer -> old + newer }
    )
    val assign = action<Int, Int>(
        apply = { _, value, length -> value * length },
        compose = { _, newer -> newer }
    )

    val original = intArrayOf(2, 4, 6)
    val answer = original.onlineRangeProgram(sum) {
        check(strategy == RangeIndexStrategy.POINT_UPDATES)
        check(size == 3)
        val first = query(0, 3)
        set(1, first)
        val second = query(1..2)
        set(0, second)
        query(0, 3)
    }
    check(answer == 36)
    check(original.contentEquals(intArrayOf(2, 4, 6)))

    val words = listOf("ab", "cd", "e").onlineRangeProgram(concat) {
        val a = query(0, 3)
        set(1, a)
        query(0, 3)
    }
    check(words == "ababcdee")

    check(arrayOf(2, 9, 3).onlineRangeProgram(legacyMax) { query(0, 3) } == 9)
    check(longArrayOf(2L, 4L).onlineRangeProgram(monoid(0L) { a: Long, b: Long -> a + b }) {
        set(1, query(0, 2))
        all()
    } == 8L)
    check(emptyList<String>().onlineRangeProgram(concat) { query(0, 0) } == "")

    val paint = intArrayOf(1, 2, 3, 4).onlineRangeProgram(sum, assign) {
        val before = query(0, 4)
        update(1, 3, before)
        val after = query(0, 4)
        update(0, size, after)
        all()
    }
    check(paint == 100)

    val legacyAdd = intArrayOf(1, 2, 3).onlineRangeProgram(
        aggregateOp(0) { a: Int, b: Int -> a + b },
        RangeAction<Int, Int>(
            apply = { total, delta, length -> total + delta * length },
            compose = { old, newer -> old + newer }
        )
    ) {
        update(0, 3, 10)
        query(1..2)
    }
    check(legacyAdd == 25)

    mustReject { intArrayOf(3).onlineRangeProgram(sum) { query(-1, 1) } }
    mustReject { intArrayOf(3).onlineRangeProgram(sum) { query(0, 2) } }
    mustReject { intArrayOf(3).onlineRangeProgram(sum) { query(0..Int.MAX_VALUE) } }
    mustReject { intArrayOf(3).onlineRangeProgram(sum) { set(1, 2) } }
    mustReject { intArrayOf().onlineRangeProgram(sum) { set(0, 2) } }
    mustReject { intArrayOf(3).onlineRangeProgram(sum, addition) { update(1, 0, 1) } }

    val rand = Random(20261109)
    var comparisons = 0
    repeat(260) { round ->
        val n = rand.nextInt(0, 46)
        val source = IntArray(n) { rand.nextInt(-20, 21) }
        val expected = source.copyOf()
        val received = source.onlineRangeProgram(sum) {
            val output = ArrayList<Int>()
            repeat(110) {
                val from = rand.nextInt(n + 1)
                val until = rand.nextInt(from, n + 1)
                if (n > 0 && rand.nextInt(4) == 0) {
                    val index = rand.nextInt(n)
                    // Immediate query dependency: cannot use deferred rangeProgram.
                    val updated = query(from, until) + rand.nextInt(-10, 11)
                    set(index, updated)
                    expected[index] = updated
                } else {
                    output.add(query(from, until))
                    val naive = (from until until).sumOf { expected[it] }
                    check(output.last() == naive) {
                        "Online sum mismatch round=$round range=[$from,$until)"
                    }
                    comparisons++
                }
            }
            output
        }
        check(source.indices.all { source[it] in -20..20 }) // unchanged
        check(received.isNotEmpty())

        val slow = source.copyOf()
        source.onlineRangeProgram(sum, addition) {
            repeat(110) {
                val from = rand.nextInt(n + 1)
                val until = rand.nextInt(from, n + 1)
                if (rand.nextBoolean()) {
                    val delta = rand.nextInt(-10, 11)
                    update(from, until, delta)
                    for (j in from until until) slow[j] += delta
                } else {
                    check(query(from, until) == (from until until).sumOf { slow[it] })
                    comparisons++
                }
            }
            check(all() == slow.sum())
        }
    }
    println("PASS: online point/lazy range DSL ($comparisons randomized oracle queries)")
}
