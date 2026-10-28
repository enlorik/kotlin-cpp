# Binary search: firstTrue, lastTrue, lowerBound, upperBound

The whole module is **one standalone Kotlin/JVM source file**:
`search/BinarySearch.kt`. It depends only on Java's built-in `LongPredicate`.
No graph, grid, or I/O source files are needed.

## Find the first true position

```kotlin
val a = intArrayOf(2, 4, 6, 8, 10, 12, 14)
val i = firstTrue(a.size) { index -> a[index] >= 9 }
// i == 4
```

The predicate must change from `false` to `true` **at most once** as the
index increases:

```text
false false false true true true
                  ^
               firstTrue
```

- `firstTrue(n)` searches indices `[0, n)`. It returns **n** if no index
  satisfies the predicate.
- `firstTrue(from, until)` searches `[from, until)`, returning **until**
  if no index satisfies the predicate. `until` is *exclusive*.
- Empty ranges return the exclusive end without evaluating the predicate.
- The interval must satisfy `from <= until`; `n` must be nonnegative.
- All of these cost O(log(range size)) predicate calls and O(1) space.

```kotlin
val first = firstTrue(-10, 10) { x -> x >= 3 } // 3
val absent = firstTrue(0, 4) { false }         // 4
```

## Find the last true position

The condition must be `true ... true, false ... false`.

```kotlin
val last = lastTrue(a.size) { index -> a[index] < 10 }
// last == 3
```

For `[0, n)`, `lastTrue(n)` returns **-1** when no index is true.
For an arbitrary `[from, until)`, `lastTrue(from, until)` returns **null**
when there is no answer. Using null makes ranges starting at
`Int.MIN_VALUE` / `Long.MIN_VALUE` safe without a fake sentinel outside the
representable integer domain.

```kotlin
val last = lastTrue(-10, 10) { x -> x < -3 } // -4 (Int?)
val none = lastTrue(-10, 10) { false }         // null
```

## Binary search on an answer, using Long

If your answer is too big for Int, use Long bounds:

```kotlin
val smallest = firstTrue(0L, 1_000_000_000_000_000_000L) { answer ->
    feasible(answer)
}
```

The condition must stay true after its first true answer.
For last true instead, use `lastTrue(0L, limitExclusive) { feasible(it) }`.

**Overflow-safe:** The `Int` midpoint uses Long intermediate math. The
`Long` midpoint uses an unsigned shift of the difference, so it works even
when searching from `Long.MIN_VALUE` toward `Long.MAX_VALUE`. This protects
the *binary-search bounds*. Your predicate must still avoid overflowing
its own arithmetic (for example, `x*x` can overflow a Long).

The Long overloads accept a Kotlin lambda through `LongPredicate` (a standard
Java functional interface). This is deliberate: it keeps `firstTrue(0, 7)
{ ... }` unambiguously **Int**, while `firstTrue(0L, bigLimit) { ... }` is
**Long**, without requiring manual annotations or type arguments.

## Lower and upper bounds in sorted arrays

For values sorted in nondecreasing order:

```kotlin
val a = intArrayOf(2, 4, 4, 4, 7)
val lower = a.lowerBound(4) // 1: first index with a[i] >= 4
val upper = a.upperBound(4) // 4: first index with a[i] > 4
val count = upper - lower   // 3 occurrences of 4
```

The result is an **insertion index** in `[fromIndex, toIndex]`. In
particular, `a.lowerBound(x) == a.size` means **no element is >= x**.

Both helpers support optional subranges:

```kotlin
val i = a.lowerBound(4, fromIndex = 2, toIndex = 5)
```

`fromIndex` is inclusive and `toIndex` is exclusive.
Invalid ranges throw `IllegalArgumentException` instead of risking an
out-of-bounds array access.

These functions are available for `IntArray`, `LongArray`, and naturally
ordered `List<T>` and `Array<T>` where `T : Comparable<T>`.

For lists using a custom order, pass a comparator:

```kotlin
val descending = listOf(9, 7, 7, 4)
val order = compareByDescending<Int> { it }
val lower = descending.lowerBound(7, order) // 1
val upper = descending.upperBound(7, order) // 3
```

These are O(log n) comparisons with random-access arrays/lists. A linked
list may require O(n log n) time due to indexed access. **No sorting is
performed by these functions**, so the input must already be sorted in
the comparator's order.

## Exact target lookup

No need to reimplement lowerBound:

```kotlin
val pos = a.lowerBound(target)
val index = if (pos < a.size && a[pos] == target) pos else -1
```

If you simply want the standard library's built-in array search,
Kotlin already provides `binarySearch` for sorted arrays. The added
`lowerBound` / `upperBound` make duplicate handling and insertion
positions explicit and consistent.

## Tests and a contest example

```bash
kotlinc search/BinarySearch.kt tests/BinarySearchTests.kt -include-runtime -d binary-test.jar
java -jar binary-test.jar

kotlinc search/BinarySearch.kt io/FastIO.kt examples/BinarySearchSolution.kt \
    -include-runtime -d binary-example.jar
java -jar binary-example.jar
```

`BinarySearchTests.kt` includes 69,000 randomized subrange/array/list checks,
full Int/Long-range midpoint overflow checks, duplicate values, empty
ranges, custom comparators, and invalid interval validation.

All API search functions are pure; they do not read input or print anything.
