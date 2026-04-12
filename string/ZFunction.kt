fun z(s: CharArray): IntArray {
    val n = s.size
    val z = IntArray(n)
    z[0] = n
    var l = 0; var r = 0
    for (i in 1 until n) {
        if (i < r) z[i] = minOf(r - i, z[i - l])
        while (i + z[i] < n && s[z[i]] == s[i + z[i]]) z[i]++
        if (i + z[i] > r) { l = i; r = i + z[i] }
    }
    return z
}

fun z(s: String): IntArray = z(s.toCharArray())

fun zMatch(text: String, pattern: String): List<Int> {
    if (pattern.isEmpty()) return (text.indices).toList()
    val s = (pattern + "#" + text).toCharArray()
    val zArr = z(s)
    val m = pattern.length
    val result = mutableListOf<Int>()
    for (i in m + 1..s.size - 1) {
        if (zArr[i] >= m) result.add(i - m - 1)
    }
    return result
}
