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

# Compile ALL test entry points into one module against the actual library.
# Their generated JVM classes have distinct names, so run each main separately.
mapfile -t tests < <(grep -lE '^[[:space:]]*fun main[[:space:]]*[(]' tests/*.kt | sort)
echo "Compiling ${#tests[@]} tests together..."
kotlinc -J-Xmx2g -cp "$build_dir/library.jar" "${tests[@]}" -include-runtime -d "$build_dir/tests.jar"

passed=0
for test in "${tests[@]}"; do
    class="$(basename "$test" .kt)Kt"
    echo "Running $test..."
    java -cp "$build_dir/library.jar:$build_dir/tests.jar" "$class"
    ((passed+=1))
done
echo "PASS: all library sources + all ${#tests[@]} tests compiled together; $passed test entry points passed."
