fun berlekampMassey(a: LongArray, mod: Long): LongArray {
    val n = a.size
    var cur = LongArray(0)
    var lst = LongArray(0)
    var lf = 0; var ld = 0L
    fun pow(b: Long, e: Long): Long {
        var r = 1L; var base = b % mod; var exp = e
        while (exp > 0) { if (exp and 1L == 1L) r = r * base % mod; base = base * base % mod; exp = exp shr 1 }
        return r
    }
    for (i in 0 until n) {
        var t = 0L
        for (j in cur.indices) t = (t + cur[j] * a[i - 1 - j]) % mod
        if ((a[i] - t + mod) % mod == 0L) continue
        if (cur.isEmpty()) { cur = LongArray(i + 1); lf = i; ld = (a[i] - t + mod) % mod; continue }
        val k = (a[i] - t + mod) % mod * pow(ld, mod - 2) % mod
        val c = LongArray(i - lf - 1 + lst.size + 1)
        c[i - lf - 1] = k
        for (j in lst.indices) c[i - lf - 1 + j + 1] = (mod - k * lst[j] % mod) % mod
        if (c.size >= cur.size) { lst = cur; lf = i; ld = (a[i] - t + mod) % mod }
        val newCur = LongArray(maxOf(cur.size, c.size))
        for (j in cur.indices) newCur[j] = (newCur[j] + cur[j]) % mod
        for (j in c.indices) newCur[j] = (newCur[j] + c[j]) % mod
        cur = newCur
    }
    return cur
}

fun kthTerm(a: LongArray, c: LongArray, k: Long, mod: Long): Long {
    val m = c.size
    if (k < a.size) return a[k.toInt()]
    fun mulPoly(p: LongArray, q: LongArray): LongArray {
        val res = LongArray(p.size + q.size - 1)
        for (i in p.indices) for (j in q.indices) res[i + j] = (res[i + j] + p[i] * q[j]) % mod
        // reduce mod characteristic polynomial
        for (i in res.size - 1 downTo m) {
            val coef = res[i]
            for (j in c.indices) res[i - 1 - j] = (res[i - 1 - j] + coef * c[j]) % mod
        }
        return res.copyOf(m)
    }
    var pol = LongArray(m)
    var base = LongArray(m)
    pol[0] = 1L; if (m > 1) base[1] = 1L else base[0] = c[0]
    if (m == 1) { base[0] = 1L }
    // use matrix exponentiation via polynomial exponentiation
    var exp = k; var result = LongArray(m); result[0] = 1L
    var b = LongArray(m); if (m > 1) b[1] = 1L else b[0] = 1L
    // Simple: compute companion matrix power via poly
    var shift = LongArray(m)
    if (m > 1) shift[1] = 1L else { shift[0] = c[0] }
    result = LongArray(m); result[0] = 1L
    var pw = if (m > 1) { val t = LongArray(m); t[1] = 1L; t } else { val t = LongArray(m); t[0] = c[0]; t }
    exp = k
    result = LongArray(m); result[0] = 1L
    var ex = exp
    while (ex > 0) {
        if (ex and 1L == 1L) result = mulPoly(result, pw)
        pw = mulPoly(pw, pw)
        ex = ex shr 1
    }
    var ans = 0L
    for (i in 0 until m) ans = (ans + result[i] * a[i]) % mod
    return ans
}
