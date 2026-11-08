import kotlin.random.Random

private data class PlannedRangeStep(val kind: Int, val a: Int, val b: Int)

private fun expectIllegal(body: () -> Unit) {
    check(runCatching(body).exceptionOrNull() is IllegalArgumentException)
}

fun main() {
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val minimum = idempotentMonoid(Int.MAX_VALUE) { a: Int, b: Int -> minOf(a, b) }
    val maximum = aggregateOp(Int.MIN_VALUE, idempotent = true) { a: Int, b: Int -> maxOf(a, b) }
    val concat = monoid("") { a: String, b: String -> a + b }

    val staticSum = intArrayOf(2, 4, 6).rangeProgram(sum) {
        query(0, 3)
        query(1..2)
        query(2, 2)
    }
    check(staticSum.strategy == RangeIndexStrategy.STATIC_GENERAL)
    check(staticSum.answers == listOf(12, 10, 0))
    check(staticSum[1] == 10)

    val staticMin = intArrayOf(2, 4, 6).rangeProgram(minimum) {
        query(0, 3)
        query(2, 2)
    }
    check(staticMin.strategy == RangeIndexStrategy.STATIC_IDEMPOTENT)
    check(staticMin.answers == listOf(2, Int.MAX_VALUE))

    val legacyMax = arrayOf(3, 5, 1).rangeProgram(maximum) {
        query(0, 3)
        query(1, 3)
    }
    check(legacyMax.strategy == RangeIndexStrategy.STATIC_IDEMPOTENT)
    check(legacyMax.answers == listOf(5, 5))

    val dynamic = intArrayOf(2, 4, 6).rangeProgram(sum) {
        query(0, 3)
        set(1, 10)
        query(0, 3)
        set(0, -1)
        query(0, 2)
    }
    check(dynamic.strategy == RangeIndexStrategy.POINT_UPDATES)
    check(dynamic.answers == listOf(12, 18, 9))

    // A source modified during DSL recording cannot change the snapshot.
    val values = intArrayOf(3, 4)
    val recorded = values.rangeProgram(sum) {
        values[0] = 300
        query(0, 2)
    }
    check(recorded.answers == listOf(7))

    // Program point assignments change its private index, not the source list.
    val mutableInput = mutableListOf(3, 4)
    val privateChanges = mutableInput.rangeProgram(sum) {
        set(1, 200)
        query(0, 2)
    }
    check(privateChanges.answers == listOf(203))
    check(mutableInput == listOf(3, 4))

    val words = listOf("ab", "c", "d").rangeProgram(concat) {
        query(0, 3)
        set(1, "Z")
        query(0, 3)
        query(1..2)
    }
    check(words.strategy == RangeIndexStrategy.POINT_UPDATES)
    check(words.answers == listOf("abcd", "abZd", "Zd"))

    val empty = emptyList<String>().rangeProgram(concat) {
        query(0, 0)
        query(0..-1)
    }
    check(empty.answers == listOf("", ""))
    check(empty.strategy == RangeIndexStrategy.STATIC_GENERAL)

    val noQueries = longArrayOf(8, 9).rangeProgram(
        monoid(0L) { a: Long, b: Long -> a + b }
    ) {
        set(0, 2)
    }
    check(noQueries.answers.isEmpty())
    check(noQueries.strategy == RangeIndexStrategy.POINT_UPDATES)

    expectIllegal {
        intArrayOf(1, 2).rangeProgram(sum) { query(-1, 1) }
    }
    expectIllegal {
        intArrayOf(1, 2).rangeProgram(sum) { query(1, 0) }
    }
    expectIllegal {
        intArrayOf(1, 2).rangeProgram(sum) { query(0, 3) }
    }
    expectIllegal {
        intArrayOf(1, 2).rangeProgram(sum) { query(0..Int.MAX_VALUE) }
    }
    expectIllegal {
        intArrayOf().rangeProgram(sum) { set(0, 3) }
    }

    val rand = Random(20261108)
    var verified = 0
    repeat(400) { round ->
        val n = rand.nextInt(0, 45)
        val initial = IntArray(n) { rand.nextInt(-50, 51) }
        val events = ArrayList<PlannedRangeStep>()
        val queryOnly = round % 3 == 0
        repeat(90) { i ->
            if (!queryOnly && n > 0 && (i == 0 || rand.nextInt(4) == 0)) {
                events.add(PlannedRangeStep(1, rand.nextInt(n), rand.nextInt(-50, 51)))
            } else {
                val l = rand.nextInt(n + 1)
                val r = rand.nextInt(l, n + 1)
                events.add(PlannedRangeStep(0, l, r))
            }
        }
        val result = initial.rangeProgram(sum) {
            for (event in events) {
                if (event.kind == 0) query(event.a, event.b)
                else set(event.a, event.b)
            }
        }
        val expected = ArrayList<Int>()
        val naive = initial.copyOf()
        for (event in events) {
            if (event.kind == 0) {
                expected.add(naive.sliceArray(event.a until event.b).sum())
                verified++
            } else {
                naive[event.a] = event.b
            }
        }
        check(result.answers == expected) { "Incorrect sum result in round $round" }
        check(result.strategy == if (queryOnly || n == 0) {
            RangeIndexStrategy.STATIC_GENERAL
        } else {
            RangeIndexStrategy.POINT_UPDATES
        })

        // The static path must preserve idempotent operations, too.
        if (queryOnly) {
            val minResults = initial.rangeProgram(minimum) {
                for (event in events) query(event.a, event.b)
            }
            check(minResults.strategy == RangeIndexStrategy.STATIC_IDEMPOTENT)
            check(minResults.answers == events.map { event ->
                initial.sliceArray(event.a until event.b).minOrNull() ?: Int.MAX_VALUE
            })
        }
    }
    println("PASS: declarative range program DSL ($verified randomized oracle queries)")
}
