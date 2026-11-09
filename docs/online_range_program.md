# Online range programs: queries return answers immediately

The existing `rangeProgram()` records queries and updates first, which
allows the library to select a static sparse table if there are no mutations.
It intentionally returns `Unit` for each query inside the block.

Some competitive-programming problems are **online**: the answer to the
previous query determines the next update. Use `onlineRangeProgram` to run
each command as it is written:

```kotlin
val sum = monoid(0) { a: Int, b: Int -> a + b }
val result = intArrayOf(2, 4, 6).onlineRangeProgram(sum) {
    val previous = query(0, 3) // 12, right now
    set(1, previous)           // replace index 1 with 12
    query(0, 3)                // 20, returned directly from block
}
check(result == 20)
```

The block is **ordinary Kotlin control flow** with a typed receiver and an
immediately available result; you can branch, loop, and use an earlier query
to decide what to do. It returns the block's final value (`R`), rather
than an answer list. If you need a list, append to one inside the block.
The source is snapshotted; updates do not modify the original values.

## Online range updates

Provide a mathematically lawful `MonoidAction` when you need to update
whole intervals:

```kotlin
val add = action<Int, Int>(
    apply = { total, delta, length -> total + delta * length },
    compose = { old, newer -> old + newer }
)
val answer = intArrayOf(2, 4, 6).onlineRangeProgram(sum, add) {
    update(0, 2, 3)            // [5, 7, 6]
    val value = query(0, 3)   // 18
    update(2, 3, value)       // [5, 7, 24]
    query(0, 3)               // 36
}
```

This uses the same `LazyAggregateTree` as `rangeIndex(op, action)`, with
O(log n) per query and interval update for O(1) monoid/action operations.
Point assignments use `RangeIndexStrategy.POINT_UPDATES` with a segment
tree; the online program cannot know the future command sequence and cannot
automatically use a static sparse table. Point updates and queries are
O(log n). Building either index takes O(n) time and memory.

**Important:** Supplying arbitrary functions with `monoid` or `action`
is a mathematical promise, not a correctness proof. Only associative
aggregates and compatible, distributive lazy updates produce valid results.

All ranges are zero-based and half-open `[from, until)`, with an optional
`query(1..3)` inclusive Kotlin `IntRange` overload. `query(empty)`
returns the monoid identity. Both typed `Monoid`/`MonoidAction` and
legacy `AggregateOp`/`RangeAction` are supported, on arrays and lists.

## Standalone contest submissions and CI

```bash
python3 tools/build_submission.py --modules online-range-program \
  --solution Main.kt --output submission.kt
bash tools/test_range_index.sh
bash tools/test_submission_builder.sh
```

For **query-only** programs whose commands are known ahead of execution,
keep using `rangeProgram`, which selects a static O(1) query index.
For a specialized bounded-palindrome problem, use
`String.boundedPalindromes(k)` instead; online does not imply that a
generic boxed segment tree will win a very tight performance budget.
