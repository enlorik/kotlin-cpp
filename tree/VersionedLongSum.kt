/**
 * Persistent Long-sum range index with branching, lazy range assignments and
 * additions. Updates path-copy O(log n) nodes and never alter older versions.
 *
 * Version zero is the initial state. Explicit-version updates append an
 * independent branch without moving currentVersion; updates without a
 * version advance currentVersion. checkout/rollback only move that pointer.
 * [from, until) intervals, Long wraparound arithmetic.
 */
class VersionedLongSum(values: LongArray) {
    val size: Int = values.size
    private data class Node(
        val sum: Long,
        val left: Node? = null,
        val right: Node? = null,
        val hasAssign: Boolean = false,
        val assigned: Long = 0L,
        val added: Long = 0L
    )
    private val roots = ArrayList<Node?>()
    var currentVersion: Int = 0
        private set
    val versionCount: Int get() = roots.size

    init {
        roots.add(if (size == 0) null else build(values, 0, size))
    }

    private fun build(a: LongArray, l: Int, r: Int): Node {
        if (r - l == 1) return Node(a[l])
        val m = l + (r - l) / 2
        val x = build(a, l, m)
        val y = build(a, m, r)
        return Node(x.sum + y.sum, x, y)
    }

    private fun validateRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid range [$from,$until) for size $size"
        }
    }

    private fun root(version: Int): Node? {
        require(version in roots.indices) { "Unknown version $version" }
        return roots[version]
    }

    private fun assignNode(n: Node, len: Int, value: Long): Node =
        n.copy(sum = value * len.toLong(), hasAssign = true, assigned = value, added = 0L)

    private fun addNode(n: Node, len: Int, value: Long): Node =
        n.copy(sum = n.sum + value * len.toLong(), added = n.added + value)

    private fun push(n: Node, leftLen: Int, rightLen: Int): Pair<Node, Node> {
        var x = n.left!!
        var y = n.right!!
        if (n.hasAssign) {
            x = assignNode(x, leftLen, n.assigned)
            y = assignNode(y, rightLen, n.assigned)
        }
        if (n.added != 0L) {
            x = addNode(x, leftLen, n.added)
            y = addNode(y, rightLen, n.added)
        }
        return x to y
    }

    private fun change(n: Node, l: Int, r: Int, from: Int, until: Int,
                       assign: Boolean, value: Long): Node {
        if (from <= l && r <= until) {
            return if (assign) assignNode(n, r - l, value) else addNode(n, r - l, value)
        }
        val m = l + (r - l) / 2
        val (left, right) = push(n, m - l, r - m)
        val x = if (from < m) change(left, l, m, from, until, assign, value) else left
        val y = if (until > m) change(right, m, r, from, until, assign, value) else right
        return Node(x.sum + y.sum, x, y)
    }

    private fun changeVersion(version: Int, from: Int, until: Int,
                              isAssign: Boolean, value: Long): Int {
        val previous = root(version)
        validateRange(from, until)
        roots.add(if (from == until) previous else
            change(previous!!, 0, size, from, until, isAssign, value))
        return roots.lastIndex
    }

    /** Create a new branch from any historical version without switching HEAD. */
    fun add(version: Int, from: Int, until: Int, delta: Long): Int =
        changeVersion(version, from, until, false, delta)

    fun assign(version: Int, from: Int, until: Int, value: Long): Int =
        changeVersion(version, from, until, true, value)

    fun set(version: Int, index: Int, value: Long): Int {
        require(index in 0 until size)
        return assign(version, index, index + 1, value)
    }

    /** Advance the checked-out version with an update and return new version ID. */
    fun add(from: Int, until: Int, delta: Long): Int {
        currentVersion = add(currentVersion, from, until, delta)
        return currentVersion
    }

    fun assign(from: Int, until: Int, value: Long): Int {
        currentVersion = assign(currentVersion, from, until, value)
        return currentVersion
    }

    fun set(index: Int, value: Long): Int {
        currentVersion = set(currentVersion, index, value)
        return currentVersion
    }

    /** Rewind HEAD without destroying a later version; next update branches. */
    fun checkout(version: Int) {
        root(version)
        currentVersion = version
    }

    fun rollback(version: Int) = checkout(version)

    private fun queryNode(n: Node, l: Int, r: Int,
                          from: Int, until: Int): Long {
        if (from <= l && r <= until) return n.sum
        val overlap = minOf(r, until) - maxOf(l, from)
        if (n.hasAssign) return (n.assigned + n.added) * overlap.toLong()
        val m = l + (r - l) / 2
        var result = 0L
        if (from < m) result += queryNode(n.left!!, l, m, from, until)
        if (until > m) result += queryNode(n.right!!, m, r, from, until)
        return result + n.added * overlap.toLong()
    }

    fun query(version: Int, from: Int, until: Int): Long {
        val n = root(version)
        validateRange(from, until)
        return if (from == until) 0L else queryNode(n!!, 0, size, from, until)
    }

    fun query(from: Int, until: Int): Long = query(currentVersion, from, until)
    fun all(version: Int): Long = root(version)?.sum ?: 0L
    fun all(): Long = all(currentVersion)
}

fun LongArray.versionedLongSum(): VersionedLongSum = VersionedLongSum(this)
fun IntArray.versionedLongSum(): VersionedLongSum =
    VersionedLongSum(LongArray(size) { this[it].toLong() })
