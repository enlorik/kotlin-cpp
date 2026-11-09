#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Compile actual library sources together, not fake or copied implementations.
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt shared/CustomAlgebra.kt \
    shared/TypedAlgebra.kt shared/TypedAlgebraBridge.kt \
    shared/LongPrimitiveMonoids.kt shared/LongSumCapabilities.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    range/SparseTables.kt tree/ConfigurableLazySegmentTree.kt \
    tree/PersistentAggregateTree.kt tree/TypedAlgebraTrees.kt \
    tree/AssignmentLazyTree.kt range/RangeIndex.kt \
    tree/LongSumLazyTree.kt range/LongPrimitiveRangeIndex.kt \
    tree/VersionedLongSum.kt tree/LongBeatsTree.kt \
    range/LongQueryPlanner.kt \
    tests/VersionedLongSumTests.kt tests/LongBeatsTreeTests.kt \
    tests/LongQueryPlannerTests.kt benchmarks/LongQueryPlannerBenchmark.kt \
    -include-runtime -d "$TMP/query-planner.jar"
java -Xmx128m -cp "$TMP/query-planner.jar" VersionedLongSumTestsKt
java -Xmx128m -cp "$TMP/query-planner.jar" LongBeatsTreeTestsKt
java -Xmx128m -cp "$TMP/query-planner.jar" LongQueryPlannerTestsKt
java -Xmx128m -cp "$TMP/query-planner.jar" LongQueryPlannerBenchmarkKt
