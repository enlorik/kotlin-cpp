/** One pattern occurrence in half-open text interval [from, until). */
data class PatternMatch(val patternIndex: Int, val from: Int, val until: Int)

/**
 * Aho-Corasick multi-pattern index. All patterns must be nonempty.
 * Duplicates remain distinct by patternIndex. Alphabet: UTF-16 Char units.
 *
 * Construction O(total pattern length) expected hash-map operations.
 * Search O(text length + number of occurrences) amortized.
 * Uses dictionary suffix links instead of copying terminal lists to
 * every failure node, avoiding quadratic output storage.
 */
class AhoCorasick(patterns: List<String>) {
    private class Node(
        val edges: HashMap<Char, Int> = HashMap(),
        val terminalIds: ArrayList<Int> = ArrayList(),
        var failure: Int = 0,
        var outputLink: Int = -1
    )
    private val lengths = IntArray(patterns.size)
    private val nodes = ArrayList<Node>()
    val patternCount: Int get() = lengths.size
    val stateCount: Int get() = nodes.size

    init {
        nodes.add(Node())
        for ((id, pattern) in patterns.withIndex()) {
            require(pattern.isNotEmpty()) { "Aho patterns must be nonempty" }
            lengths[id] = pattern.length
            var state = 0
            for (ch in pattern) {
                var next = nodes[state].edges[ch]
                if (next == null) {
                    next = nodes.size
                    nodes.add(Node())
                    nodes[state].edges[ch] = next
                }
                state = next
            }
            nodes[state].terminalIds.add(id)
        }

        val queue = ArrayDeque<Int>()
        for (child in nodes[0].edges.values) queue.addLast(child)
        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            val node = nodes[state]
            for ((ch, child) in node.edges) {
                var fallback = node.failure
                while (fallback != 0 && nodes[fallback].edges[ch] == null)
                    fallback = nodes[fallback].failure
                val next = nodes[fallback].edges[ch] ?: 0
                nodes[child].failure = next
                nodes[child].outputLink =
                    if (nodes[next].terminalIds.isNotEmpty()) next
                    else nodes[next].outputLink
                queue.addLast(child)
            }
        }
    }

    /** All occurrences including overlapping matches and duplicate patterns. */
    fun findAll(text: CharSequence): List<PatternMatch> {
        val matches = ArrayList<PatternMatch>()
        var state = 0
        for (end in text.indices) {
            val ch = text[end]
            while (state != 0 && nodes[state].edges[ch] == null)
                state = nodes[state].failure
            state = nodes[state].edges[ch] ?: 0
            var output = state
            while (output > 0) {
                for (id in nodes[output].terminalIds) {
                    matches.add(PatternMatch(id, end - lengths[id] + 1, end + 1))
                }
                output = nodes[output].outputLink
            }
        }
        return matches
    }

    fun countPerPattern(text: CharSequence): LongArray {
        val counts = LongArray(patternCount)
        var state = 0
        for (ch in text) {
            while (state != 0 && nodes[state].edges[ch] == null)
                state = nodes[state].failure
            state = nodes[state].edges[ch] ?: 0
            var output = state
            while (output > 0) {
                for (id in nodes[output].terminalIds) counts[id]++
                output = nodes[output].outputLink
            }
        }
        return counts
    }
}

fun List<String>.ahoCorasick(): AhoCorasick = AhoCorasick(this)
