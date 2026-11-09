fun main() {
    val min = longArrayOf(12L, 4L, 8L).rangeIndex(LongMinMonoid)
    check(min.query(0, 3) == 4L)
    val xor = longArrayOf(1L, 3L, 1L).rangeIndex(LongXorMonoid)
    check(xor.query(0, 3) == 3L)
    val gcd = longArrayOf(12L, 18L, 6L).rangeIndex(LongGcdMonoid)
    check(gcd.query(0, 3) == 6L)
    val on = longArrayOf(2L, 4L).onlineLongPoints(LongMaxMonoid) {
        set(0, 10L)
        query(0, 2)
    }
    check(on == 10L)
    val batched = intArrayOf(1, 2, 3).blockAssignSum()
    batched.recompute(0, 3) { _, old -> old * 2 }
    batched.setMany(intArrayOf(0, 2), intArrayOf(10, 30))
    check(batched.sum(0, 3) == 44L)
    println("standalone primitive monoids OK")
}
