import java.math.BigInteger
import kotlin.random.Random
import kotlin.math.abs

private fun bruteFactors(n: Long): Map<Long,Int> {
    var value = n
    val result = sortedMapOf<Long,Int>()
    var p = 2L
    while (p <= value / p) {
        while (value % p == 0L) {
            result[p] = (result[p] ?: 0) + 1
            value /= p
        }
        p++
    }
    if (value > 1) result[value] = (result[value] ?: 0) + 1
    return result
}

fun main() {
    check(!isPrime64(-7) && !isPrime64(0) && !isPrime64(1))
    check(isPrime64(2) && isPrime64(3) && isPrime64(2147483647L))
    check(!isPrime64(Long.MAX_VALUE))
    for (n in 1L..20000L) check(isPrime64(n) ==
        (n >= 2 && (2L..(kotlin.math.sqrt(n.toDouble()).toLong()))
            .none { n % it == 0L }))
    check(factorize64(1).isEmpty())
    check(factorize64(1L shl 60) == mapOf(2L to 60))
    check(factorize64(1000003L*1000033L) == mapOf(
        1000003L to 1, 1000033L to 1))
    check(eulerTotient(1L) == 1L)
    check(eulerTotient(36L) == 12L)
    check(inverseMod(3,11) == 4L)
    check(powerMod(-2,5,13) == 7L)
    check(runCatching { inverseMod(6,15) }.isFailure)
    check(runCatching { factorize64(0) }.isFailure)

    val rng = Random(871293)
    repeat(320) {
        val n = rng.nextLong(1,1_000_000)
        check(factorize64(n) == bruteFactors(n))
        var product = BigInteger.ONE
        for ((p,degree) in factorize64(n)) {
            check(isPrime64(p))
            product = product.multiply(BigInteger.valueOf(p).pow(degree))
        }
        check(product == BigInteger.valueOf(n))
        val modulus = rng.nextLong(1,Long.MAX_VALUE)
        val x = rng.nextLong()
        val y = rng.nextLong()
        val expected = BigInteger.valueOf(x)
            .multiply(BigInteger.valueOf(y))
            .mod(BigInteger.valueOf(modulus)).toLong()
        check(multiplyMod(x,y,modulus) == expected)
        check(normalizeMod(x,modulus) == BigInteger.valueOf(x)
            .mod(BigInteger.valueOf(modulus)).toLong())
    }
    println("PASS: deterministic primality and Pollard-Rho factorization")
}
