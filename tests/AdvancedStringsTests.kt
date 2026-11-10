import kotlin.random.Random

private fun bruteSubstrings(s: String): Map<String, Int> {
    val count = HashMap<String, Int>()
    for (i in s.indices) for (j in i + 1..s.length) {
        val x = s.substring(i, j)
        count[x] = (count[x] ?: 0) + 1
    }
    return count
}

private fun brutePalindromes(s: String): Map<String, Int> =
    bruteSubstrings(s).filterKeys { str -> str == str.reversed() }

private fun bruteLcs(a: String, b: String): Int {
    var best = 0
    for (i in a.indices) for (j in i + 1..a.length) {
        if (b.contains(a.substring(i,j))) best = maxOf(best, j-i)
    }
    return best
}

fun main() {
    val emptySam = "".suffixAutomaton()
    check(emptySam.distinctSubstringCount() == 0L)
    check(emptySam.contains("") && emptySam.countOccurrences("") == 1L)
    check(emptySam.longestCommonSubstring("hello") == "")
    val sample = "ababa".suffixAutomaton()
    check(sample.countOccurrences("aba") == 2L)
    check(sample.countOccurrences("ba") == 2L)
    check(sample.countOccurrences("abb") == 0L)
    check(sample.countOccurrences("") == 6L)
    check(sample.longestCommonSubstring("xxbaba!") == "baba")
    sample.append('b')
    check(sample.countOccurrences("bab") == 2L)

    val emptyPal = "".palindromicTree()
    check(emptyPal.totalPalindromeCount == 0L)
    check(emptyPal.distinctPalindromeCount == 0)
    check(emptyPal.longestPalindrome() == "")
    val p = "ababa".palindromicTree()
    check(p.distinctPalindromeCount == 5)
    check(p.totalPalindromeCount == 9L)
    check(p.longestPalindrome() == "ababa")
    check(p.palindromicSuffixCount == 3)
    check(p.palindromes().associate { it.palindrome to it.occurrences } ==
        mapOf("a" to 3L, "b" to 2L, "aba" to 2L, "bab" to 1L, "ababa" to 1L))

    val rng = Random(120711)
    var cases = 0
    repeat(500) {
        val n = rng.nextInt(0, 38)
        val chars = CharArray(n) { "abc"[rng.nextInt(3)] }
        val s = String(chars)
        val sam = SuffixAutomaton()
        val tree = PalindromicTree()
        for (i in s.indices) {
            sam.append(s[i])
            tree.append(s[i])
            val prefix = s.substring(0,i+1)
            val all = bruteSubstrings(prefix)
            val pals = brutePalindromes(prefix)
            check(sam.distinctSubstringCount() == all.size.toLong())
            check(tree.distinctPalindromeCount == pals.size)
            check(tree.totalPalindromeCount == pals.values.sum().toLong())
            check(tree.longestPalindrome().length ==
                (pals.keys.maxOfOrNull { it.length } ?: 0))
            val palResults = tree.palindromes().associate { it.palindrome to it.occurrences }
            check(palResults == pals.mapValues { it.value.toLong() })
        }
        val all = bruteSubstrings(s)
        repeat(25) {
            val pattern = if (s.isNotEmpty() && rng.nextBoolean()) {
                val from = rng.nextInt(s.length)
                val end = rng.nextInt(from+1,s.length+1)
                s.substring(from,end)
            } else (0 until rng.nextInt(0, 6))
                .map { "abc"[rng.nextInt(3)] }.joinToString("")
            check(sam.contains(pattern) == (pattern.isEmpty() || s.contains(pattern)))
            check(sam.countOccurrences(pattern) ==
                if (pattern.isEmpty()) s.length.toLong()+1
                else (all[pattern] ?: 0).toLong())
            val other = (0 until rng.nextInt(0, 30))
                .map { "abc"[rng.nextInt(3)] }.joinToString("")
            val common = sam.longestCommonSubstring(other)
            check(s.contains(common) && other.contains(common))
            check(common.length == bruteLcs(s,other))
        }
        cases++
    }
    val unicode = "žž🙂ž".suffixAutomaton()
    check(unicode.countOccurrences("ž") == 3L)
    check("žž".palindromicTree().totalPalindromeCount == 3L)
    println("PASS: suffix automaton and eertree ($cases randomized strings)")
}
