fun main() {
    check(PrimeCombinatorics(7,6).lucas(10,3) == 1)
    check(garnerMod(longArrayOf(2,3),longArrayOf(3,5),100) == 8L)
    val bits = intArrayOf(3,5,9).subsetSumBitset(12)
    check(8 in bits && 7 !in bits && bits.bestAtMost() == 12)
}
