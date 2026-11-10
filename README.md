# kotlin-cpp

- **DP and adversarial games:** [docs/dp_and_games.md](docs/dp_and_games.md) adds monotone divide/conquer DP, lower convex hull trick, Sprague-Grundy, retrograde outcomes and minimax.

- **Online string algorithms:** [docs/advanced_strings.md](docs/advanced_strings.md) covers suffix automata and palindromic trees with membership, occurrences and counts.

- **Rerooting tree DP and rollback connectivity:** [docs/tree_dp_connectivity.md](docs/tree_dp_connectivity.md) adds generic all-roots DP, rollback DSU, and offline add/remove connectivity.

- **Full competitive-programming dream roadmap:** [seven territories, complete checklist and verification standards](docs/competitive_programming_dream.md).

- **Advanced algorithm DSL:** [docs/advanced_algorithms.md](docs/advanced_algorithms.md) introduces signed Wavelet Matrix, Li Chao line optimization and iterative Heavy-Light Decomposition with order-preserving monoids.

- **Declarative query planner:** [docs/query_planner.md](docs/query_planner.md) chooses the data structure from required operations and named algebra.
- **Persistent history and Segment Tree Beats:** [docs/advanced_range_structures.md](docs/advanced_range_structures.md) supports rollback, branching and conditional range clamps.
- **Performance guardrails:** [docs/query_planner.md](docs/query_planner.md#performance-regression-detection) checks identical deterministic workloads against generic backends.

- **Builtin Long algebra:** [docs/primitive_long_reductions.md](docs/primitive_long_reductions.md) selects unboxed static or point-update backends for named SUM/MIN/MAX/GCD/XOR operations.
- **Batched local recomputation:** [docs/primitive_long_reductions.md](docs/primitive_long_reductions.md#batched-local-recomputation) explains `BlockAssignSum.recompute` and `setMany`, avoiding per-position tree traversal.

- **Primitive Long lazy sums:** [docs/primitive_long_lazy.md](docs/primitive_long_lazy.md) lets named `LongSumMonoid` + `LongAddAction`/`LongAssignAction` select an unboxed O(log n) range-update tree.

- **Online range DSL:** [docs/online_range_program.md](docs/online_range_program.md) adds `onlineRangeProgram()` for immediate queries that control later point or lazy range updates.

- **Fast bounded palindrome DSL:** [docs/bounded_palindromes.md](docs/bounded_palindromes.md) adds `s.boundedPalindromes(k)` with online `assign`/`count`, backed by primitive block sums and lazy painted strings; [DSL roadmap](docs/dsl_roadmap.md) tracks the remaining gaps.

Reusable Kotlin competitive-programming algorithms and fast I/O.

- **Declarative range programs:** [docs/range_program.md](docs/range_program.md) lets you write ordered `query`/`set` blocks and automatically selects static or mutable indexes.
- **Unboxed sum segment tree:** [docs/primitive_sum_segment_tree.md](docs/primitive_sum_segment_tree.md) adds a fast opt-in `a.sumSegmentTree()` for LongArray and IntArray without breaking arbitrary aggregation APIs.
- **Kotlin/C++ performance baseline:** [docs/segment_tree_benchmark.md](docs/segment_tree_benchmark.md) gives a deterministic, checksum-verified point-update/range-sum segment tree comparison using the real Kotlin library and optimized C++.
- **Automatic shortest paths:** [docs/automatic_shortest_paths.md](docs/automatic_shortest_paths.md) explains graph.shortestPaths(source), which selects BFS, 0-1 BFS, Dijkstra or Bellman-Ford by edge weights.
- **Standalone contest submissions:** [docs/standalone_submissions.md](docs/standalone_submissions.md) explains how to generate one Kotlin `.kt` file from selected, tested library modules using `python3 tools/build_submission.py --list`.
- **Automatic custom-operation range index:** [docs/range_index.md](docs/range_index.md) introduces `a.rangeIndex(op)` (static sparse/disjoint), `a.rangeIndex(op, mutable=true)` (point updates), and `a.rangeIndex(op, action)` (lazy range updates). Run `bash tools/test_range_index.sh` and optional `bash tools/benchmark_range_index.sh`.
- **Offline/persistent ranges:** [docs/range_offline_persistent.md](docs/range_offline_persistent.md) introduces `moQueries()`, `moDistinctCounts()`, and persistent historical segment trees.
- **Lazy and static range structures:** [docs/range_advanced.md](docs/range_advanced.md) covers `lazySegmentTree()`, idempotent `sparseTable()`, and general `disjointSparseTable()`.
- **Range basics:** [docs/range_basics.md](docs/range_basics.md) provides `prefixSums()`, `differenceArray()`, and `fenwickTree()` for fast range operations.
- **Advanced binary search:** [docs/advanced_binary_search.md](docs/advanced_binary_search.md) covers `firstTrueFrom`, `bisect`, `bisectRoot`, and `partitionPoint`.
- **Fractional cascading:** [docs/fractional_cascading.md](docs/fractional_cascading.md) preprocesses sorted Int catalogs for all lower/upper bounds in O(log N + K) per query.
- **Parallel binary search:** [docs/parallel_binary_search.md](docs/parallel_binary_search.md) groups multiple monotone queries by midpoint for efficient offline sweeps.
- **Type-safe algebra:** [docs/typed_algebra.md](docs/typed_algebra.md) introduces `monoid`, `idempotentMonoid`, `group`, `action`, compile-time capability requirements and seeded `checkLaws` with counterexamples.
- **Any monoid: prefixes and range assignment:** [docs/custom_algebra.md](docs/custom_algebra.md) adds `prefixAggregates(op).prefix(until)` and `lazySegmentTree(op).set(from, until, value)` without assuming an inverse.
- **Custom algebra:** [docs/custom_algebra.md](docs/custom_algebra.md) introduces `aggregation(identity) { ... }` and `withInverse { ... }` to reuse invented operations across segment trees, sparse tables, Fenwick, prefix/difference arrays, Mo, and lazy/persistent trees when mathematically valid.
- **Operation-driven range APIs:** [docs/operation_driven_range_apis.md](docs/operation_driven_range_apis.md) lets Fenwick SUM/XOR, persistent SUM/MIN/MAX/XOR and configurable lazy ADD/ASSIGN use shared operations safely.
- **Shared aggregation ops:** `shared/AggregationOp.kt` defines `AggregateOp`, `IntAggregations` and `LongAggregations` once for grids, graphs and trees. No graph source needed for a segment tree.
- **Segment tree prefix search:** [docs/segment_tree.md](docs/segment_tree.md) introduces `a.segmentTree()`, `tree.query`, `tree.set` and `tree.findPrefix` in O(log n).
- **Binary search:** [docs/binary_search.md](docs/binary_search.md) explains `firstTrue`, `lastTrue`, `lowerBound`, and `upperBound` for Int/Long ranges and sorted arrays, with overflow-safe midpoints.

- **Unified aggregation vocabulary:** [docs/unified_aggregate.md](docs/unified_aggregate.md) demonstrates matching `Nearest`, `Radius`, `Neighbors`, and `op` names for both grids and graphs.
- **Grid aggregation:** [docs/grid_aggregate.md](docs/grid_aggregate.md) explains `grid.aggregate(...)` with `Radius`, `Offsets`, and `Nearest`.
- **Graph aggregation:** [docs/graph_aggregate.md](docs/graph_aggregate.md) documents `g.aggregate(...)` with matching `Nearest`, `Radius`, `Neighbors` selectors (and legacy graph-specific names), including fast OR, safe distinct SUM and directed graphs.
- **Graph input DSL:** [docs/graph_input_dsl.md](docs/graph_input_dsl.md) lets you declare edge columns, directed/indexing flags, and value arrays in their true input order via `readGraph { ... }` or `graph { ... }`.
- **Graph I/O:** `readGraph(n, m)` reads edge lists; `printValues(values)` prints graph results.
- **Fast I/O:** [docs/fast_io.md](docs/fast_io.md) introduces `readInts()`, `readIntGrid(h, w)` and `printGrid(h, w) { r, c -> ... }`.
- **Full example:** [examples/NearestOrSolution.kt](examples/NearestOrSolution.kt) solves the nearest-tied nonzero bitwise OR problem in a short `main()`.

```kotlin
fun main() {
    val (h, w) = readInts()
    val grid = readIntGrid(h, w)
    val result = grid.aggregate(
        selection = Nearest(sources = { r, c -> grid[r][c] != 0 }, maxDistance = 5),
        op = IntAggregations.OR
    )
    printGrid(h, w) { r, c ->
        if (grid[r][c] != 0) -1 else result.values[r][c]
    }
}
```

For a standalone judge file using the shared vocabulary, include `io/FastIO.kt`, `grid/GridAggregate.kt`, `grid/GridBfs.kt`, `graph/GraphAggregate.kt`, the base graph types, and `shared/UnifiedAggregate.kt`.

Graph-version example: [examples/GraphNearestOrSolution.kt](examples/GraphNearestOrSolution.kt). This uses `readGraph(n, m)`, `GraphNearest` and `IntAggregations.OR` to calculate the nearest-tied source OR in O(V+E).

For the shared grid/graph selector names and grid `op = IntAggregations.SUM` calls, include `shared/UnifiedAggregate.kt` along with both aggregation engines in the compiled source bundle.

### Describe the input format

```kotlin
val g = readGraph {
    header()               // reads n m
    edges { from(); to() } // reads m pairs
    values()               // n vertex values after the edges
}
val answer = g.aggregate(
    selection = Nearest(sources = { v -> g.values[v] != 0 }),
    op = IntAggregations.OR
)
printValues(g.n) { v -> if (g.values[v] == 0) answer.values[v] else -1 }
```

For input with values before edges, move `values()` above `edges()`. For `u v w`, write `edges { from(); to(); weight() }`.

### Binary search without boilerplate

```kotlin
val a = intArrayOf(2, 4, 4, 4, 7)
val first = a.lowerBound(4)     // 1
val after = a.upperBound(4)     // 4
val count = after - first       // 3

val firstNine = firstTrue(a.size) { i -> a[i] >= 9 } // a.size when absent
val lastSmall = lastTrue(a.size) { i -> a[i] < 7 }   // 3
val bigAnswer = firstTrue(0L, 1_000_000_000_000_000_000L) { x -> feasible(x) }
```

For standalone submissions, add only `search/BinarySearch.kt` to your solution. It does not require the graph/grid modules.

### Friendlier segment-tree syntax

```kotlin
val tree = intArrayOf(2, 4, 6, 8, 10).segmentTree(IntAggregations.SUM)
val before = tree[1..3]       // 4 + 6 + 8 = 18 (inclusive Kotlin range)
tree[2] = 100                // point update
val after = tree[1..3]        // 4 + 100 + 8 = 112
val index = tree.findPrefix { it >= 110 } // 3, when prefix values are monotone
```

For these calls, include `shared/AggregationOp.kt`, `tree/SegmentTree.kt`, and `shared/SegmentTreeAggregations.kt`. No graph/grid dependency. All traditional `tree.query(from, until)` calls retain half-open bounds.

### Run every range-query regression suite

From the repository root, with `kotlinc` (Kotlin 1.9+) and Java available:

```bash
bash tools/test_range_queries.sh
```

This runs the five standalone test programs and compiles the new modules
together, without requiring the graph/grid code.

### Match each range algorithm to its mathematics

```kotlin
val a = intArrayOf(2, 4, 6, 8)

val minimum = a.segmentTree(IntAggregations.MIN)
val fenwick = a.fenwickTree(IntAggregations.XOR)
val history = a.persistentSegmentTree(IntAggregations.MAX)
val lazy = a.lazySegmentTree {
    aggregate = IntAggregations.MIN
    updates = RangeUpdates.ADD
}
lazy.add(1, 4, 5)
println(lazy.query(0, 4)) // 2
```

`a.fenwickTree()`, `a.lazySegmentTree()`, and
`a.persistentSegmentTree()` continue to use the legacy Long-sum APIs.
For supported combinations and explicit custom algebra see the
[operation-driven API guide](docs/operation_driven_range_apis.md).
Validate with `bash tools/test_operation_apis.sh`.
