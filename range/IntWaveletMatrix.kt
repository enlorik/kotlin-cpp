/**
 * Static Wavelet Matrix over SIGNED Int values.
 *
 * Values are mapped with xor Int.MIN_VALUE so unsigned bit order equals
 * signed numeric order. O(32*n) build time/memory; O(32) kth/rank/
 * frequency/countLess/predecessor/successor. No per-query allocations.
 * Every index range is zero-based and half-open [from, until).
 */
class IntWaveletMatrix(values: IntArray) {
    val size: Int = values.size
    private val zeroPrefix: Array<IntArray> = Array(32) { IntArray(size + 1) }
    private val zeroCount = IntArray(32)

    init {
        var current = IntArray(size) { values[it] xor Int.MIN_VALUE }
        var next = IntArray(size)
        for (bit in 31 downTo 0) {
            val prefix = zeroPrefix[bit]
            for (i in 0 until size) {
                prefix[i + 1] = prefix[i] + if (((current[i] ushr bit) and 1) == 0) 1 else 0
            }
            val zeros = prefix[size]
            zeroCount[bit] = zeros
            var z = 0
            var o = zeros
            for (x in current) {
                if (((x ushr bit) and 1) == 0) next[z++] = x else next[o++] = x
            }
            val temp = current
            current = next
            next = temp
        }
    }

    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid range [$from, $until) for size $size"
        }
    }

    private fun requireK(from: Int, until: Int, k: Int) {
        checkRange(from, until)
        require(k >= 0 && k < until - from) { "Rank $k outside range length ${until - from}" }
    }

    /** The zero-based kth smallest value in [from, until). */
    fun kthSmallest(from: Int, until: Int, k: Int): Int {
        requireK(from, until, k)
        var l = from
        var r = until
        var index = k
        var encoded = 0
        for (bit in 31 downTo 0) {
            val prefix = zeroPrefix[bit]
            val leftZeros = prefix[l]
            val rightZeros = prefix[r]
            val zerosHere = rightZeros - leftZeros
            if (index < zerosHere) {
                l = leftZeros
                r = rightZeros
            } else {
                index -= zerosHere
                encoded = encoded or (1 shl bit)
                l = zeroCount[bit] + l - leftZeros
                r = zeroCount[bit] + r - rightZeros
            }
        }
        return encoded xor Int.MIN_VALUE
    }

    fun kthLargest(from: Int, until: Int, k: Int): Int {
        requireK(from, until, k)
        return kthSmallest(from, until, until - from - 1 - k)
    }

    /** Occurrences of value inside the selected range. */
    fun rank(value: Int, from: Int, until: Int): Int {
        checkRange(from, until)
        var l = from
        var r = until
        val encoded = value xor Int.MIN_VALUE
        for (bit in 31 downTo 0) {
            val prefix = zeroPrefix[bit]
            val zL = prefix[l]
            val zR = prefix[r]
            if (((encoded ushr bit) and 1) == 0) {
                l = zL
                r = zR
            } else {
                l = zeroCount[bit] + l - zL
                r = zeroCount[bit] + r - zR
            }
        }
        return r - l
    }

    /** Number of elements strictly less than value, in signed Int order. */
    fun countLess(from: Int, until: Int, value: Int): Int {
        checkRange(from, until)
        val encoded = value xor Int.MIN_VALUE
        var l = from
        var r = until
        var result = 0
        for (bit in 31 downTo 0) {
            val prefix = zeroPrefix[bit]
            val zL = prefix[l]
            val zR = prefix[r]
            if (((encoded ushr bit) and 1) != 0) {
                result += zR - zL
                l = zeroCount[bit] + l - zL
                r = zeroCount[bit] + r - zR
            } else {
                l = zL
                r = zR
            }
        }
        return result
    }

    /** Count values lowerInclusive <= x < upperExclusive. */
    fun countBetween(from: Int, until: Int, lowerInclusive: Int, upperExclusive: Int): Int {
        checkRange(from, until)
        if (lowerInclusive >= upperExclusive) return 0
        return countLess(from, until, upperExclusive) -
            countLess(from, until, lowerInclusive)
    }

    /** Largest value strictly below upperExclusive, or null if absent. */
    fun predecessor(from: Int, until: Int, upperExclusive: Int): Int? {
        val count = countLess(from, until, upperExclusive)
        return if (count == 0) null else kthSmallest(from, until, count - 1)
    }

    /** Smallest value >= lowerInclusive, or null if absent. */
    fun successor(from: Int, until: Int, lowerInclusive: Int): Int? {
        val count = countLess(from, until, lowerInclusive)
        return if (count == until - from) null else kthSmallest(from, until, count)
    }
}

fun IntArray.waveletMatrix(): IntWaveletMatrix = IntWaveletMatrix(this)
