// Run: kotlinc grid/GridBfs.kt grid/GridDijkstra.kt tests/GridDijkstraTests.kt -include-runtime -d grid-dijkstra-tests.jar
//      java -jar grid-dijkstra-tests.jar
private fun gdAssert(ok: Boolean, message: String) { if (!ok) error(message) }
private inline fun gdExpectFailure(block: () -> Unit) {
    var failed = false
    try { block() } catch (_: IllegalArgumentException) { failed = true }
    gdAssert(failed, "Expected IllegalArgumentException")
}
fun main() {
    val costs = arrayOf(intArrayOf(1,5,2), intArrayOf(3,9,1), intArrayOf(2,1,4))
    val result = costs.dijkstraFlex(start = 0 to 0)
    gdAssert(result.dist[2][2] == 10L && result.distance(2,2) == 10L, "minimum entry cost")
    gdAssert(result.reconstructPath(2 to 2) ==
        listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2), "optimal path")
    gdAssert(result.order.size == 9 && result.isFinalized(2,2), "finalized all")
    gdAssert(result.dist[0][0] == 0L && result.order.first() == (0 to 0), "free start")
    val atRoot = costs.dijkstraFlex(start = 0 to 0,
        startCost = costs[0][0].toLong(), target = 0 to 0)
    gdAssert(atRoot.dist[0][0] == 1L && atRoot.order.size == 1, "optional start cost")
    gdAssert(!atRoot.hasPath(0,1), "source target stops before exploration")

    val board = listOf("..#..", ".##..", ".....")
    val relaxes = mutableListOf<Pair<Pair<Int,Int>,Long>>()
    val finalizes = mutableListOf<Pair<Pair<Int,Int>,Long>>()
    var checked = 0
    val maze = board.dijkstraFlex(start = 0 to 0,
        canGo = { r, c ->
            gdAssert(r in board.indices && c in board[0].indices, "out-of-bounds callback")
            checked++
            board[r][c] != '#'
        },
        onRelax = { _, _, r, c, d -> relaxes.add((r to c) to d) },
        onFinalize = { r, c, d -> finalizes.add((r to c) to d) })
    gdAssert(maze.dist[2][4] == 6L && maze.distance(2,4) == 6L, "maze detour")
    gdAssert(!maze.hasPath(0,2) && maze.distance(0,2) == null, "unreachable wall")
    gdAssert(maze.reconstructPath(0,2).isEmpty(), "unreachable path")
    gdAssert(checked > 0 && relaxes.size >= 8, "hooks invoked")
    gdAssert(finalizes.size == maze.order.size && finalizes.distinctBy { it.first }.size == finalizes.size,
        "onFinalize exactly once per cell")
    gdAssert(finalizes.map { it.second } == finalizes.map { it.second }.sorted(), "finalized in distance order")
    gdAssert(maze.order == finalizes.map { it.first }, "order of finalization")

    val knight = listOf("...", "...", "...").dijkstraFlex(0 to 0, directions = KNIGHT)
    gdAssert(knight.dist[1][2] == 1L, "knight")
    val diagonal = listOf("..", "..").dijkstraFlex(0 to 0, directions = EIGHT)
    gdAssert(diagonal.dist[1][1] == 1L, "diagonal")
    val jump = listOf(".......").dijkstraFlex(0 to 0, directions = CUSTOM(0 to 2))
    gdAssert(jump.dist[0][6] == 3L && jump.dist[0][1] == GRID_DIJKSTRA_INF, "custom jumps")
    gdAssert(jump.reconstructPath(0 to 6) ==
        listOf(0 to 0, 0 to 2, 0 to 4, 0 to 6), "custom path")

    val heights = arrayOf(intArrayOf(2,9), intArrayOf(4,7))
    val climb = heights.dijkstraFlex(0 to 0,
        cost = { _, _ -> 1000L },
        moveCost = { fr, fc, r, c -> kotlin.math.abs(heights[r][c] - heights[fr][fc]).toLong() })
    gdAssert(climb.dist[1][1] == 5L, "moveCost replaces destination cost")
    val danger = arrayOf("..", "X.")
    gdAssert(danger.dijkstraFlex(0 to 0,
        canGo = { r,c -> danger[r][c] != 'X' }).dist[1][0] == GRID_DIJKSTRA_INF, "string array")
    val chars = arrayOf(".#", "..").map { it.toCharArray() }.toTypedArray()
    gdAssert(chars.dijkstraFlex(0 to 0,
        canGo = { r,c -> chars[r][c] != '#' }).dist[0][1] == GRID_DIJKSTRA_INF, "char array")
    val implicit = gridDijkstraFlex(2,3, 0 to 0,
        cost = { r,c -> (r+c+1).toLong() })
    gdAssert(implicit.dist[1][2] == 9L, "shape-only grid")

    val improved = listOf("..", "..").dijkstraFlex(start = 0 to 0,
        moveCost = { fr,fc,r,c -> if (fr == 0 && fc == 0 && r == 0 && c == 1) 20L else 1L })
    gdAssert(improved.dist[0][1] == 3L, "cheaper tentative path replaces old path")
    val early = costs.dijkstraFlex(start = 0 to 0, target = 1 to 0)
    gdAssert(early.dist[1][0] == 3L && early.isFinalized(1,0), "target is finalized")
    gdAssert(!early.isFinalized(1,1), "other tentative cells not guaranteed final")

    gdExpectFailure { listOf<String>().dijkstraFlex(0 to 0) }
    gdExpectFailure { listOf("").dijkstraFlex(0 to 0) }
    gdExpectFailure { listOf("..", ".").dijkstraFlex(0 to 0) }
    gdExpectFailure { listOf("..").dijkstraFlex(2 to 0) }
    gdExpectFailure { listOf("..").dijkstraFlex(0 to 0, target = 1 to 1) }
    gdExpectFailure { listOf("..").dijkstraFlex(0 to 0, startCost = -1) }
    gdExpectFailure { listOf("..").dijkstraFlex(0 to 0, cost = { _, _ -> -1L }) }
    gdExpectFailure { listOf("..").dijkstraFlex(0 to 0, cost = { _, _ -> GRID_DIJKSTRA_INF }) }
    gdExpectFailure { listOf("..").dijkstraFlex(0 to 0, moveCost = { _, _, _, _ -> -1L }) }
    val huge = listOf("...").dijkstraFlex(0 to 0,
        moveCost = { _, _, _, _ -> GRID_DIJKSTRA_INF - 2 })
    gdAssert(!huge.hasPath(0,2), "unrepresentably large path ignored")

    // 150 small grids checked against independent iterative distance relaxation.
    for (h in 1..5) for (w in 1..5) for (seed in 0..5) {
        val grid = Array(h) { r -> IntArray(w) { c -> (r*31 + c*17 + seed*13) % 7 } }
        val blocked = Array(h) { r -> BooleanArray(w) { c ->
            (r != 0 || c != 0) && ((r*19 + c*11 + seed*7) % 9 == 0) } }
        val actual = grid.dijkstraFlex(0 to 0, canGo = { r,c -> !blocked[r][c] })
        val reference = Array(h) { LongArray(w) { GRID_DIJKSTRA_INF } }
        reference[0][0] = 0L
        repeat(h*w) {
            for (r in 0 until h) for (c in 0 until w) {
                if (reference[r][c] == GRID_DIJKSTRA_INF) continue
                for ((dr,dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                    val nr = r+dr; val nc = c+dc
                    if (nr !in 0 until h || nc !in 0 until w || blocked[nr][nc]) continue
                    reference[nr][nc] = minOf(reference[nr][nc],
                        reference[r][c] + grid[nr][nc].toLong())
                }
            }
        }
        for (r in 0 until h) for (c in 0 until w)
            gdAssert(actual.dist[r][c] == reference[r][c], "oracle h=$h w=$w seed=$seed at $r,$c")
    }

    val large = gridDijkstraFlex(1, 100_000, 0 to 0, directions = CUSTOM(0 to 1))
    gdAssert(large.dist[0][99_999] == 99_999L && large.orderIndices.size == 100_000, "100k cells")
    println("PASS: grid Dijkstra weighted paths, hooks, directions, safety, 150 references and 100k cells")
}
