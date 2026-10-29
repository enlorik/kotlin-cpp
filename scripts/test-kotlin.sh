#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
build_dir="$(mktemp -d)"
trap 'rm -rf "$build_dir"' EXIT

mapfile -t sources < <(find graph grid io search shared tree -type f -name '*.kt' | sort)
echo "Compiling all ${#sources[@]} library source files together..."
kotlinc -J-Xmx2g "${sources[@]}" -d "$build_dir/library.jar"

# Confirm the standalone search/tree API does NOT require any graph source.
echo "Checking lightweight aggregation + segment tree independently..."
kotlinc -J-Xmx2g \
    shared/AggregationOp.kt tree/SegmentTree.kt \
    shared/SegmentTreeAggregations.kt tests/SearchApiPolishTests.kt \
    -include-runtime -d "$build_dir/standalone.jar"
java -jar "$build_dir/standalone.jar"

passed=0
for test in tests/*.kt; do
    # Each test is an independent Kotlin main() to avoid conflicting entry points.
    if ! grep -qE '^[[:space:]]*fun main[[:space:]]*\(' "$test"; then
        echo "Skipping test without top-level main: $test"
        continue
    fi
    class="$(basename "$test" .kt)Kt"
    echo "Compiling + running $test..."
    kotlinc -J-Xmx2g -cp "$build_dir/library.jar" "$test" -include-runtime -d "$build_dir/test.jar"
    java -cp "$build_dir/library.jar:$build_dir/test.jar" "$class"
    ((passed+=1))
done
echo "PASS: all library sources compiled together; $passed standalone test entry points passed."
