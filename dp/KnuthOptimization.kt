/**
 * Knuth-optimized interval DP for
 * dp[l][r] = cost(l,r) + min_{l<=k<r} (dp[l][k] + dp[k+1][r]),
 * with dp[i][i] = 0.
 *
 * REQUIRES opt[l][r-1] <= opt[l][r] <= opt[l+1][r]
 * for a consistent leftmost optimal split on ties. Typical sufficient
 * conditions are monotone interval costs with a quadrangle inequality.
 * Kotlin cannot prove this property for arbitrary cost lambdas.
 *
 * O(n^2) cost evaluations, O(n^2) storage. Indices inclusive [l,r].
 * All costs and intermediate sums must fit signed Long.
 */
class KnuthIntervalResult(
    val values: Array<LongArray>,
    val split: Array<IntArray>
) {
    val size: Int get() = values.size

    fun cost(from: Int, through: Int): Long {
        require(from in 0 until size && through in from until size)
        return values[from][through]
    }

    fun optimalSplit(from: Int, through: Int): Int {
        require(from in 0 until size && through in from until size)
        return split[from][through]
    }
}

fun knuthIntervalDp(
    n: Int,
    cost: (from: Int, through: Int) -> Long
): KnuthIntervalResult {
    require(n >= 0)
    val dp = Array(n) { LongArray(n) }
    val opt = Array(n) { IntArray(n) { -1 } }
    for (i in 0 until n) opt[i][i] = i

    for (length in 2..n) {
        for (from in 0..n - length) {
            val through = from + length - 1
            val first = maxOf(from, opt[from][through - 1])
            val last = minOf(through - 1, opt[from + 1][through])
            check(first <= last) {
                "Knuth monotonicity bounds inconsistent on [$from,$through]"
            }
            val base = cost(from, through)
            var best = Long.MAX_VALUE
            var chosen = first
            for (k in first..last) {
                val candidate = dp[from][k] + dp[k + 1][through] + base
                if (candidate < best) {
                    best = candidate
                    chosen = k
                }
            }
            dp[from][through] = best
            opt[from][through] = chosen
        }
    }
    return KnuthIntervalResult(dp, opt)
}
