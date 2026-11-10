/**
 * Impartial normal-play game theory. Sprague-Grundy results require
 * finite acyclic move graphs and the convention: no moves means a loss.
 */
fun mex(values: Iterable<Int>): Int {
    val seen = HashSet<Int>()
    for (v in values) if (v >= 0) seen.add(v)
    var candidate = 0
    while (candidate in seen) candidate++
    return candidate
}

/**
 * Grundy numbers for states [0, states), with moves ONLY to lower indices.
 * This strict decreasing-state requirement guarantees acyclicity.
 */
fun spragueGrundy(states: Int, moves: (state: Int) -> Iterable<Int>): IntArray {
    require(states >= 0)
    val grundy = IntArray(states)
    for (state in 0 until states) {
        val reachable = HashSet<Int>()
        for (next in moves(state)) {
            require(next in 0 until state) {
                "SG transitions must lead to a smaller nonnegative state"
            }
            reachable.add(grundy[next])
        }
        grundy[state] = mex(reachable)
    }
    return grundy
}

fun subtractionGrundy(maxStones: Int, moves: IntArray): IntArray {
    require(maxStones >= 0)
    require(moves.all { it > 0 }) { "Subtraction amounts must be positive" }
    return spragueGrundy(maxStones + 1) { stones ->
        moves.asSequence().filter { it <= stones }.map { stones - it }.toList()
    }
}

fun nimSum(piles: Iterable<Int>): Int {
    var xor = 0
    for (pile in piles) {
        require(pile >= 0)
        xor = xor xor pile
    }
    return xor
}

enum class GameOutcome { WIN, LOSE, DRAW }

/**
 * Retrograde classification of every vertex in a directed finite game graph.
 * Terminal vertices are LOSE, vertices with a LOSE successor are WIN,
 * vertices whose successors are all WIN are LOSE; remaining cycles are DRAW.
 * Returns outcomes under optimal play with infinite play treated as draw.
 */
fun retrogradeGame(
    vertices: Int,
    directedMoves: List<Pair<Int, Int>>
): Array<GameOutcome> {
    require(vertices >= 0)
    val reverse = Array(vertices) { ArrayList<Int>() }
    val remaining = IntArray(vertices)
    for ((u, v) in directedMoves) {
        require(u in 0 until vertices && v in 0 until vertices)
        reverse[v].add(u)
        remaining[u]++
    }
    val outcome = arrayOfNulls<GameOutcome>(vertices)
    val queue = ArrayDeque<Int>()
    for (v in 0 until vertices) if (remaining[v] == 0) {
        outcome[v] = GameOutcome.LOSE
        queue.addLast(v)
    }
    while (queue.isNotEmpty()) {
        val v = queue.removeFirst()
        for (u in reverse[v]) {
            if (outcome[u] != null) continue
            if (outcome[v] == GameOutcome.LOSE) {
                outcome[u] = GameOutcome.WIN
                queue.addLast(u)
            } else {
                remaining[u]--
                if (remaining[u] == 0) {
                    outcome[u] = GameOutcome.LOSE
                    queue.addLast(u)
                }
            }
        }
    }
    return Array(vertices) { outcome[it] ?: GameOutcome.DRAW }
}

/**
 * Depth-limited, perfect-information two-player minimax with alpha-beta.
 * evaluate(state) is the score from the maximizing player's perspective.
 * Depth 0 and positions with no legal children are evaluated directly.
 * No global caching; a depth limit guarantees termination even on cycles.
 */
fun <S> alphaBetaMinimax(
    state: S,
    depth: Int,
    maximizing: Boolean = true,
    evaluate: (S) -> Long,
    moves: (S) -> Iterable<S>
): Long {
    require(depth >= 0)
    fun solve(position: S, remaining: Int, maxTurn: Boolean,
              alphaArg: Long, betaArg: Long): Long {
        if (remaining == 0) return evaluate(position)
        val iterator = moves(position).iterator()
        if (!iterator.hasNext()) return evaluate(position)
        var alpha = alphaArg
        var beta = betaArg
        var best = if (maxTurn) Long.MIN_VALUE else Long.MAX_VALUE
        do {
            val value = solve(iterator.next(), remaining - 1, !maxTurn, alpha, beta)
            if (maxTurn) {
                best = maxOf(best, value)
                alpha = maxOf(alpha, best)
            } else {
                best = minOf(best, value)
                beta = minOf(beta, best)
            }
            if (alpha >= beta) break
        } while (iterator.hasNext())
        return best
    }
    return solve(state, depth, maximizing, Long.MIN_VALUE, Long.MAX_VALUE)
}
