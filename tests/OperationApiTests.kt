import kotlin.random.Random

private fun fails(body: () -> Unit) {
    var rejected = false
    try { body() } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected) { "Expected unsupported combination / invalid range to throw" }
}

fun main() {
    val random = Random(251102)
    val ops = listOf(IntAggregations.SUM, IntAggregations.MIN, IntAggregations.MAX,
        IntAggregations.XOR, IntAggregations.OR, IntAggregations.AND)
    var checks = 0

    // History is immutable, even when different branches start from version 0.
    repeat(230) {
        val a = IntArray(random.nextInt(0, 45)) { random.nextInt(-35, 36) }
        for (op in ops) {
            val tree = a.persistentSegmentTree(op)
            val versions = mutableListOf(a.copyOf())
            repeat(25) {
                val id = random.nextInt(versions.size)
                val current = versions[id]
                val l = random.nextInt(current.size + 1)
                val r = random.nextInt(l, current.size + 1)
                val expected = (l until r).fold(op.initial) { acc, i -> op.combine(acc, current[i]) }
                check(tree.query(id, l, r) == expected)
                if (current.isNotEmpty()) {
                    val index = random.nextInt(current.size)
                    val value = random.nextInt(-35, 36)
                    val updated = current.copyOf()
                    val newVersion = if (random.nextBoolean()) {
                        updated[index] = value
                        tree.set(id, index, value)
                    } else {
                        updated[index] = op.combine(updated[index], value)
                        tree.apply(id, index, value)
                    }
                    check(newVersion == versions.size)
                    versions.add(updated)
                }
                checks++
            }
            check(tree.versionCount == versions.size)
        }
    }
    val words = listOf("a", "bc", "d").persistentSegmentTree(
        AggregateOp("", { x: String, y: String -> x + y }))
    val v1 = words.set(0, 1, "!")
    val v2 = words.set(0, 0, "?")
    check(words.query(0, 0, 3) == "abcd")
    check(words.query(v1, 0, 3) == "a!d")
    check(words.query(v2, 0, 3) == "?bcd")
    val emptyHistory = intArrayOf().persistentSegmentTree(IntAggregations.MAX)
    check(emptyHistory.query(0, 0, 0) == Int.MIN_VALUE)
    fails { emptyHistory.all(1) }

    // General Fenwick range queries need invertibility (SUM and XOR).
    repeat(750) {
        val a = IntArray(random.nextInt(0, 80)) { random.nextInt(-100, 101) }
        for (op in listOf(IntAggregations.SUM, IntAggregations.XOR)) {
            val tree = a.fenwickTree(op)
            val copy = a.copyOf()
            repeat(30) {
                if (copy.isNotEmpty() && random.nextBoolean()) {
                    val i = random.nextInt(copy.size)
                    val value = random.nextInt(-50, 51)
                    if (random.nextBoolean()) {
                        tree.apply(i, value)
                        copy[i] = op.combine(copy[i], value)
                    } else {
                        tree.set(i, value)
                        copy[i] = value
                    }
                } else {
                    val l = random.nextInt(copy.size + 1)
                    val r = random.nextInt(l, copy.size + 1)
                    val expected = (l until r).fold(op.initial) { acc, i -> op.combine(acc, copy[i]) }
                    check(tree.query(l, r) == expected)
                }
                checks++
            }
        }
    }
    fails { intArrayOf(1, 2).fenwickTree(IntAggregations.MIN) }
    fails { intArrayOf(1, 2).fenwickTree(IntAggregations.OR) }
    fails { longArrayOf(1, 2).fenwickTree(LongAggregations.AND) }
    val mod7 = AbelianGroup(AggregateOp(0, { x: Int, y: Int -> (x + y) % 7 }),
        { x: Int -> (7 - x) % 7 })
    val customGroup = listOf(2, 3, 4).fenwickTree(mod7)
    check(customGroup.query(1, 3) == 0)
    customGroup.set(2, 5)
    check(customGroup.query(0, 3) == 3)
    val xorLong = longArrayOf(2, 4, 8).fenwickTree(LongAggregations.XOR)
    xorLong.apply(1, 4)
    check(xorLong.all() == 10L)

    // Both update composition orders matter: add then set and set then add.
    for (op in ops) {
        val modes = if (op === IntAggregations.SUM || op === IntAggregations.MIN || op === IntAggregations.MAX)
            listOf(RangeUpdates.ADD, RangeUpdates.ASSIGN, RangeUpdates.ADD_ASSIGN)
            else listOf(RangeUpdates.ASSIGN)
        for (mode in modes) repeat(300) {
            val a = IntArray(random.nextInt(0, 50)) { random.nextInt(-16, 17) }
            val tree = a.lazySegmentTree { aggregate = op; updates = mode }
            repeat(32) {
                val l = random.nextInt(a.size + 1)
                val r = random.nextInt(l, a.size + 1)
                when (random.nextInt(3)) {
                    0 -> if (mode != RangeUpdates.ASSIGN) {
                        val delta = random.nextInt(-7, 8)
                        tree.add(l, r, delta)
                        for (i in l until r) a[i] += delta
                    }
                    1 -> if (mode != RangeUpdates.ADD) {
                        val value = random.nextInt(-7, 8)
                        tree.set(l, r, value)
                        for (i in l until r) a[i] = value
                    }
                }
                val expected = (l until r).fold(op.initial) { acc, i -> op.combine(acc, a[i]) }
                check(tree.query(l, r) == expected)
                check(tree.all() == a.fold(op.initial, op.combine))
                checks++
            }
        }
    }
    val longMin = longArrayOf(2, -5, 9).lazySegmentTree {
        aggregate = LongAggregations.MIN
        updates = RangeUpdates.ADD_ASSIGN
    }
    longMin.add(0, 3, 7L)
    longMin.set(1, 2, 100L)
    check(longMin.query(0, 3) == 9L)
    val longXor = longArrayOf(1, 2, 3).lazySegmentTree {
        aggregate = LongAggregations.XOR
        updates = RangeUpdates.ASSIGN
    }
    longXor.set(0, 3, 7L)
    check(longXor.query(0, 3) == 7L)
    fails { intArrayOf(1, 2).lazySegmentTree {
        aggregate = IntAggregations.XOR; updates = RangeUpdates.ADD
    } }
    fails { intArrayOf(1, 2).lazySegmentTree {
        aggregate = IntAggregations.OR; updates = RangeUpdates.ADD_ASSIGN
    } }
    fails { intArrayOf(1).lazySegmentTree {
        updates = RangeUpdates.ADD
    }.set(0, 1, 5) }
    fails { intArrayOf(1).lazySegmentTree {
        updates = RangeUpdates.ASSIGN
    }.add(0, 1, 5) }

    // Custom range actions preserve order even for noncommutative operations.
    val custom = listOf("a", "b", "c").lazySegmentTree(
        AggregateOp("", { x: String, y: String -> x + y }),
        RangeAction<String, String>(
            apply = { _, text, length -> text.repeat(length) },
            compose = { _, newer -> newer }
        )
    )
    custom.update(0, 2, "!")
    custom.update(1, 3, "*")
    check(custom.query(0, 3) == "!**")
    check(custom.query(1, 1) == "")
    println("PASS: $checks randomized range queries/updates plus custom and invalid-mode checks")
}
