# Graph aggregation: one aggregate() API for neighbors, radius and nearest sources

The **same extension name** used for grids now works on `SparseGraph`:
`g.aggregate(...)`. You choose which vertices contribute, then what operation
combines their values. Graph-specific selectors avoid collision with grid
`Radius`, `Nearest` and `Offsets`:

- `GraphNeighbors`: each **distinct immediate outgoing neighbor**, excluding
  the center, even if a self-loop exists. Parallel edges never double-count.
- `GraphRadius(radius, minDistance = 0)`: distinct vertices at **unweighted
  shortest-path** distances `minDistance..radius`, inclusive. Distance zero
  includes the center. `GraphRadius(2, 2)` is a distance-2 ring.
- `GraphNearest(sources = { v -> ... }, maxDistance = 5)`: only **qualifying
  source vertices tied for the minimum distance**, not farther sources.

On directed graphs, distance is computed along **outgoing edges from each
center TO a contributing vertex**. All distances count edges and ignore
`Edge.w`. For weighted graph-distance aggregation, Dijkstra-based variants
would be a separate feature.

## A complete pretty graph solution

This is the graph version of the original C++ nearest-nonzero problem.
Input format: `n m`, then `n` vertex integers, then `m` **1-indexed
undirected** edge endpoints.

```kotlin
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
```

`IntAggregations.OR` declares that bitwise OR can be efficiently merged along
multiple shortest paths. The nearest source's distance is `result.distance(v)`
or `null` if no qualifying source is reachable within maxDistance.

Example:

```text
4 3
0 2 4 8
1 2
1 3
2 4
```

Output:

```text
6 -1 -1 -1
```

Full source: `examples/GraphNearestOrSolution.kt`.

## Neighbor sum, distance-2 sum and minimum

```kotlin
val sumOfNeighbors = g.aggregate(
    selection = GraphNeighbors,
    value = { v -> values[v] },
    op = IntAggregations.SUM
)
// One center:
val sumNearZero = g.aggregate(
    center = 0, selection = GraphRadius(2),
    value = { v -> values[v] }, op = IntAggregations.SUM
)

val minAtExactlyTwo = g.aggregate(
    center = 0, selection = GraphRadius(2, minDistance = 2),
    value = { v -> values[v] }, op = IntAggregations.MIN
)
```

`GraphRadius(2)` includes center 0. For exactly two edges away use
`GraphRadius(2, 2)`. A vertex is aggregated **once**, even if multiple paths
or parallel edges lead to it.

## Custom aggregation and filtering

Built-in `IntAggregations` operations: `OR`, `AND`, `MIN`, `MAX`,
`SUM`, `XOR`, each with the correct initial identity value.

Use a custom lambda for other operations:

```kotlin
val result = g.aggregate(
    selection = GraphRadius(2),
    value = { v -> values[v].toLong() },
    initial = 0L,
    combine = { a, b -> a + b },
    include = { v -> values[v] > 0 },
    canTraverse = { u, e -> e.w <= 10 }
)
```

The optional `include` controls **which vertices contribute**. It does not
block traversal. `canTraverse(u, edge)` controls which edges BFS can follow.
Both work with directed graphs, and you can aggregate custom types with
`GraphAggregateOp<T>` or `initial + combine`.

## IMPORTANT: the fast path versus distinct-source sums

For `GraphNearest` there are two fundamentally different cases:

1. `IntAggregations.OR`, `AND`, `MIN`, `MAX` are **idempotent**:
   `combine(x,x)==x`. One reverse multi-source BFS determines the minimum
   reachable-source distance; shortest-distance-layer propagation combines
   all equally nearest values. Duplicate paths are harmless. Time **O(V+E)**
   and space **O(V+E)**.
2. `IntAggregations.SUM`, `XOR` and arbitrary non-idempotent operations
   must count each **distinct source vertex exactly once**, even if many
   shortest paths lead from a center to it. The implementation first computes
   all shortest-to-source distances in **O(V+E)**, then performs an additional
   bounded BFS per applicable center to enumerate exactly the distinct nearest
   source vertices. This is correct, but can be **O(V*(V+E)) worst case**.

The custom lambda API defaults to correctness for distinct vertices:

```kotlin
g.aggregate(
    selection = GraphNearest(sources = { v -> values[v] != 0 }),
    value = { v -> values[v] },
    initial = 0,
    combine = { a, b -> a + b }
)
```

If you **know** your custom combine is idempotent, explicitly opt into the
linear-time path:

```kotlin
g.aggregate(
    selection = GraphNearest(sources = { v -> values[v] != 0 }),
    value = { v -> values[v] },
    initial = 0,
    combine = { a, b -> a or b },
    idempotent = true
)
```

For radius aggregation across all vertices, BFS is repeated from each center
and may take **O(V*(V+E))** worst case. `GraphNeighbors` is O(V+E).
The single-center `g.aggregate(center = ..., ...)` runs just one bounded BFS.

For operations where reduction order matters, use associative and
commutative combiners when possible; source/adjacency iteration order
is not guaranteed to be the same across different algorithms.

## Fast graph input and array output

```kotlin
val graph = readGraph(n, m) // n vertices, m undirected edges, 1-based labels
val directed = readGraph(n, m, directed = true)
val weighted = readGraph(n, m, weighted = true) // edges: u v w
val zeroBased = readGraph(n, m, oneIndexed = false)
printValues(answer) // IntArray, LongArray, List<T>
printValues(n) { v -> values[v] }
```

`readGraph` uses the same fast scanner as `readInts` and converts 1-based
input labels to 0-based internal vertex IDs by default.

## Dependency and compilation

```bash
kotlinc graph/*.kt io/FastIO.kt tests/GraphAggregateTests.kt -include-runtime -d graph-aggregate-tests.jar
java -cp graph-aggregate-tests.jar GraphAggregateTestsKt

kotlinc graph/*.kt io/FastIO.kt examples/GraphNearestOrSolution.kt -include-runtime -d graph-solution.jar
java -jar graph-solution.jar
```

Tests cover independent BFS oracles, source ties, parallel edges,
self-loops, directed orientation, exact shortest-radius semantics,
distinct-source summing, and 100,000-vertex fast-path cases.

The source required by the graph example is `graph/GraphAggregate.kt`,
`graph/GraphIO.kt`, `graph/SparseGraph.kt`,
`graph/GraphTypes.kt`, `graph/GraphResults.kt` and `io/FastIO.kt`.
Your judge still requires the library sources bundled with the solution.
