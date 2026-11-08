#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Compile against the repository's REAL Kotlin source files, not mocks.
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt shared/CustomAlgebra.kt \
    shared/TypedAlgebra.kt shared/TypedAlgebraBridge.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    range/SparseTables.kt \
    tree/ConfigurableLazySegmentTree.kt tree/PersistentAggregateTree.kt \
    tree/TypedAlgebraTrees.kt \
    tree/AssignmentLazyTree.kt \
    range/RangeIndex.kt tests/RangeIndexTests.kt \
    -include-runtime -d "$TMP/range-index-tests.jar"
java -jar "$TMP/range-index-tests.jar"
echo "PASS: auto range index custom/typed operation regressions"
