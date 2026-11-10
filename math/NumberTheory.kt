import java.math.BigInteger

/**
 * Signed-64-bit number theory with overflow-safe modular arithmetic.
 * BigInteger is used internally for exact multiplication and GCD.
 */
private fun big64(value: Long): BigInteger = BigInteger.valueOf(value)

fun normalizeMod(value: Long, modulus: Long): Long {
    require(modulus > 0) { "Modulus must be positive" }
    val remainder = value % modulus
    return if (remainder >= 0L) remainder else remainder + modulus
}

fun multiplyMod(a: Long, b: Long, modulus: Long): Long {
    require(modulus > 0)
    val m = big64(modulus)
    return big64(a).mod(m).multiply(big64(b).mod(m)).mod(m).toLong()
}

fun powerMod(base: Long, exponent: Long, modulus: Long): Long {
    require(exponent >= 0L && modulus > 0L)
    return big64(base).mod(big64(modulus))
        .modPow(big64(exponent), big64(modulus)).toLong()
}

/** Modular inverse exists only when gcd(value, modulus) == 1. */
fun inverseMod(value: Long, modulus: Long): Long {
    require(modulus > 1L)
    val m = big64(modulus)
    val a = big64(value).mod(m)
    require(a.gcd(m) == BigInteger.ONE) { "Modular inverse does not exist" }
    return a.modInverse(m).toLong()
}

private val millerBases = longArrayOf(
    2L, 325L, 9375L, 28178L, 450775L, 9780504L, 1795265022L
)
private val trialPrimes = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

/**
 * Deterministic Miller–Rabin for every positive signed Long (n < 2^63).
 * These seven bases cover the larger unsigned 64-bit range as well.
 */
fun isPrime64(n: Long): Boolean {
    if (n < 2L) return false
    for (p in trialPrimes) {
        if (n == p) return true
        if (n % p == 0L) return false
    }
    val nn = big64(n)
    val nm1 = nn.subtract(BigInteger.ONE)
    var d = n - 1
    var twos = 0
    while ((d and 1L) == 0L) {
        d = d shr 1
        twos++
    }
    val exponent = big64(d)
    for (a in millerBases) {
        if (a % n == 0L) continue
        var x = big64(a % n).modPow(exponent, nn)
        if (x == BigInteger.ONE || x == nm1) continue
        var passed = false
        repeat(twos - 1) {
            x = x.multiply(x).mod(nn)
            if (x == nm1) passed = true
        }
        if (!passed) return false
    }
    return true
}

/**
 * Pollard–Brent rho over odd composite integers; retry with different c on
 * unlucky cycles. This is randomized-style factorization with deterministic
 * c sequence. Arithmetic and GCD are exact BigInteger operations.
 */
private fun pollardRhoFactor(n: BigInteger): BigInteger {
    val one = BigInteger.ONE
    val two = BigInteger.TWO
    if (n.mod(two) == BigInteger.ZERO) return two
    var c = one
    while (true) {
        fun step(x: BigInteger): BigInteger =
            x.multiply(x).add(c).mod(n)
        var y = two
        var x = two
        var saved = two
        var blockLength = 1
        var divisor = one
        while (divisor == one) {
            x = y
            repeat(blockLength) { y = step(y) }
            var processed = 0
            while (processed < blockLength && divisor == one) {
                saved = y
                var product = one
                val group = minOf(64, blockLength - processed)
                repeat(group) {
                    y = step(y)
                    product = product.multiply(x.subtract(y).abs()).mod(n)
                }
                divisor = product.gcd(n)
                processed += group
            }
            blockLength = blockLength * 2
            if (blockLength <= 0) throw ArithmeticException("Factorization iteration overflow")
        }
        if (divisor == n) {
            do {
                saved = step(saved)
                divisor = x.subtract(saved).abs().gcd(n)
            } while (divisor == one)
        }
        if (divisor != n) return divisor
        c = c.add(one)
    }
}

/**
 * Prime factorization of a positive signed Long, ascending prime keys.
 * n=1 yields empty map. Complexity is randomized/subexponential rather
 * than a guaranteed polynomial-time bound. Strict contest limits may
 * favour a specialized primitive modular multiplication backend.
 */
fun factorize64(n: Long): Map<Long, Int> {
    require(n > 0L) { "Factorization requires a positive integer" }
    val factors = java.util.TreeMap<Long, Int>()
    fun factor(value: BigInteger) {
        if (value == BigInteger.ONE) return
        val x = value.longValueExact()
        if (isPrime64(x)) {
            factors[x] = (factors[x] ?: 0) + 1
            return
        }
        val divisor = pollardRhoFactor(value)
        factor(divisor)
        factor(value.divide(divisor))
    }
    factor(big64(n))
    return factors
}

fun eulerTotient(n: Long): Long {
    require(n > 0L)
    var result = n
    for (prime in factorize64(n).keys) result -= result / prime
    return result
}
