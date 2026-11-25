# Suffix arrays and dynamic bipartiteness

```kotlin
val index = "banana".suffixArray()
val positions = index.occurrences("ana") // [3, 1], suffix order
val common = index.commonPrefix(1, 3) // 3
val answers = offlineBipartiteness(3) {
    add(0, 1); add(1, 2); query()
    add(2, 0); query()
    remove(0, 2); query()
} // [true, false, true]
```

SuffixArray uses stable counting-sort doubling, Kasai LCP and a linear-space
segment tree for O(log n) LCP queries. Build O(n log n + 65536), space
O(n + 65536), UTF-16 code-unit order, no empty suffix. Pattern search
O(pattern length * log n + output size); empty pattern matches all included
suffixes. Distinct substring count uses Long. matchRange returns a Kotlin
IntRange representing the half-open interval of matching ranks.

ParityRollbackDsu supports XOR constraints, including parity zero, snapshots
and contradictions. A parity returned while inconsistent describes only the
spanning forest, not a valid global coloring. Rollback restores the exact
constraint state. OfflineDynamicBipartiteness applies parity-one edges over
a time segment tree, supports parallel edges and self loops, rejects removal
of an absent edge, and returns Boolean answers in query order.
For m events and k active intervals, time O((m+k) log(m+1) log(n+1))
and space O(m+k log(m+1)+n). No online deletion is claimed.

Standalone modules: suffix-array, parity-connectivity.
