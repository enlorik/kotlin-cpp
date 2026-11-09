# Capability-driven primitive Long range algebra

This module adds explicitly typed built-in mathematical capabilities.
It does **not** infer whether `monoid(0L) { a, b -> a + b }` is addition
from a lambda body. That would be unsafe and would break custom algebra.

## Automatic backend selection

```kotlin
val minIndex = longArrayOf(9, 2, 5).rangeIndex(LongMinMonoid)
check(minIndex.query(0, 3) == 2L)
check(minIndex.strategy == LongPrimitiveStrategy.STATIC_SPARSE)

val xorIndex = longArrayOf(1, 3, 1).rangeIndex(LongXorMonoid)
check(xorIndex.query(0, 3) == 3L)
check(xorIndex.strategy == LongPrimitiveStrategy.STATIC_PREFIX)

val changing = longArrayOf(9, 2, 5)
    .rangeIndex(LongMaxMonoid, mutable = true)
changing.set(1, 20)
check(changing.query(0, 3) == 20L)
```

| Built-in operation | Static index, query | Dynamic point index |
|---|---|---|
| `LongSumMonoid` | Prefix sum, O(n) build/O(1) query | Unboxed segment tree |
| `LongXorMonoid` | Prefix XOR, O(n) build/O(1) query | Unboxed segment tree |
| `LongMinMonoid` | Sparse table, O(n log n) build/O(1) query | Unboxed segment tree |
| `LongMaxMonoid` | Sparse table, O(n log n) build/O(1) query | Unboxed segment tree |
| `LongGcdMonoid` | Sparse table, O(n log n) build/O(1) query | Unboxed segment tree |

Dynamic point queries and `set(index,value)` cost O(log n), O(n) build,
O(n) memory; static sparse tables cost O(n log n) memory.
All ranges are zero-based half-open `[from, until)`. Empty queries return
the operation identity. The input is snapshotted. Sums use Long wraparound.

**GCD accepts nonnegative Long values only**, including zero. Negative
values are rejected at index creation and update. This matters because
the usual absolute-value GCD is not idempotent on negative inputs:
`gcd(-3,-3) == 3`, not `-3`.

The exact, sealed `LongBuiltInMonoid` capability makes the specialized
`LongArray.rangeIndex` overload win at compile time. If the operation is
typed only as `Monoid<Long>`, the existing general `rangeIndex` is chosen.
Unknown functions and **noncommutative custom monoids** retain the generic
implementation, which preserves left-to-right combination order.

For online queries whose answers control the next point assignment:

```kotlin
val answer = longArrayOf(2, 4, 6).onlineLongPoints(LongMaxMonoid) {
    val previous = query(0, 3)
    set(0, previous + 10)
    all()
}
```

For entire interval **add/assign**, use the previous optimized lazy
`onlineLongSum(LongAddAction)`, or `rangeIndex(LongSumMonoid, LongAssignAction)`.
MIN/MAX/GCD/XOR only support point changes here; their arbitrary range-add
actions are NOT interchangeable.

## Batched local recomputation

`IntArray.blockAssignSum()` now also offers two general-purpose methods:

```kotlin
val blocks = intArrayOf(1, 2, 3, 4, 5).blockAssignSum()
blocks.recompute(1, 4) { index, old -> old + index }
// [1, 3, 5, 7, 5]
blocks.setMany(intArrayOf(0, 2, 4), intArrayOf(9, 9, 9))
// [9, 3, 9, 7, 9]
check(blocks.sum(0, 5) == 37L)
```

`recompute(from,until,callback)` rebuilds each touched block once, with
O(length + touchedBlocks*blockSize) time. The callback receives the current
value and must return its new value. Unlike a transaction, if the callback
throws, changes made before the exception remain applied.

`setMany(indices,values)` requires **strictly increasing, distinct**
indices, validates all bounds before editing, and rebuilds each touched
block once. It costs O(number of indices + touchedBlocks*blockSize).
Both methods avoid repeated generic O(log n) tree calls for adjacent
changes, useful for local recomputation and dynamic strings.

## Compiling, testing, and contest bundling

```bash
bash tools/test_range_index.sh
bash tools/test_bounded_palindromes.sh
bash tools/test_submission_builder.sh
python3 tools/build_submission.py \
  --modules primitive-long-index \
  --solution Main.kt --output submission.kt
```

`primitive-long-index` is a source bundle with named capabilities and the
unboxed static/point engine; `bounded-palindromes` includes the batch block
backend. Tests compare against naive oracles and generic monoids, including
zero-length arrays, negative values where valid, GCD domain violations,
source immutability, and noncommutative concatenation.
