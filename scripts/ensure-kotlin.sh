#!/usr/bin/env bash
set -euo pipefail

# Ubuntu GitHub Actions runners ship Kotlin already. Avoid GitHub Releases
# download outages; fall back to the pinned compiler for minimal runners.
if command -v kotlinc >/dev/null 2>&1; then
    echo "Using preinstalled Kotlin compiler:"
    kotlinc -version
    exit 0
fi

echo "No preinstalled kotlinc found: downloading Kotlin 1.9.25"
curl --fail --location --retry 3 --silent --show-error \
    --output "$RUNNER_TEMP/kotlin-compiler.zip" \
    https://github.com/JetBrains/kotlin/releases/download/v1.9.25/kotlin-compiler-1.9.25.zip
unzip -q "$RUNNER_TEMP/kotlin-compiler.zip" -d "$RUNNER_TEMP/kotlin"
echo "$RUNNER_TEMP/kotlin/kotlinc/bin" >> "$GITHUB_PATH"
"$RUNNER_TEMP/kotlin/kotlinc/bin/kotlinc" -version
