/**
 * Immutable historical versions of a range-sum segment tree.
 * Each set/add creates a new version in O(log n) time and nodes, preserving
 * every previous root. Query any version in O(log n), half-open ranges.
 */
class PersistentSegmentTree private constructor(values: LongArray) {
    val size: Int = values.size
    private data class Node(val sum: Long, val left: Node?, val right: Node?)
    private val roots = ArrayList<Node?>()
    val versionCount: Int get() = roots.size

    init { roots.add(if (size == 0) null else build(values, 0, size)) }

    private fun build(a: LongArray, lo: Int, hi: Int): Node {
        if (hi - lo == 1) return Node(a[lo], null, null)
        val mid = lo + (hi - lo) / 2
        val left = build(a, lo, mid)
        val right = build(a, mid, hi)
        return Node(left.sum + right.sum, left, right)
    }

    private fun root(version: Int): Node? {
        require(version in roots.indices) { "Version $version out of bounds" }
        return roots[version]
    }

    private fun checkIndex(index: Int) { require(index in 0 until size) { "Index out of bounds" } }
    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from,$until)" }
    }

    /** Replace one element in a version; returns a new version ID. */
    fun set(version: Int, index: Int, value: Long): Int {
        val old = root(version)
        checkIndex(index)
        roots.add(replace(old!!, 0, size, index, value))
        return roots.lastIndex
    }

    /** Add delta at an index; returns a new version ID. */
    fun add(version: Int, index: Int, delta: Long): Int = set(version, index, get(version, index) + delta)

    private fun replace(node: Node, lo: Int, hi: Int, index: Int, value: Long): Node {
        if (hi - lo == 1) return Node(value, null, null)
        val mid = lo + (hi - lo) / 2
        return if (index < mid) {
            val left = replace(node.left!!, lo, mid, index, value)
            Node(left.sum + node.right!!.sum, left, node.right)
        } else {
            val right = replace(node.right!!, mid, hi, index, value)
            Node(node.left!!.sum + right.sum, node.left, right)
        }
    }

    fun query(version: Int, from: Int, until: Int): Long {
        val old = root(version)
        checkRange(from, until)
        return if (from == until) 0L else range(old!!, 0, size, from, until)
    }

    private fun range(node: Node, lo: Int, hi: Int, from: Int, until: Int): Long {
        if (from <= lo && hi <= until) return node.sum
        val mid = lo + (hi - lo) / 2
        var result = 0L
        if (from < mid) result += range(node.left!!, lo, mid, from, until)
        if (until > mid) result += range(node.right!!, mid, hi, from, until)
        return result
    }

    fun get(version: Int, index: Int): Long {
        checkIndex(index)
        return query(version, index, index + 1)
    }

    fun all(version: Int): Long = root(version)?.sum ?: 0L

    companion object {
        fun of(values: LongArray): PersistentSegmentTree = PersistentSegmentTree(values.copyOf())
        fun zeros(size: Int): PersistentSegmentTree {
            require(size >= 0) { "Size must be nonnegative" }
            return PersistentSegmentTree(LongArray(size))
        }
    }
}

fun persistentSegmentTree(n: Int): PersistentSegmentTree = PersistentSegmentTree.zeros(n)
fun IntArray.persistentSegmentTree(): PersistentSegmentTree =
    PersistentSegmentTree.of(LongArray(size) { this[it].toLong() })
fun LongArray.persistentSegmentTree(): PersistentSegmentTree = PersistentSegmentTree.of(this)
