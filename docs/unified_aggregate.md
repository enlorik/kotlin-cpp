# One aggregation vocabulary for grids and graphs

Both structures use exactly the same public names:

- `aggregate(...)` selects cells/vertices and combines their values.
- `Nearest(...)` chooses **only equally nearest** eligible sources.
- `Radius(2)` chooses positions/vertices at distance 0, 1 or 2.
- `Radius(2, minDistance = 2)` chooses positions/vertices at **exactly** distance 2.
- `Neighbors` chooses immediate neighbors.
- `op = IntAggregations.OR / SUM / MIN / MAX / AND / XOR` chooses the operation.

Kotlin resolves the implementation using the receiver:
`grid.aggregate(...)` handles grid positions; `g.aggregate(...)` handles
graph vertices. `Nearest(sources={r,c->...})` constructs the grid selector,
while `Nearest(sources={v->...})` constructs the graph selector. You do not
need to mention `GraphNearest` or `GraphRadius` in new solutions.

## Compare the same problem in both shapes

**Grid**: tied closest nonzero cells within Manhattan distance 5.

```kotlin
val nearestGrid = grid.aggregate(
    selection = Nearest(
        sources = { r, c -> grid[r][c] != 0 },
        maxDistance = 5
    ),
    op = IntAggregations.OR
)
printGrid(h, w) { r, c ->
    if (grid[r][c] != 0) -1 else nearestGrid.values[r][c]
}
```

**Graph**: tied closest nonzero vertices within 5 unweighted edges.

```kotlin
val nearestGraph = g.aggregate(
    selection = Nearest(
        sources = { v -> values[v] != 0 },
        maxDistance = 5
    ),
    value = { v -> values[v] },
    op = IntAggregations.OR
)
printValues(n) { v ->
    if (values[v] != 0) -1 else nearestGraph.values[v]
}
```

The grid defaults to the integer stored at each cell, so it can omit
`value`. Graphs can also omit `value` when created with
`readGraph { header(); values(); edges() }` or `g.withValues(values)`.
For graphs without attached Int values, provide an explicit vertex `value`
lambda as shown above. See [Graph input DSL](graph_input_dsl.md).

## Shorter when graph values are attached

```kotlin
val g = readGraph {
    header()
    values()
    edges()
}
val nearestGraph = g.aggregate(
    selection = Nearest(sources = { v -> g.values[v] != 0 }),
    op = IntAggregations.OR
)
```

Both graph and grid can now use an `op` without an explicit `value`
lambda in the common Int-valued case. You can still override `value`
for counts, degrees and custom calculations.

## Other matching examples

**Sum immediate neighbors:**

```kotlin
val a = grid.aggregate(selection = Neighbors, op = IntAggregations.SUM)
val b = g.aggregate(selection = Neighbors, value = { v -> values[v] },
    op = IntAggregations.SUM)
```

`Neighbors` means FOUR orthogonal cells for grids (center excluded).
On graphs it means **distinct adjacent vertices** reachable by an
outgoing edge, also excluding the center. Parallel edges do not duplicate
a neighbor. On grids, `Offsets(EIGHT)`, `Offsets(KNIGHT)`, and
`Offsets(CUSTOM(...))` still enable custom physical positions.

**Sum distance up to 2:**

```kotlin
val a = grid.aggregate(selection = Radius(2), op = IntAggregations.SUM)
val b = g.aggregate(selection = Radius(2), value = { v -> values[v] },
    op = IntAggregations.SUM)
```

**Minimum at exactly distance 2 from one center:**

```kotlin
val a = grid.aggregate(center = 1 to 1, selection = Radius(2, minDistance = 2),
    op = IntAggregations.MIN)
val b = g.aggregate(center = 0, selection = Radius(2, minDistance = 2),
    value = { v -> values[v] }, op = IntAggregations.MIN)
```

Both variants support old-style custom `initial` plus `combine`
lambdas. Graph nearest-source custom combines default to exact distinct
source contributions; set `idempotent = true` only for operations such
as OR/MIN/MAX/AND when using the custom graph lambda overload.

## Distances and runtime are not identical

Even though the **names** match, the structures are different:

| Selector | Grid meaning | Graph meaning |
|---|---|---|
| `Neighbors` | Four cardinal cells | Distinct outgoing adjacent vertices |
| `Radius(2)` | Manhattan distance <= 2 | Shortest path <= 2 edges |
| `Nearest(...)` | Nearest qualifying cells by Manhattan distance | Nearest qualifying vertices by outgoing-edge shortest path |

Graph distance is **unweighted** (number of edges). Graph `Radius` does
not support `metric = GridMetric.CHEBYSHEV`; passing it raises a clear
`IllegalArgumentException`. On a grid, `Radius(2, metric =
GridMetric.CHEBYSHEV)` is still a valid square neighborhood.

Algorithms retain their existing complexity. In particular, graph
nearest OR with `IntAggregations.OR` is O(V+E), while nearest SUM must
avoid counting the same source through multiple shortest paths and can
cost O(V*(V+E)) in the worst case. Grid `Nearest` uses a grid-specific
Manhattan distance method and has its own costs; uniform syntax does not
promise uniform runtime.

## Backward compatibility

Nothing has been removed:

- `GraphNearest(...)`, `GraphRadius(...)`, `GraphNeighbors` continue working.
- `Nearest({r,c->...})`, `Radius(...)`, `Offsets(FOUR)`, etc. for grids continue working.
- `grid.aggregate(selection = ..., combine = {a,b -> ...})` still works.
- `g.aggregate(selection = GraphNearest(...), value = ..., op = ...)` still works.

The unified syntax is supplied by `shared/UnifiedAggregate.kt`, which
depends on `grid/GridAggregate.kt` and `graph/GraphAggregate.kt`.
Those modules depend on their original graph/grid types. Single-file
online judge submissions must include these sources or bundle them before
submission. The current symbol registry is not receiver-aware and cannot
automatically resolve every overloaded `aggregate` or `Nearest` usage;
include both aggregation engines plus the adapter when using the shared API.

## Regression check

```bash
kotlinc graph/*.kt grid/*.kt shared/UnifiedAggregate.kt \
    tests/UnifiedAggregateTests.kt -include-runtime -d unified-test.jar
java -jar unified-test.jar
```

This test exercises both receivers in the same compilation, including
the distinct-source SUM guarantee and compatibility with old selectors.

## Reusing aggregation operations in segment trees

`IntAggregations` and `LongAggregations` now live in
`shared/AggregationOp.kt`, independent of graph implementations.
The public type `AggregateOp<T>` holds the operation, its identity, and
an optional idempotence property. `GraphAggregateOp<T>` remains a
source-compatible type alias.

```kotlin
val t = intArrayOf(2, 4, 6).segmentTree(IntAggregations.SUM)
val first = t[0..1] // 6
t[1] = 100
val second = t.query(0, 3) // 108; half-open range
```

Only the shared aggregation and segment-tree modules are needed here.
