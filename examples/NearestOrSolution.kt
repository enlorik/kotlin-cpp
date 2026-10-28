/**
 * Full solution for the nearest-nonzero, tied-minimum-Manhattan-distance
 * bitwise OR problem. Include:
 *   io/FastIO.kt, grid/GridBfs.kt, grid/GridAggregate.kt
 *   graph/GraphTypes.kt, graph/GraphResults.kt, graph/SparseGraph.kt
 *   graph/GraphAggregate.kt, shared/UnifiedAggregate.kt
 *
 * Nonzero input cells print -1; zero cells OR only the nearest nonzero
 * values if that minimum distance is at most 5, otherwise print 0.
 */
fun main() {
    val (h, w) = readInts()
    val grid = readIntGrid(h, w)

    val result = grid.aggregate(
        selection = Nearest(
            sources = { r, c -> grid[r][c] != 0 },
            maxDistance = 5
        ),
        op = IntAggregations.OR
    )

    printGrid(h, w) { r, c ->
        if (grid[r][c] != 0) -1 else result.values[r][c]
    }
}
