import kotlin.math.*

data class Complex(val re: Double, val im: Double) {
    operator fun plus(o: Complex) = Complex(re + o.re, im + o.im)
    operator fun minus(o: Complex) = Complex(re - o.re, im - o.im)
    operator fun times(o: Complex) = Complex(re * o.re - im * o.im, re * o.im + im * o.re)
    fun conjugate() = Complex(re, -im)
}

fun fft(a: Array<Complex>, invert: Boolean) {
    val n = a.size
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
        j = j xor bit
        if (i < j) { val t = a[i]; a[i] = a[j]; a[j] = t }
    }
    var len = 2
    while (len <= n) {
        val ang = 2 * PI / len * if (invert) -1 else 1
        val wlen = Complex(cos(ang), sin(ang))
        var i = 0
        while (i < n) {
            var w = Complex(1.0, 0.0)
            for (jj in 0 until len / 2) {
                val u = a[i + jj]; val v = a[i + jj + len / 2] * w
                a[i + jj] = u + v; a[i + jj + len / 2] = u - v
                w = w * wlen
            }
            i += len
        }
        len = len shl 1
    }
    if (invert) for (i in a.indices) a[i] = Complex(a[i].re / n, a[i].im / n)
}

fun polyMulDouble(a: LongArray, b: LongArray): LongArray {
    var n = 1
    while (n < a.size + b.size) n = n shl 1
    val fa = Array(n) { if (it < a.size) Complex(a[it].toDouble(), 0.0) else Complex(0.0, 0.0) }
    val fb = Array(n) { if (it < b.size) Complex(b[it].toDouble(), 0.0) else Complex(0.0, 0.0) }
    fft(fa, false); fft(fb, false)
    for (i in 0 until n) fa[i] = fa[i] * fb[i]
    fft(fa, true)
    return LongArray(a.size + b.size - 1) { fa[it].re.roundToLong() }
}

private fun Double.roundToLong() = (this + 0.5).toLong()

private fun powMod(b: Long, e: Long, m: Long): Long {
    var r = 1L; var base = b % m; var exp = e
    while (exp > 0) { if (exp and 1L == 1L) r = r * base % m; base = base * base % m; exp = exp shr 1 }
    return r
}

fun ntt(a: LongArray, invert: Boolean, mod: Long = 998_244_353L) {
    val n = a.size
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
        j = j xor bit
        if (i < j) { val t = a[i]; a[i] = a[j]; a[j] = t }
    }
    var len = 2
    while (len <= n) {
        val g = if (invert) powMod(3L, mod - 1 - (mod - 1) / len, mod) else powMod(3L, (mod - 1) / len, mod)
        var i = 0
        while (i < n) {
            var w = 1L
            for (jj in 0 until len / 2) {
                val u = a[i + jj]; val v = a[i + jj + len / 2] * w % mod
                a[i + jj] = (u + v) % mod; a[i + jj + len / 2] = (u - v + mod) % mod
                w = w * g % mod
            }
            i += len
        }
        len = len shl 1
    }
    if (invert) {
        val inv = powMod(n.toLong(), mod - 2, mod)
        for (i in a.indices) a[i] = a[i] * inv % mod
    }
}

fun polyMul(a: LongArray, b: LongArray, mod: Long = 998_244_353L): LongArray {
    var n = 1
    while (n < a.size + b.size) n = n shl 1
    val fa = LongArray(n) { if (it < a.size) a[it] else 0L }
    val fb = LongArray(n) { if (it < b.size) b[it] else 0L }
    ntt(fa, false, mod); ntt(fb, false, mod)
    for (i in 0 until n) fa[i] = fa[i] * fb[i] % mod
    ntt(fa, true, mod)
    return LongArray(a.size + b.size - 1) { fa[it] }
}
