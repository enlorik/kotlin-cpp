import kotlin.math.abs

fun main() {
    val numbers = intArrayOf(2, 4, 6, 8)
    val tree = numbers.segmentTree()
    check(tree.query(1, 3) == 10)
    tree[2] = 10
    check(tree[1..3] == 22)
    check(firstTrue(numbers.size) { numbers[it] >= 6 } == 2)
    check(abs(-7) == 7)
    println("submission basics OK")
}
