fun main() {
    val sam = "banana".suffixAutomaton()
    check(sam.countOccurrences("ana") == 2L)
    check(sam.contains("nan"))
    check(sam.longestCommonSubstring("anagram") == "ana")
    val pal = "ababa".palindromicTree()
    check(pal.distinctPalindromeCount == 5)
    check(pal.totalPalindromeCount == 9L)
    check(pal.longestPalindrome() == "ababa")
    println("standalone string algorithms OK")
}
