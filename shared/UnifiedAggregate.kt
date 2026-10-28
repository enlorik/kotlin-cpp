/**
 * Shared, receiver-aware aggregation vocabulary.
 *
 * The existing grid and graph algorithms remain independent and unchanged.
 * Kotlin resolves aggregate() based on its receiver type. Nearest() overloads
 * resolve based on the number of lambda parameters (r,c versus v).
 * Backward-compatible GraphNearest, GraphRadius, GraphNeighbors, Offsets,
 * and manual initial/combine APIs remain supported.
 *
 * Neighbors means FOUR orthogonal neighbors for grids, and distinct outgoing
 * adjacent vertices (not self-loops) for graphs.
 */

/** Graph-specific Nearest factory; grids continue using class Nearest((r,c)->...). */
fun Nearest(
    sources: (v: Int) -> Boolean,
    maxDistance: Int = Int.MAX_VALUE
): GraphNearest = GraphNearest(sources, maxDistance)

// A Radius selector has one grid-specific optional parameter (metric).
// Graph radius has no Chebyshev interpretation, so reject that option explicitly.
private fun Radius.asGraphRadius(): GraphRadius {
    require(metric == GridMetric.MANHATTAN) {
        "Graph Radius counts edges; grid-only CHEBYSHEV metric is not valid on a graph"
    }
    return GraphRadius(radius, minDistance)
}

/** Neighbors and Radius use the same names on SparseGraph and grid receivers. */

/** Graph all-center aggregation using shared Neighbors selector. */
fun <T> SparseGraph.aggregate(
    selection: Neighbors,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> =
    aggregate(selection = GraphNeighbors, value = value, op = op,
        include = include, canTraverse = canTraverse)

fun <T> SparseGraph.aggregate(
    selection: Neighbors,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    idempotent: Boolean = false,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> =
    aggregate(selection = GraphNeighbors, value = value, initial = initial,
        combine = combine, idempotent = idempotent,
        include = include, canTraverse = canTraverse)

/** Graph single-center aggregation using shared Neighbors selector. */
fun <T> SparseGraph.aggregate(
    center: Int,
    selection: Neighbors,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T = aggregate(center = center, selection = GraphNeighbors,
    value = value, op = op, include = include, canTraverse = canTraverse)

fun <T> SparseGraph.aggregate(
    center: Int,
    selection: Neighbors,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T = aggregate(center = center, selection = GraphNeighbors,
    value = value, initial = initial, combine = combine,
    include = include, canTraverse = canTraverse)

/** Graph all-center aggregation using shared Radius selector. */
fun <T> SparseGraph.aggregate(
    selection: Radius,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> =
    aggregate(selection = selection.asGraphRadius(), value = value, op = op,
        include = include, canTraverse = canTraverse)

fun <T> SparseGraph.aggregate(
    selection: Radius,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    idempotent: Boolean = false,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): GraphAggregateResult<T> =
    aggregate(selection = selection.asGraphRadius(), value = value, initial = initial,
        combine = combine, idempotent = idempotent,
        include = include, canTraverse = canTraverse)

/** Graph single-center aggregation using shared Radius selector. */
fun <T> SparseGraph.aggregate(
    center: Int,
    selection: Radius,
    value: (v: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T = aggregate(center = center, selection = selection.asGraphRadius(),
    value = value, op = op, include = include, canTraverse = canTraverse)

fun <T> SparseGraph.aggregate(
    center: Int,
    selection: Radius,
    value: (v: Int) -> T,
    initial: T,
    combine: (T, T) -> T,
    include: (v: Int) -> Boolean = { true },
    canTraverse: (u: Int, edge: Edge) -> Boolean = { _, _ -> true }
): T = aggregate(center = center, selection = selection.asGraphRadius(),
    value = value, initial = initial, combine = combine,
    include = include, canTraverse = canTraverse)

/**
 * Grid adapters let both receivers use op = IntAggregations.OR/SUM/MIN/MAX.
 * The original grid initial+combine overloads are untouched.
 * IntArray grids can omit value when using Int operations.
 */
fun Array<IntArray>.aggregate(
    selection: GridSelection,
    op: GraphAggregateOp<Int>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<Int> = aggregate(
    selection = selection, initial = op.initial,
    combine = op.combine, include = include
)

fun Array<IntArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    op: GraphAggregateOp<Int>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): Int = aggregate(
    center = center, selection = selection, initial = op.initial,
    combine = op.combine, include = include
)

fun <T> Array<IntArray>.aggregate(
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> = aggregate(
    selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> Array<IntArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T = aggregate(
    center = center, selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> List<CharSequence>.aggregate(
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> = aggregate(
    selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> List<CharSequence>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T = aggregate(
    center = center, selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> Array<out CharSequence>.aggregate(
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> = aggregate(
    selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> Array<out CharSequence>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T = aggregate(
    center = center, selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> Array<CharArray>.aggregate(
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): GridAggregateResult<T> = aggregate(
    selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)

fun <T> Array<CharArray>.aggregate(
    center: Pair<Int, Int>,
    selection: GridSelection,
    value: (r: Int, c: Int) -> T,
    op: GraphAggregateOp<T>,
    include: (r: Int, c: Int) -> Boolean = { _, _ -> true }
): T = aggregate(
    center = center, selection = selection, initial = op.initial,
    value = value, combine = op.combine, include = include
)
