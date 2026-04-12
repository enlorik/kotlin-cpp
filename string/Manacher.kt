fun manacher(s: String): IntArray {
    val t = buildString { append('#'); for (c in s) { append(c); append('#') } }
    val n = t.length
    val p = IntArray(n)
    var c = 0; var r = 0
    for (i in 0 until n) {
        if (i < r) p[i] = minOf(r - i, p[2 * c - i])
        while (i - p[i] - 1 >= 0 && i + p[i] + 1 < n && t[i - p[i] - 1] == t[i + p[i] + 1]) p[i]++
        if (i + p[i] > r) { c = i; r = i + p[i] }
    }
    return p
}

fun longestPalindrome(s: String): String {
    if (s.isEmpty()) return ""
    val p = manacher(s)
    val t = buildString { append('#'); for (c in s) { append(c); append('#') } }
    var best = 0
    for (i in p.indices) if (p[i] > p[best]) best = i
    val start = (best - p[best]) / 2
    return s.substring(start, start + p[best])
}

fun countPalindromes(s: String): Long {
    val p = manacher(s)
    var cnt = 0L
    for (i in p.indices) cnt += (p[i] + 1) / 2
    return cnt
}

class PalindromeChecker(s: String) {
    private val p = manacher(s)
    private val n = s.length

    fun isPalindrome(l: Int, r: Int): Boolean {
        val center = l + r + 1
        val radius = r - l + 1
        return p[center] >= radius
    }
}
