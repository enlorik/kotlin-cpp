/**
 * Example input:
 *   4 3
 *   1 2
 *   1 3
 *   2 4
 *   0 2 4 8
 *
 * Header, edges, THEN a vertex-values array. The DSL executes in the
 * declared order; move values() before edges() for values-first input.
 *
 * Expected output: 6 -1 -1 -1
 */
fun main() {
    val g = readGraph {
        header()
        edges()
        values()
    }

    val result = g.aggregate(
        selection = Nearest(
            sources = { v -> g.values[v] != 0 },
            maxDistance = 5
        ),
        op = IntAggregations.OR
    )

    printValues(g.n) { v ->
        if (g.values[v] != 0) -1 else result.values[v]
    }
}
