// Tarjan's SCC algorithm (iterative to avoid stack overflow)
// Returns comp[v] = SCC index (0-based)
// SCCs are numbered in reverse topological order: comp=0 is a sink in the condensation.
fun scc(g: Array<IntArray>): IntArray {
    val n = g.size
    val comp = IntArray(n) { -1 }
    val num = IntArray(n) { -1 }
    val low = IntArray(n)
    val onStack = BooleanArray(n)
    val stk = ArrayDeque<Int>()
    var timer = 0
    var numSCC = 0

    // frame: [node, adjIndex]
    val callStack = ArrayDeque<IntArray>()

    for (s in 0 until n) {
        if (num[s] != -1) continue
        num[s] = timer; low[s] = timer++
        stk.addLast(s); onStack[s] = true
        callStack.addLast(intArrayOf(s, 0))

        while (callStack.isNotEmpty()) {
            val frame = callStack.last()
            val u = frame[0]
            val i = frame[1]
            if (i < g[u].size) {
                val v = g[u][i]
                frame[1]++
                if (num[v] == -1) {
                    num[v] = timer; low[v] = timer++
                    stk.addLast(v); onStack[v] = true
                    callStack.addLast(intArrayOf(v, 0))
                } else if (onStack[v]) {
                    if (num[v] < low[u]) low[u] = num[v]
                }
            } else {
                callStack.removeLast()
                if (callStack.isNotEmpty()) {
                    val pu = callStack.last()[0]
                    if (low[u] < low[pu]) low[pu] = low[u]
                }
                if (low[u] == num[u]) {
                    while (true) {
                        val v = stk.removeLast()
                        onStack[v] = false
                        comp[v] = numSCC
                        if (v == u) break
                    }
                    numSCC++
                }
            }
        }
    }
    return comp
}

// Kosaraju's SCC algorithm (iterative)
// Returns comp[v] = SCC index (0-based)
// SCCs are numbered in topological order: comp=0 is a source in the condensation.
fun sccKosaraju(g: Array<IntArray>): IntArray {
    val n = g.size

    // Pass 1: DFS on original graph, record finish order
    val visited = BooleanArray(n)
    val order = mutableListOf<Int>()
    for (s in 0 until n) {
        if (visited[s]) continue
        // frame: [node, adjIndex]
        val stack = ArrayDeque<IntArray>()
        visited[s] = true
        stack.addLast(intArrayOf(s, 0))
        while (stack.isNotEmpty()) {
            val frame = stack.last()
            val u = frame[0]
            val i = frame[1]
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

    // Build reverse graph
    val rg = Array(n) { mutableListOf<Int>() }
    for (u in 0 until n) for (v in g[u]) rg[v].add(u)

    // Pass 2: DFS on reverse graph in reverse finish order
    val comp = IntArray(n) { -1 }
    var sccId = 0
    for (s in order.reversed()) {
        if (comp[s] != -1) continue
        val stack = ArrayDeque<Int>()
        stack.addLast(s)
        comp[s] = sccId
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

// Build condensation DAG from graph and SCC assignment
// Returns adjacency list of the condensation (no duplicate edges)
fun condensation(g: Array<IntArray>, comp: IntArray): Array<IntArray> {
    val numSCC = (comp.maxOrNull() ?: return Array(0) { IntArray(0) }) + 1
    val seen = HashSet<Long>()
    val dag = Array(numSCC) { mutableListOf<Int>() }
    for (u in g.indices) {
        val cu = comp[u]
        for (v in g[u]) {
            val cv = comp[v]
            if (cu != cv) {
                val key = cu.toLong() * numSCC + cv
                if (seen.add(key)) dag[cu].add(cv)
            }
        }
    }
    return Array(numSCC) { dag[it].toIntArray() }
}
