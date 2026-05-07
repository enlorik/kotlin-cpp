/**
 * 2-SAT solver using Kosaraju's SCC.
 *
 * Variables are 0-indexed.  For variable [i]:
 *   - literal  i        represents  x_i = true
 *   - literal  i + vars represents  x_i = false
 *
 * Usage:
 *   val sat = TwoSAT(n)
 *   sat.addOr(a, true, b, false)   // (a == true) OR (b == false)
 *   val assignment = sat.solve()   // null if unsatisfiable
 */
class TwoSAT(val vars: Int) {

    // Implication graph: 2*vars nodes (0..vars-1 = true literals, vars..2*vars-1 = false literals)
    private val g  = Array(2 * vars) { ArrayList<Int>() }

    private fun lit(v: Int, value: Boolean) = if (value) v else v + vars

    /** Adds an implication: if (a == aval) then (b == bval). */
    fun addImplication(a: Int, aval: Boolean, b: Int, bval: Boolean) {
        g[lit(a, aval)].add(lit(b, bval))
    }

    /** Adds a clause: (a == aval) OR (b == bval). */
    fun addOr(a: Int, aval: Boolean, b: Int, bval: Boolean) {
        // (a OR b) ≡ (¬a → b) AND (¬b → a)
        addImplication(a, !aval, b,  bval)
        addImplication(b, !bval, a,  aval)
    }

    /** Adds an XOR clause: exactly one of (a == aval) or (b == bval) is true. */
    fun addXor(a: Int, aval: Boolean, b: Int, bval: Boolean) {
        addOr(a,  aval, b,  bval)
        addOr(a, !aval, b, !bval)
    }

    /** Forces variable [a] to have value [aval]. */
    fun force(a: Int, aval: Boolean) {
        addImplication(a, !aval, a, aval)
    }

    /**
     * Solves the 2-SAT instance.
     * Returns a BooleanArray of length [vars] with a satisfying assignment,
     * or null if the formula is unsatisfiable.
     */
    fun solve(): BooleanArray? {
        val nn = 2 * vars

        // Kosaraju's SCC (iterative)
        val visited     = BooleanArray(nn)
        val finishOrder = ArrayDeque<Int>()
        for (start in 0 until nn) {
            if (visited[start]) continue
            visited[start] = true
            val stack = ArrayDeque<IntArray>()   // frame = [vertex, edgeIndex]
            stack.addLast(intArrayOf(start, 0))
            while (stack.isNotEmpty()) {
                val frame = stack.last()
                val v = frame[0]; val i = frame[1]
                if (i < g[v].size) {
                    frame[1]++
                    val w = g[v][i]
                    if (!visited[w]) { visited[w] = true; stack.addLast(intArrayOf(w, 0)) }
                } else {
                    stack.removeLast(); finishOrder.addLast(v)
                }
            }
        }

        // Build reverse graph
        val rg = Array(nn) { ArrayList<Int>() }
        for (u in 0 until nn) for (w in g[u]) rg[w].add(u)

        val comp = IntArray(nn) { -1 }
        var sccCount = 0
        while (finishOrder.isNotEmpty()) {
            val start = finishOrder.removeLast()
            if (comp[start] != -1) continue
            val stack = ArrayDeque<Int>(); stack.addLast(start); comp[start] = sccCount
            while (stack.isNotEmpty()) {
                val v = stack.removeLast()
                for (w in rg[v]) if (comp[w] == -1) { comp[w] = sccCount; stack.addLast(w) }
            }
            sccCount++
        }

        // Check satisfiability: x_i and ¬x_i must be in different SCCs
        for (i in 0 until vars) if (comp[i] == comp[i + vars]) return null

        // Assign: x_i = true iff comp[i] > comp[i + vars]
        // (Kosaraju assigns lower ids to later-finished SCCs, which are earlier in topo order,
        //  so higher id = later in reverse topo = sink-side SCCs)
        return BooleanArray(vars) { i -> comp[i] > comp[i + vars] }
    }
}
