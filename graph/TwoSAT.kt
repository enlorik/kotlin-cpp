// 2-SAT solver using Kosaraju's SCC
//
// Variable encoding:
//   positive literal of variable i  =  2*i
//   negative literal of variable i  =  2*i + 1
//   negation of literal l           =  l xor 1
//
// Clause (a OR b) is encoded as two implications: (!a -> b) and (!b -> a).
class TwoSAT(n: Int) {
    private val N = n
    private val g  = Array(2 * n) { mutableListOf<Int>() }  // implication graph
    private val rg = Array(2 * n) { mutableListOf<Int>() }  // reverse graph for Kosaraju

    // Add clause: (variable u with sign uNeg) OR (variable v with sign vNeg)
    // uNeg=true means "NOT u", uNeg=false means "u"
    fun addClause(u: Int, uNeg: Boolean, v: Int, vNeg: Boolean) {
        val literalU = 2 * u + if (uNeg) 1 else 0
        val literalV = 2 * v + if (vNeg) 1 else 0
        // (!literalU -> literalV)
        g[literalU xor 1].add(literalV);  rg[literalV].add(literalU xor 1)
        // (!literalV -> literalU)
        g[literalV xor 1].add(literalU);  rg[literalU].add(literalV xor 1)
    }

    // Add clause: (u OR v) — both positive literals
    fun addClause(u: Int, v: Int) = addClause(u, false, v, false)

    // Force variable u to be true (uNeg=false) or false (uNeg=true)
    fun setVar(u: Int, value: Boolean) = addClause(u, !value, u, !value)

    // Returns a satisfying assignment, or null if unsatisfiable
    fun solve(): BooleanArray? {
        val comp = kosaraju()
        val result = BooleanArray(N)
        for (i in 0 until N) {
            if (comp[2 * i] == comp[2 * i + 1]) return null
            // With Kosaraju's numbering (0 = source), higher comp = later in topo order.
            // Assign true if positive literal is in a later (higher-numbered) SCC.
            result[i] = comp[2 * i] > comp[2 * i + 1]
        }
        return result
    }

    private fun kosaraju(): IntArray {
        val m = 2 * N
        val visited = BooleanArray(m)
        val order = mutableListOf<Int>()

        // Pass 1: finish-order DFS on g
        for (s in 0 until m) {
            if (visited[s]) continue
            val stack = ArrayDeque<IntArray>()  // [node, adjIndex]
            visited[s] = true
            stack.addLast(intArrayOf(s, 0))
            while (stack.isNotEmpty()) {
                val frame = stack.last()
                val u = frame[0]; val i = frame[1]
                if (i < g[u].size) {
                    frame[1]++
                    val v = g[u][i]
                    if (!visited[v]) { visited[v] = true; stack.addLast(intArrayOf(v, 0)) }
                } else {
                    stack.removeLast()
                    order.add(u)
                }
            }
        }

        // Pass 2: assign SCC IDs on rg in reverse finish order
        val comp = IntArray(m) { -1 }
        var sccId = 0
        for (s in order.reversed()) {
            if (comp[s] != -1) continue
            val stack = ArrayDeque<Int>()
            stack.addLast(s); comp[s] = sccId
            while (stack.isNotEmpty()) {
                val u = stack.removeLast()
                for (v in rg[u]) {
                    if (comp[v] == -1) { comp[v] = sccId; stack.addLast(v) }
                }
            }
            sccId++
        }
        return comp
    }
}
