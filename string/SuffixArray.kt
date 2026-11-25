/** UTF-16 suffix order, Kasai LCP and O(log n) suffix-LCP queries.
 * Construction O(n log n + alphabet), memory O(n + alphabet).
 * The empty suffix is not included; array getters return defensive copies.
 */
class SuffixArray(private val text: String) {
    private val n = text.length
    private val order = IntArray(n) { it }
    private val rank = IntArray(n)
    private val lcp = IntArray(n)
    private val rmq = IntArray(2 * n) { Int.MAX_VALUE }
    val suffixes: IntArray get() = order.copyOf()
    /** lcp[i] is the common prefix of suffixes i-1 and i in sorted order; lcp[0]=0. */
    val adjacentLcp: IntArray get() = lcp.copyOf()

    init {
        if (n > 0) {
            var ranks = IntArray(n) { text[it].code + 1 }
            var classes = 65536
            val work = IntArray(n)
            val next = IntArray(n)
            var width = 1
            fun sortBy(offset: Int) {
                val counts = IntArray(classes + 1)
                fun key(i: Int): Int = if (i < n - offset) ranks[i + offset] else 0
                for (i in order) counts[key(i)]++
                var sum = 0
                for (i in counts.indices) {
                    val count = counts[i]; counts[i] = sum; sum += count
                }
                for (i in order) work[counts[key(i)]++] = i
                work.copyInto(order)
            }
            while (true) {
                sortBy(width); sortBy(0)
                var groups = 0
                var prev = -1
                for (i in order) {
                    if (prev < 0 || ranks[i] != ranks[prev] ||
                        (if (i < n-width) ranks[i+width] else 0) !=
                        (if (prev < n-width) ranks[prev+width] else 0)) groups++
                    next[i] = groups
                    prev = i
                }
                ranks = next.copyOf()
                classes = groups
                if (groups == n || width >= n) break
                width = if (width > n/2) n else width*2
            }
            for (i in order.indices) rank[order[i]] = i
            var h = 0
            for (i in 0 until n) {
                val p = rank[i]
                if (p == 0) { h = 0; continue }
                val j = order[p-1]
                while (h < n-i && h < n-j && text[i+h] == text[j+h]) h++
                lcp[p] = h
                if (h > 0) h--
            }
            for (i in lcp.indices) rmq[n+i] = lcp[i]
            for (i in n-1 downTo 1) rmq[i] = minOf(rmq[2*i],rmq[2*i+1])
        }
    }

    fun commonPrefix(i: Int, j: Int): Int {
        require(i in 0 until n && j in 0 until n)
        if (i == j) return n-i
        var l = minOf(rank[i],rank[j])+1+n
        var r = maxOf(rank[i],rank[j])+1+n
        var answer = Int.MAX_VALUE
        while (l < r) {
            if (l % 2 == 1) answer = minOf(answer,rmq[l++])
            if (r % 2 == 1) answer = minOf(answer,rmq[--r])
            l /= 2; r /= 2
        }
        return answer
    }
    private fun compare(start: Int, pattern: String): Int {
        var i = 0
        while (i < pattern.length && i < n-start) {
            val c = text[start+i].compareTo(pattern[i])
            if (c != 0) return c
            i++
        }
        return if (i == pattern.length) 0 else -1
    }
    /** Matching suffix ranks, half-open; O(pattern.length * log n). */
    fun matchRange(pattern: String): IntRange {
        fun bound(upper: Boolean): Int {
            var l = 0; var r = n
            while (l < r) {
                val m = l+(r-l)/2
                val c = compare(order[m],pattern)
                if (c < 0 || (upper && c == 0)) l = m+1 else r = m
            }
            return l
        }
        return bound(false) until bound(true)
    }
    /** Occurrences in suffix order; empty pattern matches all included suffixes. */
    fun occurrences(pattern: String): IntArray =
        matchRange(pattern).map { order[it] }.toIntArray()
    fun distinctSubstringCount(): Long = n.toLong()*(n+1L)/2-lcp.sumOf { it.toLong() }
}
fun String.suffixArray(): SuffixArray = SuffixArray(this)
