import java.math.BigInteger
import java.util.TreeMap
import kotlin.random.Random

/**
 * Deterministic primality for every nonnegative signed Long.
 * The 7 Miller-Rabin bases below are sufficient for all unsigned 64-bit
 * integers, hence also for signed positive Long values.
 */
fun isPrime64(n: Long): Boolean {
    if (n < 2L) return false
    val small = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)
    for (prime in small) {
        if (n == prime) return true
        if (n % prime == 0L) return false
    }
    var d = n - 1
    var s = 0
    while ((d and 1L) == 0L) {
        d = d shr 1
        s++
    }
    val modulus = BigInteger.valueOf(n)
    val nMinusOne = modulus.subtract(BigInteger.ONE)
    for (basis in longArrayOf(2, 325, 9375, 28178, 450775, 9780504, 1795265022)) {
        val residue = basis % n
        if (residue == 0L) continue
        var x = BigInteger.valueOf(residue).modPow(BigInteger.valueOf(d), modulus)
        if (x == BigInteger.ONE || x == nMinusOne) continue
        var foundMinusOne = false
        repeat(s - 1) {
            x = x.multiply(x).mod(modulus)
            if (x == nMinusOne) foundMinusOne = true
        }
        if (!foundMinusOne) return false
    }
    return true
}

/** Pollard-Rho (Brent batching) with exact modular multiplication. */
private fun brentDivisor(n: Long, random: Random): Long {
    if (n % 2L == 0L) return 2L
    if (n % 3L == 0L) return 3L
    val modulus = BigInteger.valueOf(n)
    val one = BigInteger.ONE
    while (true) {
        val c = BigInteger.valueOf(random.nextLong(1L, n))
        var y = BigInteger.valueOf(random.nextLong(1L, n))
        fun next(x: BigInteger): BigInteger = x.multiply(x).add(c).mod(modulus)
        var g = one
        var stride = 1
        var x = one
        var ys = one
        while (g == one && stride <= (1 shl 20)) {
            x = y
            repeat(stride) { y = next(y) }
            var k = 0
            var product = one
            while (k < stride && g == one) {
                ys = y
                val count = minOf(96, stride - k)
                repeat(count) {
                    y = next(y)
                    product = product.multiply(x.subtract(y).abs()).mod(modulus)
                }
                g = product.gcd(modulus)
                k += count
            }
            stride = stride shl 1
        }
        if (g == modulus) {
            do {
                ys = next(ys)
                g = x.subtract(ys).abs().gcd(modulus)
            } while (g == one)
        }
        if (g != one && g != modulus) return g.longValueExact()
        // Retry with a different polynomial if the chosen one stalls.
    }
}

/**
 * Factor positive signed Long into sorted prime powers. 1 -> empty map.
 * Probabilistic runtime (Pollard-Rho), but primality predicates are
 * deterministic; factors returned are individually proven prime by MR.
 */
fun factor64(n: Long, seed: Int = 20261110): Map<Long, Int> {
    require(n >= 1L) { "Factorization requires a positive integer" }
    val factors = TreeMap<Long, Int>()
    if (n == 1L) return factors
    val random = Random(seed)
    val pending = ArrayDeque<Long>()
    pending.addLast(n)
    while (pending.isNotEmpty()) {
        val current = pending.removeLast()
        if (current == 1L) continue
        if (isPrime64(current)) {
            factors[current] = (factors[current] ?: 0) + 1
            continue
        }
        val divisor = brentDivisor(current, random)
        check(divisor in 2 until current && current % divisor == 0L)
        pending.addLast(divisor)
        pending.addLast(current / divisor)
    }
    return factors
}

fun eulerTotient64(n: Long): Long {
    require(n >= 1L)
    var result = n
    for (prime in factor64(n).keys) {
        result = result / prime * (prime - 1)
    }
    return result
}
