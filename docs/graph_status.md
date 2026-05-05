# Graph Library Status

## Files

| File | Description |
|------|-------------|
| `graph/GraphTypes.kt` | Shared types: `GRAPH_INF`, `Edge`, `DSU` |
| `graph/GraphResults.kt` | Result types: `ShortestPathResult`, `MSTResult`, `SCCResult`, `BridgeArtResult` |
| `graph/SparseGraph.kt` | Adjacency-list graph (directed or undirected) |
| `graph/DenseGraph.kt` | Adjacency-matrix graph |
| `graph/FlowGraph.kt` | Network flow: `FlowGraph`, `MCFlowGraph` |
| `graph/BipartiteGraph.kt` | Bipartite matching: `BipartiteGraph`, `hungarianMinCost` |
| `graph/TwoSAT.kt` | 2-SAT solver |

---

## Implemented Algorithms

### SparseGraph

| Algorithm | Method | Notes |
|-----------|--------|-------|
| BFS (unit weights) | `bfs(src)` | Returns `ShortestPathResult` |
| Multi-source BFS | `multiBfs(sources)` | Distance to nearest source |
| Dijkstra | `dijkstra(src)` | Non-negative weights; O((V+E) log V) |
| Multi-source Dijkstra | `multiDijkstra(sources)` | |
| Bellman-Ford | `bellmanFord(src)` | Negative weights; returns `(result, hasNegCycle)` |
| SPFA | `spfa(src)` | BFS-accelerated Bellman-Ford |
| 0-1 BFS | `zeroOneBfs(src)` | Weights ∈ {0,1}; uses ArrayDeque |
| Topological sort | `topoSort()` | Kahn's BFS; null if cycle exists |
| SCC (Kosaraju) | `scc()` | Two-pass iterative DFS |
| SCC (Tarjan) | `tarjanSCC()` | Single-pass iterative DFS |
| Bridges + APs | `bridgesAndArticulationPoints()` | Undirected only |
| Prim MST | `primMST()` | Undirected; null if disconnected |
| Kruskal MST | `kruskalMST()` | Undirected; null if disconnected |
| Negative cycle | `negativeCycle()` | Returns a cycle vertex list or null |
| Second-best MST | `secondBestMST()` | Kruskal + LCA max-edge; null if not applicable |
| Euler circuit | `eulerCircuit(start)` | Returns null if conditions not met |
| Euler path | `eulerPath(start?)` | Auto-detects start if null; returns null if not applicable |
| Connected components | `connectedComponents()` | Undirected only; throws for directed |
| Weak connected components | `weakConnectedComponents()` | Directed graphs (edges treated as undirected) |
| Bipartite check | `isBipartite()` | Returns `(isBipartite, coloring)` |
| LCA (binary lifting) | `buildLCA(root)` + `lca(u,v)` | Dynamic LOG from n; O(V log V) prep |
| Tree distance | `treeDist(u, v, dist)` | Requires LCA + precomputed dist array |
| Tree diameter | `treeDiameter()` | Unweighted; two BFS passes |
| Weighted tree diameter | `weightedTreeDiameter()` | Two Dijkstra passes |
| DFS pre-order | `dfsOrder(root)` | Iterative |

### DenseGraph

| Algorithm | Method | Notes |
|-----------|--------|-------|
| Floyd-Warshall APSP | `floydWarshall()` | Returns new distance matrix; O(n³) |
| Negative cycle check | `hasNegativeCycle(d)` | On Floyd-Warshall result |
| Transitive closure | `transitiveClosure()` | Boolean reachability matrix; O(n³) |

### FlowGraph (Dinic's algorithm)

| Algorithm | Method | Notes |
|-----------|--------|-------|
| Max flow | `maxFlow(s, t)` | O(V² · E); O(E√V) for unit caps |
| Min-cut side | `minCutSide(s)` | Call after `maxFlow` |
| Min-cut edges | `minCutEdges(s)` | Cut edges with original capacities |

### MCFlowGraph (Min-cost max-flow)

| Algorithm | Method | Notes |
|-----------|--------|-------|
| Min-cost flow | `minCostFlow(s, t, maxFlow?)` | SPFA augmentation; handles negative costs |
| Min-cost max-flow | `minCostMaxFlow(s, t)` | Alias for `minCostFlow(s, t)` |

### BipartiteGraph

| Algorithm | Method | Notes |
|-----------|--------|-------|
| Max bipartite matching | `maxMatching()` | Hopcroft-Karp; O(E√V) |
| Min vertex cover | `minVertexCover(matchL)` | König's theorem |
| Max independent set | `maxIndependentSet(matchL)` | Complement of min vertex cover |

### Top-level functions

| Algorithm | Signature | Notes |
|-----------|-----------|-------|
| Hungarian min-cost matching | `hungarianMinCost(cost)` | n×n cost matrix; O(n³) |

### TwoSAT

| Method | Description |
|--------|-------------|
| `addImplication(a, aval, b, bval)` | If (a==aval) then (b==bval) |
| `addOr(a, aval, b, bval)` | (a==aval) OR (b==bval) |
| `addXor(a, aval, b, bval)` | Exactly one of (a==aval), (b==bval) |
| `force(a, aval)` | Force variable a to aval |
| `solve()` | Returns `BooleanArray?`; null if UNSAT |

---

## Known Caveats

- **Bridges/APs with parallel edges**: `bridgesAndArticulationPoints()` may give incorrect results for multigraphs (multiple edges between the same pair of vertices). Use edge-id tracking if needed.
- **Euler circuit for multigraphs**: the internal edge-id pairing assigns id by first-match; correct for simple graphs and ordinary multigraphs.
- **SPFA negative-cycle detection**: stops at the first detected relaxation after n iterations; may not enumerate all affected vertices.
- **secondBestMST**: returns null if no *strictly better* second-best tree exists (all spanning trees are equally weighted).
- **LCA** works on forests; each connected tree must have `buildLCA(root)` called with its root. Vertices in unvisited components have depth -1.
- **FlowGraph DFS block** is recursive; for very deep level-graphs run in a thread with a larger stack.
- **MCFlowGraph** with negative-cost edges may be slow on adversarial inputs; potential optimization: use Johnson's reweighting for Dijkstra augmentation.

---

## Intentionally Not Implemented

The following algorithms are out of scope for this library (no stubs or placeholders):

| Algorithm | Reason |
|-----------|--------|
| Online bridges (link-cut trees) | Requires complex persistent data structures |
| Strong orientation | Niche; not commonly needed in contests |
| D'Esopo-Pape shortest paths | SPFA/Bellman-Ford cover the same use-case |
| Fixed-length path counting | Graph matrix exponentiation; separate utility |
| Kirchhoff's theorem (matrix-tree) | Requires linear algebra; separate utility |
| Edmonds-Karp max-flow | Subsumed by Dinic's algorithm |
| Push-relabel max-flow | More complex; Dinic is sufficient for most contests |
| MPM max-flow | Complex; rarely needed over Dinic |
| Flows with lower bounds | Complex API; special-purpose |
| Tarjan offline LCA | Superseded by online binary-lifting LCA |
| Heavy-light decomposition (HLD) | Tree path queries; planned for a future module |
| Centroid decomposition | Tree distance queries; planned for a future module |
| Tree painting / virtual tree | Niche tree technique |
| Prüfer code | Prufer sequence encoding/decoding |
