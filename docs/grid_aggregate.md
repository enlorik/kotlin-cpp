# One grid aggregation API: radius, offsets, nearest

This library introduces **one public extension name**: `grid.aggregate(...)`.
The **selection** decides *which coordinates contribute*; the **combination**
decides *how their values are merged*. Both are independent.

Works with `Array<IntArray>`, `List<String>`, `Array<String>`, and
`Array<CharArray>`. A standalone shape-only `gridAggregate(rows, cols, ...)`
is also available. `GridAggregate.kt` reuses `GridDirections` from
`GridBfs.kt` for `Offsets(FOUR)`, `Offsets(EIGHT)`, etc.

## 1. Four adjacent cells: up, down, left, right

```kotlin
val grid = arrayOf(
    intArrayOf(1, 2, 3),
    intArrayOf(4, 5, 6),
    intArrayOf(7, 8, 9)
)
val sum = grid.aggregate(center = 1 to 1, selection = Offsets(FOUR)) { a, b -> a + b }
println(sum) // 2 + 4 + 6 + 8 = 20
```

`Offsets(FOUR)` does **not** include the center. If you explicitly want to
include the center, add `0 to 0`:

```kotlin
val sum = grid.aggregate(
    center = 1 to 1,
    selection = Offsets(0 to 0, -1 to 0, 1 to 0, 0 to -1, 0 to 1),
    combine = { a, b -> a + b }
)
println(sum) // 25
```

`Offsets` accepts any relative offsets: `Offsets(1 to 2, -1 to 2)`, or
existing movement patterns `Offsets(FOUR)`, `Offsets(EIGHT)`,
`Offsets(KNIGHT)`, `Offsets(CUSTOM(...))`. Duplicate offsets are ignored;
out-of-bounds candidates are skipped **before** `value` and `include` run.

## 2. Radius, ring, square

```kotlin
// Manhattan diamond of radius 2, INCLUDING center:
val diamond = grid.aggregate(center = 1 to 1, selection = Radius(2)) { a, b -> a + b }

// Exclude center, but include distances 1 through 2:
val withoutCenter = grid.aggregate(
    center = 1 to 1, selection = Radius(2, minDistance = 1),
    combine = { a, b -> a + b }
)

// ONLY positions at EXACTLY distance 2:
val ring = grid.aggregate(
    center = 1 to 1, selection = Radius(2, minDistance = 2),
    combine = { a, b -> a + b }
)

// Chebyshev distance: axis-aligned square instead of a diamond.
val square = grid.aggregate(
    center = 1 to 1,
    selection = Radius(2, metric = GridMetric.CHEBYSHEV),
    combine = { a, b -> a + b }
)
```

For a single center, `grid.aggregate(center = ..., ...)` returns a value.
For **all** centers, simply omit `center` and get `result.values[r][c]`:

```kotlin
val result = grid.aggregate(selection = Radius(2)) { a, b -> a + b }
println(result.values[1][1])
println(result[1, 1]) // equivalent
```

Radius means **geometric distance**, not an obstacle-avoiding BFS path.

## 3. Closest nonzero cells only, tied at the MINIMUM distance

This reproduces the C++ problem: nonzero original cells must output `-1`,
zero cells must OR only the nearest nonzero sources within Manhattan distance
1 through 5, with `0` when no source is eligible.

```kotlin
val result = grid.aggregate(
    selection = Nearest(
        sources = { r, c -> grid[r][c] != 0 },
        maxDistance = 5
    ),
    combine = { a, b -> a or b }
)
for (r in grid.indices) {
    println(grid[r].indices.joinToString(" ") { c ->
        if (grid[r][c] != 0) "-1" else result.values[r][c].toString()
    })
}
```

**Algorithm:** One multi-source BFS from all eligible source cells computes
the minimum Manhattan distance to a source. For each center, the engine
examines **only that exact distance ring**, and combines the values of each
source on it **once**. A source at a larger distance never contributes.
That makes even non-idempotent combines like **sum** correct: the same source
won't be double-counted through multiple shortest paths.

For `Nearest`, `result.distance(r,c)` returns the nearest source distance,
or `null` when none is found inside `maxDistance`. For Radius or Offsets
results, `distance(r,c)` returns `null` because no nearest-source search
was performed.

The source cell itself is included at distance 0 if it satisfies `sources`;
the C++ example overrides those output cells with `-1` as required.

## 4. Different combining operations

```kotlin
val sum = grid.aggregate(center = 1 to 1, selection = Offsets(FOUR)) { a, b -> a + b }
val or = grid.aggregate(center = 1 to 1, selection = Offsets(FOUR)) { a, b -> a or b }
val xor = grid.aggregate(center = 1 to 1, selection = Offsets(FOUR)) { a, b -> a xor b }
val minimum = grid.aggregate(
    center = 1 to 1, selection = Offsets(FOUR),
    initial = Int.MAX_VALUE, combine = { a, b -> minOf(a, b) }
)
val maximum = grid.aggregate(
    center = 1 to 1, selection = Radius(2),
    initial = Int.MIN_VALUE, combine = { a, b -> maxOf(a, b) }
)
val countPositive = grid.aggregate(
    center = 1 to 1, selection = Radius(2), initial = 0,
    value = { r, c -> if (grid[r][c] > 0) 1 else 0 },
    combine = { a, b -> a + b }
)
```

`initial` must be an **identity** of the combine operation (0 for
addition, 1 for multiplication, 0 for bitwise OR/XOR, Int.MAX_VALUE for
minimum, Int.MIN_VALUE for maximum). `combine` should be associative; use
pure value operations when possible. For integer grids, `value` defaults
to the current cell's Int value. When overflow is a concern, request a
Long result:

```kotlin
val bigSum = grid.aggregate(
    center = 1 to 1, selection = Radius(2),
    initial = 0L,
    value = { r, c -> grid[r][c].toLong() },
    combine = { a, b -> a + b }
)
```

You may restrict eligible positions without changing the selection:

```kotlin
val sumEven = grid.aggregate(
    center = 1 to 1, selection = Radius(2),
    include = { r, c -> grid[r][c] % 2 == 0 },
    combine = { a, b -> a + b }
)
```

For `Nearest`, `include` filters which cells are **eligible sources before
computing nearest distance**. For Radius and Offsets, it filters values
within the selected geometric neighborhood.

## Performance and relationship to existing BFS

- `Offsets`: O(K) for one center, O(H*W*K) for all centers.
- `Radius`: O(radius²) per center, O(H*W*radius²) for all centers;
  large radii are clamped to grid bounds.
- `Nearest` all centers: O(H*W) multi-source BFS to find minimum
  distances, followed by O(sum over centers of their nearest-ring perimeter)
  to combine **distinct sources**. For `maxDistance = 5`, this is
  O(H*W) overall with a small constant. For very large/unlimited radii,
  it can cost O(H*W*(H+W)).
- `Nearest` one center: inspect consecutive Manhattan rings until
  qualifying sources appear; avoids a full-grid BFS.
- Space: O(H*W) for all-center results; O(1) extra for single-center queries
  apart from the supplied selectors.

`Nearest` shares the coordinate conventions and FOUR movement model with
grid BFS but does not simply call the existing single-source `bfsFlex()`:
the latter ignores repeated/equal-distance arrivals, which would lose tied
sources. Geometry-based Radius and Offsets do not need BFS at all.

## Tests

```bash
kotlinc grid/GridBfs.kt grid/GridAggregate.kt tests/GridAggregateTests.kt -include-runtime -d aggregate-tests.jar
java -jar aggregate-tests.jar
```

The suite covers sum, OR, XOR-like aggregations, min, radius, rings, squares,
custom and preset offsets, boundary safety, Long results, non-numeric grids,
source filtering, exact tie handling, no double counting, and 1216 deterministic
grids checked against the literal original C++ algorithm. It also compares
radius, offsets and nearest sums against independent brute-force results.

**Single-file bundling:** `grid/GridAggregate.kt` depends on the movement
presets in `grid/GridBfs.kt`. The current registry is not receiver-aware
when resolving overloaded extension names, so explicitly include those
sources until a proper receiver-aware bundler is implemented.
