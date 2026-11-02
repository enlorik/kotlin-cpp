# One aggregation vocabulary, mathematically safe update APIs

`IntAggregations` and `LongAggregations` remain shared across grids,
graphs, segment trees, sparse tables, and now **configurable Fenwick,
lazy, and persistent trees**. The original no-argument constructors
are preserved and continue using Long-valued sums.

All index intervals are **half-open**: `query(from, until)` includes
`from` but not `until`.

## Choose an aggregation and structure

| Structure | SUM | MIN / MAX | XOR / OR / AND | Arbitrary associative operation |
|---|---|---|---|---|
| `segmentTree(op)` | Yes | Yes | Yes | Yes |
| `sparseTable(op)` | No | Yes | Yes | Idempotent only |
| `disjointSparseTable(op)` | Yes | Yes | Yes | Yes |
| `fenwickTree(op)` | Yes | No | XOR only | Requires abelian group + inverse |
| `lazySegmentTree { ... }` | ADD / ASSIGN | ADD / ASSIGN | ASSIGN only | Explicit `RangeAction` |
| `persistentSegmentTree(op)` | Yes | Yes | Yes | Yes |
| `prefixSums()`, `differenceArray()` | Additive specializations | No | No | No |

Why these differences? A **segment tree** needs an associative operation
(monoid). A **Fenwick tree with general interval queries** also needs
an inverse and commutativity (an *abelian group*). A **lazy tree**
needs update actions that can transform an aggregate for a whole
segment without inspecting its individual elements.

## Configurable Fenwick: SUM and XOR

```kotlin
val a = intArrayOf(2, 4, 6, 8)

val sum = a.fenwickTree(IntAggregations.SUM)
sum.query(1, 3)    // 10
sum.add(2, 5)      // a[2] += 5
sum.query(1, 3)    // 15

val xor = a.fenwickTree(IntAggregations.XOR)
xor.apply(1, 7)    // a[1] = a[1] xor 7
xor.query(0, 4)    // bitwise XOR over all elements
```

`set(i, value)`, `prefix(until)`, `query(from, until)`,
`all()`, and `tree[i]` are available. All operations except
point access and `all()` use O(log n) time.

**Do not use MIN, MAX, OR, or AND here.** None has a general
inverse to recover an arbitrary interval aggregate from prefixes, so
the built-in constructor rejects them. Use `segmentTree(MIN)`
instead. A *specialized* min-Fenwick with restricted decreasing
updates is a different algorithm and is not this API.

For an explicitly defined custom abelian group:

```kotlin
val mod7 = AbelianGroup(
    op = AggregateOp(0, { a: Int, b: Int -> (a + b) % 7 }),
    inverse = { x: Int -> (7 - x) % 7 }
)
val tree = listOf(2, 3, 4).fenwickTree(mod7)
```

The caller must guarantee that the custom operation is associative,
commutative and invertible with the specified identity.

## Configurable lazy segment tree

```kotlin
val a = intArrayOf(2, 4, 6, 8)

val minimum = a.lazySegmentTree {
    aggregate = IntAggregations.MIN
    updates = RangeUpdates.ADD
}
minimum.add(1, 4, 5)
minimum.query(0, 4) // 2
minimum.query(1, 4) // 9

val total = a.lazySegmentTree {
    aggregate = IntAggregations.SUM
    updates = RangeUpdates.ADD_ASSIGN
}
total.add(0, 2, 10)
total.set(2, 4, 3)
total.query(0, 4) // 35
```

Choose `RangeUpdates.ADD`, `RangeUpdates.ASSIGN`, or
`RangeUpdates.ADD_ASSIGN`. For SUM / MIN / MAX all three modes
are valid. For XOR / OR / AND only ASSIGN is valid: assigning one
constant to every element has a known combined aggregate, whereas
adding an arbitrary number does not preserve a bitwise aggregate.

```kotlin
val bits = a.lazySegmentTree {
    aggregate = IntAggregations.XOR
    updates = RangeUpdates.ASSIGN
}
bits.set(0, 3, 7) // aggregate is 7 xor 7 xor 7 == 7
```

The default no-argument `a.lazySegmentTree()` remains the existing
**Long-valued SUM tree** with `add` and `set`.
The DSL version returns aggregates in the source type, so an Int tree
uses Int arithmetic and a Long tree uses Long arithmetic; check
overflow according to your problem constraints.

**Custom actions:** for an associative operation with custom update
algebra, provide both the segment-wide application and composition:

```kotlin
val strings = listOf("a", "b", "c").lazySegmentTree(
    op = AggregateOp("", { x: String, y: String -> x + y }),
    action = RangeAction<String, String>(
        apply = { _, token, length -> token.repeat(length) },
        compose = { _, new -> new } // latest assignment wins
    )
)
strings.update(0, 2, "*") // ["*", "*", "c"]
strings.query(0, 3)       // "**c"
```

The composition order is **older, then newer**. Your action must obey
the monoid-action laws; the library cannot prove arbitrary user-defined
algebra correct.

## Configurable persistent trees

```kotlin
val a = intArrayOf(2, 4, 6)
val minimum = a.persistentSegmentTree(IntAggregations.MIN)

val v1 = minimum.set(0, 1, 9)
val v2 = minimum.set(0, 0, 20) // another branch from version 0

minimum.query(0, 0, 3)  // 2
minimum.query(v1, 0, 3) // 2
minimum.query(v2, 0, 3) // 4
```

This also supports SUM, MAX, XOR, OR, AND and custom associative
`AggregateOp<T>`, including order-sensitive string concatenation.
`apply(version, index, delta)` *combines* at one point: it adds under
SUM and toggles bits under XOR; `set` replaces the point. Both return
a new version ID, preserving all old versions.

Construction O(n), each point update and query O(log n), and
O(log n) new nodes per version. The no-argument
`a.persistentSegmentTree()` remains the older Long-sum implementation.

## Why no `prefixSums(MIN)`?

Prefix subtraction works because sums have inverses. MIN does not.
For an immutable array, choose `sparseTable(MIN)`; for updates,
choose `segmentTree(MIN)`. For XOR, use the configurable group
Fenwick or a disjoint sparse table, depending on whether updates occur.

## Verify

```bash
bash tools/test_operation_apis.sh
```

This compiles old and new range structures together, then runs a
randomized oracle suite covering SUM/MIN/MAX/XOR/OR/AND, custom
groups, noncommutative custom actions and persistent branch isolation.
Numeric arithmetic wraps on overflow; bounds and identities must fit
your contest's required semantics.
