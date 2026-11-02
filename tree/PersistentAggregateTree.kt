/**
 * Generic persistent segment tree for any associative operation with identity.
 * Each update returns a new version ID, without mutating older versions.
 * O(n) initial build, O(log n) queries and point updates, O(log n) new nodes
 * per version. Order is retained for noncommutative operations.
 */
class PersistentAggregateTree<T>(values: List<T>, val op: AggregateOp<T>) {
    val size: Int = values.size
    private data class Node<T>(val value: T, val left: Node<T>?, val right: Node<T>?)
    private val roots = ArrayList<Node<T>?>()
    val versionCount: Int get() = roots.size

    init { roots.add(if (size == 0) null else build(values, 0, size)) }

    private fun build(values: List<T>, lo: Int, hi: Int): Node<T> {
        if (hi - lo == 1) return Node(values[lo], null, null)
        val mid = lo + (hi - lo) / 2
        val left = build(values, lo, mid)
        val right = build(values, mid, hi)
        return Node(op.combine(left.value, right.value), left, right)
    }

    private fun root(version: Int): Node<T>? {
        require(version in roots.indices) { "Version $version out of bounds" }
        return roots[version]
    }

    private fun checkIndex(index: Int) { require(index in 0 until size) { "Index $index out of bounds" } }
    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from,$until)" }
    }

    /** Replace a point and return a new, independent version. */
    fun set(version: Int, index: Int, value: T): Int {
        val previous = root(version)
        checkIndex(index)
        roots.add(replace(previous!!, 0, size, index, value))
        return roots.lastIndex
    }

    /** Combine a delta at a point. For SUM this adds; for XOR this toggles. */
    fun apply(version: Int, index: Int, delta: T): Int =
        set(version, index, op.combine(get(version, index), delta))

    private fun replace(node: Node<T>, lo: Int, hi: Int, index: Int, value: T): Node<T> {
        if (hi - lo == 1) return Node(value, null, null)
        val mid = lo + (hi - lo) / 2
        return if (index < mid) {
            val left = replace(node.left!!, lo, mid, index, value)
            Node(op.combine(left.value, node.right!!.value), left, node.right)
        } else {
            val right = replace(node.right!!, mid, hi, index, value)
            Node(op.combine(node.left!!.value, right.value), node.left, right)
        }
    }

    fun query(version: Int, from: Int, until: Int): T {
        val node = root(version)
        checkRange(from, until)
        return if (from == until) op.initial else range(node!!, 0, size, from, until)
    }

    private fun range(node: Node<T>, lo: Int, hi: Int, from: Int, until: Int): T {
        if (from <= lo && hi <= until) return node.value
        val mid = lo + (hi - lo) / 2
        if (until <= mid) return range(node.left!!, lo, mid, from, until)
        if (from >= mid) return range(node.right!!, mid, hi, from, until)
        return op.combine(range(node.left!!, lo, mid, from, until),
            range(node.right!!, mid, hi, from, until))
    }

    fun get(version: Int, index: Int): T {
        checkIndex(index)
        return query(version, index, index + 1)
    }

    fun all(version: Int): T = root(version)?.value ?: op.initial
}

fun IntArray.persistentSegmentTree(op: AggregateOp<Int>): PersistentAggregateTree<Int> =
    PersistentAggregateTree(asList(), op)
fun LongArray.persistentSegmentTree(op: AggregateOp<Long>): PersistentAggregateTree<Long> =
    PersistentAggregateTree(asList(), op)
fun <T> List<T>.persistentSegmentTree(op: AggregateOp<T>): PersistentAggregateTree<T> =
    PersistentAggregateTree(this, op)
fun <T> Array<out T>.persistentSegmentTree(op: AggregateOp<T>): PersistentAggregateTree<T> =
    PersistentAggregateTree(asList(), op)
