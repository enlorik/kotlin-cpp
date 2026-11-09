fun main() {
    val program = longArrayOf(1, 2, 3).onlineLongSum(LongAddAction) {
        val initial = query(0, 3)
        update(1, 3, initial)
        query(0, 3)
    }
    check(program == 18L)
    val index = longArrayOf(1, 2, 3).rangeIndex(LongSumMonoid, LongAssignAction)
    index.update(0, 2, 7L)
    check(index.query(0, 3) == 17L)
    println("standalone primitive long lazy OK")
}
