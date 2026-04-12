// Digit DP template
// Counts numbers in [0, n] where n is given as a decimal string.
// The caller-supplied lambda receives (pos, tight, leadZero, extra) and is called
// by the framework to build the memoisation table; the framework iterates over digits
// 0..9 and accumulates results.  extra is passed through unchanged — use it for
// any custom accumulated state you need (e.g. digit-sum modulo k).
//
// isMod / mod: when isMod is true the returned count is taken modulo mod.

fun digitDP(
    n: String,
    isMod: Boolean = false,
    mod: Long = 1_000_000_007L,
    dp: (pos: Int, tight: Boolean, leadZero: Boolean, extra: Int) -> Long
): Long {
    // We expose the framework; callers that need full control should implement their
    // own digit-DP using the pattern shown below.
    return dp(0, true, true, 0).let { if (isMod) it % mod else it }
}

// Full digit-DP example: count integers in [0..N] whose digit sum <= k.
// This shows the canonical pattern for the template above.
fun countDigitSumAtMost(n: String, k: Int): Long {
    val digits = n.map { it - '0' }
    val len = digits.size
    // memo[pos][tight][leadZero][extra] — extra = running digit sum (capped at k+1)
    val memo = Array(len) { Array(2) { Array(2) { arrayOfNulls<Long>(k + 2) } } }

    fun solve(pos: Int, tight: Boolean, leadZero: Boolean, sum: Int): Long {
        if (pos == len) return if (sum <= k) 1L else 0L
        val t = if (tight) 1 else 0
        val lz = if (leadZero) 1 else 0
        val cappedSum = minOf(sum, k + 1)
        memo[pos][t][lz][cappedSum]?.let { return it }
        val limit = if (tight) digits[pos] else 9
        var ans = 0L
        for (d in 0..limit) {
            val newSum = if (leadZero && d == 0) 0 else sum + d
            ans += solve(pos + 1, tight && d == limit, leadZero && d == 0, newSum)
        }
        memo[pos][t][lz][cappedSum] = ans
        return ans
    }
    return solve(0, tight = true, leadZero = true, sum = 0)
}
