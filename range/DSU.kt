class DSU(n: Int) {
    private val parent = IntArray(n) { it }
    private val rank = IntArray(n)
    var components = n
        private set

    fun find(x: Int): Int {
        if (parent[x] != x) parent[x] = find(parent[x])
        return parent[x]
    }

    fun union(x: Int, y: Int): Boolean {
        val rx = find(x); val ry = find(y)
        if (rx == ry) return false
        when {
            rank[rx] < rank[ry] -> parent[rx] = ry
            rank[rx] > rank[ry] -> parent[ry] = rx
            else -> { parent[ry] = rx; rank[rx]++ }
        }
        components--
        return true
    }

    fun same(x: Int, y: Int): Boolean = find(x) == find(y)
}
