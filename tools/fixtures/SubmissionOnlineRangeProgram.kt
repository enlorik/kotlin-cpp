fun main() {
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val online = intArrayOf(2, 4, 6).onlineRangeProgram(sum) {
        val previous = query(0, 3)
        set(1, previous)
        query(0, 3)
    }
    check(online == 20)

    val addition = action<Int, Int>(
        apply = { total, delta, length -> total + delta * length },
        compose = { old, newer -> old + newer }
    )
    val lazy = intArrayOf(2, 4, 6).onlineRangeProgram(sum, addition) {
        update(0, 2, 3)
        val intermediate = query(0, 3)
        update(2, 3, intermediate)
        query(0, 3)
    }
    check(lazy == 36)
    println("standalone online range DSL OK")
}
