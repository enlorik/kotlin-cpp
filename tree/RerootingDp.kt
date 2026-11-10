/**
 * Generic O(n) rerooting DP over an undirected tree.
 *
 * At vertex v, combine the contributions from its neighbours in adjacency
 * order using an associative monoid, then finish(v, combined).
 * lift(resultOfComponent, from, to) turns a rooted component result at
 * 'from' into its contribution to neighbour 'to'.
 *
 * Associativity and the identity of combine are caller promises.
 * All traversals are iterative, so a chain of 100k vertices is safe.
 */
class RerootingDp<T>(
    val size: Int,
    edges: List<Pair<Int, Int>>,
    private val identity: T,
    private val combine: (T, T) -> T,
    private val finish: (vertex: Int, contributions: T) -> T,
    private val lift: (component: T, from: Int, to: Int) -> T
) {
    private val adjacent: Array<ArrayList<Int>>
    val answers: List<T>

    init {
        require(size >= 1) { "A tree needs at least one vertex" }
        require(edges.size == size - 1) { "Expected size-1 edges" }
        adjacent = Array(size) { ArrayList() }
        for ((u, v) in edges) {
            require(u in 0 until size && v in 0 until size && u != v)
            adjacent[u].add(v)
            adjacent[v].add(u)
        }
        val parent = IntArray(size) { -1 }
        parent[0] = 0
        val order = IntArray(size)
        var count = 1
        order[0] = 0
        for (i in 0 until size) {
            if (i >= count) break
            val v = order[i]
            for (w in adjacent[v]) {
                if (w == parent[v]) continue
                require(parent[w] == -1) { "Edges contain a cycle" }
                parent[w] = v
                order[count++] = w
            }
        }
        require(count == size) { "The tree must be connected" }

        val down = arrayOfNulls<Any?>(size)
        for (i in size - 1 downTo 0) {
            val v = order[i]
            var merged = identity
            for (w in adjacent[v]) {
                if (parent[w] == v) {
                    @Suppress("UNCHECKED_CAST")
                    val fromChild = down[w] as T
                    merged = combine(merged, lift(fromChild, w, v))
                }
            }
            down[v] = finish(v, merged)
        }

        val up = arrayOfNulls<Any?>(size)
        val result = arrayOfNulls<Any?>(size)
        for (v in order) {
            val neighbors = adjacent[v]
            val degree = neighbors.size
            val contributions = arrayOfNulls<Any?>(degree)
            val prefix = arrayOfNulls<Any?>(degree + 1)
            val suffix = arrayOfNulls<Any?>(degree + 1)
            prefix[0] = identity
            for (i in 0 until degree) {
                val w = neighbors[i]
                @Suppress("UNCHECKED_CAST")
                val message = if (w == parent[v]) up[v] as T
                              else lift(down[w] as T, w, v)
                contributions[i] = message
                @Suppress("UNCHECKED_CAST")
                prefix[i + 1] = combine(prefix[i] as T, message)
            }
            suffix[degree] = identity
            for (i in degree - 1 downTo 0) {
                @Suppress("UNCHECKED_CAST")
                suffix[i] = combine(contributions[i] as T, suffix[i + 1] as T)
            }
            @Suppress("UNCHECKED_CAST")
            result[v] = finish(v, prefix[degree] as T)
            for (i in 0 until degree) {
                val w = neighbors[i]
                if (parent[w] != v) continue
                @Suppress("UNCHECKED_CAST")
                val withoutChild = combine(prefix[i] as T, suffix[i + 1] as T)
                up[w] = lift(finish(v, withoutChild), v, w)
            }
        }
        @Suppress("UNCHECKED_CAST")
        answers = result.map { it as T }
    }
}

fun <T> rerootingDp(
    n: Int,
    edges: List<Pair<Int, Int>>,
    monoid: Monoid<T>,
    finish: (vertex: Int, contributions: T) -> T,
    lift: (component: T, from: Int, to: Int) -> T
): List<T> = RerootingDp(n, edges, monoid.identity, monoid::combine, finish, lift).answers

/** Sum of unweighted graph distances from every vertex of a tree. */
fun treeDistanceSums(n: Int, edges: List<Pair<Int, Int>>): LongArray {
    data class Aggregate(val vertices: Long, val distances: Long)
    val operation = monoid(Aggregate(0, 0)) { a: Aggregate, b: Aggregate ->
        Aggregate(a.vertices + b.vertices, a.distances + b.distances)
    }
    val answers = rerootingDp(n, edges, operation,
        finish = { _, state -> Aggregate(state.vertices + 1, state.distances) },
        lift = { component, _, _ ->
            Aggregate(component.vertices, component.distances + component.vertices)
        })
    return LongArray(n) { answers[it].distances }
}
