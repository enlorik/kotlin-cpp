# Flexible DFS for grids and graphs

`grid/GridDfs.kt` and `graph/DfsFlex.kt` add iterative DFS alongside the existing
flexible BFS functions. These are **depth-first traversal** utilities, not
shortest-path algorithms. Both implementations use explicit IntArray frame
stacks to avoid JVM recursion stack overflows even for very long paths.

## Grid DFS

```kotlin
val grid = listOf("....", ".##.", "....")
val entered = mutableListOf<Pair<Int, Int>>()
val result = grid.dfsFlex(
    start = 0 to 0,
    directions = CUSTOM(0 to 1, 1 to 0), // right or down; FOUR is the default
    canGo = { r, c -> grid[r][c] !in "#X~" },
    onEnter = { r, c -> entered.add(r to c) },
    onStep = { fromR, fromC, r, c -> /* use DFS parent here */ },
    onExit = { r, c -> /* finished exploring this cell */ }
)
println(result.depth[2][3])          // DFS tree depth, NOT a shortest distance
println(result.reconstructPath(2, 3)) // DFS-tree route
```

`directions` accepts all existing GridBfs movement presets: `FOUR`, `EIGHT`,
`KNIGHT`, and `CUSTOM(dr to dc, ...)`. Out-of-bounds and previously visited
cells are skipped automatically **before** invoking `canGo`. The start is
always visited, even if `canGo` would reject it. The supplied grid must be
nonempty and rectangular. Supports `List<String>`, `Array<String>`,
`Array<CharArray>`, `Array<IntArray>`, or the shape-only
`gridDfsFlex(rows, cols, start, ...)` call. **`GridDfs.kt` requires**
`GridBfs.kt` for the shared `GridDirections` presets.

### Callback order and early stop

- `canGo(r,c)` runs for an in-bounds, undiscovered candidate destination.
- `onStep(fromR,fromC,r,c)` runs when a new cell gets its parent and depth.
- `onEnter(r,c)` runs immediately afterward (also for the start).
- `onExit(r,c)` runs after all allowed neighbors have been fully explored.
- `target = row to col` stops **when first entered**, not on exit. DFS returns
  only partial traversal information, and `onExit` is deliberately **not**
  called for the target or any still-open ancestor frames.

`GridDfsResult` exposes `depth[r][c]` (`-1` for unvisited), flattened `prev`,
`enterOrder`, `exitOrder`, `order` (alias of `enterOrder`), `hasPath(r,c)`,
`reconstructPath(r,c)` and its Pair overload. The `enterOrderIndices` and
`exitOrderIndices` are allocation-efficient flattened representations.

DFS tree depth is the number of edges along the **first discovered DFS route**.
For minimum moves, use `grid.bfsFlex()` instead.

## Graph DFS

```kotlin
val result = g.dfsFlex(
    src = 0,
    canGo = { _, edge -> edge.w <= 10L },
    onStep = { u, v, edge -> /* DFS tree edge u -> v */ },
    onEnter = { u -> /* discovered vertex */ },
    onExit = { u -> /* completely processed vertex */ }
)
println(result.reconstructPath(5))
```

`DfsFlexResult` provides `depth`, `prev`, `enterOrder`, `exitOrder`, `order`,
`hasPath(v)`, and `reconstructPath(v)`. `target` is optional (`-1` means no
target) and uses the same early-entry rule as grid DFS. Graph edge weights are
not used to calculate DFS depths. `canGo` is called only for edges to
undiscovered vertices, in original adjacency-list order. `onStep` runs before
`onEnter` after setting a child vertex's parent and depth.

**Complexity:** Grid `O(rows * cols * directions.size)` time and
`O(rows * cols)` memory. Graph `O(V + E)` time and `O(V)` memory, assuming
callbacks are O(1).

## Tests

From the repository root:

```bash
kotlinc grid/GridBfs.kt grid/GridDfs.kt tests/GridDfsTests.kt -include-runtime -d grid-dfs-tests.jar
java -jar grid-dfs-tests.jar
kotlinc graph/*.kt tests/DfsFlexTests.kt -include-runtime -d graph-dfs-tests.jar
java -jar graph-dfs-tests.jar
```

The tests verify movement presets, filters, full entry/exit ordering, tree
paths, early targets, invalid inputs, graph cycles, 150 independent reference
cases for each engine, and paths of 100,000 positions without recursion.

**Registry:** `gridDfsFlex` and `GridDfsResult` map to `grid/GridDfs.kt`,
while `dfsFlex` and `DfsFlexResult` map to `graph/DfsFlex.kt`. Like the BFS
extensions, receiver-specific grid overloads named `dfsFlex` require
receiver-aware selection (or explicit file inclusion) in a future bundler.
