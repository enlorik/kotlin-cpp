# The full competitive-programming dream: algorithm and DSL expansion

**Maintained checklist** for our Kotlin competitive-programming library.
This is a backlog, not a claim that every algorithm already exists.
Each checked item must have a reusable implementation, randomized or
exhaustive correctness tests, documented complexity/contracts, and
a working one-file submission-builder module.

## Foundations already implemented

- [x] Algebraic monoids, actions, law checks and online query planner
- [x] Fenwick/segment/sparse/disjoint, primitive Long-sum and Beats backends
- [x] Persistent range queries and branching Long range updates
- [x] Wavelet Matrix: range order statistics/frequencies
- [x] Li Chao Tree: dynamic lines and constrained line segments
- [x] Heavy-Light Decomposition: path and subtree queries
- [x] Automated regression tests and baseline performance checks

## 1. Tree dynamic programming

- [x] Generic **rerooting DP**: merge child contributions; remove one
  contribution with prefix/suffix products; calculate results for *every* root
- [ ] Tree knapsack/convolution DP and diameter/centroid templates
- [ ] Virtual trees and LCA-assisted queries
- [ ] Integrate with `SparseGraph` and monoid DSL where lawful

**Prove it:** sum of distances from every possible root; noncommutative
contributions; random trees against O(n²) all-roots brute force.

## 2. Fully dynamic/offline graph connectivity

- [x] **Rollback DSU**: snapshots, union-by-size, component sizes, undo
- [x] **Offline dynamic connectivity**: add/remove edges, connectivity and
  component-count queries using a segment tree over time
- [ ] Bipartiteness with parity rollback; divide-and-conquer on time
- [ ] Online dynamic connectivity only with an appropriate dynamic-tree
  backend, not pretending ordinary DSU can delete edges

**Prove it:** randomized add/remove/query streams against recomputed BFS.

## 3. String algorithms

- [x] **Suffix automaton**: substring membership, distinct substrings,
  occurrence counts, longest common substring
- [x] **Palindromic tree (eertree)**: distinct palindromes, occurrence counts,
  longest palindrome, online character append
- [ ] Suffix array + LCP/RMQ for offline substring queries
- [x] KMP/Z substring matching
- [ ] Rolling-hash API with explicit collision policy
- [x] Aho–Corasick dictionary matching

**Prove it:** exhaustive short strings and randomized brute-force substring
and palindrome enumeration.

## 4. Dynamic-programming optimizations

- [x] **Divide-and-conquer DP optimization** for monotone optima, with
  documented monotonicity precondition and caller-provided cost
- [x] **Convex hull trick** for monotone line slopes/queries; Li Chao is
  already available for arbitrary insertion order
- [x] Knuth optimization with quadrangle inequality preconditions
- [x] SOS DP and subset/superset zeta/Mobius transforms
- [ ] Bitset DP optimizations
- [ ] SMAWK/totally-monotone matrix search where applicable

**Prove it:** check optimized DP against O(n²) or O(n³) transitions.

## 5. Game theory

- [x] **Sprague–Grundy**: mex, win/lose states, disjoint-sum XOR
- [x] **Retrograde win/lose/draw** on directed game graphs
- [x] Generic minimax/negamax with alpha–beta pruning for finite
  perfect-information games (document depth/terminal rules)
- [ ] Nim, subtraction games, periodicity detection
- [ ] Explicit impartial/normal-play assumptions, not silently universalize SG

**Prove it:** exhaustive small-state game trees.

## 6. Computational geometry

- [x] **Exact orientation/cross products** on integer points without
  overflow; hull, segment intersection and on-segment predicates
- [x] **Closest pair** in O(n log n)
- [x] **Rotating calipers** for farthest pair/diameter and convex polygons
- [x] Polygon area and point-in-polygon
- [ ] Minkowski sums
- [ ] Floating-point geometry separately, with numerical robustness policy

**Prove it:** randomized small point sets against quadratic brute force.

## 7. Number theory and polynomial algebra

- [x] **Modular arithmetic**: normalization, multiplication that avoids
  overflow, powers, extended GCD, modular inverses
- [x] **Primality**: deterministic Miller–Rabin for signed 64-bit inputs
- [x] **Factorization**: Pollard Rho, multiplicities, divisor/totient helpers
- [x] **NTT** over compatible prime moduli (e.g. 998244353) and convolution
- [ ] **FFT** for real/complex polynomials with explicit precision limits
- [ ] Garner, formal power-series inverse/log/exp, combinatorics modulo
  primes and Lucas theorem

**Prove it:** small exact polynomial multiplication and trial-division
factorization oracles, boundary cases and seeded randomized tests.

## Packaging, product quality, and contest usability

- [ ] Each implemented topic has independent source files with no unused
      heavy dependencies and a documented standalone bundle
- [ ] Every reusable algorithm has edge-case + brute-force tests and a
      worst-case-size stress test appropriate to its complexity
- [ ] Benchmarks compare specialized and generic alternatives, without
      making misleading universal performance promises
- [ ] Keep contest `main()` for **problem-specific mathematical decisions**;
      library owns general algorithms, not disguised problem-specific solvers
- [ ] Optional planner extensions only when mathematical capabilities are
      explicit and the selection is demonstrably lawful

## Joint challenge, deliberately reserved

- [ ] Together solve **three hard Timus problems from different families**;
      use their bottlenecks to expose what the DSL still cannot express
- [ ] Establish an actual Accepted submission for Timus 2042 instead of
      treating local benchmarks as proof

## Implementation policy

Implement one independently testable slice at a time and merge only green
CI PRs. A checked box means *merged and verified*, not just proposed.
Preserve backwards compatibility, half-open range conventions where
appropriate, and exact numerical/domain contracts.
