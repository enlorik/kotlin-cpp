import kotlin.random.Random

private fun naiveNttConvolution(a: IntArray, b: IntArray): IntArray {
    if (a.isEmpty() || b.isEmpty()) return intArrayOf()
    fun norm(x: Int): Long {
        var r = x.toLong() % NTT_MOD
        if (r < 0L) r += NTT_MOD
        return r
    }
    val result = IntArray(a.size + b.size - 1)
    for (i in a.indices) for (j in b.indices) {
        val add = norm(a[i]) * norm(b[j]) % NTT_MOD
        result[i+j] = ((result[i+j].toLong() + add) % NTT_MOD).toInt()
    }
    return result
}

fun main() {
    check(convolution998244353(intArrayOf(),intArrayOf(2)).isEmpty())
    check(convolution998244353(intArrayOf(2),intArrayOf()).isEmpty())
    check(convolution998244353(intArrayOf(1,2),intArrayOf(3,4))
        .contentEquals(intArrayOf(3,10,8)))
    check(convolution998244353(intArrayOf(-1),intArrayOf(1))[0] ==
        NTT_MOD - 1)
    check(runCatching {
        numberTheoreticTransform998244353(IntArray(3))
    }.exceptionOrNull() is IllegalArgumentException)

    val random = Random(883017)
    repeat(900) {
        val n = random.nextInt(0,35)
        val m = random.nextInt(0,35)
        val a = IntArray(n) {
            if (random.nextBoolean()) random.nextInt(-1000,1001) else random.nextInt()
        }
        val b = IntArray(m) {
            if (random.nextBoolean()) random.nextInt(-1000,1001) else random.nextInt()
        }
        val expected = naiveNttConvolution(a,b)
        val actual = convolution998244353(a,b)
        check(actual.contentEquals(expected))
    }
    for (power in 0..14) {
        val n = 1 shl power
        val original = IntArray(n) { random.nextInt(0,NTT_MOD) }
        val transformed = original.copyOf()
        numberTheoreticTransform998244353(transformed)
        numberTheoreticTransform998244353(transformed, inverse = true)
        check(transformed.contentEquals(original))
    }
    println("PASS: NTT convolution against quadratic oracle and inverse transform")
}
