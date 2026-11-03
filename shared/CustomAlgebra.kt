/**
 * Declare a reusable associative reduction with an identity element.
 * Example: val product = aggregation(1) { a: Int, b: Int -> a * b }
 * The caller is responsible for associativity and correct identity.
 * Set idempotent=true only if combine(x,x) == x for every x.
 */
fun <T> aggregation(
    initial: T,
    idempotent: Boolean = false,
    combine: (T, T) -> T
): AggregateOp<T> = AggregateOp(initial, combine, idempotent)

/**
 * Enrich a reusable aggregation with an inverse to make an abelian group.
 * Requires commutativity, associativity, identity, and valid inverse.
 * Use the resulting group with Fenwick, prefix aggregates, differences, and Mo.
 */
fun <T> AggregateOp<T>.withInverse(inverse: (T) -> T): AbelianGroup<T> =
    AbelianGroup(this, inverse)

/** Construct an abelian group from an operation and its inverse. */
fun <T> abelianGroup(op: AggregateOp<T>, inverse: (T) -> T): AbelianGroup<T> =
    AbelianGroup(op, inverse)

/**
 * Prefix table with O(n) preprocessing and O(1) interval queries for a group.
 * In a commutative group, range value = inverse(prefix[from]) * prefix[until].
 */
class GroupPrefixTable<T>(values: List<T>, val group: AbelianGroup<T>) {
    private val prefix: List<T>
    val size: Int get() = prefix.size - 1

    init {
        val p = ArrayList<T>(values.size + 1)
        p.add(group.op.initial)
        for (value in values) p.add(group.op.combine(p.last(), value))
        prefix = p
    }

    fun prefix(until: Int): T {
        require(until in 0..size) { "Invalid prefix endpoint $until" }
        return prefix[until]
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        return group.op.combine(group.inverse(prefix[from]), prefix[until])
    }
}

fun <T> List<T>.prefixAggregates(group: AbelianGroup<T>): GroupPrefixTable<T> =
    GroupPrefixTable(this, group)
fun IntArray.prefixAggregates(group: AbelianGroup<Int>): GroupPrefixTable<Int> =
    GroupPrefixTable(asList(), group)
fun LongArray.prefixAggregates(group: AbelianGroup<Long>): GroupPrefixTable<Long> =
    GroupPrefixTable(asList(), group)
fun <T> Array<out T>.prefixAggregates(group: AbelianGroup<T>): GroupPrefixTable<T> =
    GroupPrefixTable(asList(), group)

/**
 * O(1) deferred interval 'combine with delta' operations for an abelian group.
 * Materialize all values in O(n); changes do not overwrite the original values.
 * For noncommutative or noninvertible operations, a segment tree is appropriate.
 */
class GroupDifferenceArray<T>(values: List<T>, val group: AbelianGroup<T>) {
    private val original = values.toList()
    val size: Int get() = original.size
    private val diff = MutableList(size + 1) { group.op.initial }

    fun apply(from: Int, until: Int, delta: T) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        if (from == until) return
        diff[from] = group.op.combine(diff[from], delta)
        diff[until] = group.op.combine(diff[until], group.inverse(delta))
    }

    fun values(): List<T> {
        val result = ArrayList<T>(size)
        var change = group.op.initial
        for (i in original.indices) {
            change = group.op.combine(change, diff[i])
            result.add(group.op.combine(original[i], change))
        }
        return result
    }
}

fun <T> List<T>.differenceArray(group: AbelianGroup<T>): GroupDifferenceArray<T> =
    GroupDifferenceArray(this, group)
fun IntArray.differenceArray(group: AbelianGroup<Int>): GroupDifferenceArray<Int> =
    GroupDifferenceArray(asList(), group)
fun LongArray.differenceArray(group: AbelianGroup<Long>): GroupDifferenceArray<Long> =
    GroupDifferenceArray(asList(), group)
fun <T> Array<out T>.differenceArray(group: AbelianGroup<T>): GroupDifferenceArray<T> =
    GroupDifferenceArray(asList(), group)

/**
 * Mo's algorithm convenience for any abelian-group range aggregate.
 * A general associative reduction cannot necessarily remove an element;
 * the inverse makes O(1) add/remove callbacks possible here.
 */
fun <T> List<T>.moAggregate(queries: List<MoQuery>, group: AbelianGroup<T>): List<T> {
    var running = group.op.initial
    return moQueries(
        n = size,
        queries = queries,
        add = { index -> running = group.op.combine(running, this[index]) },
        remove = { index -> running = group.op.combine(running, group.inverse(this[index])) },
        snapshot = { running }
    )
}

fun IntArray.moAggregate(queries: List<MoQuery>, group: AbelianGroup<Int>): List<Int> =
    asList().moAggregate(queries, group)
fun LongArray.moAggregate(queries: List<MoQuery>, group: AbelianGroup<Long>): List<Long> =
    asList().moAggregate(queries, group)
fun <T> Array<out T>.moAggregate(queries: List<MoQuery>, group: AbelianGroup<T>): List<T> =
    asList().moAggregate(queries, group)

/** Make custom lazy actions ergonomic on the same array types as other trees. */
fun <U> IntArray.lazySegmentTree(
    op: AggregateOp<Int>, action: RangeAction<Int, U>
): LazyAggregateTree<Int, U> = asList().lazySegmentTree(op, action)

fun <U> LongArray.lazySegmentTree(
    op: AggregateOp<Long>, action: RangeAction<Long, U>
): LazyAggregateTree<Long, U> = asList().lazySegmentTree(op, action)

fun <T, U> Array<out T>.lazySegmentTree(
    op: AggregateOp<T>, action: RangeAction<T, U>
): LazyAggregateTree<T, U> = asList().lazySegmentTree(op, action)
