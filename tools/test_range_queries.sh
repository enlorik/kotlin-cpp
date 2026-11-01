#!/usr/bin/env bash
set -euo pipefail

# Run the standalone range-query regression suites with Kotlin 1.9+.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

test_module() {
    local name="$1"
    shift
    echo "==> $name"
    kotlinc "$@" -include-runtime -d "$TMP/$name.jar"
    java -jar "$TMP/$name.jar"
}

test_module basics range/RangeBasics.kt tests/RangeBasicsTests.kt
test_module lazy tree/LazySegmentTree.kt tests/LazySegmentTreeTests.kt
test_module sparse shared/AggregationOp.kt range/SparseTables.kt tests/SparseTablesTests.kt
test_module mo range/MoAlgorithm.kt tests/MoAlgorithmTests.kt
test_module persistent tree/PersistentSegmentTree.kt tests/PersistentSegmentTreeTests.kt

echo "==> Combined new-modules compilation"
kotlinc shared/AggregationOp.kt range/RangeBasics.kt tree/LazySegmentTree.kt \
    range/SparseTables.kt range/MoAlgorithm.kt tree/PersistentSegmentTree.kt \
    -d "$TMP/combined.jar"

echo "All range-query tests passed."
