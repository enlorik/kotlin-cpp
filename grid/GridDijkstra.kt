import java.util.PriorityQueue

/** Infinity sentinel for grid Dijkstra; distances must remain below this value. */
const val GRID_DIJKSTRA_INF: Long = Long.MAX_VALUE / 2

/** Grid Dijkstra result. GRID_DIJKSTRA_INF marks unreachable cells. */
class GridDijkstraResult(
    val dist: Array<LongArray>,
    /** Parent cell index encoded as row * cols + col; -1 for the start/unvisited. */
    val prev: IntArray,
    val orderIndices: IntArray,
    val cols: Int,
    /** Finalized cells; when stopping early, other discovered distances are tentative. */
    val settled: BooleanArray
) {
    val rows: Int get() = dist.size
    val order: List<Pair<Int, Int>> get() = orderIndices.map { it / cols to it % cols }
    fun hasPath(r: Int, c: Int): Boolean = dist[r][c] < GRID_DIJKSTRA_INF
    fun distance(r: Int, c: Int): Long? = dist[r][c].takeIf { it < GRID_DIJKSTRA_INF }
    fun isFinalized(r: Int, c: Int): Boolean = settled[r * cols + c]
    fun reconstructPath(r: Int, c: Int): List<Pair<Int, Int>> {
        if (!hasPath(r, c)) return emptyList()
        val path = ArrayList<Pair<Int, Int>>()
        var at = r * cols + c
        while (at != -1) {
            path.add(at / cols to at % cols)
            at = prev[at]
        }
        path.reverse()
        return path
    }
    fun reconstructPath(cell: Pair<Int, Int>): List<Pair<Int, Int>> = reconstructPath(cell.first, cell.second)
}

/**
 * Dijkstra directly over a rectangular grid without constructing graph edges.
 * cost(r,c) is the cost of ENTERING the destination cell; default 1.
 * To use a cost depending on both endpoints, supply moveCost(fr,fc,r,c);
 * when provided, moveCost OVERRIDES cost. Costs must be >= 0 and < GRID_DIJKSTRA_INF.
 * startCost initializes the source distance (default 0, so source cell is free).
 * canGo only sees in-bounds, unfinalized candidate destinations.
 * The start is always admitted. onRelax runs on every distance improvement;
 * onFinalize runs once when a cell's distance becomes optimal.
 * With target, only settled cells have guaranteed optimal distances.
 * O(R*C*directions.size*log(R*C)) time with O(1) hooks.
 */
inline fun gridDijkstraFlex(
    rows: Int,
    cols: Int,
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    cost: (r: Int, c: Int) -> Long = { _, _ -> 1L },
    noinline moveCost: ((fromR: Int, fromC: Int, r: Int, c: Int) -> Long)? = null,
    startCost: Long = 0L,
    onRelax: (fromR: Int, fromC: Int, r: Int, c: Int, newDist: Long) -> Unit = { _, _, _, _, _ -> },
    onFinalize: (r: Int, c: Int, dist: Long) -> Unit = { _, _, _ -> }
): GridDijkstraResult {
    require(rows > 0 && cols > 0 && rows <= Int.MAX_VALUE / cols) {
        "Grid must be nonempty and fit Int indexing"
    }
    require(startCost >= 0 && startCost < GRID_DIJKSTRA_INF) { "startCost must be in 0 until GRID_DIJKSTRA_INF" }
    val (sr, sc) = start
    require(sr in 0 until rows && sc in 0 until cols) { "Start outside grid" }
    val targetIndex = if (target == null) -1 else {
        require(target.first in 0 until rows && target.second in 0 until cols) { "Target outside grid" }
        target.first * cols + target.second
    }

    val n = rows * cols
    val dist = Array(rows) { LongArray(cols) { GRID_DIJKSTRA_INF } }
    val prev = IntArray(n) { -1 }
    val settled = BooleanArray(n)
    val order = IntArray(n)
    var count = 0
    val pq = PriorityQueue<LongArray>(compareBy<LongArray> { it[0] }.thenBy { it[1] })
    dist[sr][sc] = startCost
    pq.add(longArrayOf(startCost, (sr * cols + sc).toLong()))

    while (pq.isNotEmpty()) {
        val entry = pq.poll()
        val u = entry[1].toInt()
        val r = u / cols
        val c = u % cols
        val d = entry[0]
        if (settled[u] || d != dist[r][c]) continue
        settled[u] = true
        order[count++] = u
        onFinalize(r, c, d)
        if (u == targetIndex) break

        for (i in directions.dr.indices) {
            val nr = r + directions.dr[i]
            val nc = c + directions.dc[i]
            if (nr !in 0 until rows || nc !in 0 until cols) continue
            val v = nr * cols + nc
            if (settled[v] || !canGo(nr, nc)) continue
            val w = if (moveCost == null) cost(nr, nc) else moveCost(r, c, nr, nc)
            require(w >= 0 && w < GRID_DIJKSTRA_INF) { "Dijkstra requires movement costs in 0 until GRID_DIJKSTRA_INF" }
            if (d > GRID_DIJKSTRA_INF - 1 - w) continue // cannot represent path below INF
            val next = d + w
            if (next < dist[nr][nc]) {
                dist[nr][nc] = next
                prev[v] = u
                onRelax(r, c, nr, nc, next)
                pq.add(longArrayOf(next, v.toLong()))
            }
        }
    }
    return GridDijkstraResult(dist, prev, order.copyOf(count), cols, settled)
}

/** Grid of chars, such as Array(n) { readLine()!!.toCharArray() }. */
inline fun Array<CharArray>.dijkstraFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    cost: (r: Int, c: Int) -> Long = { _, _ -> 1L },
    noinline moveCost: ((fromR: Int, fromC: Int, r: Int, c: Int) -> Long)? = null,
    startCost: Long = 0L,
    onRelax: (fromR: Int, fromC: Int, r: Int, c: Int, newDist: Long) -> Unit = { _, _, _, _, _ -> },
    onFinalize: (r: Int, c: Int, dist: Long) -> Unit = { _, _, _ -> }
): GridDijkstraResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridDijkstraFlex(size, cols, start, directions, target, canGo, cost, moveCost, startCost, onRelax, onFinalize)
}

/** List<String> (or List<CharSequence>) grid. */
inline fun List<CharSequence>.dijkstraFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    cost: (r: Int, c: Int) -> Long = { _, _ -> 1L },
    noinline moveCost: ((fromR: Int, fromC: Int, r: Int, c: Int) -> Long)? = null,
    startCost: Long = 0L,
    onRelax: (fromR: Int, fromC: Int, r: Int, c: Int, newDist: Long) -> Unit = { _, _, _, _, _ -> },
    onFinalize: (r: Int, c: Int, dist: Long) -> Unit = { _, _, _ -> }
): GridDijkstraResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridDijkstraFlex(size, cols, start, directions, target, canGo, cost, moveCost, startCost, onRelax, onFinalize)
}

/** Array<String> and other CharSequence arrays. */
inline fun Array<out CharSequence>.dijkstraFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    cost: (r: Int, c: Int) -> Long = { _, _ -> 1L },
    noinline moveCost: ((fromR: Int, fromC: Int, r: Int, c: Int) -> Long)? = null,
    startCost: Long = 0L,
    onRelax: (fromR: Int, fromC: Int, r: Int, c: Int, newDist: Long) -> Unit = { _, _, _, _, _ -> },
    onFinalize: (r: Int, c: Int, dist: Long) -> Unit = { _, _, _ -> }
): GridDijkstraResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].length
    require(all { it.length == cols }) { "Grid must be rectangular" }
    return gridDijkstraFlex(size, cols, start, directions, target, canGo, cost, moveCost, startCost, onRelax, onFinalize)
}

/** Array<IntArray> grid, typically terrain heights or movement costs. */
inline fun Array<IntArray>.dijkstraFlex(
    start: Pair<Int, Int>,
    directions: GridDirections = FOUR,
    target: Pair<Int, Int>? = null,
    canGo: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    cost: (r: Int, c: Int) -> Long = { r, c -> this[r][c].toLong() },
    noinline moveCost: ((fromR: Int, fromC: Int, r: Int, c: Int) -> Long)? = null,
    startCost: Long = 0L,
    onRelax: (fromR: Int, fromC: Int, r: Int, c: Int, newDist: Long) -> Unit = { _, _, _, _, _ -> },
    onFinalize: (r: Int, c: Int, dist: Long) -> Unit = { _, _, _ -> }
): GridDijkstraResult {
    require(isNotEmpty()) { "Grid must be nonempty" }
    val cols = this[0].size
    require(all { it.size == cols }) { "Grid must be rectangular" }
    return gridDijkstraFlex(size, cols, start, directions, target, canGo, cost, moveCost, startCost, onRelax, onFinalize)
}
