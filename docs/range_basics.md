# Prefix sums, difference arrays, and Fenwick trees

All indices are 0-based and all ranges are **half-open**: `[from, until)`.
Sums and updates use Long values; overflow is the caller's responsibility.

## Prefix sums: static O(1) range sums

```kotlin
val a = intArrayOf(2, 4, 6, 8)
val p = a.prefixSums()
p.query(1, 3)  // 10
p.prefix(3)    // 12, sum of a[0..2]
```

Build O(n), query O(1), memory O(n). The source is copied, so changes to
the original array do not alter the prefix table.

## Difference arrays: defer range additions

```kotlin
val d = intArrayOf(2, 4, 6, 8).differenceArray()
d.add(1, 3, 10)
d.values() // [2L, 14L, 16L, 8L]
```

Each `add` is O(1). Calling `values()` reconstructs the current array in
O(n), without consuming pending updates. It's ideal when all updates
arrive before final materialization; not appropriate for alternating
online point queries. Use `differenceArray(n)` for zero-initialized data.

## Fenwick tree: dynamic prefix/range sums

```kotlin
val f = intArrayOf(2, 4, 6, 8).fenwickTree()
f.add(1, 10)
f.sum(3)      // 22 (first three values)
f.query(1, 3) // 20
f.set(2, 1)   // replace a[2]
f[2]          // 1
```

Build O(n), updates and queries O(log n), memory O(n).

For nonnegative elements, you can also locate the first index with
inclusive prefix sum >= target in O(log n):

```kotlin
val f = intArrayOf(2, 0, 3, 1, 4).fenwickTree()
f.findPrefix(6) // 3
```

Returns `null` if the target is never reached. The prefix-search guarantee
requires nonnegative elements after **all updates**. The tree does not
automatically enforce that precondition. Empty structures are supported.

## Verification

```bash
kotlinc range/RangeBasics.kt tests/RangeBasicsTests.kt -include-runtime -d /tmp/ranges.jar
java -jar /tmp/ranges.jar
```

Randomized tests compare prefix, difference and Fenwick operations against
simple arrays; they cover zero-length arrays and empty ranges.
