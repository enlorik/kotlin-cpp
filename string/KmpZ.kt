/**
 * Prefix-function pi[i]: longest proper prefix of s[0..i] that is also
 * its suffix. Uses UTF-16 Kotlin Char units. O(n) time and memory.
 */
fun prefixFunction(s: CharSequence): IntArray {
    val pi = IntArray(s.length)
    for (i in 1 until s.length) {
        var j = pi[i - 1]
        while (j > 0 && s[i] != s[j]) j = pi[j - 1]
        if (s[i] == s[j]) j++
        pi[i] = j
    }
    return pi
}

/** Z[i] = longest common prefix of s and suffix starting at i; Z[0] = n. */
fun zFunction(s: CharSequence): IntArray {
    val n = s.length
    val z = IntArray(n)
    if (n == 0) return z
    z[0] = n
    var left = 0
    var right = 0
    for (i in 1 until n) {
        if (i < right) z[i] = minOf(right - i, z[i - left])
        while (i + z[i] < n && s[z[i]] == s[i + z[i]]) z[i]++
        if (i + z[i] > right) {
            left = i
            right = i + z[i]
        }
    }
    return z
}

/**
 * Every occurrence starting index, including overlapping matches.
 * An empty pattern occurs at all n+1 boundaries, by convention.
 * O(text length + pattern length + result size).
 */
fun kmpOccurrences(text: CharSequence, pattern: CharSequence): IntArray {
    val n = text.length
    val m = pattern.length
    if (m == 0) return IntArray(n + 1) { it }
    val pi = prefixFunction(pattern)
    val found = ArrayList<Int>()
    var matched = 0
    for (i in 0 until n) {
        while (matched > 0 && text[i] != pattern[matched])
            matched = pi[matched - 1]
        if (text[i] == pattern[matched]) matched++
        if (matched == m) {
            found.add(i - m + 1)
            matched = pi[m - 1]
        }
    }
    return found.toIntArray()
}
