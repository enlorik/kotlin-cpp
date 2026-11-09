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
check_submission graph 'graph,grid-bfs' tools/fixtures/SubmissionGraph.kt
check_submission primitive 'sum-segment-tree' tools/fixtures/SubmissionSumSegmentTree.kt
check_submission advanced 'advanced-binary-search' tools/fixtures/SubmissionAdvancedSearch.kt
check_submission palindrome 'fast-io,bounded-palindromes' tools/fixtures/SubmissionBoundedPalindromes.kt

# Every supported bundle must coexist when combined into one Kotlin file.
check_submission combined \
    'fast-io,advanced-binary-search,segment-tree,sum-segment-tree,grid-bfs,graph,range-index,range-program,bounded-palindromes' \
    tools/fixtures/SubmissionBasics.kt

echo "PASS: generated standalone Kotlin files compiled and ran successfully"
