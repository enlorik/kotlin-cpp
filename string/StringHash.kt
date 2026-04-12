class StringHash(s: String, val base: Long = 131L, val mod: Long = 1_000_000_007L) {
    private val n = s.length
    private val h = LongArray(n + 1)
    private val pw = LongArray(n + 1)

    init {
        pw[0] = 1L
        for (i in s.indices) {
            h[i + 1] = (h[i] * base + s[i].code) % mod
            pw[i + 1] = pw[i] * base % mod
        }
    }

    fun hash(l: Int, r: Int): Long = (h[r + 1] - h[l] * pw[r - l + 1] % mod + mod * mod) % mod

    fun equals(l1: Int, r1: Int, l2: Int, r2: Int): Boolean {
        if (r1 - l1 != r2 - l2) return false
        return hash(l1, r1) == hash(l2, r2)
    }
}

class DoubleHash(s: String) {
    private val h1 = StringHash(s, 131L, 1_000_000_007L)
    private val h2 = StringHash(s, 137L, 998_244_353L)
    private val n = s.length

    fun equals(l1: Int, r1: Int, l2: Int, r2: Int): Boolean =
        h1.equals(l1, r1, l2, r2) && h2.equals(l1, r1, l2, r2)

    fun lcp(i: Int, j: Int): Int {
        var lo = 0; var hi = minOf(n - i, n - j)
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (equals(i, i + mid - 1, j, j + mid - 1)) lo = mid else hi = mid - 1
        }
        return lo
    }
}
