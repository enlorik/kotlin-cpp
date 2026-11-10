# DSL roadmap: problem logic in main, reusable algorithms in library

**Principle:** the contest `main()` owns input-specific decisions and query
dispatch. General algorithms and mechanics belong in reusable library APIs.
Do not hide a 200-line one-off solver in another class to make `main()` look
shorter. Express the mathematics through honest capabilities, then optimize
each backend without changing its promises.

## Completed in this change

- [x] Unboxed `IntArray.blockAssignSum()`: O(sqrt(n)) interval assignments
      and sums, with batched `replace()` for local changes.
- [x] `String.paintedString()`: lazy substring assignment and O(1) char reads.
- [x] `String.boundedPalindromes(k)`: reusable dynamic short-palindrome index
      that owns the Manacher/local-boundary algorithm.
- [x] Timus 2042 example, brute-force tests, official sample, CI integration,
      and one-file contest submission support.

## Next priorities

- [x] Explicitly named `LongSumMonoid`, `LongAddAction` and `LongAssignAction`
      select an unboxed Long-sum lazy tree for `rangeIndex` and
      `onlineLongSum` without guessing arbitrary lambda behavior.
- [x] Capability-selected unboxed static and mutable indexes for named
      SUM, MIN, MAX, GCD, XOR Long monoids, while arbitrary lambdas retain the
      generic fallback; noncommutative user monoids preserve order.
- [x] **Online range-program DSL** returning query answers immediately with
      ordinary Kotlin control flow, point assignments and lawful lazy updates;
      the existing deferred `rangeProgram` remains unchanged.
- [x] Primitive Long range-add and range-assign using an unboxed lazy tree.
- [x] `BlockAssignSum.recompute` and `setMany`: rebuild only touched blocks
      once, instead of per-position tree operations.
- [x] Query planner `LongArray.optimize { aggregate(...); support {...} }`
      selects lawful backend from declared operations.
- [x] Persistent range add/assign via path copying, version branching and rollback.
- [x] Segment Tree Beats range chmin/chmax/add and sum/min/max, with oracle tests.
- [x] Seeded planner-vs-generic point/lazy benchmarks with correctness checks and
      CI severe-slowdown alarm (not an absolute Timus TLE guarantee).
- [ ] Generalize bounded **local contributions** only when another genuinely
      different contest problem demonstrates the same abstraction; otherwise
      it is overfitting disguised as an API.
- [ ] Verify actual Timus acceptance and investigate remaining JVM/judge costs;
      time estimates from a local machine are not a verdict.

## Advanced algorithm expansion

- [x] Signed Int Wavelet Matrix: range kth, frequencies and predecessor/successor.
- [x] Dynamic Li Chao min/max line envelopes, including segment insertion.
- [x] Iterative Heavy-Light path/subtree aggregation with noncommutative ordering
      and primitive Long-sum specialization, plus SparseGraph integration.
- [x] Randomized brute-force oracles, large-chain stack test and standalone bundling.
- [ ] Rerooting/tree DP as a separate reusable abstraction.
- [ ] Rollback DSU with offline dynamic connectivity.
- [ ] Suffix automaton and palindromic tree (eertree).

## Architecture rule

`monoid()` describes a lawful way to combine values; `action()` describes
a lawful way to update aggregates. Neither magically determines the fastest
machine representation. Explicit specialized implementations can coexist
with those mathematical APIs without pretending all lambdas are equivalent.
