import java.math.BigInteger

/** Minkowski sum of convex hulls of arbitrary point collections.
 * Hull sorting O(n log n + m log m); convex edge merge O(h1+h2).
 * Output is CCW without intermediate collinear points, starting at the
 * lexicographically smallest point, as in convexHullExact.
 * Coordinates outside signed Long throw ArithmeticException; vector
 * differences and cross products are exact BigInteger operations.
 */
fun minkowskiSumExact(a: Collection<ExactPoint>, b: Collection<ExactPoint>): List<ExactPoint> {
    if (a.isEmpty() || b.isEmpty()) return emptyList()
    val left = convexHullExact(a)
    val right = convexHullExact(b)
    fun rotate(h: List<ExactPoint>): List<ExactPoint> {
        val start = h.indices.minWithOrNull(compareBy<Int>({h[it].y},{h[it].x}))!!
        return List(h.size) { h[(start+it)%h.size] }
    }
    val p = rotate(left); val q = rotate(right)
    data class Edge(val x: BigInteger,val y: BigInteger)
    fun edges(h: List<ExactPoint>): List<Edge> {
        if (h.size == 1) return emptyList()
        return h.indices.map { i ->
            val u = h[i]; val v = h[(i+1)%h.size]
            Edge(BigInteger.valueOf(v.x)-BigInteger.valueOf(u.x),
                 BigInteger.valueOf(v.y)-BigInteger.valueOf(u.y))
        }
    }
    fun half(e: Edge): Int = if (e.y.signum()>0 || (e.y.signum()==0 && e.x.signum()>0)) 0 else 1
    fun angleCompare(u: Edge,v: Edge): Int {
        if (half(u) != half(v)) return half(u).compareTo(half(v))
        return -(u.x*v.y-u.y*v.x).signum()
    }
    val pe = edges(p); val qe = edges(q)
    var x = BigInteger.valueOf(p[0].x)+BigInteger.valueOf(q[0].x)
    var y = BigInteger.valueOf(p[0].y)+BigInteger.valueOf(q[0].y)
    val result = ArrayList<ExactPoint>()
    result.add(ExactPoint(x.longValueExact(),y.longValueExact()))
    var i = 0; var j = 0
    while (i < pe.size || j < qe.size) {
        val cmp = if (i == pe.size) 1 else if (j == qe.size) -1 else angleCompare(pe[i],qe[j])
        if (cmp <= 0) { x += pe[i].x; y += pe[i].y; i++ }
        if (cmp >= 0) { x += qe[j].x; y += qe[j].y; j++ }
        if (i < pe.size || j < qe.size) result.add(ExactPoint(x.longValueExact(),y.longValueExact()))
    }
    val start = result.indices.minWithOrNull(compareBy<Int>({result[it].x},{result[it].y}))!!
    return List(result.size) { result[(start+it)%result.size] }
}
