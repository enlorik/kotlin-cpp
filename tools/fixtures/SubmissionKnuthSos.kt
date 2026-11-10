fun main() {
    val dp = knuthIntervalDp(3) { l,r -> (r-l+1).toLong() }
    check(dp.cost(0,2) == 5L)
    val a = longArrayOf(1,2,3,4)
    check(a.subsetZeta().contentEquals(longArrayOf(1,3,4,10)))
    check(a.subsetZeta().subsetMobius().contentEquals(a))
    check(orConvolution(longArrayOf(1,2),longArrayOf(3,4))
        .contentEquals(longArrayOf(3,18)))
    println("standalone Knuth and SOS DP OK")
}
