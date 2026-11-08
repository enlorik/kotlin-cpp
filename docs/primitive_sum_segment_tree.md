# Faster sum-only segment trees with primitive storage

The generic `a.segmentTree()` is deliberately flexible: it supports custom
associative operations and can combine arbitrary Kotlin values. When you
only need **Long sums**, you can avoid generic boxing and virtual aggregation:

```kotlin
val a = longArrayOf(2, 4, 6, 8)
val sums = a.sumSegmentTree()   // LongSumSegmentTree, unboxed LongArray
println(sums.query(1, 3))      // 10, half-open [1, 3)
sums[2] = 100
println(sums[1..3])            // 112, Kotlin inclusive range
println(sums.all())            // 114
```

An `IntArray` also supports `sumSegmentTree()`, converting input elements
to Long so large totals avoid 32-bit overflow:

```kotlin
val big = intArrayOf(Int.MAX_VALUE, Int.MAX_VALUE).sumSegmentTree()
println(big.all()) // 4294967294
```

The constructor copies the input; changing the source array afterward does
not change the tree. Use `tree[index] = value` for point assignments. Like the
generic version, `findPrefix { ... }` requires a monotone false-to-true
predicate over cumulative sums, normally with nonnegative elements.

| Operation | Complexity |
|---|---|
| Build | O(n) |
| query(from, until) | O(log n) |
| set(index, value) | O(log n) |
| findPrefix(predicate) | O(log n) |
| all, get(index) | O(1) |
| Memory | O(n), unboxed LongArray |

This implementation only supports SUM; MIN, MAX, XOR, concatenation and custom
monoids still use `segmentTree(op)`. Long addition wraps on overflow, as
it does in ordinary Kotlin Long arithmetic.

**Performance and correctness:** `bash benchmarks/compare_segment_tree.sh`
compares the generic Kotlin tree, this unboxed tree, and C++ over the same
deterministic workload, requiring matching checksums. Elapsed times are
diagnostic, not release gates, and are machine/JIT dependent. The full Kotlin
suite also compares randomized queries/updates against the generic tree and a
naive oracle.

Standalone source: `tree/LongSumSegmentTree.kt` has no repository dependencies.
The submission generator supports `--modules sum-segment-tree`.
