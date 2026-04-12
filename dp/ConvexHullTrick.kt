// Convex Hull Trick

// Li Chao Tree for arbitrary queries — minimization version
class LiChaoTree(val lo: Long, val hi: Long) {
    private data class Line(val m: Long, val b: Long) { fun eval(x: Long) = m * x + b }
    private val tree = arrayOfNulls<Line>(4 * ((hi - lo + 1).coerceAtMost(1 shl 18).toInt() + 1))

    fun addLine(m: Long, b: Long) = addLine(Line(m, b), 1, lo, hi)

    private fun addLine(line: Line, node: Int, l: Long, r: Long) {
        if (l > r) return
        val mid = l + (r - l) / 2
        val cur = tree[node]
        if (cur == null) { tree[node] = line; return }
        var leftBetter = line.eval(l) < cur.eval(l)
        var midBetter = line.eval(mid) < cur.eval(mid)
        if (midBetter) { tree[node] = line }
        if (l == r) return
        if (leftBetter != midBetter) addLine(if (midBetter) cur else line, 2 * node, l, mid)
        else addLine(if (midBetter) cur else line, 2 * node + 1, mid + 1, r)
    }

    fun query(x: Long): Long = query(x, 1, lo, hi)

    private fun query(x: Long, node: Int, l: Long, r: Long): Long {
        val cur = tree[node]?.eval(x) ?: Long.MAX_VALUE / 2
        if (l == r) return cur
        val mid = l + (r - l) / 2
        return if (x <= mid) minOf(cur, query(x, 2 * node, l, mid))
        else minOf(cur, query(x, 2 * node + 1, mid + 1, r))
    }
}

// CHT with monotone queries (deque-based), lines added with decreasing slope
class CHT {
    private data class Line(val m: Long, val b: Long) { fun eval(x: Long) = m * x + b }
    private val dq = ArrayDeque<Line>()

    private fun bad(l1: Line, l2: Line, l3: Line): Boolean {
        // l2 is never the minimum
        return (l3.b - l1.b).toDouble() * (l1.m - l2.m) <= (l2.b - l1.b).toDouble() * (l1.m - l3.m)
    }

    fun addLine(m: Long, b: Long) {
        val line = Line(m, b)
        while (dq.size >= 2 && bad(dq[dq.size - 2], dq[dq.size - 1], line)) dq.removeLast()
        dq.addLast(line)
    }

    // query minimum with increasing x
    fun queryMin(x: Long): Long {
        while (dq.size >= 2 && dq[0].eval(x) >= dq[1].eval(x)) dq.removeFirst()
        return dq.first().eval(x)
    }

    // query maximum: negate slopes/intercepts externally, then call queryMin
    fun queryMax(x: Long): Long {
        while (dq.size >= 2 && dq[0].eval(x) <= dq[1].eval(x)) dq.removeFirst()
        return dq.first().eval(x)
    }
}
