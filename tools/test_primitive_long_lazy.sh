#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
kotlinc -J-Xmx2g shared/TypedAlgebra.kt shared/LongSumCapabilities.kt \
    tree/LongSumLazyTree.kt range/LongSumOptimized.kt \
    tests/LongSumLazyTreeTests.kt -include-runtime -d "$TMP/primitive.jar"
java -Xmx128m -jar "$TMP/primitive.jar"
