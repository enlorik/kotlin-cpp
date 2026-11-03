/**
 * Static prefix aggregates for any associative operation with a two-sided identity.
 *
 * Prefix queries work for every monoid, even a noncommutative one.
 * General [from, until) queries are deliberately absent: removing a
 * prefix requires more structure (typically an inverse).
 *
 * Build O(n); prefix/until and all O(1); memory O(n).
 * Values are read only when the table is built.
 */
class MonoidPrefixTable<T>(values: List<T>, val op: AggregateOp<T>) {
    val size: Int = values.size
    private val prefixes: List<T>

    init {
        val built = ArrayList<T>(values.size + 1)
        built.add(op.initial)
        for (value in values) {
            built.add(op.combine(built.last(), value))
        }
        prefixes = built
    }

    /** Aggregate of [0, until), preserving element order. */
    fun prefix(until: Int): T {
        require(until in 0..size) { "Prefix endpoint $until is outside 0..$size" }
        return prefixes[until]
    }

    /** Aggregate of the entire original sequence. */
    fun all(): T = prefix(size)
}

/**
 * A user-defined monoid supports prefixes without requiring an inverse.
 * For arbitrary interval queries, use group-based prefixAggregates(group)
 * or a segment tree / disjoint sparse table.
 */
fun <T> List<T>.prefixAggregates(op: AggregateOp<T>): MonoidPrefixTable<T> =
    MonoidPrefixTable(this, op)

fun IntArray.prefixAggregates(op: AggregateOp<Int>): MonoidPrefixTable<Int> =
    MonoidPrefixTable(asList(), op)

fun LongArray.prefixAggregates(op: AggregateOp<Long>): MonoidPrefixTable<Long> =
    MonoidPrefixTable(asList(), op)

fun <T> Array<out T>.prefixAggregates(op: AggregateOp<T>): MonoidPrefixTable<T> =
    MonoidPrefixTable(asList(), op)
