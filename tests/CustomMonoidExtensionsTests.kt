import kotlin.random.Random

private fun expectInvalidRange(block: () -> Unit) {
    var failed = false
    try { block() } catch (_: IllegalArgumentException) { failed = true }
    check(failed) { "An invalid half-open interval was accepted" }
}

fun main() {
    // One invented operation works in every lawful associative structure.
    val product = aggregateOp(1) { a: Int, b: Int -> a * b }
    val data = intArrayOf(2, 3, 4)
    check(data.segmentTree(product).query(0, 3) == 24)
    check(data.disjointSparseTable(product).query(0, 3) == 24)
    check(data.persistentSegmentTree(product).query(0, 0, 3) == 24)
    val prefix = data.prefixAggregates(product)
    check(prefix.prefix(0) == 1)
    check(prefix.prefix(2) == 6)
    check(prefix.all() == 24)

    // A typed monoid has prefix() but cannot make arbitrary subrange queries.
    val typedProduct = monoid(1) { a: Int, b: Int -> a * b }
    val typedPrefix = data.prefixAggregates(typedProduct)
    check(typedPrefix.prefix(3) == 24)
    val group = group(identity = 0, combine = { a: Int, b: Int -> a xor b }, inverse = { it })
    check(data.prefixAggregates(group).query(1, 3) == (3 xor 4))

    // Assignment is allowed for every monoid. Assignment composition is
    // newest-wins, including nested partial updates.
    val concat = monoid("") { a: String, b: String -> a + b }
    val strings = arrayOf("ab", "c", "xyz", "d")
    val tree = strings.lazySegmentTree(concat)
    check(tree.query(0, 4) == "abcxyzd")
    tree.set(1, 4, "!")
    check(tree.query(0, 4) == "ab!!!")
    tree.set(2, 3, "XX")
    check(tree.query(0, 4) == "ab!XX!")
    tree.update(0, 2, "z")
    check(tree.all() == "zzXX!")
    check(tree.query(2, 2) == "")
    check(strings.contentEquals(arrayOf("ab", "c", "xyz", "d")))

    // The optional constant-time repetition helper restores O(log n)
    // assignments when the aggregate is SUM.
    var repetitions = 0
    val sum = aggregateOp(0) { a: Int, b: Int -> a + b }
    val fastSum = intArrayOf(1, 2, 3, 4).lazySegmentTree(
        sum,
        repeatValue = { value, length ->
            repetitions++
            value * length
        }
    )
    fastSum.set(0, 4, 5)
    fastSum.set(1, 3, -2)
    check(fastSum.query(0, 4) == 6)
    check(repetitions > 0)

    // Both legacy AggregateOp and new typed Monoid APIs should compile
    // and preserve the original no-argument Long-SUM APIs.
    val oldTree = intArrayOf(1, 2, 3).lazySegmentTree()
    check(oldTree.query(0, 3) == 6L)
    val oldPrefix = intArrayOf(1, 2, 3).prefixSums()
    check(oldPrefix.query(0, 3) == 6L)
    val typedAssignment = intArrayOf(2, 3, 4).lazySegmentTree(typedProduct)
    typedAssignment.set(0, 3, 2)
    check(typedAssignment.all() == 8)
    check(intArrayOf().lazySegmentTree(typedProduct).all() == 1)

    expectInvalidRange { prefix.prefix(-1) }
    expectInvalidRange { prefix.prefix(4) }
    expectInvalidRange { typedAssignment.set(0, 4, 1) }
    expectInvalidRange { typedAssignment.query(-1, 2) }
    expectInvalidRange { typedAssignment.query(2, 1) }

    val rnd = Random(27011)
    val operations = listOf(
        aggregateOp(0) { a: Int, b: Int -> a + b },
        aggregateOp(1) { a: Int, b: Int -> a * b },
        aggregateOp(Int.MAX_VALUE, idempotent = true) { a: Int, b: Int -> minOf(a, b) },
        aggregateOp(Int.MIN_VALUE, idempotent = true) { a: Int, b: Int -> maxOf(a, b) },
        aggregateOp(0) { a: Int, b: Int -> a xor b }
    )
    var checks = 0
    repeat(1800) {
        val n = rnd.nextInt(0, 65)
        val initial = IntArray(n) { rnd.nextInt(-3, 4) }
        for (op in operations) {
            val expected = initial.copyOf()
            val monoidPrefix = initial.prefixAggregates(op)
            var aggregate = op.initial
            for (end in 0..n) {
                if (end > 0) aggregate = op.combine(aggregate, initial[end - 1])
                check(monoidPrefix.prefix(end) == aggregate)
                checks++
            }
            val assignment = initial.lazySegmentTree(op)
            repeat(35) {
                val from = rnd.nextInt(n + 1)
                val until = rnd.nextInt(from, n + 1)
                if (rnd.nextBoolean()) {
                    val value = rnd.nextInt(-3, 4)
                    assignment.set(from, until, value)
                    for (index in from until until) expected[index] = value
                }
                val actual = assignment.query(from, until)
                val brute = (from until until).fold(op.initial) { acc, i ->
                    op.combine(acc, expected[i])
                }
                check(actual == brute)
                checks++
            }
            check(assignment.all() == expected.fold(op.initial, op.combine))
            checks++
        }
    }

    // Order-sensitive noncommutative monoids must preserve left-to-right order.
    repeat(600) {
        val n = rnd.nextInt(0, 30)
        val initial = List(n) { "v${rnd.nextInt(0, 9)}" }
        val expected = initial.toMutableList()
        val lazy = initial.lazySegmentTree(concat)
        val prefixes = initial.prefixAggregates(concat)
        for (until in 0..n) check(prefixes.prefix(until) == initial.take(until).joinToString(""))
        repeat(20) {
            val a = rnd.nextInt(n + 1)
            val b = rnd.nextInt(a, n + 1)
            if (rnd.nextBoolean()) {
                val value = "x${rnd.nextInt(0, 9)}"
                lazy.set(a, b, value)
                for (i in a until b) expected[i] = value
            }
            check(lazy.query(a, b) == expected.subList(a, b).joinToString(""))
            checks++
        }
    }

    println("PASS: $checks randomized monoid prefixes and lazy assignments")
}
