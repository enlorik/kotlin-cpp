import java.util.ArrayDeque

/** A rule describing which graph vertices contribute to each vertex's aggregation. */
sealed interface GraphSelection

/** Immediate outgoing neighbors, excluding the center (even if it has a self-loop). */
data object GraphNeighbors : GraphSelection

/** Vertices at unweighted shortest-path distance minDistance..radius from each center. */
data class GraphRadius(val radius: Int, val minDistance: Int = 0) : GraphSelection {
    init {
        require(radius >= 0) { "radius must be nonnegative" }
        require(minDistance in 0..radius) { "minDistance must be in 0..radius" }
    }
}

/** All eligible source vertices tied at the minimum outgoing-path distance from each center. */
class GraphNearest(
    val sources: (v: Int) -> Boolean,
    val maxDistance: Int = Int.MAX_VALUE
) : GraphSelection {
    init { require(maxDistance >= 0) { "maxDistance must be nonnegative" } }
}

/**
 * Aggregation operation, including its identity. Set idempotent = true ONLY when
 * combine(x, x) == x, for example OR, AND, MIN or MAX. Then nearest-source
 * aggregation can safely merge contributions along multiple shortest paths in O(V+E).
 * For SUM, XOR, products or custom non-idempotent operations, leave it false:
 * the library visits each distinct nearest source exactly once per center.
 */
class GraphAggregateOp<T>(
    val initial: T,
    val combine: (T, T) -> T,
    val idempotent: Boolean = false
)

/** Useful integer operations, with their correct fast-path properties. */
object IntAggregations {
    val OR = GraphAggregateOp(0, { a: Int, b: Int -> a or b }, idempotent = true)
    val AND = GraphAggregateOp(-1, { a: Int, b: Int -> a and b }, idempotent = true)
    val MIN = GraphAggregateOp(Int.MAX_VALUE, { a: Int, b: Int -> minOf(a, b) }, idempotent = true)
    val MAX = GraphAggregateOp(Int.MIN_VALUE, { a: Int, b: Int -> maxOf(a, b) }, idempotent = true)
    val SUM = GraphAggregateOp(0, { a: Int, b: Int -> a + b })
    val XOR = GraphAggregateOp(0, { a: Int, b: Int -> a xor b })
}

/** Values for every vertex; distance(v) is populated only for GraphNearest. */
class GraphAggregateResult<T>(
    val values: List<T>,
    private val nearestDistances: IntArray? = null
) {
    operator fun get(v: Int): T = values[v]
    fun distance(v: Int): Int? = nearestDistances?.get(v)?.takeIf { it >= 0 }
}

/**
 * Compute graph aggregations for all centers. All distances count EDGES, not Edge.w.
 * Directed graphs use outgoing paths from each center to contributors.
 * canTraverse filters edges, while include filters contributing vertices (and
 * qualifying sources in GraphNearest); include does NOT obstruct traversal.
 * GraphNeighbors / GraphRadius perform one BFS per center; GraphNearest first
 * performs ONE reverse multi-source BFS to identify nearest distances.
 * With an idempotent operation, GraphNearest then propagates contributions
 * in shortest-distance order in O(V+E) total time. Otherwise, exact distinct
 * nearest sources are enumerated with a bounded BFS from each center, potentially
 * O(V*(V+E)) worst case. Distinct sources are NEVER double-counted for SUM.
 */
fun <T> SparseGraph.aggregate(
    selection: GraphSelection,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> {
    val values = MutableList(n) { op.initial }
    if (n == 0) return GraphAggregateResult(values, if (selection is GraphNearest) IntArray(0) else null)

    // A timestamp-based seen array avoids clearing BooleanArray(n) for every center.
    val seen = IntArray(n)
    var stamp = 0
    val queue = IntArray(n)
    fun nextStamp(): Int {
        if (stamp == Int.MAX_VALUE) { seen.fill(0); stamp = 0 }
        return ++stamp
    }

    when (selection) {
        GraphNeighbors, is GraphRadius -> {
            val minDist = if (selection is GraphRadius) selection.minDistance else 1
            val maxDist = if (selection is GraphRadius) selection.radius else 1
            val distance = IntArray(n)
            for (center in 0 until n) {
                val mark = nextStamp()
                var head = 0
                var tail = 0
                queue[tail++] = center
                seen[center] = mark
                distance[center] = 0
                var acc = op.initial
                while (head < tail) {
                    val u = queue[head++]
                    val d = distance[u]
                    if (d >= minDist && include(u)) acc = op.combine(acc, value(u))
                    if (d >= maxDist) continue
                    for (e in g[u]) {
                        if (!canTraverse(u, e)) continue
                        val v = e.to
                        if (seen[v] == mark) continue
                        seen[v] = mark
                        distance[v] = d + 1
                        queue[tail++] = v
                    }
                }
                values[center] = acc
            }
            return GraphAggregateResult(values)
        }
        is GraphNearest -> {
            // Reverse adjacency makes distances FROM every center TO a source
            // correct even on directed graphs (where ordinary multi-source BFS
            // would instead measure source -> center).
            val incoming = Array(n) { ArrayList<Int>() }
            for (u in 0 until n) for (e in g[u]) {
                if (canTraverse(u, e)) incoming[e.to].add(u)
            }
            val sources = BooleanArray(n)
            val distance = IntArray(n) { -1 }
            var head = 0
            var tail = 0
            for (v in 0 until n) if (selection.sources(v) && include(v)) {
                sources[v] = true
                distance[v] = 0
                queue[tail++] = v
                values[v] = op.combine(op.initial, value(v))
            }
            // Queue is in nondecreasing distance order. Every predecessor at
            // depth d-1 is processed before depth-d vertices are propagated.
            while (head < tail) {
                val u = queue[head++]
                val d = distance[u]
                if (d >= selection.maxDistance) continue
                for (v in incoming[u]) {
                    if (distance[v] == -1) {
                        distance[v] = d + 1
                        queue[tail++] = v
                    }
                    if (op.idempotent && distance[v] == d + 1) {
                        values[v] = op.combine(values[v], values[u])
                    }
                }
            }
            if (!op.idempotent) {
                // Re-evaluate each center's distinct nearest source vertices.
                // Never propagate a non-idempotent operation along path edges:
                // one source could have multiple equally short paths.
                val steps = IntArray(n)
                for (center in 0 until n) {
                    val targetDistance = distance[center]
                    if (targetDistance < 0 || targetDistance == 0) continue
                    val mark = nextStamp()
                    var first = 0
                    var last = 0
                    queue[last++] = center
                    seen[center] = mark
                    steps[center] = 0
                    var acc = op.initial
                    while (first < last) {
                        val u = queue[first++]
                        val d = steps[u]
                        if (d == targetDistance) {
                            if (sources[u]) acc = op.combine(acc, value(u))
                            continue
                        }
                        for (e in g[u]) {
                            if (!canTraverse(u, e)) continue
                            val v = e.to
                            if (seen[v] == mark) continue
                            seen[v] = mark
                            steps[v] = d + 1
                            queue[last++] = v
                        }
                    }
                    values[center] = acc
                }
            }
            return GraphAggregateResult(values, distance)
        }
    }
}

/** Custom lambda form. Default is distinct-source correctness, not fast idempotent merging. */
fun <T> SparseGraph.aggregate(
    selection: GraphSelection,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    idempotent: Boolean = false,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> = aggregate(
    selection, value, GraphAggregateOp(initial, combine, idempotent), include, canTraverse
)

/** Aggregate around a single center, without computing values for all centers. */
fun <T> SparseGraph.aggregate(
    center: Int,
    selection: GraphSelection,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T {
    require(center in 0 until n) { "center out of bounds" }
    val queue = IntArray(n)
    val distance = IntArray(n) { -1 }
    var head = 0
    var tail = 0
    queue[tail++] = center
    distance[center] = 0
    var acc = op.initial
    val minDist: Int
    val maxDist: Int
    when (selection) {
        GraphNeighbors -> { minDist = 1; maxDist = 1 }
        is GraphRadius -> { minDist = selection.minDistance; maxDist = selection.radius }
        is GraphNearest -> { minDist = 0; maxDist = selection.maxDistance }
    }
    var nearest: Int? = null
    while (head < tail) {
        val u = queue[head++]
        val d = distance[u]
        if (selection is GraphNearest) {
            if (nearest != null && d > nearest) break
            if (selection.sources(u) && include(u)) {
                nearest = d
                acc = op.combine(acc, value(u))
            }
        } else if (d >= minDist && include(u)) {
            acc = op.combine(acc, value(u))
        }
        if (d >= maxDist || nearest != null) continue
        for (e in g[u]) {
            if (!canTraverse(u, e)) continue
            val v = e.to
            if (distance[v] != -1) continue
            distance[v] = d + 1
            queue[tail++] = v
        }
    }
    return acc
}

/** Custom lambda form for a single center. */
fun <T> SparseGraph.aggregate(
    center: Int,
    selection: GraphSelection,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T = aggregate(center, selection, value, GraphAggregateOp(initial, combine), include, canTraverse)
