// Compile/test: kotlinc grid/GridBfs.kt tests/GridBfsTests.kt -include-runtime -d grid-tests.jar && java -jar grid-tests.jar

private fun test(ok: Boolean, message: String) { if (!ok) error(message) }
private inline fun mustThrow(block: () -> Unit) {
    var thrown = false
    try { block() } catch (_: IllegalArgumentException) { thrown = true }
    test(thrown, "Expected IllegalArgumentException")
}

fun main() {
    val grid = listOf("....", ".##.", "....")
    val res = grid.bfsFlex(start = 0 to 0, canGo = { r, c -> grid[r][c] != '#' })
    test(res.dist[0][0] == 0, "source distance")
    test(res.dist[0][3] == 3, "four-way distance")
    test(res.dist[2][0] == 2, "lower row")
    test(res.dist[1][1] == -1, "wall should be blocked")
    test(res.dist[2][3] == 5, "route around the walls")
    test(res.reconstructPath(2, 3).size == 6, "path length")
    test(res.reconstructPath(1, 1).isEmpty(), "unreachable path")
    test(res.order.first() == (0 to 0), "pop order")
    test(res.distance(1, 1) == null && res.distance(2, 3) == 5, "optional distance")

    val diagonal = listOf("..", "..").bfsFlex(0 to 0, directions = EIGHT)
    test(diagonal.dist[1][1] == 1, "diagonal should be one step in EIGHT")
    val cardinal = listOf("..", "..").bfsFlex(0 to 0, directions = FOUR)
    test(cardinal.dist[1][1] == 2, "diagonal should be two steps in FOUR")

    val knight = listOf("...", "...", "...").bfsFlex(0 to 0, directions = KNIGHT)
    test(knight.dist[1][2] == 1 && knight.dist[2][1] == 1, "knight move")
    test(knight.dist[0][1] == 3, "knight distance to neighbor")

    val custom = listOf(".....").bfsFlex(0 to 0, directions = CUSTOM(0 to 2))
    test(custom.dist[0][2] == 1 && custom.dist[0][4] == 2, "custom move")
    test(custom.dist[0][1] == -1 && custom.dist[0][3] == -1, "custom restrictions")

    val discover = mutableListOf<Pair<Int, Int>>()
    val popped = mutableListOf<Pair<Int, Int>>()
    val extra = Array(2) { IntArray(2) }
    val early = listOf("..", "..").bfsFlex(
        start = 0 to 0,
        target = 1 to 0,
        onDiscover = { r, c -> discover.add(r to c) },
        onPop = { r, c -> popped.add(r to c) },
        onStep = { fr, fc, r, c -> extra[r][c] = extra[fr][fc] + 10 }
    )
    test(popped == listOf(0 to 0, 1 to 0), "stop on target pop")
    test(early.order == popped, "order matches onPop")
    test(discover == listOf(1 to 0, 0 to 1), "discovery occurs before pop")
    test(extra[1][0] == 10 && extra[0][1] == 10, "onStep has previous coordinates")
    test(early.reconstructPath(1 to 0) == listOf(0 to 0, 1 to 0), "reconstruction")
    test(early.dist[0][1] == 1 && early.dist[1][1] == -1, "early stop leaves unvisited cells undiscovered")

    // Complete traversal allows custom calculations to propagate across levels.
    val fullExtra = Array(2) { IntArray(2) }
    listOf("..", "..").bfsFlex(0 to 0, onStep = { fr, fc, r, c ->
        fullExtra[r][c] = fullExtra[fr][fc] + 10
    })
    test(fullExtra[1][1] == 20, "onStep does custom calculations")

    val atStart = arrayOf("....").bfsFlex(0 to 0, target = 0 to 0)
    test(atStart.orderIndices.size == 1, "source target should stop immediately")
    test(atStart.reconstructPath(0, 0) == listOf(0 to 0), "source path")

    val chars = arrayOf("..", ".#").map { it.toCharArray() }.toTypedArray()
    val cr = chars.bfsFlex(0 to 0, canGo = { r, c -> chars[r][c] != '#' })
    test(cr.dist[1][1] == -1 && cr.dist[1][0] == 1, "CharArray overload")

    val heights = arrayOf(intArrayOf(5, 1), intArrayOf(3, 1))
    val ir = heights.bfsFlex(0 to 0, canGo = { r, c -> heights[r][c] >= 2 })
    test(ir.dist[1][0] == 1 && ir.dist[0][1] == -1, "IntArray overload")

    mustThrow { listOf("a", "bb").bfsFlex(0 to 0) }
    mustThrow { arrayOf(charArrayOf()).bfsFlex(0 to 0) }
    mustThrow { listOf("aa").bfsFlex(2 to 0) }
    mustThrow { listOf("aa").bfsFlex(0 to 0, target = 1 to 0) }

    // A small deterministic grid suite compared to a reference shortest-path algorithm.
    for (h in 1..5) for (w in 1..5) for (pattern in 0..5) {
        val board = List(h) { r -> "".padEnd(w, '.').toCharArray().also { row ->
            for (c in 0 until w) if (r != 0 || c != 0)
                if ((r * 31 + c * 17 + pattern * 7) % 5 == 0) row[c] = '#'
        }.concatToString() }
        val rr = board.bfsFlex(0 to 0, canGo = { r, c -> board[r][c] == '.' })
        val expect = Array(h) { IntArray(w) { -1 } }
        expect[0][0] = 0
        var changed: Boolean
        do {
            changed = false
            for (r in 0 until h) for (c in 0 until w) {
                if (expect[r][c] < 0) continue
                for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                    val nr = r + dr; val nc = c + dc
                    if (nr in 0 until h && nc in 0 until w && board[nr][nc] == '.' && (expect[nr][nc] < 0 || expect[nr][nc] > expect[r][c] + 1)) {
                        expect[nr][nc] = expect[r][c] + 1; changed = true
                    }
                }
            }
        } while (changed)
        for (r in 0 until h) for (c in 0 until w) test(rr.dist[r][c] == expect[r][c], "oracle mismatch at $r,$c in $h x $w")
    }

    println("PASS: grid BFS directions, walls, custom hooks, paths, early exit, grid types, validation and 150 reference cases")
}
