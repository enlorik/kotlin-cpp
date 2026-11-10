import kotlin.random.Random
import kotlin.math.abs

fun main() {
    val mod = Ntt998244353.MOD
    check(convolutionNtt(intArrayOf(),intArrayOf(1)).isEmpty())
    check(convolutionNtt(intArrayOf(2),intArrayOf(3)).contentEquals(
        intArrayOf(6)))
    check(convolutionNtt(intArrayOf(1,2),intArrayOf(3,4))
        .contentEquals(intArrayOf(3,10,8)))
    check(convolutionFft(doubleArrayOf(),doubleArrayOf(1.0)).isEmpty())
    val fft = convolutionFft(doubleArrayOf(1.0,2.0),
        doubleArrayOf(3.0,4.0))
    check(fft.size == 3)
    check(abs(fft[0]-3)<1e-8 && abs(fft[1]-10)<1e-8 &&
        abs(fft[2]-8)<1e-8)

    val rng = Random(739921)
    var comparisons = 0
    repeat(500) {
        val n = rng.nextInt(0,100)
        val m = rng.nextInt(0,100)
        val a = IntArray(n) { rng.nextInt(-200000,200001) }
        val b = IntArray(m) { rng.nextInt(-200000,200001) }
        val expected = if (n == 0 || m == 0) intArrayOf()
            else IntArray(n+m-1)
        for (i in a.indices) for (j in b.indices) {
            val add = a[i].toLong()*b[j] % mod
            val v = (expected[i+j].toLong()+add)%mod
            expected[i+j] = if (v<0) (v+mod).toInt() else v.toInt()
        }
        check(convolutionNtt(a,b).contentEquals(expected))

        val aa = DoubleArray(n) { rng.nextDouble(-10.0,10.0) }
        val bb = DoubleArray(m) { rng.nextDouble(-10.0,10.0) }
        val expectedFft = if (n == 0 || m == 0) doubleArrayOf()
            else DoubleArray(n+m-1)
        for (i in aa.indices) for (j in bb.indices)
            expectedFft[i+j] += aa[i]*bb[j]
        val actual = convolutionFft(aa,bb)
        check(actual.size == expectedFft.size)
        for (i in actual.indices)
            check(abs(actual[i]-expectedFft[i]) <
                1e-7 * maxOf(1.0,abs(expectedFft[i])))
        comparisons++
    }
    println("PASS: NTT and FFT convolution ($comparisons randomized oracle pairs)")
}
