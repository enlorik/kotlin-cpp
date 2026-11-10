import kotlin.random.Random

private fun bruteKnuth(n: Int, cost: (Int,Int)->Long): Pair<Array<LongArray>,Array<IntArray>> {
    val dp = Array(n) { LongArray(n) }
    val opt = Array(n) { IntArray(n) { -1 } }
    for (i in 0 until n) opt[i][i] = i
    for (len in 2..n) for (l in 0..n-len) {
        val r = l+len-1
        var best = Long.MAX_VALUE
        var arg = -1
        for (k in l until r) {
            val candidate = dp[l][k]+dp[k+1][r]+cost(l,r)
            if (candidate < best) { best=candidate; arg=k }
        }
        dp[l][r] = best
        opt[l][r] = arg
    }
    return dp to opt
}

fun main() {
    check(knuthIntervalDp(0) { _,_ -> 0L }.size == 0)
    check(knuthIntervalDp(1) { _,_ -> 0L }.cost(0,0) == 0L)
    check(runCatching { knuthIntervalDp(-1) { _,_ -> 0L } }.isFailure)
    check(runCatching { longArrayOf().subsetZeta() }.isFailure)
    check(runCatching { longArrayOf(1,2,3).supersetZeta() }.isFailure)
    check(intArrayOf(1,2).subsetZetaLong().contentEquals(longArrayOf(1,3)))
    check(intArrayOf(1,2).supersetZetaLong().contentEquals(longArrayOf(3,2)))

    val rng = Random(20261110)
    var checks = 0
    repeat(300) {
        val n = rng.nextInt(1,30)
        val weights = LongArray(n) { rng.nextLong(1,20) }
        val prefix = LongArray(n+1)
        for (i in 0 until n) prefix[i+1] = prefix[i]+weights[i]
        val cost = { l:Int, r:Int -> prefix[r+1]-prefix[l] }
        val optimized = knuthIntervalDp(n,cost)
        val (dp, opt) = bruteKnuth(n,cost)
        for (i in 0 until n) for (j in i until n) {
            check(optimized.values[i][j] == dp[i][j])
            check(optimized.split[i][j] == opt[i][j])
            checks++
        }
    }

    repeat(330) {
        val bits = rng.nextInt(0,8)
        val n = 1 shl bits
        val a = LongArray(n) { rng.nextLong(-20,21) }
        val b = LongArray(n) { rng.nextLong(-20,21) }
        val zeta = a.subsetZeta()
        val superZeta = a.supersetZeta()
        check(zeta.subsetMobius().contentEquals(a))
        check(superZeta.supersetMobius().contentEquals(a))
        for (mask in 0 until n) {
            val sub = (0 until n).filter { (it and mask) == it }.sumOf { a[it] }
            val sup = (0 until n).filter { (it and mask) == mask }.sumOf { a[it] }
            check(zeta[mask] == sub)
            check(superZeta[mask] == sup)
            checks++
        }
        val orExpected = LongArray(n)
        val andExpected = LongArray(n)
        for (x in 0 until n) for (y in 0 until n) {
            orExpected[x or y] += a[x]*b[y]
            andExpected[x and y] += a[x]*b[y]
        }
        check(orConvolution(a,b).contentEquals(orExpected))
        check(andConvolution(a,b).contentEquals(andExpected))
    }
    println("PASS: Knuth interval DP and subset/Mobius transforms ($checks oracle comparisons)")
}
