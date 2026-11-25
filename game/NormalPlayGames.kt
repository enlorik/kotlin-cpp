/** Explicitly impartial, finite, normal-play games with decreasing state IDs.
 * A move is available to either player; no legal move means a loss.
 */
class ImpartialNormalPlay(states: Int, moves: (Int) -> Iterable<Int>) {
    private val values = spragueGrundy(states,moves)
    fun grundy(state: Int): Int = values[state]
    fun winning(state: Int): Boolean = grundy(state) != 0
    fun disjointSum(states: Iterable<Int>): Int = nimSum(states.map { grundy(it) })
}

data class NimMove(val pile: Int,val remaining: Long)

/** Normal-play Nim, including empty games and zero-size piles. */
fun nimSum64(piles: LongArray): Long {
    require(piles.all { it >= 0 })
    return piles.fold(0L) { a,b -> a xor b }
}

/** A move to a zero-XOR position, or null in a losing position. */
fun winningNimMove(piles: LongArray): NimMove? {
    val xor = nimSum64(piles)
    if (xor == 0L) return null
    for (i in piles.indices) {
        val next = piles[i] xor xor
        if (next < piles[i]) return NimMove(i,next)
    }
    error("Nonzero XOR must have a winning move")
}

data class GrundyPeriod(val start: Int,val length: Int)

/** Exact eventual periodicity for finite positive subtraction moves.
 * Repeated full windows of maxMove Grundy values prove recurrence forever.
 * A bounded search may return no period; it never guesses.
 * O(maxComputed * (moveCount + maxMove)) worst-case time and
 * O(maxComputed * maxMove) worst-case space for stored windows.
 */
class PeriodicSubtractionGame(moves: IntArray, maxComputed: Int = 10000) {
    private val moves = moves.distinct().sorted().toIntArray()
    private val values: IntArray
    val period: GrundyPeriod?
    init {
        require(maxComputed >= 0 && maxComputed < Int.MAX_VALUE)
        require(this.moves.all { it > 0 })
        val width = this.moves.lastOrNull() ?: 1
        val computed = IntArray(maxComputed+1)
        val seen = HashMap<List<Int>,Int>()
        val marks = IntArray(this.moves.size+1) { -1 }
        var found: GrundyPeriod? = null
        var used = computed.size
        for (n in computed.indices) {
            for (move in this.moves) {
                if (move > n) break
                marks[computed[n-move]] = n
            }
            var g = 0
            while (marks[g] == n) g++
            computed[n] = g
            if (n >= width-1) {
                val start = n-width+1
                val key = computed.copyOfRange(start,n+1).toList()
                val earlier = seen.putIfAbsent(key,start)
                if (earlier != null) {
                    found = GrundyPeriod(earlier,start-earlier)
                    used = n+1
                    break
                }
            }
        }
        values = computed.copyOf(used)
        period = found
    }

    /** Throws outside the computed prefix if no full-window repeat was proved. */
    fun grundy(stones: Long): Int {
        require(stones >= 0)
        if (stones < values.size) return values[stones.toInt()]
        val p = requireNotNull(period) { "No period proved within the computation budget" }
        return values[p.start+((stones-p.start)%p.length).toInt()]
    }
    fun winning(stones: Long): Boolean = grundy(stones) != 0
    fun winningMove(stones: Long): Int? {
        if (!winning(stones)) return null
        return moves.firstOrNull { it <= stones && grundy(stones-it) == 0 }
    }
}
