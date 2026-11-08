fun main() {
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val result = intArrayOf(2, 4, 6).rangeProgram(sum) {
        query(0, 3)
        set(1, 10)
        query(0, 3)
    }
    check(result.answers == listOf(12, 18))
    check(result.strategy == RangeIndexStrategy.POINT_UPDATES)
    println("submission range program OK")
}
