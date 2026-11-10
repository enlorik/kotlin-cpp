import kotlin.random.Random
import java.math.BigInteger

fun main() {
    val min = Long.MIN_VALUE
    val max = Long.MAX_VALUE
    val a = LongPoint(min,min)
    val b = LongPoint(max,min)
    val c = LongPoint(min,max)
    check(orientation(a,b,c) == 1)
    check(orientation(a,c,b) == -1)
    check(orientation(a,b,b) == 0)
    check(squaredDistance(a,b) == BigInteger.valueOf(max).subtract(
        BigInteger.valueOf(min)).pow(2))
    check(closestPairSquared(emptyList()) == null)
    check(closestPairSquared(listOf(LongPoint(1,2))) == null)
    check(diameterSquared(emptyList()) == BigInteger.ZERO)
    val square = listOf(LongPoint(0,0),LongPoint(3,0),
        LongPoint(3,3),LongPoint(0,3),LongPoint(1,1))
    val hull = convexHull(square)
    check(hull.size == 4)
    check(polygonAreaTwice(hull) == BigInteger.valueOf(18))
    check(pointInPolygon(LongPoint(2,1),hull))
    check(pointInPolygon(LongPoint(3,3),hull))
    check(!pointInPolygon(LongPoint(5,1),hull))
    check(segmentsIntersect(LongPoint(0,0),LongPoint(2,2),
        LongPoint(0,2),LongPoint(2,0)))
    check(segmentsIntersect(LongPoint(0,0),LongPoint(3,0),
        LongPoint(2,0),LongPoint(5,0)))
    check(!segmentsIntersect(LongPoint(0,0),LongPoint(1,0),
        LongPoint(2,0),LongPoint(3,0)))

    val rng = Random(20261110)
    var checks = 0
    repeat(600) {
        val n = rng.nextInt(0,38)
        val points = (0 until n).map {
            LongPoint(rng.nextLong(-200,201),rng.nextLong(-200,201))
        }
        var closest: BigInteger? = null
        var farthest = BigInteger.ZERO
        for (i in points.indices) for (j in i+1 until points.size) {
            val distance = squaredDistance(points[i],points[j])
            closest = if (closest == null) distance else minOf(closest,distance)
            farthest = maxOf(farthest,distance)
        }
        check(closestPairSquared(points) == closest)
        check(diameterSquared(points) == farthest)
        val polygon = convexHull(points)
        check(polygonAreaTwice(polygon) >= BigInteger.ZERO)
        if (polygon.size >= 3) for (point in points) {
            check(pointInPolygon(point,polygon))
            for (i in polygon.indices) {
                val next = polygon[(i+1)%polygon.size]
                check(orientation(polygon[i],next,point) >= 0)
            }
        }
        checks++
    }
    val extremePoints = listOf(a,b,c,LongPoint(max,max),LongPoint(0,0))
    check(diameterSquared(extremePoints) ==
        squaredDistance(a,LongPoint(max,max)))
    check(closestPairSquared(listOf(LongPoint(-3,3),LongPoint(-3,3),
        LongPoint(1,2))) == BigInteger.ZERO)
    println("PASS: exact integer geometry ($checks convex/diameter/closest oracles)")
}
