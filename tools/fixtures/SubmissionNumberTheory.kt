fun main() {
    check(isPrime64(1000000007L))
    check(factor64(360L) == mapOf(2L to 3,3L to 2,5L to 1))
    check(modPower64(2,10,1000) == 24L)
    check(chineseRemainder64(listOf(2L to 3L,3L to 5L))!!.residue.toString() == "8")
    check(convolution998244353(intArrayOf(1,2),intArrayOf(3,4))
        .contentEquals(intArrayOf(3,10,8)))
    println("standalone number theory and NTT OK")
}
