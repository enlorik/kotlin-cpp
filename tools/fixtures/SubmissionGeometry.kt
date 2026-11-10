fun main() {
    val points = listOf(ExactPoint(0,0),ExactPoint(2,0),ExactPoint(2,2),ExactPoint(0,2))
    check(convexHullExact(points).size == 4)
    check(convexDiameterSquaredExact(points).toString() == "8")
    check(closestPairSquaredExact(points).toString() == "4")
    check(locateInPolygonExact(ExactPoint(1,1),points) == PointInPolygon.INSIDE)
    println("standalone exact geometry OK")
}
