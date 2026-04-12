data class Point(val x: Long, val y: Long) {
    operator fun plus(o: Point) = Point(x + o.x, y + o.y)
    operator fun minus(o: Point) = Point(x - o.x, y - o.y)
    fun cross(o: Point) = x * o.y - y * o.x
    fun dot(o: Point) = x * o.x + y * o.y
    fun norm() = x * x + y * y
}

fun cross(a: Point, b: Point, c: Point) = (b - a).cross(c - a)

fun convexHull(points: List<Point>): List<Point> {
    val pts = points.sortedWith(compareBy({ it.x }, { it.y }))
    val n = pts.size
    if (n < 2) return pts
    val hull = mutableListOf<Point>()
    for (p in pts) {
        while (hull.size >= 2 && cross(hull[hull.size - 2], hull[hull.size - 1], p) <= 0) hull.removeLast()
        hull.add(p)
    }
    val lower = hull.size + 1
    for (i in n - 2 downTo 0) {
        while (hull.size >= lower && cross(hull[hull.size - 2], hull[hull.size - 1], pts[i]) <= 0) hull.removeLast()
        hull.add(pts[i])
    }
    hull.removeLast()
    return hull
}

fun polygonArea2(poly: List<Point>): Long {
    val n = poly.size
    var area = 0L
    for (i in poly.indices) area += poly[i].cross(poly[(i + 1) % n])
    return kotlin.math.abs(area)
}

fun inConvexPolygon(poly: List<Point>, p: Point): Boolean {
    val n = poly.size
    if (n == 0) return false
    if (n == 1) return poly[0] == p
    if (cross(poly[0], poly[1], p) < 0 || cross(poly[0], poly[n - 1], p) > 0) return false
    var lo = 1; var hi = n - 1
    while (hi - lo > 1) {
        val mid = (lo + hi) / 2
        if (cross(poly[0], poly[mid], p) >= 0) lo = mid else hi = mid
    }
    return cross(poly[lo], poly[hi], p) >= 0
}

fun segmentsIntersect(a: Point, b: Point, c: Point, d: Point): Boolean {
    val d1 = cross(c, d, a); val d2 = cross(c, d, b)
    val d3 = cross(a, b, c); val d4 = cross(a, b, d)
    if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) return true
    fun onSeg(p: Point, q: Point, r: Point): Boolean {
        return minOf(p.x, q.x) <= r.x && r.x <= maxOf(p.x, q.x) &&
               minOf(p.y, q.y) <= r.y && r.y <= maxOf(p.y, q.y)
    }
    if (d1 == 0L && onSeg(c, d, a)) return true
    if (d2 == 0L && onSeg(c, d, b)) return true
    if (d3 == 0L && onSeg(a, b, c)) return true
    if (d4 == 0L && onSeg(a, b, d)) return true
    return false
}
