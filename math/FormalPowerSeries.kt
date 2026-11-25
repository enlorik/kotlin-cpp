/** Truncated formal power series over F_998244353, using Newton doubling and NTT.
 * Arrays are coefficients in ascending degree, absent coefficients are zero.
 * n <= 2^22 ensures intermediate convolutions fit the existing NTT.
 */
private fun fpsNorm(x: Int): Int = ((x % NTT_MOD) + NTT_MOD) % NTT_MOD
private fun fpsMul(a: Int,b: Int): Int = (a.toLong()*b % NTT_MOD).toInt()
private fun fpsPow(a: Int, exponent: Int): Int {
    var x = fpsNorm(a); var e = exponent; var result = 1
    while (e > 0) { if (e and 1 != 0) result = fpsMul(result,x); x = fpsMul(x,x); e = e ushr 1 }
    return result
}
private fun fpsLength(n: Int) { require(n in 0..(1 shl 22)) }
private fun fpsPrefix(a: IntArray,n: Int): IntArray = IntArray(n) { fpsNorm(a.getOrElse(it){0}) }
private fun fpsProduct(a: IntArray,b: IntArray,n: Int): IntArray = convolution998244353(a,b).copyOf(n)

/** a * inverse(a) = 1 mod x^n; requires nonzero constant for n > 0. O(n log n). */
fun fpsInverse(a: IntArray,n: Int): IntArray {
    fpsLength(n)
    if (n == 0) return intArrayOf()
    val constant = fpsNorm(a.getOrElse(0){0})
    require(constant != 0)
    var g = intArrayOf(fpsPow(constant,NTT_MOD-2))
    while (g.size < n) {
        val m = minOf(2*g.size,n)
        val ag = fpsProduct(fpsPrefix(a,m),g,m)
        for (i in ag.indices) ag[i] = fpsNorm((if (i == 0) 2 else 0)-ag[i])
        g = fpsProduct(g,ag,m)
    }
    return g
}

/** log(a) = integral(a'/a), constant must equal one for n > 0. O(n log n). */
fun fpsLog(a: IntArray,n: Int): IntArray {
    fpsLength(n)
    if (n == 0) return intArrayOf()
    require(fpsNorm(a.getOrElse(0){0}) == 1)
    if (n == 1) return intArrayOf(0)
    val derivative = IntArray(n-1) { fpsMul(fpsNorm(a.getOrElse(it+1){0}),it+1) }
    val product = fpsProduct(derivative,fpsInverse(a,n-1),n-1)
    val inverses = IntArray(n); inverses[1] = 1
    for (i in 2 until n) inverses[i] = NTT_MOD-fpsMul(NTT_MOD/i,inverses[NTT_MOD%i])
    return IntArray(n) { if (it == 0) 0 else fpsMul(product[it-1],inverses[it]) }
}

/** exp(a), constant must equal zero for n > 0. O(n log n). */
fun fpsExp(a: IntArray,n: Int): IntArray {
    fpsLength(n)
    if (n == 0) return intArrayOf()
    require(fpsNorm(a.getOrElse(0){0}) == 0)
    var g = intArrayOf(1)
    while (g.size < n) {
        val m = minOf(2*g.size,n)
        val lg = fpsLog(g,m)
        val correction = IntArray(m) { fpsNorm(fpsNorm(a.getOrElse(it){0})-lg[it]+(if (it == 0) 1 else 0)) }
        g = fpsProduct(g,correction,m)
    }
    return g
}
