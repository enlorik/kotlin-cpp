# One-file Kotlin submissions

Competitive-programming judges often require a **single source file**. The
generator selects a tested set of library sources, hoists their imports, removes
duplicate dependencies, then appends your own solution.

Requires **Python 3** for generation; the submitted Kotlin file needs no
Python or repository dependencies.

## Example

Write a normal Kotlin solution in Main.kt:

~~~kotlin
fun main() {
    val values = intArrayOf(2, 4, 6, 8)
    val tree = values.segmentTree()
    println(tree.query(1, 3)) // 10
}
~~~

From the repository root:

~~~bash
python3 tools/build_submission.py --list
python3 tools/build_submission.py \
  --modules segment-tree,fast-io \
  --solution Main.kt \
  --output submission.kt

kotlinc submission.kt -include-runtime -d submission.jar
java -jar submission.jar
~~~

Upload **submission.kt**, not the entire library. The generated output is
deterministic, has a single imports section, and contains no package directive.

## Available modules

| Module | What it provides |
|---|---|
| fast-io | Buffered input and output helpers |
| binary-search | Monotone predicate and sorted-array binary searches |
| advanced-binary-search | Advanced searches plus regular binary search |
| segment-tree | Segment tree, aggregation types and adapters |
| sum-segment-tree | Unboxed Long sum segment tree (standalone; no dependencies) |
| grid-bfs | Grid BFS, directions, and path reconstruction |
| graph | Sparse graph algorithms, automatic shortest paths and result types |
| range-index | Typed/legacy aggregation operations and automatic static/mutable/lazy range indexes |
| range-program | Declarative ordered point updates and range queries (includes range-index) |
| online-range-program | Immediately executing range queries, point assignments and lawful lazy updates |
| primitive-long-lazy | Named Long sum/add/assign algebra with an unboxed lazy tree and online DSL |
| primitive-long-index | Unboxed, capability-selected Long SUM/MIN/MAX/GCD/XOR static/point indexes |
| query-planner | Declarative operation planner selecting static, mutable, lazy, Beats or persistent backends |
| persistent-long-range | Versioned lazy Long sum range add/assign with branch and rollback |
| segment-tree-beats | Unboxed Long Segment Tree Beats with chmin/chmax/add and sum/min/max queries |
| wavelet-matrix | Signed Int rank, kth, frequency and predecessor/successor over static ranges |
| li-chao | Dynamic Long min/max line envelopes, including x-segment insertion |
| heavy-light | Iterative Heavy-Light with generic noncommutative path aggregates and Long path sums |
| heavy-light-graph | Heavy-Light APIs integrated with SparseGraph for undirected trees |
| rerooting-dp | Generic O(n) all-roots tree DP with typed monoids |
| offline-connectivity | Rollback union-find and offline edge add/remove connectivity |
| suffix-automaton | Online substring membership, counts and longest common substrings |
| palindromic-tree | Online eertree of distinct and total palindromes |
| dp-optimization | Monotone divide/conquer DP and convex hull trick |
| game-theory | Grundy numbers, retrograde outcomes and alpha-beta minimax |
| exact-geometry | Exact integer hull, intersections, polygon predicates, diameter, closest pair |
| number-theory | BigInteger-safe modular arithmetic, deterministic Miller-Rabin, Pollard-Rho factorization |
| ntt | Exact modular polynomial convolution under 998244353 |
| kmp-z | Prefix-function, Z-function and KMP match positions |
| aho-corasick | Multi-pattern matching with failure/dictionary suffix links |
| bounded-palindromes | Unboxed block-assign sums, painted string and dynamic bounded-palindrome queries |

Multiple --modules flags or a comma-separated list work. Shared files appear
only once, even when several modules require them.

**Important limitations:** The builder does not discover dependencies for
arbitrary files or prove that custom algebra is lawful. Supported modules are
explicit and tested; an unknown module fails rather than producing a broken
submission. Sources must be in Kotlin's default package and cannot use @file
annotations. If your solution defines a declaration with the same name as a
bundled library declaration, rename it. The tool refuses to overwrite source
or solution input files.

## Test

~~~bash
bash tools/test_submission_builder.sh
~~~

The test suite exercises import hoisting, deduplication, bad inputs and actual
generated Kotlin programs, including **all modules combined**. CI compiles
and executes each generated submission with Kotlin 1.9.25.
