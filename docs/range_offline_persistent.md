# Offline Mo's algorithm and persistent segment trees

Both modules use half-open ranges `[from, until)`; they solve very
different kinds of range-query problems.

## Mo's algorithm: reduce window movement across offline queries

If you know all range queries in advance, sort them so nearby ranges are
processed near one another. Maintain a window with incremental `add`
and `remove` operations.

```kotlin
val a = intArrayOf(3, 3, 5, 3)
val queries = listOf(MoQuery(0, 3), MoQuery(1, 4))
val answer = a.moDistinctCounts(queries) // [2, 2]
```

For custom statistics, maintain your own state:

```kotlin
var sum = 0L
val sums = moQueries(
    n = a.size,
    queries = queries,
    add = { i -> sum += a[i] },
    remove = { i -> sum -= a[i] },
    snapshot = { sum }
)
```

Answers are returned in the **original query order**. No updates to
the source array are supported in this basic static version.
With conventional block ordering, the total pointer movement is
O((n+q) sqrt(n)) for n elements and q queries, plus callback costs.
The returned snapshots must be stable, e.g. immutable integers or
defensive copies of mutable objects.

Mo's algorithm is valuable when removing/adding one element is cheap
but recomputing a statistic for a fresh interval is expensive.

## Persistent segment tree: previous versions remain queryable

A point update creates a **new version**, reusing unchanged immutable
nodes from its parent.

```kotlin
val tree = intArrayOf(2, 4, 6).persistentSegmentTree()
val v1 = tree.set(0, 1, 100)
val v2 = tree.add(0, 2, 5)

tree.query(0, 0, 3)  // 12L (original)
tree.query(v1, 0, 3) // 108L (changed middle element)
tree.query(v2, 0, 3) // 17L (branched directly from original)
```

Version 0 is the initial array. Every `set(version, index, value)`
or `add(version, index, delta)` creates and returns another version ID.
You can branch from any earlier version. `query(version, from, until)`,
`get(version, index)`, `all(version)` and `versionCount` are available.

Build O(n) time and space. Updates allocate O(log n) new nodes;
query O(log n); memory across V updates O(n + V log n).
This implementation is a **persistent sum tree with point updates**,
not a range-lazy persistent tree. It uses Long sums and returns zero
for empty intervals. Assume sums and updates fit Long.

## Tests

```bash
kotlinc range/MoAlgorithm.kt tests/MoAlgorithmTests.kt -include-runtime -d /tmp/mo.jar
java -jar /tmp/mo.jar

kotlinc tree/PersistentSegmentTree.kt tests/PersistentSegmentTreeTests.kt -include-runtime -d /tmp/persistent.jar
java -jar /tmp/persistent.jar
```

Mo's tests use 2500 randomized batches, compare distinct counts and sums
against naive slices, and include empty intervals. Persistence tests use
144,000 randomized historical operations, including branching updates,
plus empty trees.
