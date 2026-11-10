#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

python3 -m unittest discover -s tests -p 'test_submission_builder.py' -v

check_submission() {
    local label="$1" modules="$2" solution="$3"
    python3 tools/build_submission.py \
        --modules "$modules" --solution "$solution" \
        --output "$tmp/$label.kt"
    kotlinc -J-Xmx2g "$tmp/$label.kt" -include-runtime -d "$tmp/$label.jar"
    java -jar "$tmp/$label.jar"
}

check_submission basics 'fast-io,binary-search,segment-tree' tools/fixtures/SubmissionBasics.kt
check_submission ranges 'range-index' tools/fixtures/SubmissionRanges.kt
check_submission program 'range-program' tools/fixtures/SubmissionRangeProgram.kt
check_submission online 'online-range-program' tools/fixtures/SubmissionOnlineRangeProgram.kt
check_submission primitive_lazy 'primitive-long-lazy' tools/fixtures/SubmissionPrimitiveLazy.kt
check_submission primitive_monoids 'primitive-long-index,bounded-palindromes' tools/fixtures/SubmissionPrimitiveMonoids.kt
check_submission planner 'query-planner' tools/fixtures/SubmissionQueryPlanner.kt
check_submission advanced_ranges 'persistent-long-range,segment-tree-beats' tools/fixtures/SubmissionAdvancedRanges.kt
check_submission advanced_ds 'wavelet-matrix,li-chao,heavy-light' tools/fixtures/SubmissionAdvancedAlgorithms.kt
check_submission heavy_light_graph 'heavy-light-graph' tools/fixtures/SubmissionHeavyLightGraph.kt
check_submission graph 'graph,grid-bfs' tools/fixtures/SubmissionGraph.kt
check_submission primitive 'sum-segment-tree' tools/fixtures/SubmissionSumSegmentTree.kt
check_submission advanced 'advanced-binary-search' tools/fixtures/SubmissionAdvancedSearch.kt
check_submission palindrome 'fast-io,bounded-palindromes' tools/fixtures/SubmissionBoundedPalindromes.kt

# Every supported bundle must coexist when combined into one Kotlin file.
check_submission combined \
    'fast-io,advanced-binary-search,segment-tree,sum-segment-tree,primitive-long-lazy,primitive-long-index,query-planner,persistent-long-range,segment-tree-beats,grid-bfs,graph,range-index,range-program,online-range-program,bounded-palindromes,wavelet-matrix,li-chao,heavy-light-graph' \
    tools/fixtures/SubmissionBasics.kt


# The actual Timus 2042 example must also compile as a one-file contest entry.
python3 tools/build_submission.py \
    --modules fast-io,bounded-palindromes \
    --solution examples/Timus2042.kt \
    --output "$tmp/timus2042.kt"
kotlinc -J-Xmx2g "$tmp/timus2042.kt" -include-runtime -d "$tmp/timus2042.jar"
printf 'abacaba 4\n3\n2 1 3\n1 1 3 c\n2 1 4\n' |
    java -Xmx64m -jar "$tmp/timus2042.jar" > "$tmp/timus2042.actual"
printf '4\n10\n' > "$tmp/timus2042.expected"
diff -u "$tmp/timus2042.expected" "$tmp/timus2042.actual"

echo "PASS: generated standalone Kotlin files compiled and ran successfully"
