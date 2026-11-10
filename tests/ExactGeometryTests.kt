import java.math.BigInteger
import kotlin.random.Random

fun main() {
    val a = ExactPoint(Long.MIN_VALUE, Long.MIN_VALUE)
    val b = ExactPoint(Long.MAX_VALUE, Long.MIN_VALUE)
    val c = ExactPoint(Long.MIN_VALUE, Long.MAX_VALUE)
    check(orientationExact(a, b, c) > BigInteger.valueOf(Long.MAX_VALUE))
    check(distanceSquaredExact(a, b) > BigInteger.valueOf(Long.MAX_VALUE))
    check(pointOnSegmentExact(ExactPoint(0,0), ExactPoint(-1,-1),ExactPoint(1,1)))
    check(segmentsIntersectExact(
        ExactPoint(0,0),ExactPoint(2,2),ExactPoint(0,2),ExactPoint(2,0)))
    check(segmentsIntersectExact(
        ExactPoint(0,0),ExactPoint(2,0),ExactPoint(2,0),ExactPoint(3,0)))
    check(!segmentsIntersectExact(
        ExactPoint(0,0),ExactPoint(1,0),ExactPoint(2,0),ExactPoint(3,0)))

    check(convexHullExact(emptyList()) == emptyList<ExactPoint>())
    check(convexHullExact(listOf(ExactPoint(1,1),ExactPoint(1,1))) ==
        listOf(ExactPoint(1,1)))
    check(convexHullExact(listOf(ExactPoint(0,0),ExactPoint(1,1),ExactPoint(2,2))) ==
        listOf(ExactPoint(0,0),ExactPoint(2,2)))
    val square = listOf(
        ExactPoint(0,0),ExactPoint(2,0),ExactPoint(2,2),ExactPoint(0,2))
    check(polygonTwiceAreaExact(square) == BigInteger.valueOf(8))
    check(polygonTwiceAreaExact(square.reversed()) == BigInteger.valueOf(-8))
    check(locateInPolygonExact(ExactPoint(1,1),square) == PointInPolygon.INSIDE)
    check(locateInPolygonExact(ExactPoint(0,1),square) == PointInPolygon.BOUNDARY)
    check(locateInPolygonExact(ExactPoint(3,1),square) == PointInPolygon.OUTSIDE)
    check(convexDiameterSquaredExact(square) == BigInteger.valueOf(8))
    check(closestPairSquaredExact(square) == BigInteger.valueOf(4))
    check(closestPairSquaredExact(listOf(ExactPoint(1,1))) == null)
    check(closestPairSquaredExact(listOf(ExactPoint(1,1),ExactPoint(1,1))) ==
        BigInteger.ZERO)

    val random = Random(20261110)
    var comparisons = 0
    repeat(1000) {
        val n = random.nextInt(0,33)
        val points = List(n) {
            ExactPoint(random.nextLong(-30,31),random.nextLong(-30,31))
        }
        var nearest: BigInteger? = null
        var furthest = BigInteger.ZERO
        for (i in points.indices) for (j in i + 1 until n) {
            val d = distanceSquaredExact(points[i],points[j])
            nearest = if (nearest == null) d else nearest.min(d)
            furthest = furthest.max(d)
        }
        check(closestPairSquaredExact(points) == nearest)
        check(convexDiameterSquaredExact(points) == furthest)

        val hull = convexHullExact(points)
        check(hull.size == hull.toSet().size)
        if (hull.size >= 3) {
            for (i in hull.indices) {
                val x = hull[i]
                val y = hull[(i+1)%hull.size]
                val z = hull[(i+2)%hull.size]
                check(orientationExact(x,y,z).signum() > 0)
                for (p in points) check(orientationExact(x,y,p).signum() >= 0)
            }
        } else if (hull.size == 2) {
            for (p in points) check(pointOnSegmentExact(p,hull[0],hull[1]))
        }
        comparisons++
    }

    repeat(2000) {
        val x0 = random.nextLong(-25,25)
        val x1 = x0 + random.nextLong(1,25)
        val y0 = random.nextLong(-25,25)
        val y1 = y0 + random.nextLong(1,25)
        val rect = listOf(
            ExactPoint(x0,y0),ExactPoint(x1,y0),ExactPoint(x1,y1),ExactPoint(x0,y1)
        )
        val p = ExactPoint(random.nextLong(-50,50),random.nextLong(-50,50))
        val inside = p.x in x0..x1 && p.y in y0..y1
        val border = inside &&
            (p.x == x0 || p.x == x1 || p.y == y0 || p.y == y1)
        val expected = when {
            border -> PointInPolygon.BOUNDARY
            inside -> PointInPolygon.INSIDE
            else -> PointInPolygon.OUTSIDE
        }
        check(locateInPolygonExact(p,rect) == expected)
        check(locateInPolygonExact(p,rect.reversed()) == expected)
        comparisons++
    }
    println("PASS: exact geometry ($comparisons random oracle cases)")
}
