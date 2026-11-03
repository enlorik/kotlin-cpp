import kotlin.random.Random

private fun expectLaw(expected: String, block: () -> Unit) {
    try {
        block()
        error("Expected failing law: $expected")
    } catch (error: AlgebraLawViolation) {
        check(error.law == expected) { "Expected $expected; got ${error.law}: ${error.message}" }
        check(error.message!!.contains("Counterexample:"))
    }
}

fun main() {
    val product = monoid(identity = 1) { a: Int, b: Int -> a * b }
    product.checkLaws(listOf(-2, -1, 0, 1, 2, 3), iterations = 400)
    val values = intArrayOf(2, 3, 4)
    check(values.segmentTree(product).query(0, 3) == 24)
    check(values.disjointSparseTable(product).query(0, 3) == 24)
    val history = values.persistentSegmentTree(product)
    val changed = history.set(0, 1, 5)
    check(history.query(0, 0, 3) == 24)
    check(history.query(changed, 0, 3) == 40)

    val minimum = idempotentMonoid(Int.MAX_VALUE) { a: Int, b: Int -> minOf(a, b) }
    minimum.checkLaws(listOf(-3, -1, 0, 1, 7))
    check(values.sparseTable(minimum).query(0, 3) == 2)

    val concatenation = monoid("") { a: String, b: String -> a + b }
    concatenation.checkLaws(listOf("", "a", "bc", "d"))
    check(listOf("a", "bc", "d").disjointSparseTable(concatenation).query(0, 3) == "abcd")
    val replace = action<String, String>(
        apply = { _, text, count -> text.repeat(count) },
        compose = { _, newer -> newer }
    )
    replace.checkActionLaws(concatenation, listOf("a", "b", "xy"), listOf("*", "!", ""))
    val lazy = arrayOf("a", "b", "c").lazySegmentTree(concatenation, replace)
    lazy.update(0, 2, "x")
    lazy.update(1, 3, "!")
    check(lazy.query(0, 3) == "x!!")

    val xor = group(
        identity = 0,
        combine = { a: Int, b: Int -> a xor b },
        inverse = { x: Int -> x }
    )
    xor.checkLaws(listOf(-1, 0, 1, 2, 3, 4, Int.MIN_VALUE, Int.MAX_VALUE))
    val addition = group(
        identity = 0,
        combine = { a: Int, b: Int -> a + b },
        inverse = { x: Int -> -x }
    )
    addition.checkLaws(listOf(-3, -1, 0, 1, 2, 3, Int.MIN_VALUE, Int.MAX_VALUE))

    // Group capabilities unlock Fenwick, prefix, deferred range updates and Mo.
    val random = Random(92030)
    var checks = 0
    repeat(2000) {
        val n = random.nextInt(0, 50)
        val source = IntArray(n) { random.nextInt(-40, 41) }
        for (group in listOf(xor, addition)) {
            val fenwick = source.fenwickTree(group)
            val prefix = source.prefixAggregates(group)
            val diff = source.differenceArray(group)
            val modified = source.copyOf()
            val queries = ArrayList<MoQuery>()
            repeat(40) {
                val from = random.nextInt(n + 1)
                val until = random.nextInt(from, n + 1)
                val expected = (from until until).fold(group.identity) { current, i ->
                    group.combine(current, source[i])
                }
                check(prefix.query(from, until) == expected)
                check(fenwick.query(from, until) == expected)
                queries.add(MoQuery(from, until))
                val delta = random.nextInt(-10, 11)
                diff.apply(from, until, delta)
                for (i in from until until) modified[i] = group.combine(modified[i], delta)
                check(diff.values() == modified.toList())
                checks++
            }
            val answers = source.moAggregate(queries, group)
            for (i in queries.indices) {
                val (from, until) = queries[i]
                val expected = (from until until).fold(group.identity) { acc, pos ->
                    group.combine(acc, source[pos])
                }
                check(answers[i] == expected)
                checks++
            }
        }
    }

    // A claimed capability is not a proof: generate useful counterexamples.
    expectLaw("identity") {
        monoid(0) { a: Int, b: Int -> a + b + 1 }.checkLaws(listOf(1, 2))
    }
    expectLaw("associativity") {
        monoid(0) { a: Int, b: Int -> a + b + a * b * b }
            .checkLaws(listOf(1, 2, 3), iterations = 1000, seed = 1)
    }
    expectLaw("idempotence") {
        idempotentMonoid(0) { a: Int, b: Int -> a + b }.checkLaws(listOf(1, 2))
    }
    expectLaw("inverse") {
        group(0, { a: Int, b: Int -> a + b }, { _: Int -> 0 })
            .checkLaws(listOf(1, 2))
    }
    val sumMonoid = monoid(0) { a: Int, b: Int -> a + b }
    val incorrectAction = action<Int, Int>(
        apply = { total, change, len -> total + change * len },
        compose = { old, _ -> old }
    )
    expectLaw("action composition") {
        incorrectAction.checkActionLaws(sumMonoid, listOf(1, 2), listOf(3, 4), seed = 123)
    }
    val addAction = action<Int, Int>(
        apply = { total, change, len -> total + change * len },
        compose = { old, newer -> old + newer }
    )
    addAction.checkActionLaws(sumMonoid, listOf(-2, 0, 3), listOf(-1, 0, 5))
    val lazySum = intArrayOf(1, 2, 3).lazySegmentTree(sumMonoid, addAction)
    lazySum.update(0, 2, 5)
    check(lazySum.query(0, 3) == 16)

    check(IntAggregations.MIN.asIdempotentMonoid().combine(2, 7) == 2)
    var rejected = false
    try { IntAggregations.SUM.asIdempotentMonoid() } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    println("PASS typed algebra: $checks randomized group comparisons and explicit law counterexamples")
}
