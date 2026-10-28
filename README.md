# kotlin-cpp

Reusable Kotlin competitive-programming algorithms and fast I/O.

- **Unified aggregation vocabulary:** [docs/unified_aggregate.md](docs/unified_aggregate.md) demonstrates matching `Nearest`, `Radius`, `Neighbors`, and `op` names for both grids and graphs.
- **Grid aggregation:** [docs/grid_aggregate.md](docs/grid_aggregate.md) explains `grid.aggregate(...)` with `Radius`, `Offsets`, and `Nearest`.
- **Graph aggregation:** [docs/graph_aggregate.md](docs/graph_aggregate.md) documents `g.aggregate(...)` with matching `Nearest`, `Radius`, `Neighbors` selectors (and legacy graph-specific names), including fast OR, safe distinct SUM and directed graphs.
- **Graph input DSL:** [docs/graph_input_dsl.md](docs/graph_input_dsl.md) lets you declare edge columns, directed/indexing flags, and value arrays in their true input order via `readGraph { ... }` or `graph { ... }`.
- **Graph I/O:** `readGraph(n, m)` reads edge lists; `printValues(values)` prints graph results.
- **Fast I/O:** [docs/fast_io.md](docs/fast_io.md) introduces `readInts()`, `readIntGrid(h, w)` and `printGrid(h, w) { r, c -> ... }`.
- **Full example:** [examples/NearestOrSolution.kt](examples/NearestOrSolution.kt) solves the nearest-tied nonzero bitwise OR problem in a short `main()`.

```kotlin
fun main() {
    val (h, w) = readInts()
    val grid = readIntGrid(h, w)
    val result = grid.aggregate(
        selection = Nearest(sources = { r, c -> grid[r][c] != 0 }, maxDistance = 5),
        op = IntAggregations.OR
    )
    printGrid(h, w) { r, c ->
        if (grid[r][c] != 0) -1 else result.values[r][c]
    }
}
```

For a standalone judge file using the shared vocabulary, include `io/FastIO.kt`, `grid/GridAggregate.kt`, `grid/GridBfs.kt`, `graph/GraphAggregate.kt`, the base graph types, and `shared/UnifiedAggregate.kt`.

Graph-version example: [examples/GraphNearestOrSolution.kt](examples/GraphNearestOrSolution.kt). This uses `readGraph(n, m)`, `GraphNearest` and `IntAggregations.OR` to calculate the nearest-tied source OR in O(V+E).

For the shared grid/graph selector names and grid `op = IntAggregations.SUM` calls, include `shared/UnifiedAggregate.kt` along with both aggregation engines in the compiled source bundle.

### Describe the input format

```kotlin
val g = readGraph {
    header()               // reads n m
    edges { from(); to() } // reads m pairs
    values()               // n vertex values after the edges
}
val answer = g.aggregate(
    selection = Nearest(sources = { v -> g.values[v] != 0 }),
    op = IntAggregations.OR
)
printValues(g.n) { v -> if (g.values[v] == 0) answer.values[v] else -1 }
```

For input with values before edges, move `values()` above `edges()`. For `u v w`, write `edges { from(); to(); weight() }`.
