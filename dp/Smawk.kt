/** Leftmost row minima of an implicit totally monotone matrix.
 * The caller guarantees total monotonicity for minima (including leftmost ties)
 * on all ordered row/column subsets. Monotone full-matrix minima alone is not enough.
 * O(rows + columns) oracle calls and auxiliary space; no validation of the property.
 */
fun smawkRowMinima(rows: Int, columns: Int, value: (Int,Int) -> Long): IntArray {
    require(rows >= 0 && columns >= 0 && (rows == 0 || columns > 0))
    val answer = IntArray(rows)
    fun solve(rs: IntArray, cs: IntArray) {
        if (rs.isEmpty()) return
        val reduced = IntArray(minOf(rs.size,cs.size))
        var size = 0
        for (c in cs) {
            while (size > 0 && value(rs[size-1],c) < value(rs[size-1],reduced[size-1])) size--
            if (size < rs.size) reduced[size++] = c
        }
        val cols = reduced.copyOf(size)
        solve(IntArray(rs.size/2) { rs[2*it+1] },cols)
        val position = HashMap<Int,Int>()
        for (i in cols.indices) position[cols[i]] = i
        var start = 0
        for (i in rs.indices step 2) {
            val stop = if (i+1 < rs.size) position.getValue(answer[rs[i+1]]) else cols.lastIndex
            var best = start
            var bestValue = value(rs[i],cols[best])
            for (j in start+1..stop) {
                val v = value(rs[i],cols[j])
                if (v < bestValue) { best = j; bestValue = v }
            }
            answer[rs[i]] = cols[best]
            start = stop
        }
    }
    if (rows > 0) solve(IntArray(rows){it},IntArray(columns){it})
    return answer
}
