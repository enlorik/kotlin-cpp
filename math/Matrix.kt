fun matMul(a: Array<LongArray>, b: Array<LongArray>, mod: Long): Array<LongArray> {
    val n = a.size
    val m = b[0].size
    val k = b.size
    val c = Array(n) { LongArray(m) }
    for (i in 0 until n)
        for (j in 0 until k)
            if (a[i][j] != 0L)
                for (l in 0 until m)
                    c[i][l] = (c[i][l] + a[i][j] * b[j][l]) % mod
    return c
}

fun matPow(a: Array<LongArray>, p: Long, mod: Long): Array<LongArray> {
    val n = a.size
    var result = matId(n)
    var base = a.map { it.clone() }.toTypedArray()
    var exp = p
    while (exp > 0) {
        if (exp and 1L == 1L) result = matMul(result, base, mod)
        base = matMul(base, base, mod)
        exp = exp shr 1
    }
    return result
}

fun matId(n: Int): Array<LongArray> {
    val m = Array(n) { LongArray(n) }
    for (i in 0 until n) m[i][i] = 1L
    return m
}
