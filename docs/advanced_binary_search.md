# More beautiful binary search

The standalone module `search/AdvancedBinarySearch.kt` extends
`search/BinarySearch.kt`.

## Unknown upper bound: exponential search

```kotlin
val answer: Long? = firstTrueFrom(0L) { x -> feasible(x) }
```

Probe 0, 1, 3, 7, 15, ... until a true point is discovered, then bisect
the last false/true interval. This uses O(log(distance + 1)) predicate
evaluations, even when the answer is extremely large.

The result is nullable because there may be **no true value** even at
`Long.MAX_VALUE` (or `Int.MAX_VALUE`). The algorithm safely saturates
instead of overflowing. If `start` is already true, return `start`.
The predicate must be monotonic on the searched suffix, false then true.

```kotlin
val first = firstTrueFrom(10) { it >= 100 }   // 100 (Int?)
val absent = firstTrueFrom(0L) { false }       // null
```

## Real-number bisection

```kotlin
val rootTwo = bisect(1.0, 2.0) { x -> x * x >= 2.0 }
val root = bisectRoot(1.0, 2.0) { x -> x * x - 2.0 }
```

`bisect` requires **finite** bounds with `left <= right`; the predicate
must be monotonic false-to-true and true at the right bound. It returns
`left` when already true there; otherwise it returns an approximation
from the true side. `iterations` defaults to 100; the search also stops
when the midpoint can no longer advance in Double precision.

`bisectRoot` is for continuous functions with **opposite signs at
the endpoints**, and supports either increasing or decreasing sign
changes. This guarantees a root exists if the function is continuous.
It returns an approximate root, not necessarily a true-side boundary.

Both reject invalid bounds/iteration counts and non-finite function
values. A floating-point function is limited by `Double` precision;
in particular avoid arithmetic that generates NaN or Infinity.

## Generic partition points

Sometimes values are ordered but you want a predicate instead of a
particular target:

```kotlin
val a = intArrayOf(2, 4, 6, 8, 10)
val index = a.partitionPoint { it >= 7 } // 3
```

This is equivalent to `firstTrue(a.size) { predicate(a[it]) }`.

Works with `IntArray`, `LongArray`, `List<T>`, and `Array<T>`, plus
optional half-open index subranges `(fromIndex, toIndex)`. Returns
`toIndex` when nothing qualifies; inputs must be monotonic under
the predicate. The helper does **not** sort data.

## Verification

```bash
kotlinc search/*.kt tests/AdvancedBinarySearchTests.kt -include-runtime -d advanced.jar
java -jar advanced.jar
```

The regression suite covers 6000 random arrays and boundaries,
signed Int/Long extreme ranges, no-result cases, positive/negative
root finding, and finite-input validation.
