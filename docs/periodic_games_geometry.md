# Periodic normal-play games and geometry

```kotlin
val game = PeriodicSubtractionGame(intArrayOf(1, 3, 4))
val g = game.grundy(1_000_000_000_000L)
val move = winningNimMove(longArrayOf(3, 5, 9))
val sum = minkowskiSumExact(shapeA, shapeB)
val sign = orientationDouble(a, b, c)
```

Normal play means that the player with no moves loses; impartial means
both players have identical legal moves. ImpartialNormalPlay wraps the
existing decreasing-state Sprague-Grundy solver explicitly. Nim's XOR and
winning move are O(number of piles) for nonnegative Long piles.

PeriodicSubtractionGame is restricted to fixed positive subtraction moves.
A repeated window of maxMove Grundy numbers proves the entire future
recurrence. It does not infer periodicity from a few repeated values.
Construction is bounded by maxComputed, with O(maxComputed *
(moveCount+maxMove)) worst-case time and O(maxComputed*maxMove) window
storage. Choose a sensible budget for large moves. Queries are O(1) if
in the computed prefix or a proved period, otherwise rejected. A single
pile's winning move can be found by scanning allowed moves.

Minkowski sum takes convex hulls of both point collections, merges
their polar-ordered edge vectors, and emits the convex sum. Hull sorting
costs O(n log n + m log m); the merge is linear in hull sizes.
Coordinates must fit signed Long, or the operation throws. All edge
arithmetic is exact; empty sets, singleton points and segments are supported.

DoublePoint rejects infinities and NaN. The separate Double geometry API
uses exact BigDecimal predicates for the represented IEEE-754 values,
not approximate epsilon tests. This avoids determinant underflow,
overflow and cancellation, but does not undo input rounding or provide
exact intersection coordinates. It trades speed for correctness.

Standalone modules: normal-play-games, minkowski-sum, double-geometry.
