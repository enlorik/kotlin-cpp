# Fast mutable strings and bounded palindrome queries

`String.boundedPalindromes(maxLength)` gives an **online** range-assignment
and palindrome counter, for any character alphabet. It targets short
palindromes (`maxLength` small relative to the string), including Timus 2042.

```kotlin
val index = "abacaba".boundedPalindromes(4)
index.count(0, 3)         // 4: a, b, a, aba
index.assign(0, 3, 'c')   // "ccccaba"
index.count(0, 4)         // 10
```

All calls use half-open intervals `[from, until)`. To pass inclusive 1-based
Timus `l, r`, call `assign(l - 1, r, c)` or `count(l - 1, r)`.

## Mathematical and performance design

- `PaintedString`: lazy per-block character assignments; reads O(1) and
  interval paints O(B + n/B).
- `BlockAssignSum`: unboxed Int values, Long sums; supports `assign`,
  `replace` (batched nearby values), and `sum`. Range sum/assignment cost
  O(B + n/B); batched replacement costs O(length + B).
- `BoundedPalindromeIndex`: Manacher + a difference array builds the number
  of length <= k palindromes starting at each position in O(n). After a paint,
  only starts within k of the two boundaries need local recalculation;
  fully repainted interior starts have contribution k. For queries, stored
  starting-position contributions are summed except for the final <k starts,
  whose palindromes must not extend beyond the requested range.

**Time:** O(n) preprocessing and O(k + B + n/B) per update or query.
**Memory:** O(n + k) additional. Default B is approximately sqrt(n).
Supports empty strings and k > n. Requires k >= 1.

## Reusable vs general algebra

`rangeIndex(monoid, action)` remains correct for arbitrary *lawful*
aggregations, but its generic boxed lazy segment tree cannot infer that a
lambda is addition or that updates are assignments. The specialized primitive
backend has explicit capabilities. No automatic heuristic silently changes
your mathematical semantics. Palindromes are a reusable string algorithm,
not a contest-specific `Nikita` wrapper. Input and query dispatch stay in
`main()`.

## Submission and tests

The full contest solution is in [examples/Timus2042.kt](../examples/Timus2042.kt).

```bash
python3 tools/build_submission.py --modules fast-io,bounded-palindromes \
    --solution examples/Timus2042.kt --output timus2042.kt
bash tools/test_bounded_palindromes.sh
```

The module bundles only three new primitive Kotlin files, not the generic
range-index dependency graph. Tests include official sample and randomized
brute-force checks over k=1..50 and overlapping assignments.

Local 64 MB heap stress checks do **not** guarantee Timus acceptance;
only an accepted judge submission establishes that.
