# Knuth optimization and subset transforms

## Knuth interval DP: O(n²) rather than O(n³)

For inclusive interval recurrence

`dp[l][r] = cost(l,r) + min_{l<=k<r}(dp[l][k]+dp[k+1][r])`

with `dp[i][i] = 0`, use:

```kotlin
val weights = longArrayOf(1, 2, 3)
val prefix = longArrayOf(0, 1, 3, 6)
val result = knuthIntervalDp(3) { l,r -> prefix[r+1]-prefix[l] }
println(result.cost(0,2))
println(result.optimalSplit(0,2))
```

**Mathematical requirement:** the leftmost optimum must satisfy
`opt[l][r-1] <= opt[l][r] <= opt[l+1][r]`; standard sufficient
conditions involve monotonicity and a quadrangle inequality for the cost.
The library cannot infer those properties from arbitrary lambdas.
Under them, Knuth reduces O(n³) split enumeration to O(n²) transitions;
storage is O(n²), and all intermediate values must fit signed Long.

## SOS / subset and superset zeta transforms

```kotlin
val values = longArrayOf(1,2,3,4) // masks 00,01,10,11
val subsets = values.subsetZeta()   // [1,3,4,10]
check(subsets.subsetMobius().contentEquals(values))
val supersets = values.supersetZeta()
check(supersets.supersetMobius().contentEquals(values))
```

`subsetZeta()[mask] = sum(values[sub])` over all submasks.
`supersetZeta()[mask] = sum(values[sup])` over all supermasks.
Möbius transforms invert them. All transforms require an array whose
length is a **positive power of two** and run in O(b*2^b) time.
The input array is not modified. Arithmetic uses Long wraparound.

The same transforms accelerate convolutions:

```kotlin
val orProduct = orConvolution(a,b)
val andProduct = andConvolution(a,b)
```

They compute sums of `a[x]*b[y]` over `x|y==mask` and
`x&y==mask` respectively, in O(b*2^b), rather than O(4^b).

## Contest bundling and correctness

```bash
python3 tools/build_submission.py --modules knuth-dp,subset-transforms \
    --solution Main.kt --output submission.kt
```

CI compares Knuth results to cubic brute-force interval DP for valid
positive interval-sum costs; subset transforms to explicit subset and
superset enumeration; and OR/AND convolution to quadratic double loops.
