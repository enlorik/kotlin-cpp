#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

kotlinc -J-Xmx2g \
    shared/TypedAlgebra.kt tree/SegmentTree.kt tree/LongSumSegmentTree.kt \
    range/IntWaveletMatrix.kt tree/LiChaoTree.kt tree/HeavyLightTree.kt \
    tests/IntWaveletMatrixTests.kt tests/LiChaoTreeTests.kt \
    tests/HeavyLightTreeTests.kt \
    -include-runtime -d "$TMP/advanced.jar"

java -Xmx160m -cp "$TMP/advanced.jar" IntWaveletMatrixTestsKt
java -Xmx160m -cp "$TMP/advanced.jar" LiChaoTreeTestsKt
java -Xmx160m -cp "$TMP/advanced.jar" HeavyLightTreeTestsKt
