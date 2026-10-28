// Run: kotlinc grid/GridBfs.kt grid/GridDfs.kt tests/GridDfsTests.kt -include-runtime -d grid-dfs-tests.jar
//      java -jar grid-dfs-tests.jar
private fun gdCheck(ok: Boolean, msg: String) { if (!ok) error(msg) }
private inline fun gdThrows(block: () -> Unit) {
    var thrown = false
    try { block() } catch (_: IllegalArgumentException) { thrown = true }
    gdCheck(thrown, "Expected IllegalArgumentException")
}

fun main() {
    val board = listOf("....", ".##.", "....")
    val entered = mutableListOf<Pair<Int, Int>>()
    val exited = mutableListOf<Pair<Int, Int>>()
    val steps = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
    val accum = Array(3) { IntArray(4) }
    var checked = 0
    val result = board.dfsFlex(
        start = 0 to 0,
        canGo = { r, c ->
            gdCheck(r in board.indices && c in board[0].indices, "canGo called outside grid")
            checked++
            board[r][c] != '#'
        },
        onEnter = { r, c -> entered.add(r to c) },
        onExit = { r, c -> exited.add(r to c) },
        onStep = { fr, fc, r, c ->
            gdCheck((fr to fc) in entered, "parent entered before child")
            accum[r][c] = accum[fr][fc] + 10
            steps.add((fr to fc) to (r to c))
        }
    )
    gdCheck(entered.size == 10, "should visit all 10 walkable cells")
    gdCheck(exited.size == entered.size, "every entered node must exit on a full traversal")
    gdCheck(result.enterOrder == entered && result.exitOrder == exited, "callback order should match result")
    gdCheck(result.order == result.enterOrder, "order alias")
    gdCheck(result.depth[0][0] == 0, "root depth")
    gdCheck(result.depth[1][1] == -1 && result.depth[1][2] == -1, "blocked wall")
    gdCheck(!result.hasPath(1, 1) && result.reconstructPath(1, 1).isEmpty(), "blocked path")
    gdCheck(result.hasPath(2, 3), "destination reachable")
    val path = result.reconstructPath(2 to 3)
    gdCheck(path.first() == (0 to 0) && path.last() == (2 to 3), "path endpoints")
    gdCheck(path.size - 1 == result.depth[2][3], "DFS tree depth matches path")
    gdCheck(accum[2][3] == 10 * result.depth[2][3], "onStep calculations")
    gdCheck(steps.size == entered.size - 1 && checked > 0, "steps and predicate invocations")
    val entryAt = entered.withIndex().associate { it.value to it.index }
    val exitAt = exited.withIndex().associate { it.value to it.index }
    for ((parent, child) in steps) {
        gdCheck(entryAt.getValue(parent) < entryAt.getValue(child), "parent entered before child")
        gdCheck(exitAt.getValue(child) < exitAt.getValue(parent), "child must exit before parent")
    }

    val diagonal = listOf("..", "..").dfsFlex(0 to 0, directions = EIGHT)
    gdCheck(diagonal.depth[1][1] >= 0, "EIGHT preset")
    val knight = listOf("...", "...", "...").dfsFlex(0 to 0, directions = KNIGHT)
    gdCheck(knight.depth[1][2] == 1, "KNIGHT preset")
    val custom = listOf(".....").dfsFlex(0 to 0, directions = CUSTOM(0 to 2))
    gdCheck(custom.depth[0][2] == 1 && custom.depth[0][4] == 2, "custom directions")
    gdCheck(custom.depth[0][1] == -1 && custom.depth[0][3] == -1, "custom skips")
    val onlyDownRight = listOf("..", "..").dfsFlex(0 to 0, directions = CUSTOM(0 to 1, 1 to 0))
    gdCheck(onlyDownRight.hasPath(1, 1), "down/right directions")

    val enteredEarly = mutableListOf<Pair<Int, Int>>()
    val exitedEarly = mutableListOf<Pair<Int, Int>>()
    val early = listOf("...", "...").dfsFlex(
        start = 0 to 0, target = 1 to 0,
        onEnter = { r, c -> enteredEarly.add(r to c) },
        onExit = { r, c -> exitedEarly.add(r to c) }
    )
    gdCheck(enteredEarly == listOf(0 to 0, 1 to 0), "target stops at first enter")
    gdCheck(exitedEarly.isEmpty() && early.exitOrder.isEmpty(), "active frames must not report exit")
    gdCheck(early.depth[1][0] == 1 && early.depth[0][1] == -1, "partial DFS result")
    gdCheck(early.reconstructPath(1, 0) == listOf(0 to 0, 1 to 0), "early reconstruction")
    val atRoot = listOf("..").dfsFlex(0 to 0, target = 0 to 0)
    gdCheck(atRoot.enterOrder.size == 1 && atRoot.exitOrder.isEmpty(), "target at root")

    val chars = arrayOf(charArrayOf('.', '#'), charArrayOf('.', '.'))
    gdCheck(chars.dfsFlex(0 to 0, canGo = { r,c -> chars[r][c] != '#' }).depth[0][1] == -1, "char overload")
    gdCheck(arrayOf("..", ".#").dfsFlex(0 to 0, canGo = { r,c -> !(r == 1 && c == 1) }).depth[1][1] == -1, "string-array overload")
    val heights = arrayOf(intArrayOf(3, 0), intArrayOf(2, 1))
    gdCheck(heights.dfsFlex(0 to 0, canGo = { r,c -> heights[r][c] >= 2 }).depth[1][0] == 1, "int grid overload")
    val implicit = gridDfsFlex(2, 3, start = 0 to 0, directions = CUSTOM(1 to 2))
    gdCheck(implicit.depth[1][2] == 1 && implicit.enterOrder.size == 2, "implicit grid")

    gdThrows { listOf<String>().dfsFlex(0 to 0) }
    gdThrows { listOf("", "").dfsFlex(0 to 0) }
    gdThrows { listOf("aa", "b").dfsFlex(0 to 0) }
    gdThrows { listOf("a").dfsFlex(-1 to 0) }
    gdThrows { listOf("a").dfsFlex(0 to 0, target = 1 to 0) }

    // Compare DFS reachability against independent queue flood-fill on 150 small grids.
    for (h in 1..5) for (w in 1..5) for (pattern in 0..5) {
        val b = Array(h) { r -> CharArray(w) { c ->
            if (r == 0 && c == 0) '.' else if ((r * 17 + c * 23 + pattern * 7) % 5 == 0) '#' else '.'
        } }
        val dfs = b.dfsFlex(0 to 0, canGo = { r,c -> b[r][c] == '.' })
        val seen = Array(h) { BooleanArray(w) }
        val q = ArrayDeque<Pair<Int, Int>>()
        q.add(0 to 0); seen[0][0] = true
        while (q.isNotEmpty()) {
            val (r, c) = q.removeFirst()
            for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nr = r+dr; val nc = c+dc
                if (nr in 0 until h && nc in 0 until w && b[nr][nc] == '.' && !seen[nr][nc]) {
                    seen[nr][nc] = true; q.add(nr to nc)
                }
            }
        }
        for (r in 0 until h) for (c in 0 until w)
            gdCheck(dfs.hasPath(r,c) == seen[r][c], "reference mismatch at $r,$c for $h x $w, p=$pattern")
    }

    // A deep path must work without JVM recursion stack overflow.
    val large = gridDfsFlex(1, 100_000, 0 to 0, directions = CUSTOM(0 to 1))
    gdCheck(large.depth[0][99_999] == 99_999 && large.exitOrderIndices.size == 100_000, "deep chain")
    println("PASS: grid DFS hooks, entry/exit, paths, target, directions, overloads, validation, 150 references, 100k chain")
}
