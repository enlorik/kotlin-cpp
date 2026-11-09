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
      select an unboxed Long-sum lazy tree for safe `rangeIndex`/`onlineRangeProgram`
      overloads without guessing the behavior of arbitrary lambdas.
- [ ] Extend capability-driven **primitive backend selection** to additional
      lawful built-in operations such as min/max and bitwise aggregates;
      never infer algebraic properties from arbitrary lambda bodies.
- [x] **Online range-program DSL** returning query answers immediately with
      ordinary Kotlin control flow, point assignments and lawful lazy updates;
      the existing deferred `rangeProgram` remains unchanged.
- [x] Primitive Long range-add and range-assign using an unboxed lazy tree.
- [ ] Batched localized recomputation and reproducible comparisons against
      the generic lazy tree.
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
