#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt shared/CustomAlgebra.kt \
    shared/TypedAlgebra.kt shared/TypedAlgebraBridge.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    range/SparseTables.kt range/CustomGroupRange.kt range/GroupFenwickTree.kt \
    tree/ConfigurableLazySegmentTree.kt tree/PersistentAggregateTree.kt \
    tree/TypedAlgebraTrees.kt \
    tree/AssignmentLazyTree.kt \
    range/RangeIndex.kt benchmarks/RangeIndexBenchmarks.kt \
    -include-runtime -d "$TMP/range-index-benchmarks.jar"
java -jar "$TMP/range-index-benchmarks.jar"
