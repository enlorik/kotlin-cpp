# Parallel binary search: many boundaries, one sweep per round

`parallelFirstTrue` finds boundaries for **many monotone queries**.
Instead of running completely separate searches, it groups query IDs by
midpoint and calls `evaluate(mid, ids)` in **ascending midpoint order**
within each round. Use `onRoundStart` to reset incremental state.

## Example: earliest prefix reaching each target

Input events contribute nonnegative quantities; queries request the
earliest prefix length with total sum >= target:

```kotlin
val events = intArrayOf(2, 1, 4, 3, 5)
val targets = intArrayOf(3, 5, 8, 13, 100)
var applied = 0
var sum = 0

val answer = parallelFirstTrue(
    queryCount = targets.size,
    from = 0,
    until = events.size + 1, // candidate prefix lengths 0..events.size
    onRoundStart = { applied = 0; sum = 0 },
    evaluate = { mid, ids ->
        while (applied < mid) sum += events[applied++]
        BooleanArray(ids.size) { i -> sum >= targets[ids[i]] }
    }
)
// [2, 3, 4, 5, 6]
// 6 is the sentinel: no prefix of length 0..5 satisfies target=100
```

The `evaluate` function must return the results **in the same order as
the query IDs** in `ids`. The evaluator's predicate for each query must
be false then true as midpoint increases. The method does not enforce
monotonicity automatically.

## Different ranges per query

```kotlin
val first = intArrayOf(1, 5, 10)
val until = intArrayOf(20, 30, 40)
val answers = parallelFirstTrue(first, until) { mid, ids ->
    BooleanArray(ids.size) { j -> condition(ids[j], mid) }
}
```

Ranges are half-open, per query. When no true midpoint exists, the
exclusive right endpoint is returned. Empty query sets and empty ranges
are valid. Query arrays are copied; the caller's input is not modified.

**Complexity:** O(Q log R log Q) for grouping and scheduling, plus the
cost of the batch evaluations; O(Q) auxiliary space.
`R` denotes the maximum range width. Grouping can reduce expensive
repeated work when the evaluator can process midpoints incrementally.
If the evaluation has no reusable computation, ordinary per-query
`firstTrue` may be simpler and faster. Queries with different bounds
are supported; midpoint sorting per round is guaranteed.

Source: `search/ParallelBinarySearch.kt`. Tests in
`tests/ParallelBinarySearchTests.kt`.

```bash
kotlinc search/ParallelBinarySearch.kt tests/ParallelBinarySearchTests.kt \
    -include-runtime -d parallel.jar
java -jar parallel.jar
```
