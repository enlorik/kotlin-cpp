import java.math.BigInteger
import java.util.Arrays

/** Integer Cartesian point; predicates never rely on overflowing Long cross products. */
data class LongPoint(val x: Long, val y: Long)

private fun big(value: Long): BigInteger = BigInteger.valueOf(value)
private fun diff(a: Long, b: Long): BigInteger = big(a).subtract(big(b))

/** Exact signed cross product (b-a) x (c-a). */
fun cross(a: LongPoint, b: LongPoint, c: LongPoint): BigInteger =
    diff(b.x, a.x).multiply(diff(c.y, a.y))
        .subtract(diff(b.y, a.y).multiply(diff(c.x, a.x)))

/** +1: counterclockwise, 0: collinear, -1: clockwise. */
fun orientation(a: LongPoint, b: LongPoint, c: LongPoint): Int =
    cross(a, b, c).signum()

fun squaredDistance(a: LongPoint, b: LongPoint): BigInteger {
    val dx = diff(a.x, b.x)
    val dy = diff(a.y, b.y)
    return dx.multiply(dx).add(dy.multiply(dy))
}

fun pointOnSegment(a: LongPoint, b: LongPoint, point: LongPoint): Boolean =
    orientation(a, b, point) == 0 &&
        point.x >= minOf(a.x, b.x) && point.x <= maxOf(a.x, b.x) &&
        point.y >= minOf(a.y, b.y) && point.y <= maxOf(a.y, b.y)

/** Boundary and collinear-overlap cases count as intersections. */
fun segmentsIntersect(
    a: LongPoint, b: LongPoint, c: LongPoint, d: LongPoint
): Boolean {
    val abC = orientation(a, b, c)
    val abD = orientation(a, b, d)
    val cdA = orientation(c, d, a)
    val cdB = orientation(c, d, b)
    if (abC == 0 && pointOnSegment(a, b, c)) return true
    if (abD == 0 && pointOnSegment(a, b, d)) return true
    if (cdA == 0 && pointOnSegment(c, d, a)) return true
    if (cdB == 0 && pointOnSegment(c, d, b)) return true
    return abC * abD < 0 && cdA * cdB < 0
}

/**
 * Strict convex hull in CCW order, no repeated endpoint and no collinear
 * interior boundary vertices. Duplicates are removed. O(n log n).
 */
fun convexHull(points: List<LongPoint>): List<LongPoint> {
    val sorted = points.distinct().sortedWith(compareBy<LongPoint> { it.x }.thenBy { it.y })
    if (sorted.size <= 2) return sorted
    val lower = ArrayList<LongPoint>()
    for (p in sorted) {
        while (lower.size >= 2 &&
            orientation(lower[lower.size - 2], lower.last(), p) <= 0) {
            lower.removeAt(lower.lastIndex)
        }
        lower.add(p)
    }
    val upper = ArrayList<LongPoint>()
    for (p in sorted.asReversed()) {
        while (upper.size >= 2 &&
            orientation(upper[upper.size - 2], upper.last(), p) <= 0) {
            upper.removeAt(upper.lastIndex)
        }
        upper.add(p)
    }
    lower.removeAt(lower.lastIndex)
    upper.removeAt(upper.lastIndex)
    return lower + upper
}

/** Signed double area via exact shoelace formula. */
fun polygonAreaTwice(polygon: List<LongPoint>): BigInteger {
    if (polygon.size < 3) return BigInteger.ZERO
    var total = BigInteger.ZERO
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        total = total.add(big(a.x).multiply(big(b.y)))
            .subtract(big(a.y).multiply(big(b.x)))
    }
    return total
}

/** Inclusive boundary, even-odd rule for a simple polygon. */
fun pointInPolygon(point: LongPoint, polygon: List<LongPoint>): Boolean {
    if (polygon.isEmpty()) return false
    var inside = false
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        if (pointOnSegment(a, b, point)) return true
        val upward = a.y <= point.y && point.y < b.y
        val downward = b.y <= point.y && point.y < a.y
        if (upward && orientation(a, b, point) > 0) inside = !inside
        if (downward && orientation(a, b, point) < 0) inside = !inside
    }
    return inside
}

/** Rotating-calipers convex diameter, exact squared distance. O(n log n). */
fun diameterSquared(points: List<LongPoint>): BigInteger {
    val hull = convexHull(points)
    if (hull.size < 2) return BigInteger.ZERO
    if (hull.size == 2) return squaredDistance(hull[0], hull[1])
    val n = hull.size
    var j = 1
    var best = BigInteger.ZERO
    for (i in 0 until n) {
        val next = (i + 1) % n
        while (true) {
            val after = (j + 1) % n
            val beforeArea = cross(hull[i], hull[next], hull[j]).abs()
            val afterArea = cross(hull[i], hull[next], hull[after]).abs()
            if (afterArea <= beforeArea) break
            j = after
        }
        best = maxOf(best, squaredDistance(hull[i], hull[j]))
        best = maxOf(best, squaredDistance(hull[next], hull[j]))
    }
    return best
}

/**
 * Divide-and-conquer closest pair, O(n log n) comparisons.
 * Returns null if fewer than two points. Exact arbitrary-precision
 * squared distances support even extreme Long coordinates.
 */
fun closestPairSquared(input: List<LongPoint>): BigInteger? {
    if (input.size < 2) return null
    val points = input.sortedWith(compareBy<LongPoint> { it.x }.thenBy { it.y })
        .toTypedArray()
    val buffer = arrayOfNulls<LongPoint>(points.size)
    val byY = compareBy<LongPoint> { it.y }.thenBy { it.x }

    fun better(a: BigInteger?, b: BigInteger?): BigInteger? = when {
        a == null -> b
        b == null -> a
        else -> minOf(a, b)
    }

    fun solve(start: Int, end: Int): BigInteger? {
        if (end - start <= 3) {
            var best: BigInteger? = null
            for (i in start until end)
                for (j in i + 1 until end)
                    best = better(best, squaredDistance(points[i], points[j]))
            Arrays.sort(points, start, end, byY)
            return best
        }
        val mid = (start + end) / 2
        val midX = points[mid].x
        var best = better(solve(start, mid), solve(mid, end))
        var l = start
        var r = mid
        var p = start
        while (l < mid && r < end) {
            buffer[p++] = if (byY.compare(points[l], points[r]) <= 0)
                points[l++] else points[r++]
        }
        while (l < mid) buffer[p++] = points[l++]
        while (r < end) buffer[p++] = points[r++]
        for (i in start until end) points[i] = buffer[i]!!

        val strip = ArrayList<LongPoint>()
        for (i in start until end) {
            val point = points[i]
            val dx = diff(point.x, midX)
            if (best != null && dx.multiply(dx) >= best) continue
            for (j in strip.lastIndex downTo 0) {
                val other = strip[j]
                val dy = diff(point.y, other.y)
                if (best != null && dy.multiply(dy) >= best) break
                best = better(best, squaredDistance(point, other))
            }
            strip.add(point)
        }
        return best
    }
    return solve(0, points.size)
}
