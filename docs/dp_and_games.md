# DP optimization and mathematical games

## Divide-and-conquer monotone partition DP

```kotlin
val row = divideConquerPartitionRow(previous) { split, until ->
    cost(split, until)
}
println(row.values[j])
println(row.split[j])
```

Computes `dp[j] = min(previous[i] + cost(i,j))` over `0 <= i < j`.
It *requires* nondecreasing optimal split indices, a mathematical
precondition such as the quadrangle inequality for suitable costs.
This property is not inferred or checked by the library. Under the
property, evaluates O(n log n) transitions; otherwise results can be
wrong. `previous[0]` represents the empty prefix; `values[0]` remains
infinity. Use `Long.MAX_VALUE/4` for inaccessible states.

## Monotone convex hull trick

```kotlin
val hull = monotoneMinHull {
    add(slope = 4, intercept = 1)
    add(slope = 2, intercept = 5)
}
println(hull.query(3))
```

Inserts lower-envelope lines in **nonincreasing slope** order.
Uses exact BigInteger cross-multiplication to decide whether a middle
line is redundant. Insertion is amortized O(1) hull operations and
query is O(log n) binary search for arbitrary x; evaluating a line
must still fit signed Long. Use the existing Li Chao tree when lines
arrive in arbitrary order or you need maximum queries.

## Impartial games

`spragueGrundy(n) { moves(state) }` computes Grundy numbers for
transitions strictly to *smaller* state indices. It enforces this
acyclicity precondition. `subtractionGrundy(maxStones, moves)`
handles allowed positive removals; `nimSum()` XORs Grundy values.

```kotlin
val g = subtractionGrundy(20, intArrayOf(1,3,4))
val playerOneWins = g[20] != 0
```

These are normal-play, impartial finite games only.
`retrogradeGame(n, directedMoves)` classifies WIN/LOSE/DRAW in finite
directed turn graphs, where infinite cyclic play is a DRAW.

For finite perfect-information games with numeric terminal evaluations,
`alphaBetaMinimax(state, depth, maximizing, evaluate, moves)`
implements depth-limited alpha-beta minimax without memoization.

## Standalone contest bundle

```bash
python3 tools/build_submission.py --modules dp-optimization,game-theory \
  --solution Main.kt --output submission.kt
```

Tests compare randomized DP rows to quadratic enumeration, hull queries
to brute-force line evaluation, SG to exhaustive DAG game values,
retrograde classifications to fixed-point logic, and alpha-beta to
unpruned minimax.
