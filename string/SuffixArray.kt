fun suffixArray(s: IntArray): IntArray {
    val n = s.size
    if (n == 1) return intArrayOf(0)
    var cls = s.copyOf()
    var sa = (0 until n).sortedBy { cls[it] }.toIntArray()
    var c = IntArray(n)
    c[sa[0]] = 0
    for (i in 1 until n) c[sa[i]] = c[sa[i - 1]] + if (cls[sa[i]] != cls[sa[i - 1]]) 1 else 0
    var len = 1
    while (len < n) {
        val sa2 = IntArray(n) { (sa[it] - len + n) % n }
        val cnt = IntArray(n + 1)
        for (x in c) cnt[x + 1]++
        for (i in 1..n) cnt[i] += cnt[i - 1]
        val newSa = IntArray(n)
        for (x in sa2) newSa[cnt[c[x]]++] = x
        val newC = IntArray(n)
        newC[newSa[0]] = 0
        for (i in 1 until n) {
            val prev = newSa[i - 1]; val cur = newSa[i]
            newC[cur] = newC[prev] + if (c[cur] != c[prev] || c[(cur + len) % n] != c[(prev + len) % n]) 1 else 0
        }
        sa = newSa; c = newC; len *= 2
    }
    return sa
}

fun suffixArray(s: String): IntArray {
    val n = s.length
    if (n == 0) return IntArray(0)
    val mapped = IntArray(n + 1)
    for (i in s.indices) mapped[i] = s[i].code
    mapped[n] = 0
    val sa = suffixArray(mapped)
    return sa.drop(1).toIntArray()
}

fun lcpArray(s: String, sa: IntArray): IntArray {
    val n = s.length
    val rank = IntArray(n)
    for (i in 0 until n) rank[sa[i]] = i
    val lcp = IntArray(n)
    var k = 0
    for (i in 0 until n) {
        if (rank[i] == 0) { k = 0; continue }
        val j = sa[rank[i] - 1]
        while (i + k < n && j + k < n && s[i + k] == s[j + k]) k++
        lcp[rank[i]] = k
        if (k > 0) k--
    }
    return lcp
}

fun saSearch(s: String, sa: IntArray, pattern: String): IntRange {
    val n = sa.size
    val m = pattern.length
    fun cmp(i: Int): Int {
        val sub = s.substring(sa[i], minOf(sa[i] + m, s.length))
        return sub.compareTo(pattern)
    }
    var lo = 0; var hi = n
    while (lo < hi) { val mid = (lo + hi) / 2; if (cmp(mid) < 0) lo = mid + 1 else hi = mid }
    val left = lo
    hi = n
    while (lo < hi) { val mid = (lo + hi) / 2; if (cmp(mid) <= 0) lo = mid + 1 else hi = mid }
    val right = lo - 1
    return if (left > right) IntRange.EMPTY else left..right
}
