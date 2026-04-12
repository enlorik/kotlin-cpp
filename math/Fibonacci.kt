// Matrix represented as 2x2 LongArray: [a00, a01, a10, a11]
private fun matMul(A: LongArray, B: LongArray): LongArray = longArrayOf(
    A[0] * B[0] + A[1] * B[2],
    A[0] * B[1] + A[1] * B[3],
    A[2] * B[0] + A[3] * B[2],
    A[2] * B[1] + A[3] * B[3]
)

private fun matMulMod(A: LongArray, B: LongArray, mod: Long): LongArray = longArrayOf(
    (A[0] * B[0] + A[1] * B[2]) % mod,
    (A[0] * B[1] + A[1] * B[3]) % mod,
    (A[2] * B[0] + A[3] * B[2]) % mod,
    (A[2] * B[1] + A[3] * B[3]) % mod
)

private fun matPow(mat: LongArray, n: Long): LongArray {
    var result = longArrayOf(1L, 0L, 0L, 1L) // identity
    var base = mat
    var e = n
    while (e > 0L) {
        if (e and 1L == 1L) result = matMul(result, base)
        base = matMul(base, base)
        e = e shr 1
    }
    return result
}

private fun matPowMod(mat: LongArray, n: Long, mod: Long): LongArray {
    var result = longArrayOf(1L, 0L, 0L, 1L)
    var base = mat
    var e = n
    while (e > 0L) {
        if (e and 1L == 1L) result = matMulMod(result, base, mod)
        base = matMulMod(base, base, mod)
        e = e shr 1
    }
    return result
}

/**
 * nth Fibonacci number via matrix exponentiation.
 * F(0)=0, F(1)=1. Overflows Long for n > 85.
 */
fun fib(n: Int): Long {
    if (n <= 0) return 0L
    if (n == 1) return 1L
    val m = matPow(longArrayOf(1L, 1L, 1L, 0L), n.toLong() - 1)
    return m[0]
}

/** nth Fibonacci number modulo mod via matrix exponentiation. */
fun fib(n: Long, mod: Long): Long {
    if (n <= 0L) return 0L
    if (n == 1L) return 1L % mod
    val m = matPowMod(longArrayOf(1L, 1L, 1L, 0L), n - 1L, mod)
    return m[0]
}
