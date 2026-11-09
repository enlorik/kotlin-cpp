# Persistent lazy ranges and Segment Tree Beats

Two algorithms that are too expensive or too specialized to improvise in
a contest now live behind reusable interfaces.

## Persistent Long ranges

`LongArray.versionedLongSum()` implements immutable root/path copying
with lazy interval addition and assignment. Each update creates a new
version in O(log n) new nodes and time; each query is O(log n). Initial
build O(n). Querying does **not** allocate or mutate old nodes.

```kotlin
val tree = longArrayOf(1, 2, 3).versionedLongSum()
val v1 = tree.add(0, 3, 5)     // current HEAD advances to version 1
tree.checkout(0)              // rewind without deleting v1
val v2 = tree.assign(0, 2, 9)  // new version 2
check(tree.query(v1, 0, 3) == 21L)
check(tree.query(v2, 0, 3) == 21L)
```

`add(version,from,until,delta)` and `assign(version,...)` create branches
from any version *without changing HEAD*. `checkout(version)` and
`rollback(version)` only move the current version pointer. Later versions
remain queryable and can be branched again. Empty updates produce an
independent version of the same root. `set(version,index,value)` assigns one
point. Values are Long and arithmetic wraps on overflow.

These semantics differ from **destructive rollback**, which deletes later
history; that operation is intentionally not provided.

## Segment Tree Beats

`LongArray.beatsTree()` implements range `chmin` (cap each element),
`chmax` (raise each element), `add`, and `assign`, plus `sum`, `min`
and `max` range queries:

```kotlin
val tree = longArrayOf(2, 10, 5).beatsTree()
tree.chmin(0, 3, 7)
tree.chmax(0, 2, 4)
tree.add(0, 3, 1)
check(tree.sum(0, 3) == 19L)
check(tree.min(0, 3) == 5L)
check(tree.max(0, 3) == 8L)
```

The implementation keeps min/max, second-min/second-max, counts of
extreme values, lazy addition and sums in primitive arrays. In the
usual Segment Tree Beats analysis, conditional updates have amortized
O(log² n) complexity and queries take O(log n), with O(n) space.

**Numeric contract:** all values and intermediate sums must fit in
signed Long without overflow. Actual `Long.MIN_VALUE` and
`Long.MAX_VALUE` elements are not supported because those values
are reserved second-extreme sentinels. Clamp updates must not introduce
those sentinel values. Typical Timus values within ordinary 32-bit
integer ranges are unaffected.

All intervals are zero-based and half-open. Empty range sum returns 0,
empty range minimum `Long.MAX_VALUE`, and empty maximum `Long.MIN_VALUE`.

## Tests and bundling

```bash
bash tools/test_query_planner.sh
python3 tools/build_submission.py --modules persistent-long-range \
    --solution Main.kt --output submission.kt
python3 tools/build_submission.py --modules segment-tree-beats \
    --solution Main.kt --output submission.kt
```

Both modules are standalone without graph, sparse table or generic
algebra dependencies. Randomized tests compare thousands of branching
histories and overlapping clamp/add patterns against naive arrays.
