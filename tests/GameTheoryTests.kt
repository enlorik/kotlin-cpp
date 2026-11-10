import kotlin.random.Random

fun main() {
    check(mex(listOf(3,0,1,3,-1)) == 2)
    check(mex(emptyList()) == 0)
    check(subtractionGrundy(10, intArrayOf(1))[7] == 1)
    check(nimSum(listOf(1,2,3)) == 0)
    check(retrogradeGame(3, listOf(0 to 1,1 to 2,2 to 0))
        .all { it == GameOutcome.DRAW })
    check(retrogradeGame(3, listOf(0 to 1,1 to 2)).contentEquals(
        arrayOf(GameOutcome.LOSE,GameOutcome.WIN,GameOutcome.LOSE)))
    check(runCatching { subtractionGrundy(4,intArrayOf(0)) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching { spragueGrundy(3) { listOf(3) } }
        .exceptionOrNull() is IllegalArgumentException)

    // Small impartial DAGs: verify exact Grundy and win/lose by recursive game trees.
    val rng = Random(20261110)
    var checks = 0
    repeat(400) {
        val n = rng.nextInt(0,30)
        val moves = Array(n) { v ->
            (0 until v).filter { rng.nextBoolean() }
        }
        val g = spragueGrundy(n) { moves[it] }
        for (v in 0 until n) {
            check(g[v] == mex(moves[v].map { g[it] }))
            check((g[v] != 0) == moves[v].any { g[it] == 0 })
            checks++
        }
    }

    // Random general directed games, including cycles and terminal positions.
    repeat(350) {
        val n = rng.nextInt(1,20)
        val edges = ArrayList<Pair<Int,Int>>()
        val out = Array(n) { ArrayList<Int>() }
        repeat(rng.nextInt(0,70)) {
            val u = rng.nextInt(n)
            val v = rng.nextInt(n)
            edges.add(u to v)
            out[u].add(v)
        }
        val expected = arrayOfNulls<GameOutcome>(n)
        for (v in 0 until n) if (out[v].isEmpty()) expected[v] = GameOutcome.LOSE
        repeat(n+1) {
            for (v in 0 until n) if (expected[v] == null) {
                if (out[v].any { expected[it] == GameOutcome.LOSE })
                    expected[v] = GameOutcome.WIN
                else if (out[v].all { expected[it] == GameOutcome.WIN })
                    expected[v] = GameOutcome.LOSE
            }
        }
        val real = retrogradeGame(n,edges)
        for (v in 0 until n) {
            check(real[v] == (expected[v] ?: GameOutcome.DRAW))
            checks++
        }
    }

    repeat(180) {
        val depth = rng.nextInt(0,8)
        val branching = rng.nextInt(1,4)
        val tree = ArrayList<List<Int>>()
        val scores = ArrayList<Long>()
        tree.add(emptyList())
        scores.add(0L)
        for (level in 0 until depth) {
            val n = tree.size
            for (i in 0 until n) {
                if (tree[i].isNotEmpty()) continue
                val children = ArrayList<Int>()
                repeat(branching) {
                    val id = tree.size
                    children.add(id)
                    tree.add(emptyList())
                    scores.add(rng.nextLong(-100,101))
                }
                tree[i] = children
            }
        }
        fun brute(node: Int, d: Int, maxTurn: Boolean): Long {
            if (d == 0 || tree[node].isEmpty()) return scores[node]
            val results = tree[node].map { brute(it,d-1,!maxTurn) }
            return if (maxTurn) results.max() else results.min()
        }
        for (maximize in listOf(true,false)) {
            val actual = alphaBetaMinimax(0,depth,maximize,
                evaluate = { scores[it] }, moves = { tree[it] })
            check(actual == brute(0,depth,maximize))
            checks++
        }
    }
    println("PASS: SG, retrograde game states and alpha-beta ($checks oracle checks)")
}
