# Exact integer geometry

`ExactPoint(x: Long,y: Long)` stores signed integer coordinates.
All determinants, squared lengths and polygon areas use `BigInteger`
to avoid overflow, including differences of extreme 64-bit coordinates.

```kotlin
val points = listOf(
    ExactPoint(0, 0), ExactPoint(2, 0),
    ExactPoint(2, 2), ExactPoint(0, 2)
)
val hull = convexHullExact(points)
check(convexDiameterSquaredExact(points).toString() == "8")
check(closestPairSquaredExact(points).toString() == "4")
check(locateInPolygonExact(ExactPoint(1, 1),hull) == PointInPolygon.INSIDE)
```

Available operations:

- `orientationExact(a,b,c)`: signed cross product, positive for counterclockwise.
- `pointOnSegmentExact(p,a,b)`: boundary-inclusive segment membership.
- `segmentsIntersectExact(a,b,c,d)`: includes touching and collinear overlap.
- `convexHullExact(points)`: CCW hull, excluding interior collinear points; O(n log n).
- `polygonTwiceAreaExact(polygon)`: exact signed twice-area (CCW positive).
- `locateInPolygonExact(p,polygon)`: inside, outside or boundary for a simple polygon.
- `convexDiameterSquaredExact(points)`: exact largest pair squared distance using rotating calipers after hull; O(n log n) total.
- `closestPairSquaredExact(points)`: exact smallest pair squared distance via merge-sort divide-and-conquer; O(n log n), null for fewer than two points.

Important domain boundaries: polygon containment assumes a **simple**
(non-self-intersecting) polygon; convex hull returns a two-endpoint
segment when all input points are collinear. The closest-pair and diameter
functions return squared distances because square roots need floating-point
approximation and should be handled by the contest solution.

```bash
python3 tools/build_submission.py --modules exact-geometry \
  --solution Main.kt --output submission.kt
```

Random tests compare diameter and closest pair against quadratic brute
force, hull orientations against all points, and polygon inclusion against
axis-aligned rectangle ground truth. The standalone source does not depend
on graph or range-index libraries.
