/**
 * Iterative Heavy-Light Decomposition for one connected, undirected tree.
 * Construction O(n); path splits O(log n) chains, and rooted subtree
 * vertices occupy a contiguous interval in heavy-first Euler order.
 */
class HeavyLightTopology(
    val n: Int,
    edges: List<Pair<Int, Int>>,
    val root: Int = 0
) {
    val parent = IntArray(n) { -1 }
    val depth = IntArray(n)
    val heavy = IntArray(n) { -1 }
    val head = IntArray(n)
    val position = IntArray(n)
    val vertexAt = IntArray(n)
    val subtreeSize = IntArray(n) { 1 }

    init {
        require(n > 0) { "Heavy-Light needs at least one vertex" }
        require(root in 0 until n)
        require(edges.size == n - 1) { "A tree on n vertices needs n-1 edges" }

        val adjacent = Array(n) { ArrayList<Int>() }
        for ((u, v) in edges) {
            require(u in 0 until n && v in 0 until n && u != v) {
                "Invalid tree edge ($u,$v)"
            }
            adjacent[u].add(v)
            adjacent[v].add(u)
        }

        // Iterative DFS avoids JVM stack overflow on chain-shaped trees.
        val order = IntArray(n)
        val stack = IntArray(n)
        var size = 0
        var top = 0
        parent[root] = root
        stack[top++] = root
        while (top > 0) {
            val v = stack[--top]
            order[size++] = v
            for (to in adjacent[v]) {
                if (to == parent[v]) continue
                require(parent[to] == -1) { "Graph contains a cycle" }
                parent[to] = v
                depth[to] = depth[v] + 1
                stack[top++] = to
            }
        }
        require(size == n) { "Tree must be connected" }

        for (i in n - 1 downTo 0) {
            val v = order[i]
            var largestChild = 0
            for (to in adjacent[v]) {
                if (parent[to] != v || to == v) continue
                subtreeSize[v] += subtreeSize[to]
                if (subtreeSize[to] > largestChild) {
                    largestChild = subtreeSize[to]
                    heavy[v] = to
                }
            }
        }

        // Heavy-chain first traversal ensures every rooted subtree stays
        // contiguous, including light branches.
        val pending = ArrayDeque<Pair<Int, Int>>()
        pending.addLast(root to root)
        var clock = 0
        while (pending.isNotEmpty()) {
            val (start, chainHead) = pending.removeLast()
            var v = start
            while (v != -1) {
                head[v] = chainHead
                position[v] = clock
                vertexAt[clock++] = v
                for (to in adjacent[v]) {
                    if (parent[to] == v && to != heavy[v]) {
                        pending.addLast(to to to)
                    }
                }
                v = heavy[v]
            }
        }
        check(clock == n) { "Invalid heavy-first traversal" }
        parent[root] = -1
    }

    private fun checkVertex(v: Int) {
        require(v in 0 until n) { "Vertex $v outside 0 until $n" }
    }

    fun lca(u: Int, v: Int): Int {
        checkVertex(u)
        checkVertex(v)
        var a = u
        var b = v
        while (head[a] != head[b]) {
            if (depth[head[a]] > depth[head[b]]) {
                a = parent[head[a]]
            } else {
                b = parent[head[b]]
            }
        }
        return if (depth[a] <= depth[b]) a else b
    }

    fun distance(u: Int, v: Int): Int {
        val ancestor = lca(u, v)
        return depth[u] + depth[v] - 2 * depth[ancestor]
    }

    fun isAncestor(ancestor: Int, vertex: Int): Boolean {
        checkVertex(ancestor)
        checkVertex(vertex)
        return position[vertex] >= position[ancestor] &&
            position[vertex] < position[ancestor] + subtreeSize[ancestor]
    }
}

/** Both directions for a segment, so noncommutative path queries are valid. */
private data class HldPair<T>(val forward: T, val backward: T)

/**
 * Generic vertex-valued path and rooted-subtree monoid aggregates.
 * Path u->v honors traversal order, even for concatenation/matrices.
 * Point assignment O(log n), subtree aggregate O(log n), path O(log^2 n).
 */
class HeavyLightIndex<T>(
    values: List<T>,
    edges: List<Pair<Int, Int>>,
    val operation: Monoid<T>,
    root: Int = 0
) {
    val topology = HeavyLightTopology(values.size, edges, root)
    val size: Int = values.size

    private val tree: SegmentTree<HldPair<T>>

    init {
        val identity = HldPair(operation.identity, operation.identity)
        val ordered = MutableList(size) { identity }
        for (v in 0 until size) {
            ordered[topology.position[v]] = HldPair(values[v], values[v])
        }
        tree = SegmentTree(ordered, identity) { a, b ->
            HldPair(
                operation.combine(a.forward, b.forward),
                operation.combine(b.backward, a.backward)
            )
        }
    }

    fun set(vertex: Int, value: T) {
        require(vertex in 0 until size)
        tree[topology.position[vertex]] = HldPair(value, value)
    }

    /** Vertex values on u -> v, including both endpoints, in path order. */
    fun pathQuery(u: Int, v: Int): T {
        topology.lca(u, v)  // validate both vertices
        var a = u
        var b = v
        var fromStart = operation.identity
        var fromEnd = operation.identity
        while (topology.head[a] != topology.head[b]) {
            if (topology.depth[topology.head[a]] >=
                topology.depth[topology.head[b]]) {
                val section = tree.query(topology.position[topology.head[a]],
                    topology.position[a] + 1)
                fromStart = operation.combine(fromStart, section.backward)
                a = topology.parent[topology.head[a]]
            } else {
                val section = tree.query(topology.position[topology.head[b]],
                    topology.position[b] + 1)
                fromEnd = operation.combine(section.forward, fromEnd)
                b = topology.parent[topology.head[b]]
            }
        }
        if (topology.depth[a] >= topology.depth[b]) {
            val section = tree.query(topology.position[b], topology.position[a] + 1)
            fromStart = operation.combine(fromStart, section.backward)
        } else {
            val section = tree.query(topology.position[a], topology.position[b] + 1)
            fromEnd = operation.combine(section.forward, fromEnd)
        }
        return operation.combine(fromStart, fromEnd)
    }

    /** Rooted subtree aggregate, using heavy-first Euler interval. */
    fun subtreeQuery(vertex: Int): T {
        require(vertex in 0 until size)
        val from = topology.position[vertex]
        return tree.query(from, from + topology.subtreeSize[vertex]).forward
    }

    fun lca(u: Int, v: Int): Int = topology.lca(u, v)
    fun distance(u: Int, v: Int): Int = topology.distance(u, v)
}

/** Primitive Long-sum variant, avoiding boxed generic node aggregates. */
class HeavyLightLongSums(
    values: LongArray,
    edges: List<Pair<Int, Int>>,
    root: Int = 0
) {
    val topology = HeavyLightTopology(values.size, edges, root)
    val size: Int = values.size
    private val tree: LongSumSegmentTree

    init {
        val ordered = LongArray(size)
        for (v in 0 until size) ordered[topology.position[v]] = values[v]
        tree = ordered.sumSegmentTree()
    }

    fun set(vertex: Int, value: Long) {
        require(vertex in 0 until size)
        tree[topology.position[vertex]] = value
    }

    fun pathSum(u: Int, v: Int): Long {
        topology.lca(u, v)
        var a = u
        var b = v
        var total = 0L
        while (topology.head[a] != topology.head[b]) {
            if (topology.depth[topology.head[a]] <
                topology.depth[topology.head[b]]) {
                val tmp = a; a = b; b = tmp
            }
            total += tree.query(topology.position[topology.head[a]],
                topology.position[a] + 1)
            a = topology.parent[topology.head[a]]
        }
        if (topology.depth[a] > topology.depth[b]) {
            val tmp = a; a = b; b = tmp
        }
        return total + tree.query(topology.position[a], topology.position[b] + 1)
    }

    fun subtreeSum(vertex: Int): Long {
        require(vertex in 0 until size)
        val start = topology.position[vertex]
        return tree.query(start, start + topology.subtreeSize[vertex])
    }

    fun lca(u: Int, v: Int): Int = topology.lca(u, v)
    fun distance(u: Int, v: Int): Int = topology.distance(u, v)
}

fun <T> List<T>.heavyLightTree(
    edges: List<Pair<Int, Int>>,
    operation: Monoid<T>,
    root: Int = 0
): HeavyLightIndex<T> = HeavyLightIndex(this, edges, operation, root)

fun LongArray.heavyLightSums(
    edges: List<Pair<Int, Int>>,
    root: Int = 0
): HeavyLightLongSums = HeavyLightLongSums(this, edges, root)

fun IntArray.heavyLightSums(
    edges: List<Pair<Int, Int>>,
    root: Int = 0
): HeavyLightLongSums =
    HeavyLightLongSums(LongArray(size) { this[it].toLong() }, edges, root)
