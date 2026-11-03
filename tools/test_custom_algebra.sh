#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Include *real* source files, not mocks, for compatibility checking.
kotlinc \
    shared/AggregationOp.kt \
    range/RangeBasics.kt range/GroupFenwickTree.kt range/MoAlgorithm.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    range/SparseTables.kt tree/PersistentAggregateTree.kt \
    tree/ConfigurableLazySegmentTree.kt shared/CustomAlgebra.kt \
    tests/CustomAlgebraTests.kt \
    -include-runtime -d "$TMP/custom-algebra.jar"

java -jar "$TMP/custom-algebra.jar"
