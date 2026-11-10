import java.math.BigInteger

/**
 * Exact modular arithmetic on positive signed-Long moduli.
 * Uses BigInteger when a multiplication could overflow Long.
 * Exponents must be nonnegative.
 */
private fun big(value: Long): BigInteger = BigInteger.valueOf(value)

fun modNormalize64(value: Long, modulus: Long): Long {
    require(modulus > 0L) { "Modulus must be positive" }
    val r = value % modulus
    return if (r < 0L) r + modulus else r
}

fun modAdd64(a: Long, b: Long, modulus: Long): Long {
    val x = modNormalize64(a, modulus)
    val y = modNormalize64(b, modulus)
    return if (x >= modulus - y) x - (modulus - y) else x + y
}

fun modSubtract64(a: Long, b: Long, modulus: Long): Long {
    val x = modNormalize64(a, modulus)
    val y = modNormalize64(b, modulus)
    return if (x >= y) x - y else modulus - (y - x)
}

fun modMultiply64(a: Long, b: Long, modulus: Long): Long {
    require(modulus > 0L)
    val m = big(modulus)
    return big(a).mod(m).multiply(big(b).mod(m)).mod(m).longValueExact()
}

fun modPower64(base: Long, exponent: Long, modulus: Long): Long {
    require(exponent >= 0L)
    require(modulus > 0L)
    val m = big(modulus)
    return big(base).mod(m).modPow(big(exponent), m).longValueExact()
}

data class ExtendedGcdExact(
    val gcd: BigInteger,
    val x: BigInteger,
    val y: BigInteger
)

/** a*x + b*y = gcd(a,b), with gcd >= 0; safe even for Long.MIN_VALUE. */
fun extendedGcdExact(a: Long, b: Long): ExtendedGcdExact {
    var oldR = big(a)
    var r = big(b)
    var oldX = BigInteger.ONE
    var x = BigInteger.ZERO
    var oldY = BigInteger.ZERO
    var y = BigInteger.ONE
    while (r != BigInteger.ZERO) {
        val q = oldR.divide(r)
        val nextR = oldR.subtract(q.multiply(r))
        oldR = r
        r = nextR
        val nextX = oldX.subtract(q.multiply(x))
        oldX = x
        x = nextX
        val nextY = oldY.subtract(q.multiply(y))
        oldY = y
        y = nextY
    }
    if (oldR.signum() < 0) {
        oldR = oldR.negate()
        oldX = oldX.negate()
        oldY = oldY.negate()
    }
    return ExtendedGcdExact(oldR, oldX, oldY)
}

/** Returns null unless inverse exists. Requires modulus >= 2. */
fun modInverse64(value: Long, modulus: Long): Long? {
    require(modulus >= 2L)
    val m = big(modulus)
    val v = big(value).mod(m)
    return if (v.gcd(m) == BigInteger.ONE) v.modInverse(m).longValueExact()
    else null
}

data class ChineseRemainderResult(
    val residue: BigInteger,
    val modulus: BigInteger
)

/**
 * Generalized CRT: pair (residue, positive modulus), even for noncoprime moduli.
 * Returns null for inconsistent systems. Empty system: 0 mod 1.
 */
fun chineseRemainder64(equations: List<Pair<Long, Long>>): ChineseRemainderResult? {
    var a = BigInteger.ZERO
    var m = BigInteger.ONE
    for ((residue, modulus) in equations) {
        require(modulus > 0L) { "CRT moduli must be positive" }
        val n = big(modulus)
        val b = big(residue).mod(n)
        val g = m.gcd(n)
        val difference = b.subtract(a)
        if (difference.mod(g) != BigInteger.ZERO) return null
        val factor = n.divide(g)
        val t = if (factor == BigInteger.ONE) BigInteger.ZERO else
            difference.divide(g).multiply(m.divide(g).modInverse(factor)).mod(factor)
        a = a.add(m.multiply(t))
        m = m.multiply(factor)
        a = a.mod(m)
    }
    return ChineseRemainderResult(a, m)
}
