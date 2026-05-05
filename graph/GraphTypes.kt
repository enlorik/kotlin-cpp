const val GRAPH_INF = Long.MAX_VALUE / 2

/** Weighted directed edge to vertex [to] with weight [w]. */
data class Edge(val to: Int, val w: Long = 1L)

/**
 * Disjoint Set Union (Union-Find) with iterative path compression and union by rank.
 * Supports component size queries and component count.
 */
class DSU(val n: Int) {
    val parent = IntArray(n) { it }
    val rank   = IntArray(n)
    val size   = IntArray(n) { 1 }

    fun find(x: Int): Int {
        var root = x
        while (parent[root] != root) root = parent[root]
        var cur = x
        while (parent[cur] != root) { val next = parent[cur]; parent[cur] = root; cur = next }
        return root
    }

    /** Returns true if [x] and [y] were in different components (i.e. a merge happened). */
    fun union(x: Int, y: Int): Boolean {
        val px = find(x); val py = find(y)
        if (px == py) return false
        if (rank[px] < rank[py]) {
            parent[px] = py; size[py] += size[px]
        } else if (rank[px] > rank[py]) {
            parent[py] = px; size[px] += size[py]
        } else {
            parent[py] = px; size[px] += size[py]; rank[px]++
        }
        return true
    }

    fun connected(x: Int, y: Int) = find(x) == find(y)
    fun componentSize(x: Int)    = size[find(x)]
    fun componentCount()         = (0 until n).count { parent[it] == it }
}
