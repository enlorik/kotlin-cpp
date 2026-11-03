/**
 * Operation-capability overloads: a plain Monoid cannot accidentally be sent
 * to Fenwick or overlapping SparseTable. Kotlin checks this before running.
 * Legacy AggregateOp and AbelianGroup overloads are unchanged.
 */

// A static disjoint sparse table works for any associative operation.
fun IntArray.disjointSparseTable(op: Monoid<Int>): DisjointSparseTable<Int> =
    disjointSparseTable(op.asAggregateOp())
fun LongArray.disjointSparseTable(op: Monoid<Long>): DisjointSparseTable<Long> =
    disjointSparseTable(op.asAggregateOp())
fun <T> List<T>.disjointSparseTable(op: Monoid<T>): DisjointSparseTable<T> =
    disjointSparseTable(op.asAggregateOp())

// Overlapping sparse queries require the idempotence capability.
fun IntArray.sparseTable(op: IdempotentMonoid<Int>): SparseTable<Int> =
    sparseTable(op.asAggregateOp())
fun LongArray.sparseTable(op: IdempotentMonoid<Long>): SparseTable<Long> =
    sparseTable(op.asAggregateOp())
fun <T> List<T>.sparseTable(op: IdempotentMonoid<T>): SparseTable<T> =
    sparseTable(op.asAggregateOp())

// Fenwick with general interval queries needs an abelian group and inverse.
fun IntArray.fenwickTree(group: AbelianGroupSpec<Int>): GroupFenwickTree<Int> =
    fenwickTree(AbelianGroup(group.asAggregateOp(), group::inverse))
fun LongArray.fenwickTree(group: AbelianGroupSpec<Long>): GroupFenwickTree<Long> =
    fenwickTree(AbelianGroup(group.asAggregateOp(), group::inverse))
fun <T> List<T>.fenwickTree(group: AbelianGroupSpec<T>): GroupFenwickTree<T> =
    fenwickTree(AbelianGroup(group.asAggregateOp(), group::inverse))

// Static interval prefix subtraction and deferred difference updates need inverses.
fun IntArray.prefixAggregates(group: AbelianGroupSpec<Int>): GroupPrefixTable<Int> =
    prefixAggregates(AbelianGroup(group.asAggregateOp(), group::inverse))
fun LongArray.prefixAggregates(group: AbelianGroupSpec<Long>): GroupPrefixTable<Long> =
    prefixAggregates(AbelianGroup(group.asAggregateOp(), group::inverse))
fun <T> List<T>.prefixAggregates(group: AbelianGroupSpec<T>): GroupPrefixTable<T> =
    prefixAggregates(AbelianGroup(group.asAggregateOp(), group::inverse))

fun IntArray.differenceArray(group: AbelianGroupSpec<Int>): GroupDifferenceArray<Int> =
    differenceArray(AbelianGroup(group.asAggregateOp(), group::inverse))
fun LongArray.differenceArray(group: AbelianGroupSpec<Long>): GroupDifferenceArray<Long> =
    differenceArray(AbelianGroup(group.asAggregateOp(), group::inverse))
fun <T> List<T>.differenceArray(group: AbelianGroupSpec<T>): GroupDifferenceArray<T> =
    differenceArray(AbelianGroup(group.asAggregateOp(), group::inverse))

// Mo's helper likewise uses the inverse to undo removed window elements.
fun IntArray.moAggregate(queries: List<MoQuery>, group: AbelianGroupSpec<Int>): List<Int> =
    moAggregate(queries, AbelianGroup(group.asAggregateOp(), group::inverse))
fun LongArray.moAggregate(queries: List<MoQuery>, group: AbelianGroupSpec<Long>): List<Long> =
    moAggregate(queries, AbelianGroup(group.asAggregateOp(), group::inverse))
fun <T> List<T>.moAggregate(queries: List<MoQuery>, group: AbelianGroupSpec<T>): List<T> =
    moAggregate(queries, AbelianGroup(group.asAggregateOp(), group::inverse))

/**
 * Any typed monoid has prefix aggregates, even without inverse.
 * A typed abelian group additionally offers `query(from, until)`
 * via the more specific prefixAggregates(group) overload.
 */
fun <T> List<T>.prefixAggregates(op: Monoid<T>): MonoidPrefixTable<T> =
    prefixAggregates(op.asAggregateOp())
fun IntArray.prefixAggregates(op: Monoid<Int>): MonoidPrefixTable<Int> =
    prefixAggregates(op.asAggregateOp())
fun LongArray.prefixAggregates(op: Monoid<Long>): MonoidPrefixTable<Long> =
    prefixAggregates(op.asAggregateOp())
fun <T> Array<out T>.prefixAggregates(op: Monoid<T>): MonoidPrefixTable<T> =
    prefixAggregates(op.asAggregateOp())
