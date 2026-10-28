# Segment trees and binary search over prefixes

The standalone `tree/SegmentTree.kt` supports point updates and half-open
range aggregation, with an O(log n) prefix boundary search.

```kotlin
val a = intArrayOf(2, 4, 6, 8, 10)
val tree = a.segmentTree()        // SUM by default
tree.query(1, 4)                 // 18 (4 + 6 + 8)
tree.set(2, 100)
tree.query(1, 4)                 // 112
tree.findPrefix { it >= 110 }   // 3: 2+4+100+8 >= 110
```

For operations consistent with graph/grid aggregation, optionally include
`shared/SegmentTreeAggregations.kt` and `graph/GraphAggregate.kt`:

```kotlin
val tree = a.segmentTree(IntAggregations.MIN)
tree.query(0, a.size) // minimum
```

Or use any associative operation with a valid identity:

```kotlin
val strings = listOf("a", "bc", "d")
val tree = strings.segmentTree("") { left, right -> left + right }
tree.query(0, 3) // "abcd"
```

The library preserves **left-to-right reducer order**, so associative but
noncommutative operations (such as string concatenation) are correct.

## `findPrefix` and its invariant

```kotlin
val index: Int? = tree.findPrefix { prefixSum -> prefixSum >= k }
```

Returns the **first index i** where the aggregate of elements
`0..i` satisfies a false-to-true predicate. Returns `null` if none;
for an empty tree always returns `null`. Requires a *monotone predicate
over prefixes*. For SUM >= k, this typically means all array values
must be nonnegative. After point updates, this condition must still hold.

It descends a tree by checking the left child's aggregate in O(log n),
rather than making O(log n) separate range queries (O(log² n)).

`query(from, until)` is half-open, `set(index, value)` replaces a point,
`tree[index]` reads a point, `all()` aggregates the full array.
Each is O(log n) except point access and `all()` O(1).
Construction is O(n), memory O(n); generic values are stored boxed.
A tree of size zero is permitted; query(0,0) returns the identity.

Source: `tree/SegmentTree.kt` and optional
`shared/SegmentTreeAggregations.kt`.
Tests: `tests/SegmentTreeTests.kt`.

```bash
kotlinc tree/SegmentTree.kt graph/GraphTypes.kt graph/GraphResults.kt \
  graph/SparseGraph.kt graph/GraphAggregate.kt \
  shared/SegmentTreeAggregations.kt tests/SegmentTreeTests.kt \
  -include-runtime -d segment-test.jar
java -jar segment-test.jar
```
