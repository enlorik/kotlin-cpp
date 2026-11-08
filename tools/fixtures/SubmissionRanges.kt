fun main() {
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val values = intArrayOf(2, 4, 6)
    val staticIndex = values.rangeIndex(sum)
    check(staticIndex.query(0, 3) == 12)
    val changing = values.rangeIndex(sum, mutable = true)
    changing.set(1, 10)
    check(changing.query(0, 3) == 18)
    println("submission ranges OK")
}
