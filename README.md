# kotlin-cpp

Reusable Kotlin competitive-programming algorithms and fast I/O.

- **Advanced binary search:** [docs/advanced_binary_search.md](docs/advanced_binary_search.md) covers `firstTrueFrom`, `bisect`, `bisectRoot`, and `partitionPoint`.
- **Fractional cascading:** [docs/fractional_cascading.md](docs/fractional_cascading.md) preprocesses sorted Int catalogs for all lower/upper bounds in O(log N + K) per query.
- **Parallel binary search:** [docs/parallel_binary_search.md](docs/parallel_binary_search.md) groups multiple monotone queries by midpoint for efficient offline sweeps.
- **Shared aggregation ops:** `shared/AggregationOp.kt` defines `AggregateOp`, `IntAggregations` and `LongAggregations` once for grids, graphs and trees. No graph source needed for a segment tree.
- **Segment tree prefix search:** [docs/segment_tree.md](docs/segment_tree.md) introduces `a.segmentTree()`, `tree.query`, `tree.set` and `tree.findPrefix` in O(log n).
- **Binary search:** [docs/binary_search.md](docs/binary_search.md) explains `firstTrue`, `lastTrue`, `lowerBound`, and `upperBound` for Int/Long ranges and sorted arrays, with overflow-safe midpoints.

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

### Binary search without boilerplate

```kotlin
val a = intArrayOf(2, 4, 4, 4, 7)
val first = a.lowerBound(4)     // 1
val after = a.upperBound(4)     // 4
val count = after - first       // 3

val firstNine = firstTrue(a.size) { i -> a[i] >= 9 } // a.size when absent
val lastSmall = lastTrue(a.size) { i -> a[i] < 7 }   // 3
val bigAnswer = firstTrue(0L, 1_000_000_000_000_000_000L) { x -> feasible(x) }
```

For standalone submissions, add only `search/BinarySearch.kt` to your solution. It does not require the graph/grid modules.

### Friendlier segment-tree syntax

```kotlin
val tree = intArrayOf(2, 4, 6, 8, 10).segmentTree(IntAggregations.SUM)
val before = tree[1..3]       // 4 + 6 + 8 = 18 (inclusive Kotlin range)
tree[2] = 100                // point update
val after = tree[1..3]        // 4 + 100 + 8 = 112
val index = tree.findPrefix { it >= 110 } // 3, when prefix values are monotone
```

For these calls, include `shared/AggregationOp.kt`, `tree/SegmentTree.kt`, and `shared/SegmentTreeAggregations.kt`. No graph/grid dependency. All traditional `tree.query(from, until)` calls retain half-open bounds.
