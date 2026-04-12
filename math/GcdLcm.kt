fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
fun lcm(a: Int, b: Int): Long = a.toLong() / gcd(a, b) * b.toLong()

/** Returns (gcd, x, y) such that a*x + b*y = gcd(a, b). */
fun extGcd(a: Long, b: Long): Triple<Long, Long, Long> {
    if (b == 0L) return Triple(a, 1L, 0L)
    val (g, x1, y1) = extGcd(b, a % b)
    return Triple(g, y1, x1 - (a / b) * y1)
}
