/** Grid movement offsets. Use FOUR, EIGHT, KNIGHT or CUSTOM(dr to dc, ...). */
class GridDirections(val dr: IntArray, val dc: IntArray) {
    init { require(dr.size == dc.size) { "Direction arrays must have the same size" } }
}

/** Up, down, left, right. */
val FOUR = GridDirections(intArrayOf(-1, 1, 0, 0), intArrayOf(0, 0, -1, 1))

/** Four cardinal moves plus four diagonals. */
val EIGHT = GridDirections(
    intArrayOf(-1, 1, 0, 0, -1, -1, 1, 1),
    intArrayOf(0, 0, -1, 1, -1, 1, -1, 1)
)

/** All eight chess-knight moves. */
val KNIGHT = GridDirections(
    intArrayOf(-2, -2, -1, -1, 1, 1, 2, 2),
    intArrayOf(-1, 1, -2, 2, -2, 2, -1, 1)
)

/** Build any movement pattern, e.g. CUSTOM(0 to 2, 1 to 0). */
fun CUSTOM(vararg moves: Pair<Int, Int>): GridDirections = GridDirections(
    IntArray(moves.size) { moves[it].first },
    IntArray(moves.size) { moves[it].second }
)

/**
 * Result of BFS on a rectangular grid.
 * dist[r][c] = shortest number of moves, or -1 if unreachable.
 * prev[index] stores the previous cell's flattened index (r*cols+c), or -1.
 * orderIndices records popped cells in flattened form; order gives (row, col) pairs.
 */
class GridBfsResult(
    val dist: Array<IntArray>,
    val prev: IntArray,
    val orderIndices: IntArray,
    val cols: Int
) {
    val rows: Int get() = dist.size
    val order: List<Pair<Int, Int>> get() = orderIndices.map { it / cols to it % cols }

    fun hasPath(r: Int, c: Int): Boolean = dist[r][c] >= 0
    fun distance(r: Int, c: Int): Int? = dist[r][c].takeIf { it >= 0 }
    fun reconstructPath(r: Int, c: Int): List<Pair<Int, Int>> {
        if (!hasPath(r, c)) return emptyList()
        val path = ArrayList<Pair<Int, Int>>()
        var pos = r * cols + c
        while (pos != -1) {
            path.add(pos / cols to pos % cols)
            pos = prev[pos]
        }
        path.reverse()
        return path
    }
    fun reconstructPath(cell: Pair<Int, Int>): List<Pair<Int, Int>> = reconstructPath(cell.first, cell.second)
}

/**
 * Flexible BFS over a rectangular grid without building graph edges.
 * Distances count MOVES, not tile costs. For weighted moves use Dijkstra instead.
 * canGo(r,c) is called for in-bounds, not-yet-discovered destinations only.
 * The starting cell is always included, even if canGo would reject it.
 * onDiscover(r,c) and onStep(fromR,fromC,r,c) run after dist and prev are set;
 * onPop(r,c) runs before checking target. When target is popped, BFS stops early.
 * Time: O(rows*cols*directions) for O(1) hooks. Space: O(rows*cols).
 */
inline fun gridBfsFlex(
    rows: Int,
    cols: Int,
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onDiscover: (r: Int, c: Int) -> Unit = { _, _ -> },
    onPop: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridBfsResult {
    require(rows > 0 && cols > 0) { "Grid must be nonempty" }
    val (sr, sc) = start
    require(sr in 0 until rows && sc in 0 until cols) { "Start outside grid" }
    val targetIndex = if (target == null) -1 else {
        require(target.first in 0 until rows && target.second in 0 until cols) { "Target outside grid" }
        target.first * cols + target.second
    }
    val dist = Array(rows) { IntArray(cols) { -1 } }
    val prev = IntArray(rows * cols) { -1 }
    val queue = IntArray(rows * cols)
    var head = 0
    var tail = 0
    dist[sr][sc] = 0
    queue[tail++] = sr * cols + sc

    while (head < tail) {
        val u = queue[head++]
        val r = u / cols
        val c = u % cols
        onPop(r, c)
        if (u == targetIndex) break

        for (i in directions.dr.indices) {
            val nr = r + directions.dr[i]
            val nc = c + directions.dc[i]
            if (nr !in 0 until rows || nc !in 0 until cols) continue
            if (dist[nr][nc] != -1 || !canGo(nr, nc)) continue
            dist[nr][nc] = dist[r][c] + 1
            val v = nr * cols + nc
            prev[v] = u
            onDiscover(nr, nc)
            onStep(r, c, nr, nc)
            queue[tail++] = v
        }
    }
    return GridBfsResult(dist, prev, queue.copyOf(head), cols)
}

/** Char-array grid, e.g. Array(n) { readLine()!!.toCharArray() }. */
inline fun Array<CharArray>.bfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onDiscover: (r: Int, c: Int) -> Unit = { _, _ -> },
    onPop: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridBfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridBfsFlex(size, cols, start, directions, target, canGo, onDiscover, onPop, onStep)
}

/** String grids (List<String>) including listOf(".#", ".."). */
inline fun List<CharSequence>.bfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onDiscover: (r: Int, c: Int) -> Unit = { _, _ -> },
    onPop: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridBfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridBfsFlex(size, cols, start, directions, target, canGo, onDiscover, onPop, onStep)
}

/** String arrays (Array<String>). */
inline fun Array<out CharSequence>.bfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onDiscover: (r: Int, c: Int) -> Unit = { _, _ -> },
    onPop: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridBfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridBfsFlex(size, cols, start, directions, target, canGo, onDiscover, onPop, onStep)
}

/** Int-array grid (Array<IntArray>), for height maps, flood fills, etc. */
inline fun Array<IntArray>.bfsFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    onDiscover: (r: Int, c: Int) -> Unit = { _, _ -> },
    onPop: (r: Int, c: Int) -> Unit = { _, _ -> },
    onStep: (fromR: Int, fromC: Int, r: Int, c: Int) -> Unit = { _, _, _, _ -> }
): GridBfsResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridBfsFlex(size, cols, start, directions, target, canGo, onDiscover, onPop, onStep)
}
