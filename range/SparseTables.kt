/**
 * Static O(1) range queries for an idempotent associative operation (MIN/MAX/GCD).
 * Build O(n log n). Overlapping power-of-two intervals are only safe if
 * combine(x,x) == x; e.g. SUM is NOT supported.
 */
class SparseTable<T>(values: List<T>, private val op: AggregateOp<T>) {
    val size: Int = values.size
    private val levels = ArrayList<List<T>>()

    init {
        require(op.idempotent) { "SparseTable requires an idempotent operation; use DisjointSparseTable for SUM" }
        if (size > 0) {
            levels.add(values.toList())
            var half = 1
            while (half <= size / 2) {
                val previous = levels.last()
                val block = half * 2
                val current = ArrayList<T>(size - block + 1)
                for (i in 0..size - block) current.add(op.combine(previous[i], previous[i + half]))
                levels.add(current)
                half = block
            }
        }
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        if (from == until) return op.initial
        val len = until - from
        val k = 31 - Integer.numberOfLeadingZeros(len)
        val step = 1 shl k
        return op.combine(levels[k][from], levels[k][until - step])
    }
}

/**
 * Static O(1) queries for ANY associative reducer, including SUM and
 * noncommutative operations such as string concatenation. O(n log n) build.
 */
class DisjointSparseTable<T>(values: List<T>, private val op: AggregateOp<T>) {
    val size: Int = values.size
    private val originals = values.toList()
    private val levels = ArrayList<List<T>>()

    init {
        var half = 1L
        while (half < size) {
            val data = originals.toMutableList()
            val block = half * 2
            var start = 0L
            while (start < size) {
                val center = minOf(start + half, size.toLong()).toInt()
                val end = minOf(start + block, size.toLong()).toInt()
                val begin = start.toInt()
                if (center > begin) {
                    data[center - 1] = originals[center - 1]
                    for (i in center - 2 downTo begin) data[i] = op.combine(originals[i], data[i + 1])
                }
                if (center < end) {
                    data[center] = originals[center]
                    for (i in center + 1 until end) data[i] = op.combine(data[i - 1], originals[i])
                }
                start += block
            }
            levels.add(data)
            half = block
        }
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        if (from == until) return op.initial
        if (until == from + 1) return originals[from]
        val level = 31 - Integer.numberOfLeadingZeros(from xor (until - 1))
        return op.combine(levels[level][from], levels[level][until - 1])
    }
}

fun <T> List<T>.sparseTable(op: AggregateOp<T>): SparseTable<T> = SparseTable(this, op)
fun IntArray.sparseTable(op: AggregateOp<Int>): SparseTable<Int> = SparseTable(asList(), op)
fun LongArray.sparseTable(op: AggregateOp<Long>): SparseTable<Long> = SparseTable(asList(), op)

fun <T> List<T>.disjointSparseTable(op: AggregateOp<T>): DisjointSparseTable<T> = DisjointSparseTable(this, op)
fun IntArray.disjointSparseTable(op: AggregateOp<Int>): DisjointSparseTable<Int> = DisjointSparseTable(asList(), op)
fun LongArray.disjointSparseTable(op: AggregateOp<Long>): DisjointSparseTable<Long> = DisjointSparseTable(asList(), op)
