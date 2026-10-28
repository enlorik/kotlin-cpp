# Flexible Dijkstra: weighted grids and graphs

Dijkstra finds **minimum-total-cost paths** with nonnegative movement costs.
Unlike BFS, its distances are not necessarily the number of moves; unlike DFS,
it guarantees minimum cost for each finalized node.

Both new implementations support filtering, path reconstruction, and customizable
callbacks. Existing `g.dijkstra(src)` and `g.multiDijkstra(sources)` stay
unchanged.

## Weighted grid: the simplest way

```kotlin
val costs = arrayOf(
    intArrayOf(1, 5, 2),
    intArrayOf(3, 9, 1),
    intArrayOf(2, 1, 4)
)

// On Array<IntArray>, the default movement cost is the destination's cell value.
val result = costs.dijkstraFlex(start = 0 to 0, target = 2 to 2)
println(result.distance(2, 2))          // 10
println(result.reconstructPath(2 to 2)) // [(0,0), (1,0), (2,0), (2,1), (2,2)]
```

For `Array<CharArray>`, `Array<String>`, `List<String>` and shape-only
`gridDijkstraFlex(rows, cols, ...)`, the default cost is **1** per move.

## Custom movements, obstacles and cell cost

```kotlin
val grid = listOf(".....", ".#X..", ".....")
val terrainCost = Array(grid.size) { LongArray(grid[0].length) { 2L } }

val result = grid.dijkstraFlex(
    start = 0 to 0,
    target = 2 to 4,
    directions = CUSTOM(1 to 2, -1 to 2), // or FOUR / EIGHT / KNIGHT
    canGo = { r, c -> grid[r][c] !in "#X~" },
    cost = { r, c -> terrainCost[r][c] },
    onRelax = { fromR, fromC, r, c, newDist ->
        // A cheaper tentative path to (r,c) was found via (fromR,fromC).
    },
    onFinalize = { r, c, finalDist ->
        // Minimum path cost to (r,c) is now proven.
    }
)
println(result.reconstructPath(2, 4))
```

**The boundary and finalized-visited checks are automatic**, before `canGo`
or `cost` runs. The start is always included even if `canGo` would block it.

### Cost depends on the current AND next cell

```kotlin
val height = arrayOf(intArrayOf(2, 9), intArrayOf(4, 7))
val result = height.dijkstraFlex(
    start = 0 to 0,
    moveCost = { fromR, fromC, r, c ->
        kotlin.math.abs(height[r][c] - height[fromR][fromC]).toLong()
    }
)
println(result.distance(1, 1)) // 5
```

If supplied, `moveCost(fromR,fromC,r,c)` **overrides** the simpler
`cost(r,c)`. Use `cost` for ordinary cell-entry weights and `moveCost`
for directional or transition-dependent costs.

### Cost of the starting cell

Default distance at the source is `0`, meaning **only entering the next
cells is charged**. To include the source cell's weight:

```kotlin
val res = costs.dijkstraFlex(
    start = 0 to 0,
    startCost = costs[0][0].toLong()
)
```

## Graph Dijkstra

The existing ordinary functions continue working:

```kotlin
val g = SparseGraph(4, directed = true)
g.addEdge(0, 1, 5L)
g.addEdge(0, 2, 2L)
g.addEdge(2, 1, 1L)
println(g.dijkstra(0).dist[1]) // 3
```

New flexible extension:

```kotlin
val result = g.dijkstraFlex(
    src = 0,
    target = 1,
    canGo = { u, edge -> edge.to !in forbidden },
    cost = { u, edge -> edge.w }, // default: original edge weight
    onRelax = { u, v, edge, newDist -> /* improved tentative path */ },
    onFinalize = { u, finalDist -> /* shortest cost is proven */ }
)
println(result.reconstructPath(1)) // [0, 2, 1]
```

You can override graph edge weights through `cost(u, edge)`.
The value must be nonnegative and less than `GRAPH_INF`.

## Results and callback semantics

Grid `GridDijkstraResult` exposes:
- `dist[r][c]`: minimum path cost on full traversal; `GRID_DIJKSTRA_INF` if unreachable.
- `distance(r,c): Long?`: null when unreachable.
- `prev`, `reconstructPath(r,c)`, and `reconstructPath(r to c)`.
- `order`, `orderIndices`: cells in finalization order.
- `hasPath(r,c)`, `isFinalized(r,c)`, and `settled`.

Graph `DijkstraFlexResult` similarly exposes `dist[v]`, `distance(v)`,
`prev`, `reconstructPath(v)`, `order`, `hasPath(v)`,
`isFinalized(v)`, and `settled`.

**Important:** `onRelax` may run multiple times for the same cell/vertex:
Dijkstra might find an initially expensive path and improve it later.
`onFinalize` runs once, when the priority queue confirms optimal distance.
When `target` is supplied, the algorithm stops after finalizing that target,
so other discovered distances can still be **tentative**. Use `isFinalized`
to tell which distances are proven optimal. Without a target, all reachable
positions are finalized.

Negative costs are forbidden (an evaluated negative cost throws). Even zero
costs are supported, including zero-cost cycles. Distances are `Long`, and
paths that would reach the infinity sentinel are ignored rather than overflowing.
Use Bellman-Ford for negative edges.

## Complexity, dependencies and tests

With constant-time hooks, grid Dijkstra takes
`O(rows * cols * directions.size * log(rows*cols))` time and memory
`O(rows*cols + queued candidates)`. Graph Dijkstra takes
`O((V+E) log V)` time with priority-queue memory depending on relaxations.

The grid implementation requires `grid/GridBfs.kt` for movement presets but
does **not** require `graph/GraphTypes.kt`. The graph implementation uses
`SparseGraph`, `Edge`, and `GRAPH_INF` from existing `graph/` files.

From the repository root:

```bash
kotlinc grid/GridBfs.kt grid/GridDijkstra.kt tests/GridDijkstraTests.kt -include-runtime -d grid-dijkstra-tests.jar
java -jar grid-dijkstra-tests.jar

kotlinc graph/*.kt tests/DijkstraFlexTests.kt -include-runtime -d graph-dijkstra-tests.jar
java -jar graph-dijkstra-tests.jar
```

Test suites cover default and custom costs, obstacles, movement presets,
direction-dependent movement cost, distance improvements, early stopping,
paths, invalid costs, 150 independent reference inputs per algorithm, and
100,000-position cases.

**Registry note:** The registry lists the graph extension named
`dijkstraFlex` plus the grid function `gridDijkstraFlex`, so a future
bundler needs receiver-aware extension resolution (or explicitly include
`grid/GridDijkstra.kt` and its `GridBfs.kt` dependency) for grid-only
`grid.dijkstraFlex(...)` calls.
