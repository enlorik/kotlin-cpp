import kotlin.random.Random

fun main() {
    val rng = Random(7145)
    repeat(300) {
        val moves = IntArray(rng.nextInt(0,6)) { rng.nextInt(1,9) }
        val game = PeriodicSubtractionGame(moves,2000)
        val brute = subtractionGrundy(2000,moves)
        for (i in brute.indices) check(game.grundy(i.toLong()) == brute[i])
        game.period?.let { p ->
            for (i in p.start until brute.size-p.length) check(brute[i] == brute[i+p.length])
        }
        val piles = LongArray(rng.nextInt(0,15)) { rng.nextLong(0,Long.MAX_VALUE) }
        val move = winningNimMove(piles)
        if (move == null) check(nimSum64(piles) == 0L) else {
            check(move.remaining < piles[move.pile])
            piles[move.pile] = move.remaining
            check(nimSum64(piles) == 0L)
        }
    }
    val parity = PeriodicSubtractionGame(intArrayOf(1))
    check(parity.grundy(Long.MAX_VALUE) == 1)
    check(parity.winningMove(Long.MAX_VALUE) == 1)
    check(PeriodicSubtractionGame(intArrayOf()).grundy(Long.MAX_VALUE) == 0)
    val bounded = PeriodicSubtractionGame(intArrayOf(20),3)
    check(bounded.period == null && runCatching { bounded.grundy(100) }.isFailure)
    val normal = ImpartialNormalPlay(5) { if (it == 0) emptyList() else listOf(it-1) }
    check(normal.winning(3) && normal.disjointSum(listOf(1,3)) == 0)
    repeat(600) {
        fun points() = List(rng.nextInt(0,15)) { ExactPoint(rng.nextLong(-10,11),rng.nextLong(-10,11)) }
        val a = points(); val b = points()
        val expected = convexHullExact(a.flatMap { p -> b.map { q -> ExactPoint(p.x+q.x,p.y+q.y) } })
        check(minkowskiSumExact(a,b) == expected) { "Minkowski mismatch: $a $b" }
        val doubles = a.map { DoublePoint(it.x.toDouble(),it.y.toDouble()) }
        check(convexHullDouble(doubles) == convexHullExact(a).map { DoublePoint(it.x.toDouble(),it.y.toDouble()) })
        val p = List(4) { ExactPoint(rng.nextLong(-10,11),rng.nextLong(-10,11)) }
        val q = p.map { DoublePoint(it.x.toDouble(),it.y.toDouble()) }
        check(orientationDouble(q[0],q[1],q[2]) == orientationExact(p[0],p[1],p[2]).signum())
        check(segmentsIntersectDouble(q[0],q[1],q[2],q[3]) == segmentsIntersectExact(p[0],p[1],p[2],p[3]))
    }
    check(runCatching { minkowskiSumExact(listOf(ExactPoint(Long.MAX_VALUE,0)),listOf(ExactPoint(1,0))) }.isFailure)
    check(minkowskiSumExact(listOf(ExactPoint(Long.MIN_VALUE,0),ExactPoint(Long.MAX_VALUE,0)),listOf(ExactPoint(0,0))).size == 2)
    check(orientationDouble(DoublePoint(0.0,0.0),DoublePoint(Double.MIN_VALUE,0.0),DoublePoint(0.0,Double.MIN_VALUE)) == 1)
    check(orientationDouble(DoublePoint(-Double.MAX_VALUE,0.0),DoublePoint(Double.MAX_VALUE,0.0),DoublePoint(0.0,1.0)) == 1)
    check(convexHullDouble(listOf(DoublePoint(-0.0,0.0),DoublePoint(0.0,-0.0))) == listOf(DoublePoint(0.0,0.0)))
    check(runCatching { DoublePoint(Double.NaN,0.0) }.isFailure)
    val line = (0..10000).map { ExactPoint(it.toLong(),0) }
    check(minkowskiSumExact(line,line) == listOf(ExactPoint(0,0),ExactPoint(20000,0)))
    println("PASS: periodic normal-play games and robust geometry")
}
