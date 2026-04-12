/** Modular inverse via Fermat's little theorem. Requires mod to be prime. */
fun modInv(a: Long, mod: Long): Long = _ma_pow(a, mod - 2L, mod)

/** Modular inverse via extended GCD. Works for any mod coprime with a. */
fun modInvExtGcd(a: Long, mod: Long): Long {
    val (_, x, _) = _ma_egcd(a % mod, mod)
    return (x % mod + mod) % mod
}

/**
 * Chinese Remainder Theorem: find x such that x ≡ r1 (mod m1) and x ≡ r2 (mod m2).
 * Returns (remainder, lcm(m1,m2)), or (-1,-1) if no solution exists.
 */
fun crt(r1: Long, m1: Long, r2: Long, m2: Long): Pair<Long, Long> {
    val (g, p, _) = _ma_egcd(m1, m2)
    if ((r2 - r1) % g != 0L) return Pair(-1L, -1L)
    val lcm = m1 / g * m2
    val diff = ((r2 - r1) / g % (m2 / g) + m2 / g) % (m2 / g)
    val rem = (r1 + m1 * (diff % (m2 / g) * (p % (m2 / g)) % (m2 / g))) % lcm
    return Pair((rem + lcm) % lcm, lcm)
}

/** Euler's totient function φ(n). */
fun eulerTotient(n: Long): Long {
    var result = n
    var x = n
    var i = 2L
    while (i * i <= x) {
        if (x % i == 0L) {
            while (x % i == 0L) x /= i
            result -= result / i
        }
        i++
    }
    if (x > 1L) result -= result / x
    return result
}

// --- file-private helpers ---

private fun _ma_pow(base: Long, exp: Long, mod: Long): Long {
    var result = 1L; var b = base % mod; var e = exp
    while (e > 0L) { if (e and 1L == 1L) result = result * b % mod; b = b * b % mod; e = e shr 1 }
    return result
}

private fun _ma_egcd(a: Long, b: Long): Triple<Long, Long, Long> {
    if (b == 0L) return Triple(a, 1L, 0L)
    val (g, x1, y1) = _ma_egcd(b, a % b)
    return Triple(g, y1, x1 - (a / b) * y1)
}
