/**
 * Converts fraction p/q (0 < p/q, gcd(p,q)=1) to its Stern-Brocot tree path.
 * Returns a string of 'L' and 'R' characters. Empty string represents 1/1.
 */
fun fractionToSB(p: Int, q: Int): String {
    val sb = StringBuilder()
    var lo = Pair(0, 1)   // 0/1
    var hi = Pair(1, 0)   // 1/0
    var lp = p; var lq = q
    while (true) {
        val mp = lo.first + hi.first
        val mq = lo.second + hi.second
        when {
            lp * mq == lq * mp -> return sb.toString()
            lp * mq < lq * mp  -> { sb.append('L'); hi = Pair(mp, mq) }
            else               -> { sb.append('R'); lo = Pair(mp, mq) }
        }
    }
}

/**
 * Converts a Stern-Brocot path string (L/R) back to the fraction p/q.
 * Returns Pair(p, q). Empty path returns Pair(1, 1).
 */
fun sbToFraction(path: String): Pair<Int, Int> {
    var loP = 0; var loQ = 1
    var hiP = 1; var hiQ = 0
    for (c in path) {
        val mp = loP + hiP
        val mq = loQ + hiQ
        when (c) {
            'L'  -> { hiP = mp; hiQ = mq }
            else -> { loP = mp; loQ = mq }
        }
    }
    return Pair(loP + hiP, loQ + hiQ)
}
