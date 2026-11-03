# Invent an operation once, reuse it where the algebra allows

The Kotlin vocabulary for a user-defined operation is `aggregation(identity) { a, b -> ... }` or `aggregateOp(identity) { a, b -> ... }`.
It creates the same `AggregateOp<T>` used by grids, graphs, trees, and static range tables.

## 1. Custom associative operation: PRODUCT

```kotlin
val product = aggregation(1) { a: Int, b: Int -> a * b }
val a = intArrayOf(2, 3, 4)

val segment = a.segmentTree(product)
val static = a.disjointSparseTable(product)
val history = a.persistentSegmentTree(product)

segment.query(0, 3) // 24
static.query(0, 3) // 24
history.query(0, 0, 3) // 24 (version 0)
```

These operations need **associativity** `(a*b)*c == a*(b*c)` and the
correct two-sided identity. They do **not** need an inverse. Custom
operations can also be noncommutative, such as string concatenation.

For an idempotent operation, set `idempotent = true` and use the
overlapping sparse table, too:

```kotlin
val minimum = aggregation(Int.MAX_VALUE, idempotent = true) {
    a: Int, b: Int -> minOf(a, b)
}
val table = a.sparseTable(minimum)
```

Don't mark an operation idempotent unless `combine(x,x) == x`.

## 2. No inverse? You still get prefixes AND range assignment

Any associative operation has an efficient prefix table, including
noncommutative string concatenation:

```kotlin
val product = aggregateOp(1) { a: Int, b: Int -> a * b }
val a = intArrayOf(2, 3, 4)
val prefixes = a.prefixAggregates(product)
prefixes.prefix(0) // 1: identity, empty prefix
prefixes.prefix(2) // 6
prefixes.all()     // 24
```

For any `AggregateOp<T>` or typed `Monoid<T>`, static prefixes take
O(n) to build and O(1) to query. **Only prefixes** are available:
`MonoidPrefixTable` intentionally has no `query(from, until)`
because arbitrary interval subtraction needs an inverse.

A particularly useful surprise: **range assignment needs no inverse**.
Assigning one value to an entire segment means combining it with
itself a known number of times.

```kotlin
val product = aggregateOp(1) { a: Int, b: Int -> a * b }
val a = intArrayOf(2, 3, 4)
val tree = a.lazySegmentTree(product)
tree.set(1, 3, 5)  // The underlying values are now [2, 5, 5].
tree.query(0, 3)   // 50
```

This also works for noncommutative monoids:

```kotlin
val concat = monoid("") { a: String, b: String -> a + b }
val tree = listOf("a", "b", "c").lazySegmentTree(concat)
tree.set(0, 2, "x")
tree.query(0, 3) // "xxc"
```

The default uses *binary exponentiation in the monoid* to aggregate
one repeated value in O(log length). Thus construction costs O(n),
assignment O(log² n), and a range query O(log n).

If you can compute the aggregate of `length` repeated values in O(1),
supply the optional `repeatValue` function and updates become O(log n):

```kotlin
val sum = aggregateOp(0) { a: Int, b: Int -> a + b }
val tree = a.lazySegmentTree(sum, repeatValue = { x, length -> x * length })
tree.set(0, 3, 7)
```

For updates beyond ASSIGN, use `lazySegmentTree(op, action)` and
supply the full monoid-action laws.

## 3. Give your operation an inverse to unlock more algorithms

```kotlin
val xor = aggregation(0) { a: Int, b: Int -> a xor b }
    .withInverse { it } // XOR is self-inverse

val a = intArrayOf(2, 4, 6, 8)

val fenwick = a.fenwickTree(xor)
val prefixes = a.prefixAggregates(xor)
val updates = a.differenceArray(xor)

prefixes.query(1, 3) // 4 xor 6
updates.apply(1, 3, 7) // toggle these elements by XOR 7
updates.values() // current values, computed in O(n)
```

You can also run offline Mo queries with `a.moAggregate(queries, xor)`:

```kotlin
val queries = listOf(MoQuery(0, 2), MoQuery(1, 4))
val answer = a.moAggregate(queries, xor)
```

To give an `AggregateOp<T>` an inverse, the operation must form an
**abelian group**: associative, commutative, with a two-sided identity
and inverse for *every element* in its domain. No inverse exists
for general MIN/MAX, so they cannot use the group helpers.

For example, addition modulo seven is a valid custom group:

```kotlin
val mod7 = aggregation(0) { a: Int, b: Int -> (a + b) % 7 }
    .withInverse { x -> (7 - x) % 7 }
val table = intArrayOf(2, 3, 4).prefixAggregates(mod7)
table.query(0, 3) // 2
```

Keep mod7 values in `0..6` and stay within numeric limits.

**Performance:** `prefixAggregates(group)` builds O(n) and queries
O(1). `differenceArray(group)` applies an interval update in O(1)
and materializes all values in O(n). `fenwickTree(group)` supports
O(log n) point updates/range queries. `moAggregate` uses the existing
offline block-order Mo implementation with constant-time add/remove
group callbacks.

## 4. Invent an update action for lazy segment trees

An aggregation alone cannot tell a lazy segment tree how an arbitrary
update changes an entire interval. Provide a lawful `RangeAction`:

```kotlin
val concat = aggregation("") { a: String, b: String -> a + b }

val setAll = RangeAction<String, String>(
    apply = { _, value, length -> value.repeat(length) },
    compose = { _, newer -> newer } // last assignment wins
)
val words = arrayOf("a", "b", "c")
val lazy = words.lazySegmentTree(concat, setAll)

lazy.update(0, 2, "x")
lazy.query(0, 3) // "xxc"
```

The `compose(old, newer)` function must mean **first old, then newer**,
and `apply(aggregate, update, length)` must correctly transform
the whole segment without inspecting its elements. As with algebraic
operations generally, the compiler cannot prove these laws.

The custom `RangeAction<T,U>` may use a *different update type U*
from the aggregate type T; this works for `IntArray`, `LongArray`,
object arrays and `List<T>`.

## Which algorithms accept my invented operation?

| Structure | Minimum law for an efficient generic API |
|---|---|
| Segment tree, persistent segment tree, disjoint sparse table | Associative + identity |
| Overlapping sparse table | Above, plus idempotence |
| Static monoid prefix table | Associative + identity; prefixes only |
| Lazy range assignment | Associative + identity (O(log² n) by default) |
| Fenwick (general range queries), general-interval prefix/difference, Mo group convenience | Abelian group with inverse |
| Lazy updates other than assignment | Associative aggregation + compatible update action |
| Mo with custom callbacks | Operation-specific add/remove state supplied by you |
| Ordinary binary search | Monotonic predicate, not an aggregation |
| Fractional cascading | Comparable sorted catalogs, not an aggregation |

**Not every possible two-argument function is associative.** For
example, subtraction can't be turned into an O(log n) segment-tree
aggregation simply by passing a lambda. Use a linear left-to-right
fold if you truly need arbitrary order-dependent nonassociative
functions.

All range queries use `[from, until)`. The original no-argument
SUM APIs remain unchanged.

## Test

```bash
bash tools/test_custom_algebra.sh
```

The command compiles actual repository modules and runs both
`tests/CustomAlgebraTests.kt` and `tests/CustomMonoidExtensionsTests.kt`,
including randomized tests for associative operations, monoid prefixes,
order-sensitive concatenation and overlapping range assignments.
