fun grundy(n: Int, moves: (Int) -> List<Int>): IntArray {
    val g = IntArray(n)
    for (v in 0 until n) {
        val reachable = moves(v).map { g[it] }.toHashSet()
        var mex = 0
        while (reachable.contains(mex)) mex++
        g[v] = mex
    }
    return g
}

fun nimWin(piles: LongArray): Boolean = piles.fold(0L) { acc, p -> acc xor p } != 0L

fun staircaseNim(piles: LongArray): Boolean {
    var xorSum = 0L
    for (i in piles.indices) if (i % 2 == 1) xorSum = xorSum xor piles[i]
    return xorSum != 0L
}
