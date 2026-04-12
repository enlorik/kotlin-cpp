// Kuhn's algorithm (augmenting-path bipartite matching)
// n: left-side vertex count, m: right-side vertex count
// g: adjacency list from left vertices to right vertices (0-indexed on each side)

// Returns the maximum matching size without allocating the full matching array
fun kuhn(n: Int, m: Int, g: Array<IntArray>): Int {
    val matchR = IntArray(m) { -1 }
    var matched = 0

    fun tryAugment(u: Int, visited: BooleanArray): Boolean {
        for (v in g[u]) {
            if (visited[v]) continue
            visited[v] = true
            if (matchR[v] == -1 || tryAugment(matchR[v], visited)) {
                matchR[v] = u
                return true
            }
        }
        return false
    }

    for (u in 0 until n) {
        if (tryAugment(u, BooleanArray(m))) matched++
    }
    return matched
}

// Returns matching array where result[leftVertex] = matched rightVertex, or -1 if unmatched
fun kuhnMatch(n: Int, m: Int, g: Array<IntArray>): IntArray {
    val matchL = IntArray(n) { -1 }
    val matchR = IntArray(m) { -1 }

    fun tryAugment(u: Int, visited: BooleanArray): Boolean {
        for (v in g[u]) {
            if (visited[v]) continue
            visited[v] = true
            if (matchR[v] == -1 || tryAugment(matchR[v], visited)) {
                matchL[u] = v
                matchR[v] = u
                return true
            }
        }
        return false
    }

    for (u in 0 until n) tryAugment(u, BooleanArray(m))
    return matchL
}
