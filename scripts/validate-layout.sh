#!/usr/bin/env bash
set -euo pipefail

required_paths=(
  "pom.xml"
  "README.md"
  "src/main/java"
  "src/test/java"
  "rules"
  "lexicons"
  "scripts"
)

for path in "${required_paths[@]}"; do
  if [ ! -e "$path" ]; then
    echo "Missing required path: $path"
    exit 1
  fi
done

echo "Repository layout looks OK."