fun main() {
    val game = PeriodicSubtractionGame(intArrayOf(1,2))
    check(game.grundy(1000000000000L) == 1)
    check(winningNimMove(longArrayOf(1,2)) == NimMove(1,1))
    val a = listOf(ExactPoint(0,0),ExactPoint(1,0))
    check(minkowskiSumExact(a,a) == listOf(ExactPoint(0,0),ExactPoint(2,0)))
    check(orientationDouble(DoublePoint(0.0,0.0),DoublePoint(1.0,0.0),DoublePoint(0.0,1.0)) == 1)
}
