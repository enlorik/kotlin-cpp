# Virtual trees, SMAWK and formal power series

- VirtualTreeIndex(parents): one -1 root, all other vertices have a parent.
  Iterative DFS and binary lifting, O(n log n) preprocessing and memory.
  lca is O(log n); build(terminals) returns at most 2k-1 vertices, including
  required LCAs, with compressed parent indices and original edge distances,
  in O(k log k + k log n). Duplicates are ignored and empty input is valid.
- smawkRowMinima(rows, columns, value): leftmost minima in a totally monotone
  implicit matrix, O(rows+columns) oracle calls and space, with expected
  linear hash-map overhead. The property must hold in every ordered
  submatrix; merely monotone full-matrix optima are insufficient.
- fpsInverse(a,n), fpsLog(a,n), fpsExp(a,n): truncated series in the field
  modulo 998244353, O(n log n) via NTT/Newton doubling. Constants must be
  nonzero, one, and zero respectively; n=0 returns empty. Inputs are
  normalized and zero-padded; n <= 2^22 is the transform-length bound,
  not a promise that the largest size fits any contest memory/time limit.
  These are formal series, not floating-point analytic functions.

```kotlin
val compressed = VirtualTreeIndex(parents).build(marked)
val best = smawkRowMinima(rows, columns) { i,j -> cost(i,j) }
val geometric = fpsInverse(intArrayOf(1,-1), 10)
```

Standalone modules: virtual-tree, smawk, formal-power-series (includes NTT).
Garner, Lucas and modular combinatorics remain separate roadmap items.
