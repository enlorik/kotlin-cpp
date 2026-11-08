import kotlin.random.Random

/**
 * Reproducible *observations*, not a microbenchmark suite or a speed guarantee.
 * Run on your own machine, preferably with repetitions and JVM warm-up.
 */
fun main() {
    val n = 50_000
    val queryCount = 25_000
    val rnd = Random(401)
    val a = IntArray(n) { rnd.nextInt(-100, 101) }
    val range = List(queryCount) {
        val from = rnd.nextInt(n)
        from to rnd.nextInt(from + 1, n + 1)
    }
    val min = idempotentMonoid(Int.MAX_VALUE) { x: Int, y: Int -> minOf(x, y) }
    val sum = monoid(0) { x: Int, y: Int -> x + y }
    val sumGroup = aggregateOp(0) { x: Int, y: Int -> x + y }
        .withInverse { x -> -x }

    fun timed(label: String, block: () -> Long) {
        val started = System.nanoTime()
        val checksum = block()
        val ms = (System.nanoTime() - started) / 1_000_000.0
        println("$label: $ms ms; checksum=$checksum")
    }

    println("RangeIndex benchmark: n=$n, queries=$queryCount; seed=401")
    timed("static idempotent MIN / sparse table") {
        val tree = a.rangeIndex(min)
        var checksum = 0L
        for ((from, until) in range) checksum += tree.query(from, until)
        checksum
    }
    timed("static associative SUM / disjoint sparse table") {
        val tree = a.rangeIndex(sum)
        var checksum = 0L
        for ((from, until) in range) checksum += tree.query(from, until)
        checksum
    }
    timed("static SUM / group prefix table") {
        val prefix = a.prefixAggregates(sumGroup)
        var checksum = 0L
        for ((from, until) in range) checksum += prefix.query(from, until)
        checksum
    }
    timed("point-update SUM / segment tree") {
        val tree = a.rangeIndex(sum, mutable = true)
        var checksum = 0L
        for (i in range.indices) {
            val (from, until) = range[i]
            if ((i and 3) == 0) tree.set(from, i % 100)
            else checksum += tree.query(from, until)
        }
        checksum
    }
    timed("range-assign MIN / generic lazy tree") {
        val tree = a.lazySegmentTree(min)
        var checksum = 0L
        for (i in range.indices) {
            val (from, until) = range[i]
            if ((i and 3) == 0) tree.set(from, until, i % 100)
            else checksum += tree.query(from, until)
        }
        checksum
    }
    println("Single-run wall-clock figures include construction/JIT/GC; do not use as CI thresholds.")
}
