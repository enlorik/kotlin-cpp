# Primitive Long-sum lazy range operations

The generic `monoid(0L) { a, b -> a + b }` and `action(...)` APIs
support arbitrary mathematical operations, but Kotlin cannot inspect lambdas
to prove that they mean addition or assignment. Guessing would be unsafe.

**Explicitly named capability objects** select a primitive `LongArray`
backend without changing generic APIs:

```kotlin
val answer = longArrayOf(2, 4, 6)
    .onlineLongSum(LongAddAction) {
        val previous = query(0, 3) // 12
        update(0, 2, previous)      // [14, 16, 6]
        query(0, 3)                 // 36
    }
```

The same API works for range replacement:

```kotlin
val index = longArrayOf(2, 4, 6).rangeIndex(LongSumMonoid, LongAssignAction)
index.update(0, 2, 10L)
check(index.query(0, 3) == 26L)
```

The reusable implementation also supports both operations in one tree:

```kotlin
val tree = longArrayOf(1, 2, 3).longSumLazyTree()
tree.add(0, 3, 4)
tree.assign(1, 3, 0)
check(tree.all() == 5L)
```

All intervals are half-open `[from, until)`, and assignments overwrite
earlier additions. The tree snapshots the input; all sums use Long
arithmetic, with normal signed overflow behavior.

## Why this is a separate backend

- A named `LongSumMonoid` + `LongAddAction` or `LongAssignAction`
  uses the unboxed `LongSumLazyTree` through exact static overloads.
- Kotlin receiver-lambda overloads are ambiguous here, so the unboxed online
  shortcut is intentionally called `onlineLongSum(action)`, not another
  `onlineRangeProgram` overload.
- An arbitrary `Monoid<Long>` + `MonoidAction<Long, U>` keeps the existing
  generic `LazyAggregateTree`, even when a lambda happens to add.
- The optimized version uses primitive arrays for sums and lazy tags,
  avoiding boxed values and per-node lambda dispatch.
- Both versions take O(n) preprocessing and O(log n) per operation, but
  constant factors and memory use differ.
- As with any mutable data structure, this does *not* replace the
  problem-specific mathematical insight behind Timus 2042. Bounded
  palindrome queries still prefer `boundedPalindromes(k)`.

## Build and test

```bash
python3 tools/build_submission.py --modules fast-io,primitive-long-lazy \
  --solution Main.kt --output submission.kt
bash tools/test_primitive_long_lazy.sh
bash tools/test_submission_builder.sh
```

Randomized tests compare lazy add/assign operations against a naive
array and validate the typed mathematical laws.
