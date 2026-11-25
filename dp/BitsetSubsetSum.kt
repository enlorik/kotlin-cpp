/** Packed 0/1 subset-sum reachability for sums 0..maxSum.
 * Adding one nonnegative weight uses O(maxSum/64+1) word operations and
 * updates in descending order, so every item is used at most once.
 * O(maxSum/64+1) memory. Reachability query is O(1).
 */
class SubsetSumBitset(val maxSum: Int) {
    init { require(maxSum >= 0) }
    private val words = LongArray(maxSum/64+1)
    init { words[0] = 1L }
    fun add(weight: Int) {
        require(weight >= 0)
        if (weight == 0 || weight > maxSum) return
        val whole = weight ushr 6
        val shift = weight and 63
        for (i in words.lastIndex downTo whole) {
            val j = i-whole
            var moved = words[j] shl shift
            if (shift != 0 && j > 0) moved = moved or (words[j-1] ushr (64-shift))
            words[i] = words[i] or moved
        }
        val remainder = maxSum and 63
        if (remainder != 63) words[words.lastIndex] =
            words.last() and ((1L shl (remainder+1))-1L)
    }
    fun addAll(weights: IntArray) { for (weight in weights) add(weight) }
    operator fun contains(sum: Int): Boolean =
        sum in 0..maxSum && (words[sum ushr 6] and (1L shl (sum and 63))) != 0L
    /** Greatest reachable sum <= limit (zero is always reachable). */
    fun bestAtMost(limit: Int = maxSum): Int {
        require(limit in 0..maxSum)
        var i = limit ushr 6
        val bit = limit and 63
        var word = if (bit == 63) words[i] else words[i] and ((1L shl (bit+1))-1L)
        while (word == 0L) { i--; word = words[i] }
        return i*64 + 63-java.lang.Long.numberOfLeadingZeros(word)
    }
}
fun IntArray.subsetSumBitset(maxSum: Int): SubsetSumBitset =
    SubsetSumBitset(maxSum).also { it.addAll(this) }
