/** Finds the maximum of a unimodal function on [lo, hi] (continuous). */
fun ternarySearch(lo: Double, hi: Double, f: (Double) -> Double, eps: Double = 1e-9): Double {
    var l = lo; var r = hi
    while (r - l > eps) {
        val m1 = l + (r - l) / 3.0
        val m2 = r - (r - l) / 3.0
        if (f(m1) < f(m2)) l = m1 else r = m2
    }
    return (l + r) / 2.0
}

/** Finds the minimum of a unimodal function on [lo, hi] (continuous). */
fun ternarySearchMin(lo: Double, hi: Double, f: (Double) -> Double, eps: Double = 1e-9): Double {
    var l = lo; var r = hi
    while (r - l > eps) {
        val m1 = l + (r - l) / 3.0
        val m2 = r - (r - l) / 3.0
        if (f(m1) > f(m2)) l = m1 else r = m2
    }
    return (l + r) / 2.0
}

/**
 * Finds the integer x in [lo, hi] maximising unimodal function f.
 * Assumes f first increases then decreases (or is monotone).
 */
fun ternarySearchInt(lo: Long, hi: Long, f: (Long) -> Long): Long {
    var l = lo; var r = hi
    while (r - l > 2L) {
        val m1 = l + (r - l) / 3L
        val m2 = r - (r - l) / 3L
        if (f(m1) < f(m2)) l = m1 else r = m2
    }
    var best = l; var bestVal = f(l)
    for (x in l + 1..r) { val v = f(x); if (v > bestVal) { bestVal = v; best = x } }
    return best
}
