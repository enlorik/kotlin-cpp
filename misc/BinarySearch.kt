fun lowerBound(a: LongArray, v: Long): Int {
    var lo = 0; var hi = a.size
    while (lo < hi) { val mid = (lo + hi) ushr 1; if (a[mid] < v) lo = mid + 1 else hi = mid }
    return lo
}

fun upperBound(a: LongArray, v: Long): Int {
    var lo = 0; var hi = a.size
    while (lo < hi) { val mid = (lo + hi) ushr 1; if (a[mid] <= v) lo = mid + 1 else hi = mid }
    return lo
}

fun lowerBound(a: IntArray, v: Int): Int {
    var lo = 0; var hi = a.size
    while (lo < hi) { val mid = (lo + hi) ushr 1; if (a[mid] < v) lo = mid + 1 else hi = mid }
    return lo
}

fun upperBound(a: IntArray, v: Int): Int {
    var lo = 0; var hi = a.size
    while (lo < hi) { val mid = (lo + hi) ushr 1; if (a[mid] <= v) lo = mid + 1 else hi = mid }
    return lo
}

fun bsLong(lo: Long, hi: Long, check: (Long) -> Boolean): Long {
    var l = lo; var r = hi; var ans = l - 1
    while (l <= r) { val mid = l + (r - l) / 2; if (check(mid)) { ans = mid; l = mid + 1 } else r = mid - 1 }
    return ans
}

fun bsInt(lo: Int, hi: Int, check: (Int) -> Boolean): Int {
    var l = lo; var r = hi; var ans = l - 1
    while (l <= r) { val mid = l + (r - l) / 2; if (check(mid)) { ans = mid; l = mid + 1 } else r = mid - 1 }
    return ans
}
