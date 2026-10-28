/** Result of iterative depth-first traversal of a rectangular grid. */
class GridDfsResult(
    /** DFS tree depth, or -1 if the cell was not visited. NOT a shortest-path distance. */
    val depth: Array<IntArray>,
    /** Previous cell in the DFS tree, stored as a flat row*cols+col index; -1 for the root. */
    val prev: IntArray,
    val enterOrderIndices: IntArray,
    val exitOrderIndices: IntArray,
    val cols: Int
) {
    val rows: Int get() = depth.size
    val enterOrder: List<Pair<Int, Int>> get() = enterOrderIndices.map { it / cols to it % cols }
    val exitOrder: List<Pair<Int, Int>> get() = exitOrderIndices.map { it / cols to it % cols }
    /** Alias for discovery order. */
    val order: List<Pair<Int, Int>> get() = enterOrder

    fun hasPath(r: Int, c: Int): Boolean = depth[r][c] != -1
    fun reconstructPath(r: Int, c: Int): List<Pair<Int, Int>> {
        if (!hasPath(r, c)) return emptyList()
        val path = ArrayList<Pair<Int, Int>>()
        var v = r * cols + c
        while (v != -1) {
            path.add(v / cols to v % cols)
            v = prev[v]
        }
        path.reverse()
        return path
    }
    fun reconstructPath(cell: Pair<Int, Int>): List<Pair<Int, Int>> = reconstructPath(cell.first, cell.second)
}

/**
 * Iterative, customizable depth-first search of a rectangular grid.
 * Does not allocate graph edges and cannot overflow the JVM call stack.
 *
 * canGo is only called for in-bounds, unvisited destinations. The start is
 * always visited regardless of canGo. The DFS follows direction order.
 * onStep runs after depth/prev are set and immediately before onEnter for a
 * new cell. onExit runs AFTER all of a cell's permitted neighbors are explored.
 * If target is specified, traversal stops upon entering it: onExit is NOT
 * called for the target or any still-active ancestor frames.
 *
 * O(rows * cols * directions.size) time for O(1) callbacks;
 * O(rows * cols) extra space. depth is DFS-tree depth, not shortest distance.
 */
inline fun gridDfsFlex(
    rows: Int,
    cols: Int,
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onEnter: (r: Int, c: Int) -> Unit = { _, _ -> },
    onExit: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridDfsResult {
    require(rows > 0 && cols > 0 && rows <= Int.MAX_VALUE / cols) {
        "Grid must be nonempty and fit in Int indexing"
    }
    val (sr, sc) = start
    require(sr in 0 until rows && sc in 0 until cols) { "Start outside grid" }
    val targetIndex = if (target == null) -1 else {
        require(target.first in 0 until rows && target.second in 0 until cols) { "Target outside grid" }
        target.first * cols + target.second
    }

    val n = rows * cols
    val depth = Array(rows) { IntArray(cols) { -1 } }
    val prev = IntArray(n) { -1 }
    val enter = IntArray(n)
    val exit = IntArray(n)
    // Explicit recursion frames: (cell index, next direction to consider).
    val stackCell = IntArray(n)
    val stackNext = IntArray(n)
    var top = 0
    var entered = 1
    var exited = 0
    val root = sr * cols + sc
    stackCell[0] = root
    enter[0] = root
    depth[sr][sc] = 0
    onEnter(sr, sc)

    if (root != targetIndex) {
        while (top >= 0) {
            val u = stackCell[top]
            val r = u / cols
            val c = u % cols
            val i = stackNext[top]
            if (i == directions.dr.size) {
                onExit(r, c)
                exit[exited++] = u
                top--
                continue
            }
            stackNext[top] = i + 1
            val nr = r + directions.dr[i]
            val nc = c + directions.dc[i]
            if (nr !in 0 until rows || nc !in 0 until cols) continue
            if (depth[nr][nc] != -1 || !canGo(nr, nc)) continue

            val v = nr * cols + nc
            depth[nr][nc] = depth[r][c] + 1
            prev[v] = u
            onStep(r, c, nr, nc)
            enter[entered++] = v
            top++
            stackCell[top] = v
            stackNext[top] = 0
            onEnter(nr, nc)
            if (v == targetIndex) break
        }
    }
    return GridDfsResult(depth, prev, enter.copyOf(entered), exit.copyOf(exited), cols)
}

/** DFS over a rectangular grid of characters. */
inline fun Array<CharArray>.dfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onEnter: (r: Int, c: Int) -> Unit = { _, _ -> },
    onExit: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridDfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridDfsFlex(size, cols, start, directions, target, canGo, onEnter, onExit, onStep)
}

/** DFS over List<String> or any List<CharSequence>. */
inline fun List<CharSequence>.dfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onEnter: (r: Int, c: Int) -> Unit = { _, _ -> },
    onExit: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridDfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridDfsFlex(size, cols, start, directions, target, canGo, onEnter, onExit, onStep)
}

/** DFS over Array<String> or other character sequences. */
inline fun Array<out CharSequence>.dfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onEnter: (r: Int, c: Int) -> Unit = { _, _ -> },
    onExit: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridDfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridDfsFlex(size, cols, start, directions, target, canGo, onEnter, onExit, onStep)
}

/** DFS over integer grids (heights, terrain kinds, etc.). */
inline fun Array<IntArray>.dfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onEnter: (r: Int, c: Int) -> Unit = { _, _ -> },
    onExit: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridDfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridDfsFlex(size, cols, start, directions, target, canGo, onEnter, onExit, onStep)
}
