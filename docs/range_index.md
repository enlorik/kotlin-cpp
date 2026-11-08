# Auto-select an index for a user-defined associative operation

The library already lets you invent operations with `aggregateOp`, `aggregation`,
`monoid`, `idempotentMonoid`, and `group`, then reuse them across trees, sparse
tables, Fenwick, difference arrays, Mo, and generic range assignment. See
[custom_algebra.md](custom_algebra.md) and [typed_algebra.md](typed_algebra.md).

`rangeIndex` adds a tiny **optional façade** when you do not want to choose
between the three most common range-index structures manually.

## Declare the operation once

```kotlin
val min = idempotentMonoid(Int.MAX_VALUE) { a: Int, b: Int -> minOf(a, b) }
val sum = monoid(0) { a: Int, b: Int -> a + b }
val concat = monoid("") { a: String, b: String -> a + b }

val numbers = intArrayOf(4, 1, 7, 3)
val minimum = numbers.rangeIndex(min)
minimum.query(1, 4)    // 1
minimum.strategy       // STATIC_IDEMPOTENT

val totals = numbers.rangeIndex(sum)
totals.query(1, 4)      // 11
totals.strategy         // STATIC_GENERAL

val words = listOf("ab", "c", "def").rangeIndex(concat)
words.query(0, 3)      // "abcdef"
```

The façade selects based only on the caller-declared mathematical capability:

| Request | Chosen structure | Build | Query | Update |
|---|---|---|---|---|
| `rangeIndex(idempotentOp)` | SparseTable | O(n log n) | O(1) | Static |
| `rangeIndex(otherMonoid)` | DisjointSparseTable | O(n log n) | O(1) | Static |
| `rangeIndex(op, mutable = true)` | SegmentTree | O(n) | O(log n) | O(log n) point `set` |
| `rangeIndex(op, action)` | LazyAggregateTree | O(n) | O(log n) | O(log n) range `update` if action is O(1) |

`IdempotentMonoid` is a type-level claim that `combine(x, x) == x`;
`AggregateOp.idempotent` is an equivalent legacy declaration. Neither is
mathematically proved by Kotlin. Use `.validated(samples)` / `.checkLaws(...)`
to search for counterexamples.

## For changing data

```kotlin
val changing = numbers.rangeIndex(sum, mutable = true)
changing.set(1, 100)
changing.query(0, 3) // 111
changing.strategy    // POINT_UPDATES
```

Calling `set` on a static index throws `IllegalStateException` rather than
silently rebuilding an O(n log n) sparse table.

For *range updates* use an explicit lawful action:

```kotlin
val add = action<Int, Int>(
    apply = { total, delta, length -> total + delta * length },
    compose = { old, newer -> old + newer }
)
val lazy = numbers.rangeIndex(sum, add)
lazy.update(0, 2, 5)
lazy.query(0, 4) // 25
```

The overload returning `LazyAggregateTree` requires a properly distributed
segment update action, not merely an arbitrary element-level lambda.

## Choosing explicit algorithms remains useful

- For generic **range assignment** with any associative operation, the existing
  `a.lazySegmentTree(op)` works already. Its default assignment is O(log² n)
  unless you supply an O(1) `repeatValue` for O(log n) updates.
- For **static SUM/XOR and an inverse**, `a.prefixAggregates(group)` offers
  O(1) range queries after just O(n) preprocessing, usually leaner than a
  disjoint sparse table; the façade deliberately does not infer an inverse.
- For **Fenwick** O(log n) dynamic queries, declare `group`/`withInverse`
  explicitly. MIN and MAX lack inverses for arbitrary group range queries.
- For **non-associative** operations such as subtraction, no segment or
  sparse table can magically produce correct arbitrary range aggregates.

All indexes use zero-based half-open `[from, until)` intervals. All constructors
accept empty arrays/lists; an empty range returns the operation identity.
The convenience façade performs no algebraic-law proving and makes no promise
that every operation works with every update.

## Verify and benchmark

```bash
bash tools/test_range_index.sh
bash tools/benchmark_range_index.sh
```

The first command compiles against **actual repository sources**, tests typed
and legacy APIs, and runs randomized naive-oracle comparisons. The second
prints deterministic single-run timing observations for static idempotent and
general indices, group prefix queries, point updates, and lazy range assignment.
JVM JIT/GC/environment affect timings: they are **not** pass/fail criteria and
should not be treated as a formal performance ranking.
