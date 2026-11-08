# Automatically choose shortest-path algorithms

The graph API can inspect current edge weights and select the right existing
shortest-path algorithm for you.

~~~kotlin
val graph = SparseGraph(4)
graph.addEdge(0, 1, 1)
graph.addEdge(1, 2, 1)
val answer = graph.shortestPaths(0)
println(answer.algorithm)           // BFS
println(answer.distTo(2))            // 2
println(answer.reconstructPath(2))   // [0, 1, 2]
~~~

## Selection

| Current weights anywhere in the graph | Selected algorithm | Query cost including scan |
|---|---|---|
| All weights equal 1, or no edges | BFS | O(V + E) |
| All weights in {0, 1}, not all 1 | 0-1 BFS | O(V + E) |
| All weights nonnegative, otherwise | Dijkstra | O((V + E) log V) |
| At least one negative weight | Bellman-Ford | O(VE) |

The graph is scanned **on every call**; new edges are accounted for.
The result provides the algorithm, original ShortestPathResult, distances,
parents, path reconstruction and nullable distances to unreachable vertices.

- A **reachable negative cycle** causes IllegalStateException instead of
  misleading shortest-path values.
- A negative edge in an unreachable component may conservatively trigger
  Bellman-Ford even if the relevant component has no negative weights.
- The same Long overflow and GRAPH_INF distance limits apply as to the
  existing algorithms.

For standalone code, include graph/GraphTypes.kt, graph/GraphResults.kt,
graph/SparseGraph.kt and graph/AutoShortestPaths.kt; the submission generator's
graph bundle automatically includes these files.

Tests compare 600 deterministic random graphs to Floyd-Warshall, covering
unit, binary, nonnegative and negative DAG edge weights.

~~~bash
bash scripts/test-kotlin.sh
~~~
