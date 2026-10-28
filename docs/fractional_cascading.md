# Fractional cascading: search many sorted catalogs efficiently

Suppose you have several sorted arrays and need the insertion position
of the **same target** in every array. Repeating binary search takes
O(K log N) time for K similarly sized arrays.

Fractional cascading preprocesses the catalogs so each query needs
just **one binary search**, then O(1) work per additional catalog:

```kotlin
val a = intArrayOf(1, 2, 2, 6)
val b = intArrayOf(0, 2, 5)
val c = intArrayOf(2, 2, 3)

val fc = fractionalCascade(a, b, c)
val first = fc.lowerBounds(2) // [1, 1, 0]
val after = fc.upperBounds(2) // [3, 2, 2]
```

For catalog `i`, `lowerBounds(x)[i]` is the first index with
`catalog[i][index] >= x`, or its length if none exists.
`upperBounds(x)[i]` similarly finds the first value > x.

## How the data structure works

From the last catalog upward, each augmented catalog merges its own
elements with **every second element of the next augmented catalog**.
Each merged position remembers (1) how many original entries came before
it and (2) the preceding sampled position in the next layer.

We binary search only the top augmented catalog. Then the stored hints
bring us within **two comparisons** of the insertion position in each
subsequent layer. Counts of original entries translate those augmented
positions back into the original arrays.

Construction: O(N) time and space, where N is the sum of all original
catalog sizes. Each `lowerBounds` or `upperBounds` query takes
O(log(N + 1) + K) time and returns an IntArray of K indices.
The input arrays are copied logically into the augmented representation,
so mutating the caller's arrays after construction is safe.

## Contracts

- Catalogs must be sorted ascending. Duplicates are supported.
- Empty catalogs and even zero catalogs are valid.
- Queries are read-only. To change a catalog, rebuild the cascade.
- This version supports `IntArray` catalogs. It is optimized for repeated
  searches for **one target across many fixed sorted catalogs**.
- It's not a speedup over one binary search on one array. The benefit
  appears when K is large and there are many queries.

This is the **linear-chain catalog** variant of fractional cascading.
It is not a fully general dynamic fractional-cascading data structure
for arbitrary graph navigation or updates.

Source: `search/FractionalCascading.kt`.
Tests: `tests/FractionalCascadingTests.kt`.

```bash
kotlinc search/FractionalCascading.kt tests/FractionalCascadingTests.kt \
    -include-runtime -d fractional-tests.jar
java -jar fractional-tests.jar
```
