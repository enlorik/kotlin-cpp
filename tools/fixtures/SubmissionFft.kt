fun main() {
    val c = convolutionFft(doubleArrayOf(1.0,2.0),doubleArrayOf(3.0,4.0))
    check(c.size == 3 && kotlin.math.abs(c[1]-10.0)<1e-8)
}
