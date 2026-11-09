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

- [ ] Capability-driven **primitive backend selection** for typed
      `monoid()` + `action()` where operations are explicitly named and lawful;
      no unsafe guessing from arbitrary lambdas.
- [x] **Online range-program DSL** returning query answers immediately with
      ordinary Kotlin control flow, point assignments and lawful lazy updates;
      the existing deferred `rangeProgram` remains unchanged.
- [ ] Primitive Long range-add and range-assign, batched localized recompute
      APIs, and benchmarks against the generic lazy tree.
- [ ] Generalize bounded **local contributions** only when another genuinely
      different contest problem demonstrates the same abstraction; otherwise
      it is overfitting disguised as an API.
- [ ] Verify actual Timus acceptance and investigate remaining JVM/judge costs;
      time estimates from a local machine are not a verdict.

## Architecture rule

`monoid()` describes a lawful way to combine values; `action()` describes
a lawful way to update aggregates. Neither magically determines the fastest
machine representation. Explicit specialized implementations can coexist
with those mathematical APIs without pretending all lambdas are equivalent.
