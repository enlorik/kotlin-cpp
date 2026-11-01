# Lazy segment trees and static sparse tables

Use **half-open** ranges `[from, until)` throughout. The new modules
are independent of graph code, and the sparse tables reuse
`shared/AggregationOp.kt`.

## Lazy segment tree: range add, range assign, range sum

```kotlin
val tree = intArrayOf(2, 4, 6, 8).lazySegmentTree()
tree.add(1, 3, 10)   // [2, 14, 16, 8]
tree.set(0, 2, 5)    // [5, 5, 16, 8]
tree.query(1, 4)     // 29L
tree.all()           // 34L
```

A node stores an aggregate sum plus deferred operations. A deferred
assignment **overwrites** previous pending additions; a later addition
modifies the pending assignment. When descending, deferred changes are
pushed to children. Every range update/query takes O(log n), construction
O(n), memory O(n). All values are Long, even for Int input.
Assume values, aggregate sums and multiplication by segment lengths fit
in Long. Empty trees and ranges are valid.

Unlike `DifferenceArray`, this works with **interleaved updates and
queries** without rebuilding the entire array.

## SparseTable: immutable MIN/MAX/other idempotent operations

```kotlin
val table = intArrayOf(5, 2, 7, 1).sparseTable(IntAggregations.MIN)
table.query(1, 4) // 1
```

Preprocess in O(n log n), answer each query in O(1), memory O(n log n).
It combines two overlapping power-of-two intervals. This is correct
**only when the operation is associative and idempotent**, meaning
`combine(x,x) == x`; examples include MIN, MAX, GCD, OR and AND.
The constructor rejects an `AggregateOp` whose `idempotent` flag is false.

## DisjointSparseTable: O(1) with any associative operation

```kotlin
val sum = intArrayOf(5, 2, 7, 1).disjointSparseTable(IntAggregations.SUM)
sum.query(1, 4) // 10

val glue = AggregateOp("", { a: String, b: String -> a + b })
val text = listOf("ab", "c", "d").disjointSparseTable(glue)
text.query(0, 3) // "abcd"
```

A disjoint sparse table precomputes suffixes and prefixes for each
power-of-two block. Each query picks the highest bit where the two
endpoints differ, then combines exactly two **nonoverlapping**
precomputed pieces. This handles SUM and even noncommutative operations
such as string concatenation, as long as they are associative.
Build O(n log n); query O(1); memory O(n log n).

Both sparse-table variants are **static**: rebuild if values change.
Both return the provided identity on empty ranges.

## Tests

```bash
kotlinc tree/LazySegmentTree.kt tests/LazySegmentTreeTests.kt -include-runtime -d /tmp/lazy.jar
java -jar /tmp/lazy.jar

kotlinc shared/AggregationOp.kt range/SparseTables.kt tests/SparseTablesTests.kt -include-runtime -d /tmp/sparse.jar
java -jar /tmp/sparse.jar
```

The checked-in regression suites compare against naive arrays across
150,000 random lazy-tree operations and 66,000 random sparse-table
subranges, including empty intervals and noncommutative combinations.
