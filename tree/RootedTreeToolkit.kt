data class TreeDiameterResult(val first: Int,val second: Int,val length: Int)

/** Validated unweighted rooted tree, with iterative traversal and immutable topology. */
class RootedTreeToolkit(parents: IntArray) {
    private val parent = parents.copyOf()
    val size = parent.size
    val root: Int
    private val adjacent: Array<ArrayList<Int>>
    private val order: IntArray
    init {
        require(size > 0)
        root = parent.indexOf(-1)
        require(root >= 0 && parent.count { it == -1 } == 1)
        adjacent = Array(size) { ArrayList<Int>() }
        for (v in parent.indices) if (v != root) {
            val u = parent[v]
            require(u in parent.indices && u != v)
            adjacent[u].add(v); adjacent[v].add(u)
        }
        order = IntArray(size)
        order[0] = root
        var used = 1
        for (i in 0 until size) {
            if (i == used) break
            val v = order[i]
            for (u in adjacent[v]) if (parent[u] == v) order[used++] = u
        }
        require(used == size) { "Parents must form one rooted tree" }
    }
    fun parents(): IntArray = parent.copyOf()
    /** Unweighted diameter, two linear BFS traversals. */
    fun diameter(): TreeDiameterResult {
        fun farthest(start: Int): Pair<Int,Int> {
            val dist = IntArray(size) { -1 }
            val queue = IntArray(size)
            var used = 1; queue[0] = start; dist[start] = 0
            var i = 0
            while (i < used) {
                val v = queue[i++]
                for (u in adjacent[v]) if (dist[u] < 0) {
                    dist[u] = dist[v]+1; queue[used++] = u
                }
            }
            val end = queue[used-1]
            return end to dist[end]
        }
        val a = farthest(root).first
        val (b,d) = farthest(a)
        return TreeDiameterResult(a,b,d)
    }

    /** Centroid-decomposition parent for each original vertex (-1 at root).
     * Each removed centroid splits its current component into pieces of at most
     * half the size. O(n log n) time, O(n) workspace, no recursive DFS.
     */
    fun centroidParents(): IntArray {
        val removed = BooleanArray(size)
        val result = IntArray(size) { -2 }
        val prev = IntArray(size)
        val subtree = IntArray(size)
        val component = IntArray(size)
        val jobs = ArrayDeque<Pair<Int,Int>>()
        jobs.add(root to -1)
        while (jobs.isNotEmpty()) {
            val (start,ancestor) = jobs.removeLast()
            var count = 1
            component[0] = start; prev[start] = -1
            var i = 0
            while (i < count) {
                val v = component[i++]
                for (u in adjacent[v]) if (!removed[u] && u != prev[v]) {
                    prev[u] = v; component[count++] = u
                }
            }
            for (j in count-1 downTo 0) {
                val v = component[j]
                subtree[v] = 1
                for (u in adjacent[v]) if (!removed[u] && prev[u] == v) subtree[v] += subtree[u]
            }
            var centroid = -1
            for (j in 0 until count) {
                val v = component[j]
                var largest = count-subtree[v]
                for (u in adjacent[v]) if (!removed[u] && prev[u] == v)
                    largest = maxOf(largest,subtree[u])
                if (largest <= count/2) { centroid = v; break }
            }
            check(centroid >= 0)
            result[centroid] = ancestor
            removed[centroid] = true
            for (u in adjacent[centroid]) if (!removed[u]) jobs.add(u to centroid)
        }
        return result
    }

    /** Max weight of a connected subset containing root, for each exact
     * cardinality 1..min(limit,size); zero is the optional empty subset.
     * Null means unreachable. Checked Long addition; O(n*limit^2) upper bound,
     * O(n*limit) upper bound memory. Children may be skipped or joined.
     */
    fun connectedKnapsack(weights: LongArray,limit: Int): List<Long?> {
        require(weights.size == size && limit >= 0)
        val cap = minOf(limit,size)
        if (cap == 0) return listOf(0L)
        val dp = arrayOfNulls<Array<Long?>>(size)
        for (i in order.indices.reversed()) {
            val v = order[i]
            var current = arrayOf<Long?>(null,weights[v])
            for (u in adjacent[v]) if (parent[u] == v) {
                val child = dp[u]!!
                val merged = arrayOfNulls<Long>(minOf(cap,current.lastIndex+child.lastIndex)+1)
                for (a in 1..current.lastIndex) {
                    val x = current[a] ?: continue
                    merged[a] = merged[a]?.let { maxOf(it,x) } ?: x
                    for (b in 1..minOf(child.lastIndex,cap-a)) {
                        val y = child[b] ?: continue
                        val z = Math.addExact(x,y)
                        merged[a+b] = merged[a+b]?.let { maxOf(it,z) } ?: z
                    }
                }
                current = merged; dp[u] = null
            }
            dp[v] = current
        }
        val answer = dp[root]!!
        answer[0] = 0L
        return answer.toList()
    }
}
