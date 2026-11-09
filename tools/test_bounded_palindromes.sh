#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
kotlinc -J-Xmx2g range/BlockAssignSum.kt string/PaintedString.kt \
    string/BoundedPalindromeIndex.kt tests/BoundedPalindromeIndexTests.kt \
    -include-runtime -d "$TMP/bounded-palindromes.jar"
java -Xmx64m -jar "$TMP/bounded-palindromes.jar"
