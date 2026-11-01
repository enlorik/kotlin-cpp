/** Static range sums. query(from, until) uses a half-open interval. */
class PrefixSums private constructor(private val prefix: LongArray) {
    val size: Int get() = prefix.size - 1

    fun query(from: Int, until: Int): Long {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        return prefix[until] - prefix[from]
    }

    fun prefix(until: Int): Long = query(0, until)

    companion object {
        fun of(values: LongArray): PrefixSums {
            val p = LongArray(values.size + 1)
            for (i in values.indices) p[i + 1] = p[i] + values[i]
            return PrefixSums(p)
        }
    }
}

/** O(n) preprocessing, O(1) static range sum; totals must fit in Long. */
fun IntArray.prefixSums(): PrefixSums = PrefixSums.of(LongArray(size) { this[it].toLong() })
fun LongArray.prefixSums(): PrefixSums = PrefixSums.of(this)

/** Deferred range additions: each add is O(1), values() materializes in O(n). */
class DifferenceArray private constructor(private val base: LongArray) {
    val size: Int get() = base.size
    private val diff = LongArray(size + 1)

    fun add(from: Int, until: Int, delta: Long) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        diff[from] += delta
        diff[until] -= delta
    }

    /** Non-destructive: further updates are allowed after materialization. */
    fun values(): LongArray {
        val result = base.copyOf()
        var change = 0L
        for (i in result.indices) {
            change += diff[i]
            result[i] += change
        }
        return result
    }

    companion object {
        fun of(values: LongArray): DifferenceArray = DifferenceArray(values.copyOf())
        fun zeros(size: Int): DifferenceArray {
            require(size >= 0) { "Size must be nonnegative" }
            return DifferenceArray(LongArray(size))
        }
    }
}

fun differenceArray(n: Int): DifferenceArray = DifferenceArray.zeros(n)
fun IntArray.differenceArray(): DifferenceArray = DifferenceArray.of(LongArray(size) { this[it].toLong() })
fun LongArray.differenceArray(): DifferenceArray = DifferenceArray.of(this)

/** Mutable point additions and prefix/range sums, all O(log n). */
class FenwickTree private constructor(private val elements: LongArray) {
    val size: Int get() = elements.size
    private val bit = LongArray(size + 1)

    init {
        for (i in elements.indices) {
            bit[i + 1] += elements[i]
            val parent = (i + 1) + ((i + 1) and -(i + 1))
            if (parent <= size) bit[parent] += bit[i + 1]
        }
    }

    operator fun get(index: Int): Long {
        require(index in 0 until size) { "Index out of bounds" }
        return elements[index]
    }

    fun add(index: Int, delta: Long) {
        require(index in 0 until size) { "Index out of bounds" }
        elements[index] += delta
        var i = index + 1
        while (i <= size) {
            bit[i] += delta
            i += i and -i
        }
    }

    fun set(index: Int, value: Long) = add(index, value - get(index))

    fun sum(until: Int): Long {
        require(until in 0..size) { "Prefix end out of bounds" }
        var result = 0L
        var i = until
        while (i > 0) {
            result += bit[i]
            i -= i and -i
        }
        return result
    }

    fun query(from: Int, until: Int): Long {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        return sum(until) - sum(from)
    }

    /** First index whose inclusive prefix sum reaches target; null if none.
     *  Only valid while all stored elements are nonnegative. */
    fun findPrefix(target: Long): Int? {
        if (size == 0) return null
        if (target <= 0L) return 0
        var pos = 0
        var sum = 0L
        var step = Integer.highestOneBit(size)
        while (step > 0) {
            val next = pos + step
            if (next <= size && bit[next] < target - sum) {
                sum += bit[next]
                pos = next
            }
            step = step ushr 1
        }
        return if (pos == size) null else pos
    }

    companion object {
        fun of(values: LongArray): FenwickTree = FenwickTree(values.copyOf())
        fun zeros(size: Int): FenwickTree {
            require(size >= 0) { "Size must be nonnegative" }
            return FenwickTree(LongArray(size))
        }
    }
}

fun fenwickTree(n: Int): FenwickTree = FenwickTree.zeros(n)
fun IntArray.fenwickTree(): FenwickTree = FenwickTree.of(LongArray(size) { this[it].toLong() })
fun LongArray.fenwickTree(): FenwickTree = FenwickTree.of(this)
