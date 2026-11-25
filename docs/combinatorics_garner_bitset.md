# Prime combinatorics, Garner and packed subset sum

```kotlin
val combinations = PrimeCombinatorics(1_000_000_007, 200_000)
val answer = combinations.choose(n,k)
val huge = PrimeCombinatorics(7,6).lucas(1_000_000_000_000L,10L)
val residue = garnerMod(residues,moduli,target)
val reachable = weights.subsetSumBitset(capacity)
val best = reachable.bestAtMost()
```

PrimeCombinatorics validates primality and requires 0 <= maxN < prime.
O(maxN + log prime) setup, O(maxN) memory, O(1) binomial queries.
Lucas uses a complete table through prime-1, so it is intended for small
primes; O(log_p n) digit steps. Invalid k yields zero.

GarnerIndex precomputes pairwise modular inverses for coprime source
moduli >=2. Canonical mixed-radix digits are reconstructed modulo any
positive signed-Long target (including noncoprime targets), using O(k²)
modular operations and O(k²) precomputed storage. No overflowing product is
constructed. For noncoprime sources, use existing chineseRemainder64.

SubsetSumBitset packs reachable states into 64-bit words. Descending
shift-OR updates represent 0/1 items with nonnegative weights, not unbounded
knapsack. O(n*(capacity/64+1)) time, O(capacity/64+1) space, O(1)
membership. bestAtMost scans words downward. Negative weights are rejected;
out-of-range membership is false. Zero is always reachable.

Standalone modules: prime-combinatorics, garner, bitset-subset-sum.
