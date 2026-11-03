import kotlin.random.Random

fun main() {
    val product = aggregation(1) { a: Int, b: Int -> a * b }
    check(product.combine(product.combine(2, 3), 4) == 24)
    val minimum = aggregation(Int.MAX_VALUE, idempotent = true) { a: Int, b: Int -> minOf(a, b) }
    check(minimum.idempotent && minimum.combine(5, 3) == 3)

    val xor = aggregation(0) { a: Int, b: Int -> a xor b }.withInverse { it }
    val sum = aggregation(0) { a: Int, b: Int -> a + b }.withInverse { -it }
    val mod7 = abelianGroup(aggregation(0) { a: Int, b: Int -> (a + b) % 7 }) { (7 - it) % 7 }
    check(mod7.op.initial == 0)

    val random = Random(113003)
    var checks = 0
    repeat(4000) {
        val n = random.nextInt(0, 60)
        val a = IntArray(n) { random.nextInt(0, 30) }
        for (group in listOf(xor, sum, mod7)) {
            val values = if (group === mod7) a.map { it % 7 }.toIntArray() else a
            val prefix = values.prefixAggregates(group)
            val diff = values.differenceArray(group)
            val reference = values.toMutableList()
            val queries = mutableListOf<MoQuery>()
            repeat(20) {
                val from = random.nextInt(n + 1)
                val until = random.nextInt(from, n + 1)
                val expected = (from until until).fold(group.op.initial) { acc, i ->
                    group.op.combine(acc, values[i])
                }
                check(prefix.query(from, until) == expected)
                check(prefix.prefix(until) == (0 until until).fold(group.op.initial) { acc, i ->
                    group.op.combine(acc, values[i])
                })
                queries.add(MoQuery(from, until))
                val delta = random.nextInt(0, 7)
                diff.apply(from, until, delta)
                for (i in from until until) reference[i] = group.op.combine(reference[i], delta)
                check(diff.values() == reference)
                checks++
            }
            val answers = values.moAggregate(queries, group)
            for (i in queries.indices) {
                val (from, until) = queries[i]
                val expected = (from until until).fold(group.op.initial) { acc, j ->
                    group.op.combine(acc, values[j])
                }
                check(answers[i] == expected)
                checks++
            }
        }
    }

    check(intArrayOf().prefixAggregates(xor).query(0, 0) == 0)
    val emptyChanges = intArrayOf().differenceArray(xor)
    emptyChanges.apply(0, 0, 10)
    check(emptyChanges.values().isEmpty())
    check(intArrayOf().moAggregate(listOf(MoQuery(0, 0)), xor) == listOf(0))
    var rejected = false
    try { intArrayOf(1, 2).prefixAggregates(xor).query(-1, 2) }
    catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)
    rejected = false
    try { intArrayOf(1, 2).differenceArray(xor).apply(2, 1, 3) }
    catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    // Invented operations reuse the preexisting trees without changing them.
    val numbers = intArrayOf(2, 3, 4)
    check(numbers.segmentTree(product).query(0, 3) == 24)
    check(numbers.disjointSparseTable(product).query(0, 3) == 24)
    check(numbers.persistentSegmentTree(product).query(0, 0, 3) == 24)
    check(numbers.sparseTable(minimum).query(0, 3) == 2)
    val words = aggregation("") { a: String, b: String -> a + b }
    val historical = listOf("a", "b", "c").persistentSegmentTree(words)
    val newVersion = historical.set(0, 1, "!")
    check(historical.query(0, 0, 3) == "abc")
    check(historical.query(newVersion, 0, 3) == "a!c")

    val assignment = RangeAction<String, String>(
        apply = { _, token, length -> token.repeat(length) },
        compose = { _, newer -> newer }
    )
    val lazy = arrayOf("a", "b", "c").lazySegmentTree(words, assignment)
    lazy.update(0, 2, "x")
    check(lazy.query(0, 3) == "xxc")

    // Different update and aggregate types must work on primitive arrays.
    val intAction = RangeAction<Int, String>(
        apply = { total, token, length -> total + token.length * length },
        compose = { older, newer -> older + newer }
    )
    val customLazy = intArrayOf(1, 2).lazySegmentTree(sum.op, intAction)
    customLazy.update(0, 2, "xx")
    check(customLazy.query(0, 2) == 7)

    println("PASS custom aggregation: $checks randomized range comparisons + cross-tree cases")
}
