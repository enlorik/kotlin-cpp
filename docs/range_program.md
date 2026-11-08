# Write range queries as a tiny Kotlin program

A batch of range queries often contains a mixture of queries and point updates.
Instead of choosing a data structure, record **what the problem asks**:

```kotlin
val sum = monoid(0) { a: Int, b: Int -> a + b }
val a = intArrayOf(2, 4, 6)
val result = a.rangeProgram(sum) {
    query(0, 3)       // sum over [0, 3)
    set(1, 10)        // update a[1] for subsequent queries
    query(0, 3)
    query(1..2)       // Kotlin's inclusive range, a[1] + a[2]
}
println(result.answers)  // [12, 18, 16]
println(result.strategy) // POINT_UPDATES
```

**Key idea:** the block is *declarative*. `query(...)` records a command and
returns `Unit`; answers become available after the block finishes, in query
order. This means one query's answer cannot control the next command in the
same block. For interactive/online problems, use `a.rangeIndex(op)` directly.

## Automatic selection

The library records the commands, checks the bounds, snapshots the original
values, then constructs exactly one of the existing indices.

| Commands in program | Strategy | Build | Query | Point assignment |
|---|---|---|---|---|
| Queries only, idempotent op (MIN, MAX, GCD) | SparseTable | O(n log n) | O(1) | N/A |
| Queries only, general associative op (SUM, XOR, string concatenation) | DisjointSparseTable | O(n log n) | O(1) | N/A |
| Any `set` command | SegmentTree | O(n) | O(log n) | O(log n) |

The recorder uses O(q) extra memory for q operations, plus the selected
structure and answer list. Order matters: updates affect **later** queries,
and recorded queries never change retroactively. All index bounds are
zero-based, and `query(from, until)` uses half-open intervals. Empty arrays
and empty queries return the operation identity. Source values are not mutated.

As with `rangeIndex()`, algebraic laws are the caller's responsibility:
`monoid` promises associativity and an identity; `idempotentMonoid` additionally
promises idempotency. Use `checkLaws()` for sampled counterexample testing.

## Operations and inputs

Works with `List<T>`, `Array<T>`, `IntArray`, and `LongArray`; accepts
typed `Monoid<T>` as well as legacy `AggregateOp<T>`. Supports
noncommutative operations, as long as they are associative:

```kotlin
val concat = monoid("") { a: String, b: String -> a + b }
val output = listOf("ab", "c", "d").rangeProgram(concat) {
    query(0, 3)
    set(1, "Z")
    query(0, 3)
}
println(output.answers) // [abcd, abZd]
```

Arbitrary lazy range updates are deliberately **not** offered here: they
require a compatible `MonoidAction`, so use `rangeIndex(op, action)`.

## Single-file submission

Use `--modules range-program` with the standalone generator. This brings in
the necessary range-index and algebra sources, with no graph dependencies.

```bash
python3 tools/build_submission.py --modules range-program \
  --solution Main.kt --output submission.kt
bash tools/test_range_index.sh
bash tools/test_submission_builder.sh
```
