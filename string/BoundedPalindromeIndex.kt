/**
 * Dynamic count of palindromic substrings of length at most maxLength.
 * Repaints and counts operate on half-open ranges [from, until).
 *
 * The key locality theorem: after painting [l,r), the count of length <= k
 * palindromes beginning at i changes only for i in [l-k+1,r). Interior
 * starts [l,r-k+1) are trivially k each; only O(k) boundary starts must be
 * recomputed. Uses primitive block-assign sums and a lazy painted string.
 *
 * Each operation: O(k + blockSize + n/blockSize) time, O(n+k) memory.
 */
class BoundedPalindromeIndex(source: String, val maxLength: Int, blockSize: Int = maxOf(1, kotlin.math.sqrt(source.length.toDouble()).toInt())) {
    val size: Int = source.length
    private val chars = source.paintedString(blockSize)
    // A bound longer than the entire string cannot add any palindromes.
    private val k = minOf(maxLength, maxOf(1, size))
    private val window = BoundedPalindromeWindows(k)
    private val contributions = window.initialCounts(source).blockAssignSum(blockSize)
    private val scratch = IntArray(2 * k)

    init { require(maxLength >= 1) { "maxLength must be positive" } }

    fun assign(from: Int, until: Int, char: Char) {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        if (from == until || k == 1) return
        chars.assign(from, until, char)

        val interiorEnd = until - k + 1
        if (interiorEnd > from) contributions.assign(from, interiorEnd, k)

        val affectedStart = maxOf(0, from - k + 1)
        if (until - from < k) {
            refresh(affectedStart, until)
        } else {
            refresh(affectedStart, from)
            refresh(maxOf(from, interiorEnd), until)
        }
    }

    fun count(from: Int, until: Int): Long {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        if (k == 1) return (until - from).toLong()
        val boundary = maxOf(from, until - k + 1)
        return contributions.sum(from, boundary) + window.countWithin(chars, boundary, until)
    }

    private fun refresh(from: Int, until: Int) {
        if (from >= until) return
        window.startCounts(chars, from, until, scratch)
        contributions.replace(from, scratch, length = until - from)
    }
}

/** A convenient online palindrome index, separate from the deferred rangeProgram DSL. */
fun String.boundedPalindromes(maxLength: Int, blockSize: Int = maxOf(1, kotlin.math.sqrt(length.toDouble()).toInt())): BoundedPalindromeIndex =
    BoundedPalindromeIndex(this, maxLength, blockSize)

/** Manacher with difference-array accumulation of palindromes by starting index. */
private class BoundedPalindromeWindows(private val k: Int) {
    init { require(k >= 1) }
    private val local = CharArray(3 * k + 4)
    private val odd = IntArray(3 * k + 4)
    private val even = IntArray(3 * k + 4)
    private val differences = IntArray(3 * k + 5)

    private fun calculate(s: CharArray, n: Int, odd: IntArray, even: IntArray, diff: IntArray) {
        diff.fill(0, 0, n + 1)
        var left = 0
        var right = -1
        for (center in 0 until n) {
            var radius = if (center > right) 1 else minOf(odd[left + right - center], right - center + 1)
            while (center - radius >= 0 && center + radius < n && s[center - radius] == s[center + radius]) radius++
            odd[center] = radius
            if (center + radius - 1 > right) {
                left = center - radius + 1
                right = center + radius - 1
            }
            val width = minOf(radius - 1, (k - 1) / 2)
            diff[center - width]++
            diff[center + 1]--
        }
        left = 0
        right = -1
        for (center in 0 until n) {
            var radius = if (center > right) 0 else minOf(even[left + right - center + 1], right - center + 1)
            while (center - radius - 1 >= 0 && center + radius < n && s[center - radius - 1] == s[center + radius]) radius++
            even[center] = radius
            if (center + radius - 1 > right) {
                left = center - radius
                right = center + radius - 1
            }
            val width = minOf(radius, k / 2)
            if (width > 0) {
                diff[center - width]++
                diff[center]--
            }
        }
    }

    fun initialCounts(source: String): IntArray {
        val chars = source.toCharArray()
        val n = chars.size
        val od = IntArray(n)
        val ev = IntArray(n)
        val diff = IntArray(n + 1)
        calculate(chars, n, od, ev, diff)
        val result = IntArray(n)
        var current = 0
        for (i in 0 until n) {
            current += diff[i]
            result[i] = current
        }
        return result
    }

    private fun load(chars: PaintedString, from: Int, until: Int) {
        require(until - from <= local.size)
        var p = 0
        for (i in from until until) local[p++] = chars[i]
        calculate(local, p, odd, even, differences)
    }

    fun startCounts(chars: PaintedString, from: Int, until: Int, output: IntArray) {
        val length = until - from
        require(length <= output.size)
        load(chars, from, minOf(chars.size, until + k - 1))
        var current = 0
        for (i in 0 until length) {
            current += differences[i]
            output[i] = current
        }
    }

    fun countWithin(chars: PaintedString, from: Int, until: Int): Long {
        val length = until - from
        if (length <= 0) return 0L
        load(chars, from, until)
        var current = 0
        var result = 0L
        for (i in 0 until length) {
            current += differences[i]
            result += current
        }
        return result
    }
}
