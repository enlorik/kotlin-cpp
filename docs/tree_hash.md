# Rooted trees and rolling hashes

```kotlin
val tree = graph.rootedTree(root)
val diameter = tree.diameter()
val centroidParent = tree.centroidParents()
val best = tree.connectedKnapsack(weights, capacity)
val compressed = graph.virtualTreeIndex(root).build(marked)
val hash = text.rollingHash()
val equal = hash.equalVerified(l, r, hash, a, b)
```

RootedTreeToolkit validates a parent array, snapshots it, and traverses
iteratively. Diameter counts edges (unweighted), O(n). Centroid decomposition
returns the centroid-tree parent of each original vertex, O(n log n) time
and O(n) space. Connected knapsack maximizes weight for each exact cardinality
of a connected subset containing the root (with optional empty subset).
Capacity is capped at n; O(n K^2) time and O(n K) space upper bounds. Long
addition is checked; overflow throws, including intermediate candidates.
This is not arbitrary-weight or disconnected knapsack.

SparseGraph bridges validate an undirected tree and ignore edge weights.
rerootingTree takes an existing Monoid and finish/lift callbacks; neighbor
order is induced by ascending child IDs in the chosen rooted tree, not
edge insertion order. For noncommutative monoids this order is observable.

RollingHash uses two fixed public primes and a base for O(1) length-aware
half-open substring fingerprints after O(n) setup. No probability guarantee
against adversarial input is claimed. probablyEqual is a fingerprint test;
equalVerified verifies matching fingerprints with direct character comparison
(O(length) in the worst case). UTF-16 code units, not grapheme clusters.
No dynamic updates. Use suffix arrays for exact indexed LCP queries.

Standalone modules: tree-toolkit, tree-toolkit-graph, rolling-hash.
