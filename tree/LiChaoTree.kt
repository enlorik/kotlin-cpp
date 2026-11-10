/**
 * Online Li Chao Tree over an INCLUSIVE Long x-domain.
 *
 * Supports minimum or maximum of integer lines f(x) = slope*x + intercept,
 * plus lines restricted to an inclusive x-interval. Query O(log width),
 * full line insertion O(log width), segment insertion O(log^2 width).
 * Values and evaluations must fit in signed Long without overflow.
 */
enum class LiChaoObjective { MIN, MAX }

data class LiChaoLine(val slope: Long, val intercept: Long) {
    fun at(x: Long): Long = slope * x + intercept
}

class LiChaoTree(
    val minX: Long,
    val maxX: Long,
    val objective: LiChaoObjective = LiChaoObjective.MIN
) {
    init { require(minX <= maxX) { "Empty Li Chao domain" } }

    private class Node(
        var line: LiChaoLine? = null,
        var left: Node? = null,
        var right: Node? = null
    )

    private val root = Node()
    var lineCount: Int = 0
        private set

    private fun better(x: Long, y: Long): Boolean =
        if (objective == LiChaoObjective.MIN) x < y else x > y

    /** Overflow-safe signed midpoint (even across [Long.MIN_VALUE, Long.MAX_VALUE]). */
    private fun middle(lo: Long, hi: Long): Long =
        (lo and hi) + ((lo xor hi) shr 1)

    fun addLine(slope: Long, intercept: Long) {
        insert(root, minX, maxX, LiChaoLine(slope, intercept))
        lineCount++
    }

    /**
     * A line is active only for x in [fromX, untilX], both inclusive.
     * Outside that interval it does not contribute to queries.
     */
    fun addSegment(
        fromX: Long,
        untilX: Long,
        slope: Long,
        intercept: Long
    ) {
        require(fromX >= minX && untilX <= maxX && fromX <= untilX) {
            "Segment must be nonempty and contained in domain"
        }
        addSegmentRec(root, minX, maxX, fromX, untilX, LiChaoLine(slope, intercept))
        lineCount++
    }

    private fun insert(node: Node, lo: Long, hi: Long, fresh: LiChaoLine) {
        val old = node.line
        if (old == null) {
            node.line = fresh
            return
        }
        val mid = middle(lo, hi)
        val leftWins = better(fresh.at(lo), old.at(lo))
        val midWins = better(fresh.at(mid), old.at(mid))
        var displaced = fresh
        if (midWins) {
            node.line = fresh
            displaced = old
        }
        if (lo == hi) return
        if (leftWins != midWins) {
            if (node.left == null) node.left = Node()
            insert(node.left!!, lo, mid, displaced)
        } else {
            if (node.right == null) node.right = Node()
            insert(node.right!!, mid + 1L, hi, displaced)
        }
    }

    private fun addSegmentRec(
        node: Node,
        lo: Long,
        hi: Long,
        from: Long,
        until: Long,
        line: LiChaoLine
    ) {
        if (from <= lo && hi <= until) {
            insert(node, lo, hi, line)
            return
        }
        val mid = middle(lo, hi)
        if (from <= mid) {
            if (node.left == null) node.left = Node()
            addSegmentRec(node.left!!, lo, mid, from, until, line)
        }
        if (until > mid) {
            if (node.right == null) node.right = Node()
            addSegmentRec(node.right!!, mid + 1L, hi, from, until, line)
        }
    }

    /** Null means no line covers this x. */
    fun query(x: Long): Long? {
        require(x in minX..maxX) { "x $x is outside [$minX,$maxX]" }
        var current: Node? = root
        var lo = minX
        var hi = maxX
        var answer: Long? = null
        while (current != null) {
            val value = current.line?.at(x)
            if (value != null) {
                answer = if (answer == null || better(value, answer)) value else answer
            }
            if (lo == hi) break
            val mid = middle(lo, hi)
            if (x <= mid) {
                current = current.left
                hi = mid
            } else {
                current = current.right
                lo = mid + 1L
            }
        }
        return answer
    }
}

fun LongRange.liChao(objective: LiChaoObjective = LiChaoObjective.MIN): LiChaoTree {
    require(!isEmpty()) { "Empty Li Chao domain" }
    return LiChaoTree(first, last, objective)
}
