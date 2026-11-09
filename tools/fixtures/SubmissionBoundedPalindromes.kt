fun main() {
    val index = "abacaba".boundedPalindromes(4)
    check(index.count(0, 3) == 4L)
    index.assign(0, 3, 'c')
    check(index.count(0, 4) == 10L)
    val blocks = intArrayOf(1, 2, 3).blockAssignSum()
    blocks.assign(0, 2, 5)
    check(blocks.sum(0, 3) == 13L)
    println("standalone bounded-palindromes OK")
}
