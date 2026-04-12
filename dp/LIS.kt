// Longest Increasing Subsequence

fun lis(a: IntArray): Int {
    val tails = mutableListOf<Int>()
    for (x in a) {
        var lo = 0; var hi = tails.size
        while (lo < hi) { val mid = (lo + hi) ushr 1; if (tails[mid] < x) lo = mid + 1 else hi = mid }
        if (lo == tails.size) tails.add(x) else tails[lo] = x
    }
    return tails.size
}

fun lis(a: LongArray): Int {
    val tails = mutableListOf<Long>()
    for (x in a) {
        var lo = 0; var hi = tails.size
        while (lo < hi) { val mid = (lo + hi) ushr 1; if (tails[mid] < x) lo = mid + 1 else hi = mid }
        if (lo == tails.size) tails.add(x) else tails[lo] = x
    }
    return tails.size
}

fun lisSequence(a: IntArray): IntArray {
    val n = a.size
    if (n == 0) return IntArray(0)
    val tails = mutableListOf<Int>()
    val pos = IntArray(n)
    val prev = IntArray(n) { -1 }
    for (i in 0 until n) {
        val x = a[i]
        var lo = 0; var hi = tails.size
        while (lo < hi) { val mid = (lo + hi) ushr 1; if (tails[mid] < x) lo = mid + 1 else hi = mid }
        pos[i] = lo
        if (lo == tails.size) tails.add(x) else tails[lo] = x
        if (lo > 0) {
            // find previous element with pos == lo-1
            for (j in i - 1 downTo 0) if (pos[j] == lo - 1) { prev[i] = j; break }
        }
    }
    val len = tails.size
    val res = IntArray(len)
    var idx = -1
    for (i in n - 1 downTo 0) if (pos[i] == len - 1) { idx = i; break }
    var p = idx; var r = len - 1
    while (p != -1) { res[r--] = a[p]; p = prev[p] }
    return res
}

fun lnds(a: IntArray): Int {
    val tails = mutableListOf<Int>()
    for (x in a) {
        var lo = 0; var hi = tails.size
        while (lo < hi) { val mid = (lo + hi) ushr 1; if (tails[mid] <= x) lo = mid + 1 else hi = mid }
        if (lo == tails.size) tails.add(x) else tails[lo] = x
    }
    return tails.size
}
