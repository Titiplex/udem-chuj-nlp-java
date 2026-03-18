#!/usr/bin/env bash
set -euo pipefail

grep -q "mvn clean verify" README.md || {
  echo "README is missing 'mvn clean verify'"
  exit 1
}

grep -q "org.titiplex.Main" README.md || {
  echo "README is missing main launch command"
  exit 1
}

echo "README checks passed."