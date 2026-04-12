class Comb(maxN: Int, val mod: Long) {
    private val fact = LongArray(maxN + 1)
    private val inv = LongArray(maxN + 1)

    init {
        fact[0] = 1L
        for (i in 1..maxN) fact[i] = fact[i - 1] * i % mod
        inv[maxN] = modpow(fact[maxN], mod - 2L, mod)
        for (i in maxN - 1 downTo 0) inv[i] = inv[i + 1] * (i + 1) % mod
    }

    fun C(n: Int, k: Int): Long {
        if (k < 0 || k > n) return 0L
        return fact[n] * inv[k] % mod * inv[n - k] % mod
    }

    fun P(n: Int, k: Int): Long {
        if (k < 0 || k > n) return 0L
        return fact[n] * inv[n - k] % mod
    }

    /** Catalan number C_n = C(2n, n) / (n+1). */
    fun catalan(n: Int): Long = C(2 * n, n) * modpow((n + 1).toLong(), mod - 2L, mod) % mod
}

/** Standalone Catalan number C_n mod p (p prime). */
fun catalan(n: Int, mod: Long): Long {
    val c = Comb(2 * n + 1, mod)
    return c.catalan(n)
}

private fun modpow(base: Long, exp: Long, mod: Long): Long {
    var result = 1L; var b = base % mod; var e = exp
    while (e > 0L) { if (e and 1L == 1L) result = result * b % mod; b = b * b % mod; e = e shr 1 }
    return result
}
