import java.math.BigInteger
import java.util.Arrays

/** Exact integer geometry: all determinants and squared distances use BigInteger. */
data class ExactPoint(val x: Long, val y: Long)

private fun integer(v: Long): BigInteger = BigInteger.valueOf(v)
private fun difference(a: Long, b: Long): BigInteger = integer(a).subtract(integer(b))
private fun squared(value: BigInteger): BigInteger = value.multiply(value)

/** Signed cross product of vectors (a -> b) and (a -> c). */
fun orientationExact(a: ExactPoint, b: ExactPoint, c: ExactPoint): BigInteger =
    difference(b.x, a.x).multiply(difference(c.y, a.y)).subtract(
        difference(b.y, a.y).multiply(difference(c.x, a.x))
    )

fun distanceSquaredExact(a: ExactPoint, b: ExactPoint): BigInteger =
    squared(difference(a.x, b.x)).add(squared(difference(a.y, b.y)))

fun pointOnSegmentExact(p: ExactPoint, a: ExactPoint, b: ExactPoint): Boolean =
    orientationExact(a, b, p).signum() == 0 &&
        p.x in minOf(a.x, b.x)..maxOf(a.x, b.x) &&
        p.y in minOf(a.y, b.y)..maxOf(a.y, b.y)

/** Segments include endpoints; touching and collinear overlap count as intersecting. */
fun segmentsIntersectExact(
    a: ExactPoint, b: ExactPoint, c: ExactPoint, d: ExactPoint
): Boolean {
    val abC = orientationExact(a, b, c).signum()
    val abD = orientationExact(a, b, d).signum()
    val cdA = orientationExact(c, d, a).signum()
    val cdB = orientationExact(c, d, b).signum()
    if (abC == 0 && pointOnSegmentExact(c, a, b)) return true
    if (abD == 0 && pointOnSegmentExact(d, a, b)) return true
    if (cdA == 0 && pointOnSegmentExact(a, c, d)) return true
    if (cdB == 0 && pointOnSegmentExact(b, c, d)) return true
    return abC * abD < 0 && cdA * cdB < 0
}

/**
 * Convex hull in counterclockwise order, excluding interior collinear points.
 * One unique point produces one hull vertex; fully collinear -> two endpoints.
 * O(n log n) sorting + O(n) orientation tests.
 */
fun convexHullExact(points: Collection<ExactPoint>): List<ExactPoint> {
    val sorted = points.distinct().sortedWith(compareBy<ExactPoint>({ it.x }, { it.y }))
    if (sorted.size < 3) return sorted
    val lower = ArrayList<ExactPoint>()
    for (point in sorted) {
        while (lower.size >= 2 && orientationExact(
                lower[lower.size - 2], lower.last(), point
            ).signum() <= 0) lower.removeAt(lower.lastIndex)
        lower.add(point)
    }
    val upper = ArrayList<ExactPoint>()
    for (point in sorted.asReversed()) {
        while (upper.size >= 2 && orientationExact(
                upper[upper.size - 2], upper.last(), point
            ).signum() <= 0) upper.removeAt(upper.lastIndex)
        upper.add(point)
    }
    lower.removeAt(lower.lastIndex)
    upper.removeAt(upper.lastIndex)
    return lower + upper
}

/** Shoelace sum: twice signed polygon area, CCW positive. */
fun polygonTwiceAreaExact(polygon: List<ExactPoint>): BigInteger {
    if (polygon.size < 3) return BigInteger.ZERO
    var twice = BigInteger.ZERO
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        twice = twice.add(integer(a.x).multiply(integer(b.y)).subtract(
            integer(a.y).multiply(integer(b.x))
        ))
    }
    return twice
}

enum class PointInPolygon { INSIDE, OUTSIDE, BOUNDARY }

/** Exact ray-crossing for a simple polygon, either winding direction. */
fun locateInPolygonExact(point: ExactPoint, polygon: List<ExactPoint>): PointInPolygon {
    if (polygon.isEmpty()) return PointInPolygon.OUTSIDE
    if (polygon.size == 1) return if (point == polygon[0]) PointInPolygon.BOUNDARY
        else PointInPolygon.OUTSIDE
    var inside = false
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        if (pointOnSegmentExact(point, a, b)) return PointInPolygon.BOUNDARY
        if ((a.y > point.y) != (b.y > point.y)) {
            val side = orientationExact(a, b, point).signum()
            if ((b.y > a.y && side > 0) || (b.y < a.y && side < 0))
                inside = !inside
        }
    }
    return if (inside) PointInPolygon.INSIDE else PointInPolygon.OUTSIDE
}

/** Rotating calipers; returns the maximum pairwise squared distance. */
fun convexDiameterSquaredExact(points: Collection<ExactPoint>): BigInteger {
    val hull = convexHullExact(points)
    val n = hull.size
    if (n < 2) return BigInteger.ZERO
    if (n == 2) return distanceSquaredExact(hull[0], hull[1])
    var furthest = BigInteger.ZERO
    var j = 1
    for (i in 0 until n) {
        val next = (i + 1) % n
        while (orientationExact(hull[i], hull[next], hull[(j + 1) % n]).abs() >
            orientationExact(hull[i], hull[next], hull[j]).abs()) {
            j = (j + 1) % n
        }
        furthest = furthest.max(distanceSquaredExact(hull[i], hull[j]))
        furthest = furthest.max(distanceSquaredExact(hull[next], hull[j]))
    }
    return furthest
}

/**
 * Standard closest-pair divide-and-conquer with in-place y merges.
 * O(n log n) comparisons and O(n) reference storage. Null for <2 points.
 * Duplicate points return distance squared zero.
 */
fun closestPairSquaredExact(input: Collection<ExactPoint>): BigInteger? {
    if (input.size < 2) return null
    val points = input.sortedWith(compareBy<ExactPoint>({ it.x }, { it.y })).toTypedArray()
    val buffer = arrayOfNulls<ExactPoint>(points.size)
    val strip = arrayOfNulls<ExactPoint>(points.size)
    val byY = compareBy<ExactPoint>({ it.y }, { it.x })

    fun better(x: BigInteger?, y: BigInteger?): BigInteger? = when {
        x == null -> y
        y == null -> x
        else -> x.min(y)
    }

    fun solve(from: Int, until: Int): BigInteger? {
        if (until - from <= 3) {
            var best: BigInteger? = null
            for (i in from until until) for (j in i + 1 until until)
                best = better(best, distanceSquaredExact(points[i], points[j]))
            Arrays.sort(points, from, until, byY)
            return best
        }
        val mid = (from + until) / 2
        val midX = points[mid].x
        var best = better(solve(from, mid), solve(mid, until))!!
        var left = from
        var right = mid
        var out = from
        while (left < mid && right < until)
            buffer[out++] = if (byY.compare(points[left], points[right]) <= 0)
                points[left++] else points[right++]
        while (left < mid) buffer[out++] = points[left++]
        while (right < until) buffer[out++] = points[right++]
        for (i in from until until) points[i] = buffer[i]!!

        var used = 0
        for (i in from until until) {
            val point = points[i]
            if (squared(difference(point.x, midX)) >= best) continue
            var j = used - 1
            while (j >= 0) {
                val previous = strip[j]!!
                if (squared(difference(point.y, previous.y)) >= best) break
                best = best.min(distanceSquaredExact(point, previous))
                j--
            }
            strip[used++] = point
        }
        return best
    }
    return solve(0, points.size)
}
