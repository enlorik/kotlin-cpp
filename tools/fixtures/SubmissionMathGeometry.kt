fun main() {
    val triangle = listOf(LongPoint(0,0),LongPoint(3,0),LongPoint(0,4))
    check(convexHull(triangle).size == 3)
    check(diameterSquared(triangle).toLong() == 25L)
    check(closestPairSquared(triangle)!!.toLong() == 9L)
    check(segmentsIntersect(LongPoint(0,0),LongPoint(2,2),
        LongPoint(0,2),LongPoint(2,0)))
    check(isPrime64(97))
    check(factorize64(360) == mapOf(2L to 3, 3L to 2, 5L to 1))
    check(powerMod(2,10,1000) == 24L)
    check(convolutionNtt(intArrayOf(1,2),intArrayOf(3,4))
        .contentEquals(intArrayOf(3,10,8)))
    println("standalone exact geometry and number theory OK")
}
