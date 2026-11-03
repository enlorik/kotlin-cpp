#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Compile directly against the real implementations and run property tests.
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt shared/CustomAlgebra.kt \
    shared/TypedAlgebra.kt shared/ValidatedAlgebra.kt shared/TypedAlgebraBridge.kt \
    range/RangeBasics.kt range/GroupFenwickTree.kt range/CustomGroupRange.kt \
    range/MoAlgorithm.kt range/MoGroup.kt range/SparseTables.kt range/MonoidPrefixTable.kt \
    range/TypedAlgebraRanges.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    tree/PersistentAggregateTree.kt \
    tree/ConfigurableLazySegmentTree.kt tree/CustomLazyAdapters.kt \
    tree/AssignmentLazyTree.kt tree/TypedAlgebraTrees.kt \
    tests/TypedAlgebraTests.kt \
    -include-runtime -d "$TMP/typed-algebra.jar"
java -jar "$TMP/typed-algebra.jar"

# Compile the same real library without main() for negative-type tests.
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt shared/TypedAlgebra.kt shared/ValidatedAlgebra.kt shared/TypedAlgebraBridge.kt \
    range/RangeBasics.kt range/GroupFenwickTree.kt range/CustomGroupRange.kt \
    range/MoAlgorithm.kt range/MoGroup.kt range/SparseTables.kt range/MonoidPrefixTable.kt range/TypedAlgebraRanges.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    tree/PersistentAggregateTree.kt tree/ConfigurableLazySegmentTree.kt \
    tree/AssignmentLazyTree.kt tree/TypedAlgebraTrees.kt \
    -d "$TMP/library.jar"

reject() {
    local name="$1"
    local code="$2"
    printf '%s\n' "$code" > "$TMP/negative.kt"
    if kotlinc -cp "$TMP/library.jar" "$TMP/negative.kt" -d "$TMP/reject.jar" \
        > "$TMP/negative.log" 2>&1; then
        echo "FAIL: invalid algebra compiled successfully: $name" >&2
        exit 1
    fi
    echo "PASS: Kotlin rejected $name"
}

reject "Monoid passed to Fenwick (inverse missing)" \
'fun test() {
    val product = monoid(1) { a: Int, b: Int -> a * b }
    intArrayOf(2, 3).fenwickTree(product)
}'

reject "Monoid passed to overlapping SparseTable (idempotence missing)" \
'fun test() {
    val addition = monoid(0) { a: Int, b: Int -> a + b }
    intArrayOf(2, 3).sparseTable(addition)
}'

reject "Group declaration without inverse" \
'fun test() {
    group(identity = 0, combine = { a: Int, b: Int -> a + b })
}'

reject "Custom lazy action missing required composition function" \
'fun test() {
    val add = action<Int, Int>(
        apply = { total, delta, length -> total + delta * length }
    )
}'

echo "PASS: typed capabilities, law checks, and compile-time rejection tests"
