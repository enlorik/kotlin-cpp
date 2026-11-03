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
    range/CustomGroupRange.kt range/MoGroup.kt \
    tree/SegmentTree.kt shared/SegmentTreeAggregations.kt \
    range/SparseTables.kt tree/PersistentAggregateTree.kt \
    tree/LazySegmentTree.kt tree/ConfigurableLazySegmentTree.kt tree/CustomLazyAdapters.kt \
    shared/CustomAlgebra.kt \
    tests/CustomAlgebraTests.kt \
    -include-runtime -d "$TMP/custom-algebra.jar"

java -jar "$TMP/custom-algebra.jar"

# Exercise the additional generic prefix / assignment overloads against the
# previous module classes. Keep test mains separate to avoid an ambiguous
# manifest when generating executable Kotlin jars.
kotlinc -J-Xmx2g -cp "$TMP/custom-algebra.jar" \
    shared/TypedAlgebra.kt shared/TypedAlgebraBridge.kt \
    range/MonoidPrefixTable.kt range/TypedAlgebraRanges.kt \
    tree/AssignmentLazyTree.kt tree/TypedAlgebraTrees.kt \
    tests/CustomMonoidExtensionsTests.kt \
    -include-runtime -d "$TMP/monoid-extensions.jar"

java -cp "$TMP/custom-algebra.jar:$TMP/monoid-extensions.jar" CustomMonoidExtensionsTestsKt
