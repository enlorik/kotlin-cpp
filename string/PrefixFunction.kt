fun pi(s: CharArray): IntArray {
    val n = s.size
    val pi = IntArray(n)
    for (i in 1 until n) {
        var j = pi[i - 1]
        while (j > 0 && s[i] != s[j]) j = pi[j - 1]
        if (s[i] == s[j]) j++
        pi[i] = j
    }
    return pi
}

fun pi(s: String): IntArray = pi(s.toCharArray())

fun kmpMatch(text: String, pattern: String): List<Int> {
    if (pattern.isEmpty()) return (text.indices).toList()
    val s = (pattern + "#" + text).toCharArray()
    val piArr = pi(s)
    val m = pattern.length
    val result = mutableListOf<Int>()
    for (i in m + 1 until s.size) {
        if (piArr[i] == m) result.add(i - 2 * m)
    }
    return result
}
