# kotlin-cpp

Reusable Kotlin competitive-programming algorithms and fast I/O.

- **Grid aggregation:** [docs/grid_aggregate.md](docs/grid_aggregate.md) explains `grid.aggregate(...)` with `Radius`, `Offsets`, and `Nearest`.
- **Graph aggregation:** [docs/graph_aggregate.md](docs/graph_aggregate.md) documents `g.aggregate(...)` with `GraphNearest`, `GraphRadius`, and `GraphNeighbors`, including fast OR, safe distinct SUM and directed graphs.
- **Graph I/O:** `readGraph(n, m)` reads edge lists; `printValues(values)` prints graph results.
- **Fast I/O:** [docs/fast_io.md](docs/fast_io.md) introduces `readInts()`, `readIntGrid(h, w)` and `printGrid(h, w) { r, c -> ... }`.
- **Full example:** [examples/NearestOrSolution.kt](examples/NearestOrSolution.kt) solves the nearest-tied nonzero bitwise OR problem in a short `main()`.

```kotlin
fun main() {
    val (h, w) = readInts()
    val grid = readIntGrid(h, w)
    val result = grid.aggregate(
        selection = Nearest(sources = { r, c -> grid[r][c] != 0 }, maxDistance = 5),
        combine = { a, b -> a or b }
    )
    printGrid(h, w) { r, c ->
        if (grid[r][c] != 0) -1 else result.values[r][c]
    }
}
```

For a standalone judge file, include `io/FastIO.kt`, `grid/GridAggregate.kt`, and `grid/GridBfs.kt`.

Graph-version example: [examples/GraphNearestOrSolution.kt](examples/GraphNearestOrSolution.kt). This uses `readGraph(n, m)`, `GraphNearest` and `IntAggregations.OR` to calculate the nearest-tied source OR in O(V+E).
