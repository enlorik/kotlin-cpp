/**
 * Online suffix automaton over Kotlin Char values (UTF-16 code units).
 * Append amortized O(1) map operations per character, O(n) states.
 * Substring membership and occurrences O(pattern.length), after one
 * cached O(number of states) end-position propagation.
 */
class SuffixAutomaton(source: String = "") {
    private class State(
        var length: Int = 0,
        var suffix: Int = -1,
        val edges: HashMap<Char, Int> = HashMap(),
        var terminals: Long = 0L
    )
    private val nodes = ArrayList<State>()
    private var last = 0
    private var occurrencesCache: LongArray? = null
    var length: Int = 0
        private set
    val stateCount: Int get() = nodes.size

    init {
        nodes.add(State())
        append(source)
    }

    fun append(text: CharSequence) {
        for (ch in text) append(ch)
    }

    fun append(ch: Char) {
        val current = nodes.size
        nodes.add(State(length = nodes[last].length + 1, terminals = 1L))
        var parent = last
        while (parent != -1 && !nodes[parent].edges.containsKey(ch)) {
            nodes[parent].edges[ch] = current
            parent = nodes[parent].suffix
        }
        if (parent == -1) {
            nodes[current].suffix = 0
        } else {
            val next = nodes[parent].edges[ch]!!
            if (nodes[parent].length + 1 == nodes[next].length) {
                nodes[current].suffix = next
            } else {
                val clone = nodes.size
                nodes.add(State(
                    length = nodes[parent].length + 1,
                    suffix = nodes[next].suffix,
                    edges = HashMap(nodes[next].edges),
                    terminals = 0L
                ))
                while (parent != -1 && nodes[parent].edges[ch] == next) {
                    nodes[parent].edges[ch] = clone
                    parent = nodes[parent].suffix
                }
                nodes[next].suffix = clone
                nodes[current].suffix = clone
            }
        }
        last = current
        length++
        occurrencesCache = null
    }

    fun contains(pattern: CharSequence): Boolean {
        var current = 0
        for (ch in pattern) {
            current = nodes[current].edges[ch] ?: return false
        }
        return true
    }

    fun distinctSubstringCount(): Long {
        var total = 0L
        for (i in 1 until nodes.size) {
            val node = nodes[i]
            total += (node.length - nodes[node.suffix].length).toLong()
        }
        return total
    }

    private fun occurrenceCounts(): LongArray {
        occurrencesCache?.let { return it }
        val maxLength = length
        val histogram = IntArray(maxLength + 1)
        for (node in nodes) histogram[node.length]++
        for (i in 1..maxLength) histogram[i] += histogram[i - 1]
        val order = IntArray(nodes.size)
        for (i in nodes.indices.reversed()) {
            val len = nodes[i].length
            order[--histogram[len]] = i
        }
        val result = LongArray(nodes.size) { nodes[it].terminals }
        for (i in order.size - 1 downTo 1) {
            val v = order[i]
            val p = nodes[v].suffix
            result[p] += result[v]
        }
        occurrencesCache = result
        return result
    }

    /** Number of appearances (possibly overlapping). Empty string: n + 1. */
    fun countOccurrences(pattern: CharSequence): Long {
        if (pattern.isEmpty()) return length.toLong() + 1
        var current = 0
        for (ch in pattern) {
            current = nodes[current].edges[ch] ?: return 0L
        }
        return occurrenceCounts()[current]
    }

    /** Find the earliest-ending longest common substring of the other text. */
    fun longestCommonSubstring(other: CharSequence): String {
        var current = 0
        var lengthHere = 0
        var bestLength = 0
        var bestEnd = 0
        for (i in other.indices) {
            val ch = other[i]
            while (current != 0 && !nodes[current].edges.containsKey(ch)) {
                current = nodes[current].suffix
                lengthHere = nodes[current].length
            }
            val next = nodes[current].edges[ch]
            if (next == null) {
                current = 0
                lengthHere = 0
            } else {
                current = next
                lengthHere++
                if (lengthHere > bestLength) {
                    bestLength = lengthHere
                    bestEnd = i + 1
                }
            }
        }
        return other.subSequence(bestEnd - bestLength, bestEnd).toString()
    }
}

fun String.suffixAutomaton(): SuffixAutomaton = SuffixAutomaton(this)
