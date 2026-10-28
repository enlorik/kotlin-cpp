/** Distance used by Radius selection. All radii are integral grid distances. */
enum class GridMetric { MANHATTAN, CHEBYSHEV }

/** Which cells contribute to each center's aggregation. */
sealed interface GridSelection

/**
 * Immediate neighbors: FOUR cardinal cells (up, down, left, right), excluding
 * the center. Use Offsets(EIGHT), Offsets(KNIGHT), or Offsets(CUSTOM(...)) for
 * other grid movement patterns. Graphs use the same public Neighbors name.
 */
data object Neighbors : GridSelection

/**
 * Select offsets at distances [minDistance] through [radius], inclusive.
 * Radius(2) is a Manhattan diamond; Radius(2, metric = CHEBYSHEV) is a square.
 * Radius(2, minDistance = 2) selects exactly the ring at distance 2.
 * The center is included when minDistance == 0.
 */
class Radius(
    val radius: Int,
    val minDistance: Int = 0,
    val metric: GridMetric = GridMetric.MANHATTAN
) : GridSelection {
    init {
        require(radius >= 0) { "radius must be nonnegative" }
        require(minDistance in 0..radius) { "minDistance must be between 0 and radius" }
    }
}

/**
 * Only these relative (deltaRow, deltaCol) positions contribute.
 * Example: Offsets(FOUR) selects the four immediate neighbors (not the center).
 * Example: Offsets(1 to 2, -1 to 2) selects two arbitrary jumps.
 * Duplicate offsets are ignored; each cell contributes at most once.
 */
class Offsets(vararg moves: Pair<Int, Int>) : GridSelection {
    val moves: List<Pair<Int, Int>> = moves.distinct()
    constructor(directions: GridDirections) : this(*Array(directions.dr.size) {
        directions.dr[it] to directions.dc[it]
    }) {
        require(directions.dr.size == directions.dc.size) { "Invalid direction arrays" }
    }
}

/**
 * Select ALL qualifying sources tied for smallest Manhattan distance to the center.
 * maxDistance (inclusive) can restrict the search; default: unlimited.
 * The center itself is selected if it is a source (distance zero).
 * Unlike forwarding aggregated values over shortest paths, every source is
 * visited at most ONCE per center: sum and other non-idempotent combines are safe.
 */
class Nearest(
    val sources: (r: Int, c: Int) -> Boolean,
    val maxDistance: Int = Int.MAX_VALUE
) : GridSelection {
    init { require(maxDistance >= 0) { "maxDistance must be nonnegative" } }
}

/** Results for all centers. Every values[r][c] is a separate aggregation. */
class GridAggregateResult<T>(
    val values: Array<MutableList<T>>,
    /** Flattened Manhattan distance to nearest eligible source; only present for Nearest. */
    private val nearestDistance: IntArray? = null
) {
    val rows: Int get() = values.size
    val cols: Int get() = if (values.isEmpty()) 0 else values[0].size
    operator fun get(r: Int, c: Int): T = values[r][c]
    operator fun get(cell: Pair<Int, Int>): T = values[cell.first][cell.second]
    /** Nearest only: distance to a closest source, or null when outside maxDistance. */
    fun distance(r: Int, c: Int): Int? = nearestDistance?.get(r * cols + c)?.takeIf { it >= 0 }
}

/**
 * Aggregate cells according to [selection], with [initial] as the identity.
 * [combine] should be associative and [initial] an identity for the operation.
 * For Radius / Offsets, [include] filters candidate cells, without obstructing
 * anything. For Nearest, [include] filters eligible source cells BEFORE distance
 * is computed, so only qualifying sources compete for minimum distance.
 *
 * All offsets are relative to each center. Grid bounds are always checked.
 * Nearest uses ONE multi-source BFS to calculate minimum Manhattan distance,
 * then scans only the corresponding distance ring per center. This avoids
 * duplicate-source counting even with sum, unlike naive BFS propagation.
 *
 * Complexity (O(1) callbacks): Radius O(R*C*radius^2), Offsets
 * O(R*C*moves.size), Nearest O(R*C + sum(4*nearestDist)); worst-case
 * O(R*C*min(maxDistance, R+C)). Space O(R*C).
 */
fun <T> gridAggregate(
    rows: Int,
    cols: Int,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> {
    require(rows > 0 && cols > 0 && rows <= Int.MAX_VALUE / cols) {
        "Grid must be nonempty and fit Int indexing"
    }
    val result = Array(rows) { MutableList(cols) { initial } }
    var nearestDistance: IntArray? = null
    when (selection) {
        Neighbors -> return gridAggregate(rows, cols, Offsets(FOUR), initial, value, combine, include)
        is Radius -> {
            val k = selection.radius
            // Clamp the radius to the maximum possible offset in this grid,
            // avoiding excessive iteration if radius >> grid size.
            val rowReach = minOf(k, rows - 1)
            val colReach = minOf(k, cols - 1)
            for (r in 0 until rows) for (c in 0 until cols) {
                var acc = initial
                for (dr in -rowReach..rowReach) {
                    val nr = r + dr
                    if (nr !in 0 until rows) continue
                    val maxDc = when (selection.metric) {
                        GridMetric.MANHATTAN -> minOf(colReach, k - kotlin.math.abs(dr))
                        GridMetric.CHEBYSHEV -> colReach
                    }
                    for (dc in -maxDc..maxDc) {
                        val nc = c + dc
                        if (nc !in 0 until cols) continue
                        val distance = when (selection.metric) {
                            GridMetric.MANHATTAN -> kotlin.math.abs(dr) + kotlin.math.abs(dc)
                            GridMetric.CHEBYSHEV -> maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
                        }
                        if (distance < selection.minDistance || !include(nr, nc)) continue
                        acc = combine(acc, value(nr, nc))
                    }
                }
                result[r][c] = acc
            }
        }
        is Offsets -> {
            for (r in 0 until rows) for (c in 0 until cols) {
                var acc = initial
                for ((dr, dc) in selection.moves) {
                    // Check delta in Long to avoid overflow if offsets are extreme.
                    val nr = r.toLong() + dr
                    val nc = c.toLong() + dc
                    if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue
                    if (!include(nr.toInt(), nc.toInt())) continue
                    acc = combine(acc, value(nr.toInt(), nc.toInt()))
                }
                result[r][c] = acc
            }
        }
        is Nearest -> {
            val size = rows * cols
            val sources = BooleanArray(size)
            val dist = IntArray(size) { -1 }
            nearestDistance = dist
            val queue = IntArray(size)
            var tail = 0
            for (r in 0 until rows) for (c in 0 until cols) {
                if (selection.sources(r, c) && include(r, c)) {
                    val pos = r * cols + c
                    sources[pos] = true
                    dist[pos] = 0
                    queue[tail++] = pos
                }
            }
            var head = 0
            val dr = intArrayOf(-1, 1, 0, 0)
            val dc = intArrayOf(0, 0, -1, 1)
            while (head < tail) {
                val u = queue[head++]
                if (dist[u] >= selection.maxDistance) continue
                val r = u / cols
                val c = u % cols
                for (i in 0..3) {
                    val nr = r + dr[i]
                    val nc = c + dc[i]
                    if (nr !in 0 until rows || nc !in 0 until cols) continue
                    val v = nr * cols + nc
                    if (dist[v] != -1) continue
                    dist[v] = dist[u] + 1
                    queue[tail++] = v
                }
            }
            for (r in 0 until rows) for (c in 0 until cols) {
                val d = dist[r * cols + c]
                if (d == -1) continue
                var acc = initial
                if (d == 0) {
                    acc = combine(acc, value(r, c))
                } else {
                    // Each source at EXACTLY distance d is counted once, so
                    // even sum, xor, multiplication, etc. have correct semantics.
                    val reach = minOf(d, rows - 1)
                    for (rowOffset in -reach..reach) {
                        val nr = r + rowOffset
                        if (nr !in 0 until rows) continue
                        val colOffset = d - kotlin.math.abs(rowOffset)
                        val left = c.toLong() - colOffset
                        val right = c.toLong() + colOffset
                        if (left >= 0 && left < cols && sources[nr * cols + left.toInt()])
                            acc = combine(acc, value(nr, left.toInt()))
                        if (colOffset != 0 && right >= 0 && right < cols && sources[nr * cols + right.toInt()])
                            acc = combine(acc, value(nr, right.toInt()))
                    }
                }
                result[r][c] = acc
            }
        }
    }
    return GridAggregateResult(result, nearestDistance)
}

/** Aggregate an entire numeric grid; cell contents are the default values. */
fun Array<IntArray>.aggregate(
    selection: GridSelection,
    initial: Int = 0,
    value: (r: Int, c: Int) -> Int = { r, c -> this[r][c] },
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    combine: (Int, Int) -> Int
): GridAggregateResult<Int> {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregate(size, this[0].size, selection, initial, value, combine, include)
}

/** Generic aggregation for numeric grids producing e.g. Long, strings, or sets. */
fun <T> Array<IntArray>.aggregate(
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregate(size, this[0].size, selection, initial, value, combine, include)
}

/** Aggregate an entire List<String> grid. */
fun <T> List<CharSequence>.aggregate(
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> {
    require(isNotEmpty() && all { it.length == this[0].length }) { "Grid must be nonempty and rectangular" }
    return gridAggregate(size, this[0].length, selection, initial, value, combine, include)
}

/** Aggregate an entire Array<String> grid. */
fun <T> Array<out CharSequence>.aggregate(
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> {
    require(isNotEmpty() && all { it.length == this[0].length }) { "Grid must be nonempty and rectangular" }
    return gridAggregate(size, this[0].length, selection, initial, value, combine, include)
}

/** Aggregate an entire Array<CharArray> grid. */
fun <T> Array<CharArray>.aggregate(
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregate(size, this[0].size, selection, initial, value, combine, include)
}

/**
 * Aggregate just ONE center, rather than computing the entire result grid.
 * Uses direct relative-position/ring enumeration, which is efficient for
 * queries with small radius or offsets. Nearest finds the first qualifying
 * Manhattan ring and combines distinct qualifying source cells on that ring.
 */
fun <T> gridAggregateAt(
    rows: Int,
    cols: Int,
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T {
    require(rows > 0 && cols > 0 && rows <= Int.MAX_VALUE / cols) {
        "Grid must be nonempty and fit Int indexing"
    }
    val (r, c) = center
    require(r in 0 until rows && c in 0 until cols) { "Center outside grid" }
    var acc = initial
    when (selection) {
        Neighbors -> return gridAggregateAt(rows, cols, center, Offsets(FOUR), initial, value, combine, include)
        is Radius -> {
            val k = selection.radius
            val rowReach = minOf(k, rows - 1)
            val colReach = minOf(k, cols - 1)
            for (dr in -rowReach..rowReach) {
                val nr = r + dr
                if (nr !in 0 until rows) continue
                val maxDc = when (selection.metric) {
                    GridMetric.MANHATTAN -> minOf(colReach, k - kotlin.math.abs(dr))
                    GridMetric.CHEBYSHEV -> colReach
                }
                for (dc in -maxDc..maxDc) {
                    val nc = c + dc
                    if (nc !in 0 until cols) continue
                    val distance = when (selection.metric) {
                        GridMetric.MANHATTAN -> kotlin.math.abs(dr) + kotlin.math.abs(dc)
                        GridMetric.CHEBYSHEV -> maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
                    }
                    if (distance >= selection.minDistance && include(nr, nc))
                        acc = combine(acc, value(nr, nc))
                }
            }
        }
        is Offsets -> {
            for ((dr, dc) in selection.moves) {
                val nr = r.toLong() + dr
                val nc = c.toLong() + dc
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && include(nr.toInt(), nc.toInt()))
                    acc = combine(acc, value(nr.toInt(), nc.toInt()))
            }
        }
        is Nearest -> {
            // The maximum Manhattan distance between any grid cells is R+C-2.
            val maxPossible = (rows.toLong() + cols - 2).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            val limit = minOf(selection.maxDistance, maxPossible)
            for (d in 0..limit) {
                var found = false
                val rowReach = minOf(d, rows - 1)
                for (dr in -rowReach..rowReach) {
                    val nr = r + dr
                    if (nr !in 0 until rows) continue
                    val dc = d - kotlin.math.abs(dr)
                    val left = c.toLong() - dc
                    val right = c.toLong() + dc
                    if (left >= 0 && left < cols && selection.sources(nr, left.toInt()) && include(nr, left.toInt())) {
                        found = true
                        acc = combine(acc, value(nr, left.toInt()))
                    }
                    if (dc != 0 && right >= 0 && right < cols && selection.sources(nr, right.toInt()) && include(nr, right.toInt())) {
                        found = true
                        acc = combine(acc, value(nr, right.toInt()))
                    }
                }
                if (found) break
            }
        }
    }
    return acc
}

/** Numeric grid overload for a single center; defaults to summing source cell integers. */
fun Array<IntArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: Int = 0,
    value: (r: Int, c: Int) -> Int = { r, c -> this[r][c] },
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true },
    combine: (Int, Int) -> Int
): Int {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregateAt(size, this[0].size, center, selection, initial, value, combine, include)
}

/** Generic numeric-grid overload for a single center (e.g. Long sums). */
fun <T> Array<IntArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregateAt(size, this[0].size, center, selection, initial, value, combine, include)
}

/** String-list grid overload for a single center. */
fun <T> List<CharSequence>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T {
    require(isNotEmpty() && all { it.length == this[0].length }) { "Grid must be nonempty and rectangular" }
    return gridAggregateAt(size, this[0].length, center, selection, initial, value, combine, include)
}

/** String-array grid overload for a single center. */
fun <T> Array<out CharSequence>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T {
    require(isNotEmpty() && all { it.length == this[0].length }) { "Grid must be nonempty and rectangular" }
    return gridAggregateAt(size, this[0].length, center, selection, initial, value, combine, include)
}

/** Char-array grid overload for a single center. */
fun <T> Array<CharArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    initial: T,
    value: (r: Int, c: Int) -> T,
    combine: (T, T) -> T,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T {
    require(isNotEmpty() && all { it.size == this[0].size }) { "Grid must be nonempty and rectangular" }
    return gridAggregateAt(size, this[0].size, center, selection, initial, value, combine, include)
}
