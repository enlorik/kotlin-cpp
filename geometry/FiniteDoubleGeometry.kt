import java.math.BigDecimal

/** Finite IEEE-754 coordinates. Predicates interpret the stored binary
 * values exactly, not as the ideal real values before input rounding.
 */
data class DoublePoint(val x: Double,val y: Double) {
    init { require(x.isFinite() && y.isFinite()) }
}

/** Exact orientation sign for finite doubles, including extreme magnitudes.
 * No epsilon; BigDecimal(Double) preserves the actual binary value.
 * Exact arithmetic is more expensive than a naive floating determinant.
 */
fun orientationDouble(a: DoublePoint,b: DoublePoint,c: DoublePoint): Int {
    val ax = BigDecimal(a.x); val ay = BigDecimal(a.y)
    val bx = BigDecimal(b.x); val by = BigDecimal(b.y)
    val cx = BigDecimal(c.x); val cy = BigDecimal(c.y)
    return ((bx-ax)*(cy-ay)-(by-ay)*(cx-ax)).signum()
}
fun pointOnSegmentDouble(p: DoublePoint,a: DoublePoint,b: DoublePoint): Boolean =
    orientationDouble(a,b,p) == 0 &&
        p.x >= minOf(a.x,b.x) && p.x <= maxOf(a.x,b.x) &&
        p.y >= minOf(a.y,b.y) && p.y <= maxOf(a.y,b.y)

/** Closed segments, exact intersection predicate on represented inputs. */
fun segmentsIntersectDouble(a: DoublePoint,b: DoublePoint,c: DoublePoint,d: DoublePoint): Boolean {
    val s = orientationDouble(a,b,c); val t = orientationDouble(a,b,d)
    val u = orientationDouble(c,d,a); val v = orientationDouble(c,d,b)
    return (s == 0 && pointOnSegmentDouble(c,a,b)) ||
        (t == 0 && pointOnSegmentDouble(d,a,b)) ||
        (u == 0 && pointOnSegmentDouble(a,c,d)) ||
        (v == 0 && pointOnSegmentDouble(b,c,d)) ||
        (s*t < 0 && u*v < 0)
}

/** Exact-predicate convex hull, O(n log n) comparisons and O(n) predicates.
 * Canonicalizes negative zero; no approximate construction or snapping.
 */
fun convexHullDouble(points: Collection<DoublePoint>): List<DoublePoint> {
    val sorted = points.map { DoublePoint(if (it.x == 0.0) 0.0 else it.x,
        if (it.y == 0.0) 0.0 else it.y) }.distinct()
        .sortedWith(compareBy<DoublePoint>({it.x},{it.y}))
    if (sorted.size < 3) return sorted
    fun chain(input: List<DoublePoint>): ArrayList<DoublePoint> {
        val result = ArrayList<DoublePoint>()
        for (p in input) {
            while (result.size >= 2 && orientationDouble(result[result.size-2],result.last(),p) <= 0)
                result.removeAt(result.lastIndex)
            result.add(p)
        }
        result.removeAt(result.lastIndex)
        return result
    }
    return chain(sorted)+chain(sorted.asReversed())
}
