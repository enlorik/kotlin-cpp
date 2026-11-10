/**
 * Online palindromic tree (eertree) over Kotlin Char / UTF-16 code units.
 * Each distinct palindrome is represented by one node, at most n+2 nodes.
 * Append amortized O(1) hash-map operations per character.
 */
data class PalindromeOccurrence(
    val palindrome: String,
    val length: Int,
    val occurrences: Long
)

class PalindromicTree(source: String = "") {
    private class Node(
        val length: Int,
        var link: Int = 0,
        val edges: HashMap<Char, Int> = HashMap(),
        val firstEnd: Int = -1,
        var terminal: Long = 0L,
        var suffixPalindromes: Int = 0
    )
    private val states = ArrayList<Node>()
    private val chars = StringBuilder()
    private var longestNode = 1
    private var last = 1
    private var total = 0L
    private var occurrencesCache: LongArray? = null

    init {
        // Odd root length -1, even root length 0.
        states.add(Node(-1, 0))
        states.add(Node(0, 0))
        states[0].link = 0
        states[1].link = 0
        append(source)
    }

    val length: Int get() = chars.length
    val distinctPalindromeCount: Int get() = states.size - 2
    val totalPalindromeCount: Long get() = total
    val palindromicSuffixCount: Int get() = states[last].suffixPalindromes

    fun append(text: CharSequence) {
        for (ch in text) append(ch)
    }

    fun append(ch: Char) {
        val index = chars.length
        chars.append(ch)
        var found = last
        while (true) {
            val previous = index - 1 - states[found].length
            if (previous >= 0 && chars[previous] == ch) break
            found = states[found].link
        }
        val existing = states[found].edges[ch]
        if (existing != null) {
            last = existing
        } else {
            val lengthOfNew = states[found].length + 2
            val newId = states.size
            val node = Node(lengthOfNew, firstEnd = index)
            states.add(node)
            states[found].edges[ch] = newId
            if (lengthOfNew == 1) {
                node.link = 1
            } else {
                var suffixCandidate = states[found].link
                while (true) {
                    val previous = index - 1 - states[suffixCandidate].length
                    if (previous >= 0 && chars[previous] == ch) break
                    suffixCandidate = states[suffixCandidate].link
                }
                node.link = states[suffixCandidate].edges[ch]!!
            }
            node.suffixPalindromes = 1 + states[node.link].suffixPalindromes
            last = newId
            if (states[last].length > states[longestNode].length) longestNode = last
        }
        states[last].terminal++
        total += states[last].suffixPalindromes.toLong()
        occurrencesCache = null
    }

    fun longestPalindrome(): String {
        val node = states[longestNode]
        if (node.length == 0) return ""
        return chars.substring(node.firstEnd - node.length + 1, node.firstEnd + 1)
    }

    private fun counts(): LongArray {
        occurrencesCache?.let { return it }
        val count = LongArray(states.size) { states[it].terminal }
        // Every new palindrome is longer than its suffix link, so descending
        // creation index processes longer to shorter. Cloned states do not exist.
        for (id in states.lastIndex downTo 2) {
            count[states[id].link] += count[id]
        }
        occurrencesCache = count
        return count
    }

    /**
     * Enumerate all distinct palindromes and their overlapping frequencies.
     * The returned strings may require Theta(n^2) output size in the worst case.
     */
    fun palindromes(): List<PalindromeOccurrence> {
        val occurrences = counts()
        return (2 until states.size).map { id ->
            val node = states[id]
            PalindromeOccurrence(
                chars.substring(node.firstEnd - node.length + 1, node.firstEnd + 1),
                node.length, occurrences[id]
            )
        }
    }
}

fun String.palindromicTree(): PalindromicTree = PalindromicTree(this)
