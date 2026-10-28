/** Compile together with graph/*.kt grid/*.kt shared/*.kt and io/FastIO.kt. */
private fun assertUnified(ok: Boolean, label: String) {
    check(ok) { "Unified aggregate API failed: $label" }
}

fun main() {
    val grid = arrayOf(
        intArrayOf(0, 1, 0),
        intArrayOf(2, 0, 4),
        intArrayOf(0, 8, 0)
    )
    val graph = SparseGraph(4)
    graph.addEdge(0, 1)
    graph.addEdge(0, 2)
    graph.addEdge(1, 3)
    val values = intArrayOf(0, 2, 4, 8)

    // Both use the literal SAME public names: Nearest, Radius, Neighbors and aggregate.
    val gridNearest = grid.aggregate(
        selection = Nearest(sources = { r, c -> grid[r][c] != 0 }, maxDistance = 5),
        op = IntAggregations.OR
    )
    val graphNearest = graph.aggregate(
        selection = Nearest(sources = { v -> values[v] != 0 }, maxDistance = 5),
        value = { v -> values[v] },
        op = IntAggregations.OR
    )
    assertUnified(gridNearest.values[1][1] == 15, "grid nearest OR of 2,4,1,8 at distance 1")
    assertUnified(gridNearest.values[0][0] == 3, "grid nearest OR at corner")
    assertUnified(graphNearest.values[0] == 6, "graph nearest OR of 2 and 4")
    assertUnified(graphNearest.distance(0) == 1, "graph nearest distance")

    val gridNeighbors = grid.aggregate(selection = Neighbors, op = IntAggregations.SUM)
    val graphNeighbors = graph.aggregate(
        selection = Neighbors, value = { v -> values[v] }, op = IntAggregations.SUM
    )
    assertUnified(gridNeighbors.values[1][1] == 15, "grid FOUR neighbors")
    assertUnified(graphNeighbors.values[0] == 6, "graph adjacency neighbors")
    assertUnified(grid.aggregate(center = 1 to 1, selection = Neighbors, op = IntAggregations.MAX) == 8,
        "grid single-center neighbors MAX")
    assertUnified(graph.aggregate(center = 0, selection = Neighbors,
        value = { v -> values[v] }, op = IntAggregations.SUM) == 6,
        "graph single-center neighbors SUM")

    val gridRadius = grid.aggregate(selection = Radius(1), op = IntAggregations.SUM)
    val graphRadius = graph.aggregate(selection = Radius(2),
        value = { v -> values[v] }, op = IntAggregations.SUM)
    assertUnified(gridRadius.values[1][1] == 15, "grid Radius 1 includes center")
    assertUnified(graphRadius.values[0] == 14, "graph Radius 2 includes distance-2 value")
    assertUnified(graph.aggregate(center = 0, selection = Radius(2, minDistance = 2),
        value = { v -> values[v] }, op = IntAggregations.SUM) == 8,
        "graph exact distance-2 ring")
    assertUnified(grid.aggregate(center = 1 to 1, selection = Radius(2, minDistance = 2),
        op = IntAggregations.SUM) == 0,
        "grid distance-2 ring has four zero-valued corners")

    // Custom operations share initial/combine interface, with optional graph idempotent fast path.
    assertUnified(graph.aggregate(selection = Radius(2), value = { values[it] },
        initial = 0, combine = { a, b -> a + b }).values[0] == 14,
        "graph custom Radius lambda")
    assertUnified(graph.aggregate(selection = Neighbors, value = { values[it] },
        initial = 0, combine = { a, b -> a + b }).values[0] == 6,
        "graph custom Neighbors lambda")
    assertUnified(grid.aggregate(selection = Neighbors, combine = { a, b -> a + b }).values[1][1] == 15,
        "grid legacy lambda combine still works")
    assertUnified(grid.aggregate(selection = Offsets(FOUR), combine = { a, b -> a + b })
        .values[1][1] == 15, "grid legacy Offsets unchanged")

    val bigSum = grid.aggregate(
        selection = Neighbors,
        value = { r, c -> grid[r][c].toLong() },
        op = GraphAggregateOp(0L, { a: Long, b: Long -> a + b })
    )
    assertUnified(bigSum.values[1][1] == 15L, "Long operations on Int grid")
    val chars = listOf("12", "34")
    val charSum = chars.aggregate(
        selection = Neighbors,
        value = { r, c -> chars[r][c].digitToInt() },
        op = IntAggregations.SUM
    )
    assertUnified(charSum.values[0][0] == 5, "String grid op overload")
    val charGrid = arrayOf(charArrayOf('5','6'), charArrayOf('7','8'))
    assertUnified(charGrid.aggregate(center = 0 to 0, selection = Neighbors,
        value = { r, c -> charGrid[r][c].digitToInt() },
        op = IntAggregations.SUM) == 13, "CharArray grid op overload")

    // Backwards compatibility: old graph-specific selectors remain supported.
    assertUnified(graph.aggregate(selection = GraphNearest({ values[it] != 0 }),
        value = { values[it] }, op = IntAggregations.OR).values[0] == 6,
        "legacy GraphNearest")
    assertUnified(graph.aggregate(selection = GraphRadius(2),
        value = { values[it] }, op = IntAggregations.SUM).values[0] == 14,
        "legacy GraphRadius")
    assertUnified(graph.aggregate(selection = GraphNeighbors,
        value = { values[it] }, op = IntAggregations.SUM).values[0] == 6,
        "legacy GraphNeighbors")
    assertUnified(grid.aggregate(selection = Nearest(sources = { r, c -> grid[r][c] != 0 }),
        combine = { a, b -> a or b }).values[1][1] == 15,
        "legacy grid nearest")

    // A graph cannot use a grid-only Chebyshev distance; fail loudly, never silently.
    var rejected = false
    try {
        graph.aggregate(selection = Radius(2, metric = GridMetric.CHEBYSHEV),
            value = { values[it] }, op = IntAggregations.OR)
    } catch (_: IllegalArgumentException) { rejected = true }
    assertUnified(rejected, "reject Chebyshev graph metric")

    // Multiple shortest paths to the same source must NOT cause duplicate SUM.
    val diamond = SparseGraph(4)
    diamond.addEdge(0, 1); diamond.addEdge(0, 2)
    diamond.addEdge(1, 3); diamond.addEdge(2, 3)
    val onlySource = intArrayOf(0, 0, 0, 7)
    val distinctSum = diamond.aggregate(
        selection = Nearest(sources = { v -> onlySource[v] != 0 }),
        value = { v -> onlySource[v] },
        op = IntAggregations.SUM
    )
    assertUnified(distinctSum.values[0] == 7, "one source counted once through two paths")
    println("PASS: shared selectors, typed reducers, single/all centers, old APIs, source ties")
}
