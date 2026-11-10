/**
 * Exact integer polynomial multiplication via iterative radix-2 NTT.
 * Modulus 998244353 = 119*2^23 + 1, primitive root 3.
 * Operates on coefficients modulo MOD, including negative input values.
 */
object Ntt998244353 {
    const val MOD = 998244353
    private const val ROOT = 3
    private const val MAX_TRANSFORM = 1 shl 23

    private fun modPower(base: Long, exponent: Int): Int {
        var b = base % MOD
        var e = exponent
        var answer = 1L
        while (e > 0) {
            if ((e and 1) != 0) answer = answer * b % MOD
            b = b * b % MOD
            e = e ushr 1
        }
        return answer.toInt()
    }

    private fun transform(a: IntArray, inverse: Boolean) {
        val n = a.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tmp = a[i]; a[i] = a[j]; a[j] = tmp
            }
        }
        var length = 2
        while (length <= n) {
            val exponent = (MOD - 1) / length
            val angle = if (inverse)
                modPower(ROOT.toLong(), MOD - 1 - exponent)
            else modPower(ROOT.toLong(), exponent)
            for (start in 0 until n step length) {
                var root = 1L
                val half = length / 2
                for (i in 0 until half) {
                    val u = a[start+i].toLong()
                    val v = root * a[start+i+half] % MOD
                    val x = u + v
                    val y = u - v
                    a[start+i] = if (x >= MOD) (x-MOD).toInt() else x.toInt()
                    a[start+i+half] = if (y < 0) (y+MOD).toInt() else y.toInt()
                    root = root * angle % MOD
                }
            }
            length = length shl 1
        }
        if (inverse) {
            val invN = modPower(n.toLong(), MOD - 2)
            for (i in a.indices) a[i] = (a[i].toLong()*invN % MOD).toInt()
        }
    }

    fun convolution(a: IntArray, b: IntArray): IntArray {
        if (a.isEmpty() || b.isEmpty()) return intArrayOf()
        val required = a.size.toLong() + b.size.toLong() - 1L
        require(required <= MAX_TRANSFORM) {
            "NTT length exceeds 2^23, the power-of-two capacity of this modulus"
        }
        var n = 1
        while (n < required) n = n shl 1
        val lhs = IntArray(n)
        val rhs = IntArray(n)
        for (i in a.indices) {
            val x = a[i] % MOD
            lhs[i] = if (x >= 0) x else x + MOD
        }
        for (i in b.indices) {
            val x = b[i] % MOD
            rhs[i] = if (x >= 0) x else x + MOD
        }
        transform(lhs, false)
        transform(rhs, false)
        for (i in 0 until n) lhs[i] = (lhs[i].toLong()*rhs[i] % MOD).toInt()
        transform(lhs, true)
        return lhs.copyOf(required.toInt())
    }
}

/** Exact convolution modulo 998244353; output length a+b-1. */
fun convolutionNtt(a: IntArray, b: IntArray): IntArray =
    Ntt998244353.convolution(a, b)
