#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

size=4096
queries=40000
if [[ -v BENCH_SIZE ]]; then size="$BENCH_SIZE"; fi
if [[ -v BENCH_QUERIES ]]; then queries="$BENCH_QUERIES"; fi
python3 benchmarks/generate_segment_tree_workload.py \
  --size "$size" --queries "$queries" > "$tmp/input.txt"

kotlinc -J-Xmx2g tree/SegmentTree.kt tree/LongSumSegmentTree.kt benchmarks/SegmentTreeKotlin.kt \
  -include-runtime -d "$tmp/kotlin.jar"
g++ -std=c++17 -O2 -Wall -Wextra -pedantic benchmarks/SegmentTreeCpp.cpp \
  -o "$tmp/cpp"

kotlin_result="$(java -jar "$tmp/kotlin.jar" < "$tmp/input.txt")"
cpp_result="$("$tmp/cpp" < "$tmp/input.txt")"
kotlin_checksum="$(printf '%s\n' "$kotlin_result" | sed -n 's/^checksum=//p')"
cpp_checksum="$(printf '%s\n' "$cpp_result" | sed -n 's/^checksum=//p')"
primitive_checksum="$(printf '%s\n' "$kotlin_result" | sed -n 's/^primitive_checksum=//p')"
if [[ -z "$kotlin_checksum" || -z "$primitive_checksum" || "$kotlin_checksum" != "$primitive_checksum" || "$kotlin_checksum" != "$cpp_checksum" ]]; then
  printf 'Kotlin result:\n%s\nC++ result:\n%s\n' "$kotlin_result" "$cpp_result" >&2
  echo "FAIL: implementations disagree" >&2
  exit 1
fi

printf 'PASS: matching checksum=%s on n=%s, q=%s\n' \
  "$kotlin_checksum" "$size" "$queries"
printf 'Kotlin/JVM:\n%s\n' "$kotlin_result"
printf 'C++ (-O2):\n%s\n' "$cpp_result"
echo "Timings are diagnostic observations, not CI pass/fail thresholds."
