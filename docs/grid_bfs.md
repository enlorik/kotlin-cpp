# Grid BFS with hooks

Kotlin competitive-programming utility in `grid/GridBfs.kt`. Grid BFS is **not**
a SparseGraph: it computes neighbors from cell coordinates instead of allocating
adjacency lists. All algorithms in the existing `graph/` folder remain unchanged.

## Four directions + walls

```kotlin
val grid = listOf(
    "....",
    ".##.",
    "...."
)
val res = grid.bfsFlex(
    start = 0 to 0,
    directions = FOUR, // default; up, down, left, right
    canGo = { r, c -> grid[r][c] != '#' }
)
println(res.dist[2][3])           // 5
println(res.reconstructPath(2, 3)) // [(0,0), ..., (2,3)]
```

## Direction presets

- `FOUR`: up/down/left/right
- `EIGHT`: four-way + diagonals
- `KNIGHT`: all eight chess-knight jumps
- `CUSTOM(0 to 2, -1 to 0)`: any sequence of (deltaRow, deltaCol) moves

```kotlin
grid.bfsFlex(start = 0 to 0, directions = EIGHT)
grid.bfsFlex(start = 0 to 0, directions = KNIGHT)
grid.bfsFlex(start = 0 to 0, directions = CUSTOM(0 to 2, 0 to -2))
```

## Custom calculations

```kotlin
val sums = Array(grid.size) { IntArray(grid[0].length) }
val popped = mutableListOf<Pair<Int, Int>>()
val result = grid.bfsFlex(
    start = 0 to 0,
    target = 2 to 3,             // optional; stop when popped
    canGo = { r, c -> grid[r][c] != '#' },
    onDiscover = { r, c -> /* new cell discovered */ },
    onStep = { fromR, fromC, r, c ->
        sums[r][c] = sums[fromR][fromC] + 1
    },
    onPop = { r, c -> popped.add(r to c) }
)
```

The algorithm maintains `dist[r][c]` (number of moves; `-1` if not
discovered), `prev` (flat parent index), `orderIndices`, and `order`
(a list of coordinate pairs). The result exposes `hasPath(r,c)`,
`distance(r,c)` (nullable), and `reconstructPath(r,c)`.

The source cell is always included, regardless of the `canGo` predicate.
`canGo` runs only on in-bounds, **undiscovered** destinations.
`onDiscover` and `onStep` execute after setting distance and predecessor.
`onPop` executes when a cell is dequeued, including the target.
Stopping at a target yields *partial* distances for unexplored cells.

Supports `List<String>`, `Array<String>`, `Array<CharArray>`,
and `Array<IntArray>`; every grid must be nonempty and rectangular.
There is also `gridBfsFlex(rows, cols, start, ...)` to traverse an implicit
rectangular space without providing grid storage.

**Complexity:** `O(rows * cols * directions.size)` time if hooks are O(1),
and `O(rows * cols)` additional space. Each move costs one BFS step;
if movement costs vary, use Dijkstra or 0-1 BFS.

**Testing:** from repository root:

```bash
kotlinc grid/GridBfs.kt tests/GridBfsTests.kt -include-runtime -d grid-tests.jar
java -jar grid-tests.jar
```

`tests/GridBfsTests.kt` tests direction presets, custom jumps, obstacles,
callbacks, early stopping, path reconstruction, grid overloads, invalid input,
and 150 deterministic grid cases checked against a relaxation-based reference.

**Registry note:** `bfsFlex` is overloaded as an extension on SparseGraph
and grid types. The registry lists `gridBfsFlex` and grid direction/result
symbols separately to avoid an ambiguous duplicate `bfsFlex` entry.
A future automatic bundler must resolve overloads by receiver type (or
explicit inclusion) when importing grid-only `bfsFlex` without a direction
constant.
