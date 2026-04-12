# CP-Algorithms Manifest for Kotlin Competitive Programming Library

> Comprehensive manifest of all algorithm articles from [cp-algorithms.com](https://cp-algorithms.com),
> mapped to Kotlin implementation targets for this library.
> Machine-readable counterpart: [`tools/cp_algo_manifest.json`](../tools/cp_algo_manifest.json)

---

## Table of Contents

1. [Algebra — Fundamentals](#algebra--fundamentals)
2. [Algebra — Prime Numbers](#algebra--prime-numbers)
3. [Algebra — Modular Arithmetic](#algebra--modular-arithmetic)
4. [Algebra — Number Systems](#algebra--number-systems)
5. [Algebra — Miscellaneous](#algebra--miscellaneous)
6. [Data Structures — Fundamentals](#data-structures--fundamentals)
7. [Data Structures — Advanced](#data-structures--advanced)
8. [Dynamic Programming — Optimizations](#dynamic-programming--optimizations)
9. [Dynamic Programming — Classical](#dynamic-programming--classical)
10. [String Processing — Fundamentals](#string-processing--fundamentals)
11. [String Processing — Advanced](#string-processing--advanced)
12. [Linear Algebra](#linear-algebra)
13. [Geometry](#geometry)
14. [Graphs — Fundamentals](#graphs--fundamentals)
15. [Graphs — Shortest Paths](#graphs--shortest-paths)
16. [Graphs — Minimum Spanning Tree](#graphs--minimum-spanning-tree)
17. [Graphs — Flows and Matching](#graphs--flows-and-matching)
18. [Graphs — Decompositions](#graphs--decompositions)
19. [Graphs — Trees](#graphs--trees)
20. [Graphs — Advanced](#graphs--advanced)
21. [Combinatorics](#combinatorics)
22. [Miscellaneous](#miscellaneous)

---

## Style Legend

| Style | Meaning |
|---|---|
| `plain_function` | Top-level stateless function(s) |
| `stateful_class` | Class with internal mutable state |
| `monoid_structure` | Generic segment-tree-style class parameterised by a monoid |
| `lazy_monoid_structure` | Lazy segment tree with separate lazy tag type |
| `graph_routine` | Graph algorithm (adjacency list / matrix input) |
| `tree_routine` | Tree-specific algorithm |
| `string_routine` | String processing routine |
| `numeric_utility` | Small arithmetic/number-theory helper |
| `dp_template` | DP with a pluggable cost/transition function |
| `offline_algorithm` | Requires all queries upfront |
| `geometry_routine` | Computational geometry primitive |
| `matrix_utility` | Matrix / linear algebra utility |
| `advanced_optional` | Complex algorithm; implement only when needed |

---

## Algebra — Fundamentals

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| GCD and LCM | `math/GCD.kt` | `gcd` | `numeric_utility` | `fun gcd(a: Long, b: Long): Long` | Euclidean; `lcm = a/gcd*b` to avoid overflow; multiple overloads for Int |
| Fibonacci Numbers | `math/Fibonacci.kt` | `fibonacci` | `numeric_utility` | `fun fibonacci(n: Int): Long` | Iterative for small n; matrix exponentiation / fast doubling for large n |
| Binary Exponentiation | `math/BinaryExp.kt` | `binpow` | `numeric_utility` | `fun binpow(base: Long, exp: Long, mod: Long): Long` | Iterative square-and-multiply; generic version for matrices |
| Sieve of Eratosthenes | `math/SieveEratosthenes.kt` | `sieve` | `plain_function` | `fun sieve(n: Int): BooleanArray` | Classic O(n log log n); segmented sieve variant in `SegmentedSieve.kt` |

---

## Algebra — Prime Numbers

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Linear Sieve | `math/LinearSieve.kt` | `linearSieve` | `plain_function` | `fun linearSieve(n: Int): Pair<IntArray, IntArray>` | O(n); returns (primes list, smallest prime factor array) |
| Primality Test (Trial Division) | `math/PrimalityTest.kt` | `isPrime` | `numeric_utility` | `fun isPrime(n: Long): Boolean` | Trial division up to √n |
| Miller-Rabin Primality Test | `math/MillerRabin.kt` | `millerRabin` | `numeric_utility` | `fun millerRabin(n: Long): Boolean` | Deterministic for n < 3.3×10²⁴ with fixed witness set; 128-bit mul via ULong |
| Integer Factorization | `math/Factorize.kt` | `factorize` | `plain_function` | `fun factorize(n: Long): Map<Long, Int>` | Trial division; SPF array for small n; combine with Pollard rho for large n |
| Pollard's Rho Factorization | `math/PollardRho.kt` | `pollardRho` | `plain_function` | `fun pollardRho(n: Long): Long` | Brent's variant; Floyd cycle detection; randomised starting values |
| Divisors of a Number | `math/Divisors.kt` | `divisors` | `plain_function` | `fun divisors(n: Long): List<Long>` | Enumerate from factorisation; divisor count = product of (e+1) |

---

## Algebra — Modular Arithmetic

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Modular Inverse | `math/ModInverse.kt` | `modInverse` | `numeric_utility` | `fun modInverse(a: Long, mod: Long): Long` | Extended Euclidean for general mod; Fermat's little theorem when mod is prime |
| Extended Euclidean Algorithm | `math/ExtGCD.kt` | `extgcd` | `plain_function` | `fun extgcd(a: Long, b: Long): Triple<Long, Long, Long>` | Returns (gcd, x, y) s.t. a·x + b·y = gcd; iterative to avoid stack overflow |
| Chinese Remainder Theorem | `math/CRT.kt` | `crt` | `plain_function` | `fun crt(rem: LongArray, mod: LongArray): Pair<Long,Long>` | Garner's algorithm for non-coprime moduli; returns (x, lcm) |
| Euler's Totient Function | `math/EulerTotient.kt` | `phi` | `numeric_utility` | `fun phi(n: Long): Long` | Single value via factorisation; sieve variant for all values ≤ n |
| Montgomery Multiplication | `math/MontgomeryMul.kt` | `MontgomeryMul` | `stateful_class` | `class MontgomeryMul(mod: Long)` | Avoids BigInteger in Miller-Rabin and Pollard rho; ULong arithmetic |

---

## Algebra — Number Systems

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Gray Code | `math/GrayCode.kt` | `grayCode` | `plain_function` | `fun grayCode(n: Int): Int` | `n ^ (n >> 1)`; inverse via repeated XOR |
| Balanced Ternary | `math/BalancedTernary.kt` | `toBalancedTernary` | `plain_function` | `fun toBalancedTernary(n: Long): IntArray` | Digits in {-1, 0, 1}; useful with ternary search |
| Ternary Search | `math/TernarySearch.kt` | `ternarySearch` | `plain_function` | `fun ternarySearchReal(lo: Double, hi: Double, f: (Double)->Double): Double` | Finds unimodal function extremum; integer and floating-point variants |

---

## Algebra — Miscellaneous

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Stern-Brocot Tree and Farey Sequence | `math/SternBrocot.kt` | `sternBrocot` | `plain_function` | `fun fractionSearch(p: Long, q: Long): List<Pair<Long,Long>>` | Mediant navigation; Farey neighbours via extended Euclidean |
| Josephus Problem | `math/Josephus.kt` | `josephus` | `plain_function` | `fun josephus(n: Int, k: Int): Int` | O(n) recurrence; O(k log n) variant for large n |
| Sprague-Grundy Theorem | `misc/SpragueGrundy.kt` | `grundy` | `plain_function` | `fun grundy(pos: Int, memo: IntArray, moves: (Int)->List<Int>): Int` | MEX of reachable Grundy values; XOR for composite games |

---

## Data Structures — Fundamentals

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Disjoint Set Union (DSU) | `misc/DSU.kt` | `DSU` | `stateful_class` | `class DSU(n: Int) { fun find(x: Int): Int; fun union(x: Int, y: Int): Boolean }` | Path compression + union by rank; rollback variant for offline LCT |
| Sparse Table (RMQ) | `range/SparseTable.kt` | `SparseTable` | `stateful_class` | `class SparseTable<T>(arr, combine)` | O(n log n) build, O(1) idempotent queries; LongArray specialisation |
| Fenwick Tree (BIT) | `range/FenwickTree.kt` | `FenwickTree` | `stateful_class` | `class FenwickTree(n: Int)` | 1-indexed; point update, prefix query; 2D BIT and order-statistic variants |
| Segment Tree | `range/SegTree.kt` | `SegTree` | `monoid_structure` | `class SegTree<T>(n, identity, combine)` | Iterative bottom-up; monoid generic; LongArray specialisation |

---

## Data Structures — Advanced

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Segment Tree with Lazy Propagation | `range/LazySegTree.kt` | `LazySegTree` | `lazy_monoid_structure` | `class LazySegTree<T, F>(n, identity, fIdentity, combine, apply, compose)` | Lazy tag composition must form a monoid; propagate before descent |
| Persistent Segment Tree | `range/PersistentSegTree.kt` | `PersistentSegTree` | `stateful_class` | `class PersistentSegTree(n: Int)` | Path-copying; roots array holds version roots; kth-order statistics offline |
| Sqrt Decomposition | `range/SqrtDecomp.kt` | `SqrtDecomp` | `stateful_class` | `class SqrtDecomp(arr: LongArray)` | Block size ≈ √n; Mo's algorithm built on same principle |
| Treap (Implicit / Explicit) | `range/Treap.kt` | `Treap` | `stateful_class` | `class Treap { fun split(); fun merge() }` | Random priority; implicit treap for sequence operations |
| Heavy-Light Decomposition | `tree/HLD.kt` | `HLD` | `stateful_class` | `class HLD(adj, root)` | Euler-tour flattening; heavy child = largest subtree child; chain heads |
| Centroid Decomposition | `tree/CentroidDecomp.kt` | `CentroidDecomp` | `stateful_class` | `class CentroidDecomp(adj)` | Recursive centroid; centroid tree depth O(log n) |
| 2D Segment Tree | `range/SegTree2D.kt` | `SegTree2D` | `stateful_class` | `class SegTree2D(rows, cols)` | Outer tree on rows; inner dynamic trees on columns; O(n log² n) space |
| Link-Cut Tree | `tree/LinkCutTree.kt` | `LinkCutTree` | `stateful_class` | `class LinkCutTree(n: Int)` | Splay-tree based; access/expose; lazy flip flag; *advanced_optional* |
| Sqrt Tree (O(1) RMQ) | `range/SqrtTree.kt` | `SqrtTree` | `stateful_class` | `class SqrtTree<T>(arr, combine)` | O(n log log n) build, O(1) query; recursive block structure; *advanced_optional* |
| Fractional Cascading | `range/FractionalCascading.kt` | `FractionalCascading` | `stateful_class` | `class FractionalCascading(lists)` | Binary search on k sorted lists in O(k + log n); *advanced_optional* |

---

## Dynamic Programming — Optimizations

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Divide & Conquer DP | `dp/DivConqDP.kt` | `divConqDP` | `dp_template` | `fun divConqDP(n, m, cost): LongArray` | Reduces O(n²m) → O(nm log n) when optimal split is monotone |
| Convex Hull Trick (CHT) | `dp/CHT.kt` | `CHT` | `stateful_class` | `class CHT(maximize: Boolean = false)` | Deque for monotone queries; Li Chao tree for arbitrary queries |
| Li Chao Tree | `dp/LiChaoTree.kt` | `LiChaoTree` | `stateful_class` | `class LiChaoTree(lo: Long, hi: Long)` | Segment tree on x-axis; O(log C) per insert/query |
| Knuth's Optimization | `dp/KnuthOpt.kt` | `knuthDP` | `dp_template` | `fun knuthDP(n, cost): LongArray` | O(n²) interval DP with quadrangle inequality + monotone opt |
| Sum over Subsets (SOS) DP | `dp/SosDP.kt` | `sosDP` | `plain_function` | `fun sosDP(f: LongArray): LongArray` | O(n·2ⁿ) zeta/Möbius transform; superset variant by bit reversal |

---

## Dynamic Programming — Classical

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Longest Increasing Subsequence (LIS) | `dp/LIS.kt` | `lis` | `plain_function` | `fun lis(arr: IntArray): Int` | O(n log n) patience sorting; reconstruct via predecessor array |
| Longest Common Subsequence (LCS) | `dp/LCS.kt` | `lcs` | `plain_function` | `fun lcs(a: String, b: String): Int` | O(nm) DP; O(min(n,m)) space; reduce to LIS for O(n log n) |
| Edit Distance (Levenshtein) | `dp/EditDistance.kt` | `editDistance` | `plain_function` | `fun editDistance(s: String, t: String): Int` | Rolling array O(min) space; Hirschberg for O(n) reconstruction |
| 0-1 Knapsack | `dp/Knapsack.kt` | `knapsack` | `plain_function` | `fun knapsack(weights, values, cap): Long` | Reverse-iteration 1D; unbounded iterates forward; bounded with binary grouping |
| Matrix Chain Multiplication | `dp/MatrixChain.kt` | `matrixChain` | `plain_function` | `fun matrixChain(dims: IntArray): Long` | Interval DP O(n³); Knuth opt applicable |
| Bitmask DP (TSP) | `dp/BitmaskDP.kt` | `tspBitmask` | `dp_template` | `fun tspBitmask(dist): Int` | Archetypal TSP; dp[mask][v]; O(2ⁿ·n²) |
| Digit DP | `dp/DigitDP.kt` | `digitDP` | `dp_template` | `fun digitDP(lo, hi, pred): Long` | Count numbers in range; tight + leading-zero flags; memoised recursion |
| Broken Profile DP | `dp/ProfileDP.kt` | `profileDP` | `dp_template` | `fun tileWays(n: Int, m: Int): Long` | Column bitmask state; canonical example: domino tiling of n×m grid |

---

## String Processing — Fundamentals

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| String Hashing | `string/StringHash.kt` | `StringHash` | `stateful_class` | `class StringHash(s, base, mod)` | Prefix hash array; double hashing reduces collision probability |
| Prefix Function (KMP) | `string/PrefixFunction.kt` | `prefixFunction` | `plain_function` | `fun prefixFunction(s: String): IntArray` | Failure function; concat pattern+'#'+text for search; O(n+m) |
| Z-Function | `string/ZFunction.kt` | `zFunction` | `plain_function` | `fun zFunction(s: String): IntArray` | Z[i] = longest match of s[i..] and s[0..]; equivalent power to KMP |
| Suffix Array | `string/SuffixArray.kt` | `buildSuffixArray` | `plain_function` | `fun buildSuffixArray(s: String): IntArray` | O(n log n) prefix doubling + radix sort; Kasai's LCP array |

---

## String Processing — Advanced

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Suffix Automaton (SAM) | `string/SuffixAutomaton.kt` | `SuffixAutomaton` | `stateful_class` | `class SuffixAutomaton` | Online O(n); suffix links = suffix link tree; endpos equivalence classes |
| Suffix Tree | `string/SuffixTree.kt` | `SuffixTree` | `stateful_class` | `class SuffixTree(s: String)` | Ukkonen's online O(n); prefer SAM in competitive programming; *advanced_optional* |
| Aho-Corasick Automaton | `string/AhoCorasick.kt` | `AhoCorasick` | `stateful_class` | `class AhoCorasick` | Trie + BFS failure links; dictionary links for overlapping matches |
| Manacher's Algorithm | `string/Manacher.kt` | `manacher` | `plain_function` | `fun manacher(s: String): IntArray` | '#'-separator trick for unified odd/even; O(n); p[i] = palindrome radius |
| Palindromic Tree (Eertree) | `string/PalindromicTree.kt` | `PalindromicTree` | `stateful_class` | `class PalindromicTree` | Two root nodes (len -1, 0); suffix links; online O(n α) |

---

## Linear Algebra

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Gaussian Elimination | `math/GaussElim.kt` | `gaussElim` | `plain_function` | `fun gaussElim(a: Array<DoubleArray>): DoubleArray?` | Partial pivoting; mod version for integer systems; null if no unique solution |
| Matrix Rank | `math/MatrixRank.kt` | `matrixRank` | `plain_function` | `fun matrixRank(a: Array<DoubleArray>): Int` | Gaussian with early stop; bitset version O(n²m/64) for GF(2) |
| Matrix Exponentiation | `math/MatrixExp.kt` | `matPow` | `plain_function` | `fun matPow(m, n: Long, mod: Long): Array<LongArray>` | Square matrices; identity as neutral element; linear recurrences |
| Berlekamp-Massey | `math/BerlekampMassey.kt` | `berlekampMassey` | `plain_function` | `fun berlekampMassey(s: LongArray, mod: Long): LongArray` | Minimal linear recurrence from sequence; combine with Cayley-Hamilton for kth term |
| Fast Fourier Transform (FFT) | `math/FFT.kt` | `fft` | `plain_function` | `fun fft(a: Array<Complex>, invert: Boolean)` | Cooley-Tukey iterative; bit-reversal; floating-point precision issues |
| Number Theoretic Transform (NTT) | `math/NTT.kt` | `ntt` | `plain_function` | `fun ntt(a: LongArray, invert: Boolean, mod: Long, g: Long)` | Mod 998244353 (g=3); arbitrary mod via CRT of three NTT primes |
| Polynomial Operations | `math/Poly.kt` | `Poly` | `stateful_class` | `class Poly(coeffs, mod)` | Multiply, inv, div, mod, sqrt, ln, exp — all via NTT; Newton iteration |

---

## Geometry

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Basic Geometry Primitives | `misc/Geometry.kt` | `Point` | `geometry_routine` | `data class Point(x: Double, y: Double)` | cross, dot, dist; PointL (Long) for exact integer geometry; epsilon comparison |
| Convex Hull | `misc/ConvexHull.kt` | `convexHull` | `geometry_routine` | `fun convexHull(points: List<Point>): List<Point>` | Andrew's monotone chain O(n log n); collinear point handling flag |
| Polygon Area (Shoelace) | `misc/PolygonArea.kt` | `polygonArea` | `geometry_routine` | `fun polygonArea(poly: List<Point>): Double` | Shoelace formula; `area * 2` as Long for exact integer result |
| Point in Polygon | `misc/PointInPolygon.kt` | `pointInPolygon` | `geometry_routine` | `fun pointInPolygon(p, poly): Int` | Ray casting; winding number; returns -1/0/1 (outside/boundary/inside) |
| Line Intersection | `misc/LineIntersection.kt` | `lineIntersect` | `geometry_routine` | `fun lineIntersect(p1, p2, p3, p4): Point?` | Cramer's rule; collinear overlap check for segment intersection |
| Minimum Enclosing Circle | `misc/MinEnclosingCircle.kt` | `minEnclosingCircle` | `geometry_routine` | `fun minEnclosingCircle(pts: MutableList<Point>): Pair<Point, Double>` | Welzl randomised O(n) expected; shuffle first; circumcircle base case |
| Rotating Calipers | `misc/RotatingCalipers.kt` | `diameter` | `geometry_routine` | `fun diameter(hull: List<Point>): Double` | O(n) on convex hull; antipodal pairs; min bounding box variant |
| Half-Plane Intersection | `misc/HalfPlaneIntersection.kt` | `halfPlaneIntersection` | `geometry_routine` | `fun halfPlaneIntersection(planes: List<HalfPlane>): List<Point>` | Sort by angle; deque O(n log n); returns convex polygon or empty |

---

## Graphs — Fundamentals

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Breadth-First Search (BFS) | `graph/BFS.kt` | `bfs` | `graph_routine` | `fun bfs(adj, start: Int): IntArray` | ArrayDeque queue; dist array (-1 = unreachable); grid 4/8-directional variant |
| Depth-First Search (DFS) | `graph/DFS.kt` | `dfs` | `graph_routine` | `fun dfs(adj, start: Int, visited, action: (Int)->Unit)` | Iterative with explicit stack; pre/post-order hooks |
| Topological Sort | `graph/TopoSort.kt` | `topoSort` | `graph_routine` | `fun topoSort(adj): IntArray?` | Kahn's BFS-based (null on cycle); DFS finish-time variant |
| Connected Components | `graph/ConnectedComponents.kt` | `connectedComponents` | `graph_routine` | `fun connectedComponents(adj): IntArray` | BFS/DFS from each unvisited; returns component ID array |
| Bipartite Check | `graph/BipartiteCheck.kt` | `isBipartite` | `graph_routine` | `fun isBipartite(adj): BooleanArray?` | 2-coloring via BFS; null if odd cycle |

---

## Graphs — Shortest Paths

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Dijkstra's Algorithm | `graph/Dijkstra.kt` | `dijkstra` | `graph_routine` | `fun dijkstra(adj, start: Int): LongArray` | PriorityQueue lazy deletion; no negative edges; prev array for path |
| Bellman-Ford | `graph/BellmanFord.kt` | `bellmanFord` | `graph_routine` | `fun bellmanFord(n, edges, start): LongArray?` | n-1 rounds + nth round for negative cycle detection |
| Floyd-Warshall | `graph/FloydWarshall.kt` | `floydWarshall` | `graph_routine` | `fun floydWarshall(dist): Boolean` | O(n³) all-pairs; in-place; false on negative cycle |
| 0-1 BFS | `graph/BFS01.kt` | `bfs01` | `graph_routine` | `fun bfs01(adj, start: Int): IntArray` | Deque: front for 0-weight, back for 1-weight; O(V+E) |
| SPFA | `graph/SPFA.kt` | `spfa` | `graph_routine` | `fun spfa(adj, start: Int): LongArray?` | Queue-based Bellman-Ford; SLF heuristic; null on negative cycle |
| Shortest Path in DAG | `graph/DAGShortestPath.kt` | `dagShortestPath` | `graph_routine` | `fun dagShortestPath(adj, start: Int): LongArray` | Topological relaxation O(V+E); handles negative edges |

---

## Graphs — Minimum Spanning Tree

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Kruskal's MST | `graph/Kruskal.kt` | `kruskal` | `graph_routine` | `fun kruskal(n, edges): Pair<Long, List<*>>` | Sort edges + DSU; returns (cost, edge list) |
| Prim's MST | `graph/Prim.kt` | `prim` | `graph_routine` | `fun prim(adj): Long` | PriorityQueue O(E log V); better on dense graphs |
| Borůvka's MST | `graph/Boruvka.kt` | `boruvka` | `graph_routine` | `fun boruvka(n, edges): Long` | O(E log V) phases; useful in parallel MST algorithms |

---

## Graphs — Flows and Matching

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Dinic's Max Flow | `graph/Dinic.kt` | `Dinic` | `stateful_class` | `class Dinic(n: Int)` | BFS level graph + DFS blocking flow; O(V²E), O(E√V) for unit graphs |
| Min-Cost Max Flow (MCMF) | `graph/MCMF.kt` | `MCMF` | `stateful_class` | `class MCMF(n: Int)` | Dijkstra with potentials (Johnson's trick); augment along min-cost paths |
| Kuhn's Bipartite Matching | `graph/KuhnMatching.kt` | `kuhn` | `graph_routine` | `fun kuhn(adjL, sizeR: Int): IntArray` | Augmenting path DFS O(VE); returns matching for right side |
| Hungarian Algorithm | `graph/Hungarian.kt` | `hungarian` | `graph_routine` | `fun hungarian(cost): Pair<Long, IntArray>` | O(n³) for n×n matrix; returns (min cost, assignment) |
| Blossom (General Matching) | `graph/Blossom.kt` | `blossom` | `graph_routine` | `fun blossom(adj): IntArray` | Edmonds' blossom contraction O(V³); general undirected; *advanced_optional* |

---

## Graphs — Decompositions

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Bridges and Bridge Tree | `graph/Bridges.kt` | `findBridges` | `graph_routine` | `fun findBridges(adj): List<Pair<Int,Int>>` | Tarjan's tin/low; iterative DFS; bridge tree = 2-edge-CC condensation |
| Articulation Points and Block-Cut Tree | `graph/ArticulationPoints.kt` | `findArticulationPoints` | `graph_routine` | `fun findArticulationPoints(adj): List<Int>` | Root ≥2 children condition; block-cut tree is bipartite |
| SCC — Kosaraju | `graph/Kosaraju.kt` | `kosaraju` | `graph_routine` | `fun kosaraju(adj): IntArray` | Two DFS passes: finish order then reverse graph |
| SCC — Tarjan | `graph/TarjanSCC.kt` | `tarjanSCC` | `graph_routine` | `fun tarjanSCC(adj): Pair<IntArray, Int>` | Single DFS + stack; low-link; SCCs in reverse topo order |
| 2-SAT | `graph/TwoSAT.kt` | `TwoSAT` | `stateful_class` | `class TwoSAT(n: Int)` | Implication graph; satisfiable iff no var in same SCC as its negation |

---

## Graphs — Trees

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| LCA — Binary Lifting | `tree/LCA.kt` | `LCA` | `stateful_class` | `class LCA(adj, root)` | O(n log n) preprocess; O(log n) query; weighted dist support |
| LCA — Euler Tour + RMQ | `tree/LCA_RMQ.kt` | `LCA_RMQ` | `stateful_class` | `class LCA_RMQ(adj, root)` | Euler tour length 2n-1; SparseTable gives O(1) query |
| Tree Diameter | `tree/TreeDiameter.kt` | `treeDiameter` | `tree_routine` | `fun treeDiameter(adj): Long` | Two BFS/DFS; farthest-from-farthest approach |
| Tree Isomorphism | `tree/TreeIsomorphism.kt` | `treeHash` | `tree_routine` | `fun treeHash(adj, root): Long` | AHU / polynomial hashing of sorted children hashes; centroid for unrooted |
| Tree DP (Re-rooting) | `tree/TreeDP.kt` | `reRootingDP` | `tree_routine` | `fun reRootingDP(adj, leafValue, merge): LongArray` | Two DFS: downward dp, then upward contribution; O(n) |

---

## Graphs — Advanced

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Euler Path and Circuit | `graph/EulerPath.kt` | `eulerPath` | `graph_routine` | `fun eulerPath(adj): List<Int>?` | Hierholzer's O(E); degree condition check; directed and undirected variants |
| Hamiltonian Path (Bitmask DP) | `graph/HamiltonianPath.kt` | `hamiltonianPath` | `graph_routine` | `fun hamiltonianPath(adj): Boolean` | O(2ⁿ·n²) bitmask DP; feasible only for n ≤ 20 |

---

## Combinatorics

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Binomial Coefficients | `math/BinomialCoeff.kt` | `nCr` | `numeric_utility` | `fun nCr(n, r, mod): Long` | Precompute factorial + inverse factorial; Lucas' theorem for large n mod small prime |
| Catalan Numbers | `math/Catalan.kt` | `catalan` | `numeric_utility` | `fun catalan(n: Int, mod: Long): Long` | C(2n,n)/(n+1); DP table for sequence |
| Inclusion-Exclusion | `math/InclusionExclusion.kt` | `inclusionExclusion` | `plain_function` | `fun inclusionExclusion(sets, intersections): Long` | Bitmask enumeration; sign = (−1)^(|S|+1) |
| Burnside's Lemma | `math/Burnside.kt` | `burnside` | `plain_function` | `fun burnside(groupOps, n, mod): Long` | Count fixed points per group element; Polya enumeration generalisation |
| Möbius Function and Inversion | `math/Mobius.kt` | `mobiusSieve` | `plain_function` | `fun mobiusSieve(n: Int): IntArray` | Sieve μ values; Dirichlet convolution / Möbius inversion |

---

## Miscellaneous

| Title | Target File | Symbol | Style | API Shape | Notes |
|---|---|---|---|---|---|
| Binary Search (Predicate) | `misc/BinarySearch.kt` | `binarySearch` | `plain_function` | `fun binarySearchInt(lo, hi, check): Int` | First true / last false; overflow-safe mid; real-valued variant |
| Coordinate Compression | `misc/CoordCompress.kt` | `compress` | `plain_function` | `fun <T: Comparable<T>> compress(arr): IntArray` | Sort + deduplicate + binary search; keeps inverse mapping array |
| Merge Sort / Inversion Count | `misc/MergeSort.kt` | `mergeSort` | `plain_function` | `fun mergeSort(arr: IntArray): Long` | Returns inversion count during merge; O(n log n); also via BIT |
| Mo's Algorithm | `misc/MosAlgorithm.kt` | `mo` | `offline_algorithm` | `fun mo(queries, add, remove, answer): LongArray` | Block size √n; alternating sort; O((n+q)√n); Mo with updates adds time dim |
| Scheduling (Greedy) | `misc/Scheduling.kt` | `scheduleDeadlines` | `plain_function` | `fun scheduleDeadlines(jobs): Int` | Profit-sorted + latest-slot DSU; job scheduling with deadlines |
| Game Theory (Nim / Grundy) | `misc/Nim.kt` | `nimValue` | `plain_function` | `fun nimWins(piles: IntArray): Boolean` | XOR of pile sizes; Sprague-Grundy for misère; Wythoff's extension |
| Segmented Sieve | `math/SegmentedSieve.kt` | `segmentedSieve` | `plain_function` | `fun segmentedSieve(lo: Long, hi: Long): BooleanArray` | Sieve √hi small primes; mark composites in block; O((hi-lo) log log hi) |
| Discrete Logarithm (BSGS) | `math/DiscreteLog.kt` | `discreteLog` | `plain_function` | `fun discreteLog(a, b, mod): Long?` | Baby-step Giant-step O(√mod); extended BSGS for non-coprime mod |
| Primitive Root | `math/PrimitiveRoot.kt` | `primitiveRoot` | `plain_function` | `fun primitiveRoot(p: Long): Long` | Try candidates; g^(φ/p) ≠ 1 for all prime factors p of φ |

---

## Summary Statistics

| Category | # Articles |
|---|---|
| Algebra (Fundamentals + Primes + Modular + Number Systems + Misc) | 21 |
| Data Structures (Fundamentals + Advanced) | 12 |
| Dynamic Programming (Optimizations + Classical) | 12 |
| String Processing (Fundamentals + Advanced) | 9 |
| Linear Algebra | 7 |
| Geometry | 8 |
| Graphs (all subcategories) | 31 |
| Combinatorics | 5 |
| Miscellaneous | 12 |
| **Total** | **117** |

---

*Generated for the `kotlin-cpp` library. See `tools/cp_algo_manifest.json` for machine-readable metadata.*
