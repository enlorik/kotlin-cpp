import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Approximate radix-2 FFT convolution on double coefficients. */
object FftConvolution {
    private fun transform(real: DoubleArray, imag: DoubleArray, inverse: Boolean) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val r = real[i]; real[i] = real[j]; real[j] = r
                val im = imag[i]; imag[i] = imag[j]; imag[j] = im
            }
        }
        var len = 2
        while (len <= n) {
            val angle = 2.0 * PI / len * (if (inverse) -1 else 1)
            val baseReal = cos(angle)
            val baseImag = sin(angle)
            for (start in 0 until n step len) {
                var wr = 1.0
                var wi = 0.0
                val half = len / 2
                for (i in 0 until half) {
                    val index = start + i
                    val next = index + half
                    val vr = real[next] * wr - imag[next] * wi
                    val vi = real[next] * wi + imag[next] * wr
                    val ur = real[index]
                    val ui = imag[index]
                    real[index] = ur + vr
                    imag[index] = ui + vi
                    real[next] = ur - vr
                    imag[next] = ui - vi
                    val newWr = wr * baseReal - wi * baseImag
                    wi = wr * baseImag + wi * baseReal
                    wr = newWr
                }
            }
            len = len shl 1
        }
        if (inverse) for (i in real.indices) {
            real[i] /= n
            imag[i] /= n
        }
    }

    /**
     * O((n+m) log(n+m)) approximate polynomial convolution.
     * Numerical errors accumulate for large arrays/coefficients. Prefer NTT
     * for exact modular integer arithmetic.
     */
    fun convolution(a: DoubleArray, b: DoubleArray): DoubleArray {
        if (a.isEmpty() || b.isEmpty()) return doubleArrayOf()
        val count = a.size.toLong() + b.size.toLong() - 1
        require(count <= (1 shl 25)) { "FFT size too large" }
        var n = 1
        while (n < count) n = n shl 1
        val ar = DoubleArray(n)
        val ai = DoubleArray(n)
        val br = DoubleArray(n)
        val bi = DoubleArray(n)
        a.copyInto(ar)
        b.copyInto(br)
        transform(ar, ai, false)
        transform(br, bi, false)
        for (i in 0 until n) {
            val re = ar[i] * br[i] - ai[i] * bi[i]
            val im = ar[i] * bi[i] + ai[i] * br[i]
            ar[i] = re
            ai[i] = im
        }
        transform(ar, ai, true)
        return ar.copyOf(count.toInt())
    }
}

fun convolutionFft(a: DoubleArray, b: DoubleArray): DoubleArray =
    FftConvolution.convolution(a, b)
