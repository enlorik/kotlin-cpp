# Algebraic query planning

The planner compiles the **mathematics and required operation types** into a
concrete data structure. It cannot deduce algebraic laws from arbitrary Kotlin
lambdas, and it does not claim to know the fastest backend on every judge.

```kotlin
val data = longArrayOf(4, 7, 1, 8)

val index = data.optimize {
    aggregate(LongSumMonoid)
    support {
        rangeQuery()
        rangeAdd()
        rangeAssign()
    }
}

check(index.strategy == PlannedLongStrategy.LAZY_SUM)
index.add(0, 3, 10L)
index.assign(1, 2, 0L)
println(index.query(0, 4))
```

All ranges are half-open `[from, until)`. The initial values are snapshotted.
Available declarations are `rangeQuery()`, `pointSet()`, `rangeAdd()`,
`rangeAssign()`, `rangeChmin()`, `rangeChmax()`,
and `historicalVersions()`. Every operation you call must have been
declared in `support { ... }` (except ordinary queries, which are required).

## Selection

| Required capabilities | Supported algebra | Selected strategy |
|---|---|---|
| Queries only | Named SUM/XOR | STATIC_PREFIX, O(1) query |
| Queries only | Named MIN/MAX/GCD | STATIC_SPARSE, O(1) query |
| Point set | Named SUM/MIN/MAX/GCD/XOR | POINT_SEGMENT, O(log n) |
| Queries/point set | Arbitrary custom monoid | GENERIC_STATIC / GENERIC_POINT |
| Range add/assign | Named SUM | LAZY_SUM, O(log n) |
| Range add/assign | Named MIN/MAX | SEGMENT_TREE_BEATS, amortized O(log² n) |
| Range chmin/chmax, optionally add/assign | Named SUM/MIN/MAX | SEGMENT_TREE_BEATS |
| Historical versions, point/range changes | Named SUM | PERSISTENT_LAZY |

Combinations outside these rules throw helpful exceptions before executing.
A custom `monoid(0L) { ... }` is not secretly reclassified as SUM; the
generic fallback keeps noncommutative aggregation order.

## Persistent versions

```kotlin
val timeline = longArrayOf(1, 2, 3).optimize {
    aggregate(LongSumMonoid)
    support { rangeQuery(); rangeAdd(); pointSet(); historicalVersions() }
}
timeline.add(0, 3, 10L)    // new version 1
check(timeline.query(0, 0, 3) == 6L)
check(timeline.query(1, 0, 3) == 36L)
timeline.rollback(0)      // switch to version 0; version 1 survives
timeline.set(0, 100L)    // new version 2, branched from version 0
check(timeline.query(1, 0, 3) == 36L)
check(timeline.query(2, 0, 3) == 105L)
```

Call `branchAdd(version,from,until,delta)` or `branchAssign(...)` to
create a branch *without* changing the currently checked-out version.
Every version keeps its own root; no historical data is overwritten.

## Conditional range clamps

```kotlin
val prices = longArrayOf(2, 10, 5).optimize {
    aggregate(LongSumMonoid)
    support { rangeQuery(); rangeChmin(); rangeChmax(); rangeAdd() }
}
prices.chmin(0, 3, 7) // [2, 7, 5]
prices.chmax(0, 2, 4) // [4, 7, 5]
prices.add(0, 3, 1)  // [5, 8, 6]
check(prices.query(0, 3) == 19L)
```

## Performance regression detection

`bash tools/test_query_planner.sh` compiles the real source modules, runs
deterministic brute-force oracles for history and Beats, then executes
`benchmarks/LongQueryPlannerBenchmark.kt`. The benchmark applies **the same
seeded point and lazy range traces** to the optimized planner and generic
monoid/action trees. It checks every checksum, warms the JVM and reports the
median of three timed runs. CI fails when the optimized path exceeds the
generic path by a severe slowdown factor (default 4×). Override with
`MAX_PLANNER_SLOWDOWN` for a controlled machine.

**Limitations:** JVM warmup, GC and runner contention mean ratios are noisy.
This is a regression alarm, not proof of performance under Timus's limits.
Absolute time/memory limits should be assessed separately on the target
judge and workloads. The planner is operation-aware, not a runtime autotuner.

## One-file submission

```bash
python3 tools/build_submission.py --modules fast-io,query-planner \
    --solution Main.kt --output submission.kt
bash tools/test_query_planner.sh
```

To avoid the full generic dependency bundle for a narrow problem,
use `--modules persistent-long-range` or `--modules segment-tree-beats`.
