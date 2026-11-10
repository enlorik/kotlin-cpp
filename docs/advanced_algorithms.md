# Advanced algorithms: Wavelet Matrix, Li Chao, and Heavy-Light

These three algorithms are reusable building blocks, not one-off Timus
solutions disguised as a short `main()`. They are independent of the query
planner's Long SUM/MIN/MAX backend, and expose stable domain-specific APIs.

## 1. Signed Int Wavelet Matrix

```kotlin
val index = intArrayOf(7, -4, 2, -4, 9).waveletMatrix()
check(index.kthSmallest(0, 5, 2) == 2) // zero-based kth
check(index.rank(-4, 0, 5) == 2)
check(index.countLess(0, 5, 3) == 3)
check(index.countBetween(0, 5, -4, 8) == 4)
check(index.predecessor(0, 5, 2) == -4)
check(index.successor(0, 5, 3) == 7)
```

- Handles the full signed Int domain, including `Int.MIN_VALUE` and
  `Int.MAX_VALUE`, via sign-bit normalization.
- Construction O(32n) time and O(32n) memory.
- All kth, rank, counting, predecessor, and successor queries O(32).
- Static only; snapshots the source array.
- Ranges are always `[from, until)`; kth is zero-based and requires a
  nonempty range.
- `predecessor` and `successor` return null when absent.

## 2. Dynamic Li Chao Tree

```kotlin
val lines = (-10L..10L).liChao(LiChaoObjective.MIN)
lines.addLine(slope = 2, intercept = 1)
lines.addLine(slope = -1, intercept = 7)
check(lines.query(0) == 1L)
lines.addSegment(-5, -1, slope = 0, intercept = -10)
check(lines.query(-3) == -10L)
```

- Dynamic Long x-domain, inclusive endpoints `[minX, maxX]`.
- `LiChaoObjective.MIN` and `MAX` supported.
- Full-line insertion O(log domainWidth), x-segment insertion
  O(log² domainWidth), point query O(log domainWidth).
- Empty envelopes and uncovered positions return null.
- Midpoint calculation is safe even across the full signed Long x-domain,
  but **line evaluation** requires `slope*x + intercept` to fit in Long;
  arbitrary precision is not implied.
- Segment intervals are inclusive, unlike the array-indexed APIs.

## 3. Heavy-Light Decomposition

```kotlin
val edges = listOf(0 to 1, 1 to 2)

val words = listOf("a", "b", "c")
    .heavyLightTree(edges, monoid("") { a, b -> a + b })

check(words.pathQuery(2, 0) == "cba") // direction preserved
words.set(1, "xyz")
check(words.pathQuery(0, 2) == "axyzc")

val sums = longArrayOf(1, 2, 3).heavyLightSums(edges)
check(sums.pathSum(0, 2) == 6L)
sums.set(1, 10L)
check(sums.pathSum(2, 0) == 14L)
```

- Generic `Monoid<T>` aggregation preserves **u-to-v order**, including
  noncommutative operations like concatenation and matrix multiplication.
- Primitive `LongArray.heavyLightSums(edges)` avoids boxed aggregates.
- Both support point `set`, rooted `subtreeQuery/subtreeSum`, `lca`,
  and edge-distance queries; the topology exposes depth, parent, position,
  and subtree sizes for advanced work.
- Construction O(n), point and subtree operations O(log n), path operations
  O(log² n). The decomposition is iterative, so a 100,000-vertex chain
  does not exhaust Kotlin's call stack.
- Input must be one connected, undirected tree with n vertices and n-1
  edges; vertices are 0-based and aggregates reside on **vertices**.
- Subtree aggregation follows the decomposition's heavy-first Euler order.
  Path aggregation explicitly follows u -> v.
- You can also call `graph.heavyLightSums(values)` or
  `graph.heavyLight(values, operation)` on an existing undirected
  `SparseGraph`.

## Verification and contest bundling

```bash
bash tools/test_advanced_algorithms.sh
bash tools/test_submission_builder.sh
python3 tools/build_submission.py --modules wavelet-matrix \
  --solution Main.kt --output submission.kt
python3 tools/build_submission.py --modules li-chao \
  --solution Main.kt --output submission.kt
python3 tools/build_submission.py --modules heavy-light \
  --solution Main.kt --output submission.kt
```

The `heavy-light-graph` bundle includes the existing graph subsystem for
`SparseGraph` integration. Use the lighter `heavy-light` module when
input is already a list of tree edges.

Randomized tests compare query results to sorting, full brute-force line
envelopes, BFS path reconstructions, and rooted subtree membership.
Standalone build tests compile generated Kotlin into runnable jars.

This expansion does not yet include rerooting DP, rollback DSU, or
suffix automata. Those belong in separate modules, each with its own
mathematical contract and tests.
