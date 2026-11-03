# Type-safe algebra: let Kotlin tell you what an algorithm needs

This optional API builds on the existing `AggregateOp<T>` and
`aggregation(...)` API. Nothing in the old library is removed or
silently changed. Use the stricter types when you want IDE completion
and compile-time checks for **which mathematical capabilities you supplied**.

## 1. Invent an operation

```kotlin
val product = monoid(identity = 1) { a: Int, b: Int -> a * b }

val a = intArrayOf(2, 3, 4)
a.segmentTree(product).query(0, 3)             // 24
a.disjointSparseTable(product).query(0, 3)      // 24
a.persistentSegmentTree(product).query(0, 0, 3) // 24
```

`Monoid<T>` declares `identity` and `combine`. The caller promises
a **two-sided identity** and **associativity**. No commutativity or inverse
is required. Noncommutative string concatenation works.

## 2. More capabilities unlock more algorithms

A static overlapping sparse table requires *idempotence*:

```kotlin
val minimum = idempotentMonoid(Int.MAX_VALUE) { a: Int, b: Int -> minOf(a, b) }
val table = a.sparseTable(minimum)
```

Fenwick with general interval queries requires an **abelian group**
(associativity, identity, commutativity, and an inverse for all elements):

```kotlin
val xor = group(
    identity = 0,
    combine = { a: Int, b: Int -> a xor b },
    inverse = { x: Int -> x }
)

val fenwick = a.fenwickTree(xor)
val prefix = a.prefixAggregates(xor)
val changes = a.differenceArray(xor)
val answer = a.moAggregate(listOf(MoQuery(0, 2)), xor)
```

Other custom abelian groups are supported. For modular addition,
make sure all sample values belong to the modular domain and avoid
unwanted numeric overflow.

The compiler **rejects** these calls with the stricter interfaces:

```kotlin
a.fenwickTree(product)  // ERROR: expected an abelian-group capability
a.sparseTable(product)  // ERROR: expected an idempotent capability
```

Likewise, `group(identity=..., combine=...)` with no `inverse` is
a Kotlin error reporting the missing required argument. VS Code with
a Kotlin language server or IntelliJ can offer completion/diagnostics
from those typed signatures; editor support quality depends on the
installed Kotlin tooling.

## 3. Lazy range updates have their own laws

```kotlin
val sum = monoid(0) { a: Int, b: Int -> a + b }
val add = action<Int, Int>(
    apply = { total, delta, length -> total + delta * length },
    compose = { old, newer -> old + newer }
)

val lazy = a.lazySegmentTree(sum, add)
lazy.update(0, 2, 5)
lazy.query(0, 3)
```

`MonoidAction<T,U>` additionally requires an update that transforms
an *entire interval aggregate*, plus a composition that means
**old update, then newer update**. The aggregate and update types
may differ. The compiler requires both functions and matching types.

## 4. Detect false mathematical promises with counterexamples

Kotlin can check a method exists; it cannot prove arbitrary
associativity, inverses, commutativity, idempotence, or action laws.

Use the built-in **seeded randomized law tests**:

```kotlin
product.checkLaws(samples = listOf(-2, -1, 0, 1, 2, 3))
minimum.checkLaws(samples = listOf(-2, 0, 2, 3))
xor.checkLaws(samples = listOf(0, 1, 2, 7, -1))

add.checkActionLaws(
    monoid = sum,
    elements = listOf(-2, 0, 3),
    updates = listOf(-5, 0, 7)
)
```

Incorrect definitions throw `AlgebraLawViolation` and report the
failed law plus a sampled counterexample:

```kotlin
val broken = monoid(0) { a: Int, b: Int -> a + b + a * b * b }
broken.checkLaws(listOf(1, 2, 3), iterations = 1000, seed = 1)
// Algebra law failed: associativity.
// Counterexample: a=..., b=..., c=..., (a*b)*c=..., a*(b*c)=...
```

Tests check:
- Monoid: left/right identity, associativity.
- IdempotentMonoid: above plus `combine(x,x) == x`.
- AbelianGroupSpec: monoid laws plus commutativity and both inverse directions.
- MonoidAction: distributivity over interval aggregation, composition
  (older then newer), empty-interval identity.

Choose representative samples; include edge values and important
update sequences. For floating-point operations, optionally provide
`equivalent = { a, b -> abs(a - b) < tolerance }`. These are
**property-based tests, not formal proofs**, and exhaustive checking
is not possible for arbitrary Kotlin functions.

## Existing operations and source compatibility

Old APIs continue working:

```kotlin
a.segmentTree(IntAggregations.MIN)
a.fenwickTree(IntAggregations.XOR)
a.persistentSegmentTree()
```

To opt into the typed interface from existing `AggregateOp<T>`:

```kotlin
val minimum = IntAggregations.MIN.asIdempotentMonoid()
val extra = a.sparseTable(minimum)
```

`asIdempotentMonoid()` checks the existing flag, but only runtime law
testing can search for violations. `asMonoid()` makes the weaker promise.

## Run IDE-typing and law checks in CI

```bash
bash tools/test_typed_algebra.sh
```

The runner uses the actual repository sources, checks randomized laws,
then compiles four deliberately invalid snippets and **requires the
Kotlin compiler to fail**. The repository-wide Kotlin test suite also
includes these law tests. See
`tests/TypedAlgebraTests.kt` for complete examples.

**Complexity:** Typed wrappers delegate to the existing data structures
and preserve their asymptotic complexities; the optional law checks
are separate development-time work, never performed on each query.
