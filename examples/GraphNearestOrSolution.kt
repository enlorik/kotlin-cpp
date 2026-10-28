/**
 * Graph version of the nearest-tied-nonzero bitwise OR problem.
 * Input: n m, n vertex integers, then m 1-indexed undirected edges.
 * Output: -1 for nonzero vertices; otherwise OR of ALL nearest nonzero
 * vertices within five unweighted edges; 0 if no eligible source.
 */
fun main() {
    val (n, m) = readInts()
    val values = readInts(n)
    val graph = readGraph(n, m)

    val result = graph.aggregate(
        selection = GraphNearest(
            sources = { v -> values[v] != 0 },
            maxDistance = 5
        ),
        value = { v -> values[v] },
        op = IntAggregations.OR
    )

    printValues(n) { v ->
        if (values[v] != 0) -1 else result.values[v]
    }
}
