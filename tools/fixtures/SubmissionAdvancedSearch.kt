fun main() {
    check(firstTrueFrom(0) { it >= 13 } == 13)
    check(intArrayOf(2, 4, 6).lowerBound(4) == 1)
    println("submission advanced search OK")
}
