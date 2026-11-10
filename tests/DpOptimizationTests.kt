import kotlin.random.Random

fun main() {
    val infinity = Long.MAX_VALUE / 4
    check(divideConquerPartitionRow(longArrayOf(0L)) { _, _ -> 0L }.values.contentEquals(
        longArrayOf(infinity)))
    check(runCatching { divideConquerPartitionRow(longArrayOf()) { _, _ -> 0L } }
        .exceptionOrNull() is IllegalArgumentException)
    val hull = MonotoneMinHull()
    check(hull.query(3) == null)
    hull.add(4, 0)
    hull.add(3, 2)
    hull.add(2, -4)
    check(hull.query(0) == -4L)
    check(hull.query(-100) == -400L)
    check(runCatching { hull.add(5,0) }.exceptionOrNull() is IllegalArgumentException)

    val rng = Random(920311)
    var checks = 0
    repeat(400) {
        val n = rng.nextInt(1, 42)
        val weights = IntArray(n) { rng.nextInt(1, 10) }
        val prefix = LongArray(n+1)
        for (i in 0 until n) prefix[i+1] = prefix[i]+weights[i]

        val prev = LongArray(n+1) { j ->
            if (j == 0) infinity else prefix[j]*prefix[j]
        }
        val cost = { i: Int, j: Int ->
            val sum = prefix[j] - prefix[i]
            sum*sum
        }
        val answer = divideConquerPartitionRow(prev, cost = cost)
        var previousArg = -1
        for (j in 1..n) {
            var brute = infinity
            var arg = -1
            for (i in 0 until j) {
                if (prev[i] == infinity) continue
                val cand = prev[i] + cost(i,j)
                if (cand < brute) { brute = cand; arg = i }
            }
            check(answer.values[j] == brute)
            check(answer.split[j] == arg)
            if (arg >= 0) {
                check(arg >= previousArg)
                previousArg = arg
            }
            checks++
        }

        val convex = MonotoneMinHull()
        val inserted = ArrayList<HullLine>()
        var slope = rng.nextLong(-10, 100)
        repeat(100) {
            slope -= rng.nextLong(0, 4)
            val line = HullLine(slope, rng.nextLong(-100, 101))
            convex.add(line.slope,line.intercept)
            inserted.add(line)
            repeat(6) {
                val x = rng.nextLong(-200, 201)
                check(convex.query(x) == inserted.minOf { it.at(x) })
                checks++
            }
        }
    }
    println("PASS: divide-conquer monotone DP and convex hull trick ($checks oracle comparisons)")
}
