#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
kotlinc \
  shared/AggregationOp.kt \
  range/RangeBasics.kt range/GroupFenwickTree.kt \
  tree/LazySegmentTree.kt tree/ConfigurableLazySegmentTree.kt \
  tree/PersistentSegmentTree.kt tree/PersistentAggregateTree.kt \
  tests/OperationApiTests.kt \
  -include-runtime -d "$TMP/operation-api-tests.jar"
java -jar "$TMP/operation-api-tests.jar"
echo "Operation-driven API regression tests passed."
