/** Polynomial arithmetic modulo the standard NTT prime 998244353 = 119*2^23+1. */
const val NTT_MOD = 998244353
private const val NTT_GENERATOR = 3
private const val NTT_MAX_SIZE = 1 shl 23

private fun normalizeNtt(value: Int): Int {
    var result = value % NTT_MOD
    if (result < 0) result += NTT_MOD
    return result
}

private fun nttPower(base: Int, exponent: Int): Int {
    var x = base.toLong()
    var e = exponent
    var result = 1L
    while (e > 0) {
        if ((e and 1) != 0) result = result * x % NTT_MOD
        x = x * x % NTT_MOD
        e = e ushr 1
    }
    return result.toInt()
}

/** In-place radix-2 NTT; input must be a power of two <= 2^23. */
fun numberTheoreticTransform998244353(values: IntArray, inverse: Boolean = false) {
    val n = values.size
    require(n >= 1 && n <= NTT_MAX_SIZE && (n and (n - 1)) == 0) {
        "NTT length must be a power of two at most 2^23"
    }
    for (i in values.indices) values[i] = normalizeNtt(values[i])
    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while ((j and bit) != 0) {
            j = j xor bit
            bit = bit shr 1
        }
        j = j xor bit
        if (i < j) {
            val tmp = values[i]
            values[i] = values[j]
            values[j] = tmp
        }
    }
    var width = 2
    while (width <= n) {
        var root = nttPower(NTT_GENERATOR, (NTT_MOD - 1) / width)
        if (inverse) root = nttPower(root, NTT_MOD - 2)
        val half = width shr 1
        var start = 0
        while (start < n) {
            var omega = 1L
            for (offset in 0 until half) {
                val first = values[start + offset]
                val second = (values[start + offset + half] * omega % NTT_MOD).toInt()
                var added = first + second
                if (added >= NTT_MOD) added -= NTT_MOD
                var subtracted = first - second
                if (subtracted < 0) subtracted += NTT_MOD
                values[start + offset] = added
                values[start + offset + half] = subtracted
                omega = omega * root % NTT_MOD
            }
            start += width
        }
        width = width shl 1
    }
    if (inverse) {
        val inverseLength = nttPower(n, NTT_MOD - 2)
        for (i in values.indices) {
            values[i] = (values[i].toLong() * inverseLength % NTT_MOD).toInt()
        }
    }
}

/**
 * Convolution of polynomial coefficients modulo 998244353.
 * Empty input yields empty output. O((n+m)log(n+m)) time and O(n+m) memory.
 * Any signed Int coefficients are normalized mod NTT_MOD.
 */
fun convolution998244353(a: IntArray, b: IntArray): IntArray {
    if (a.isEmpty() || b.isEmpty()) return intArrayOf()
    val required = a.size.toLong() + b.size.toLong() - 1L
    require(required <= NTT_MAX_SIZE) {
        "Polynomial convolution exceeds NTT 2^23 root-of-unity limit"
    }
    var length = 1
    while (length < required.toInt()) length = length shl 1
    val left = IntArray(length)
    val right = IntArray(length)
    for (i in a.indices) left[i] = normalizeNtt(a[i])
    for (i in b.indices) right[i] = normalizeNtt(b[i])
    numberTheoreticTransform998244353(left)
    numberTheoreticTransform998244353(right)
    for (i in 0 until length) {
        left[i] = (left[i].toLong() * right[i] % NTT_MOD).toInt()
    }
    numberTheoreticTransform998244353(left, inverse = true)
    return left.copyOf(required.toInt())
}
