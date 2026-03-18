#!/usr/bin/env bash
set -euo pipefail

echo "[1/3] Running repository checks..."
bash scripts/validate-layout.sh

echo "[2/3] Running Maven verification..."
mvn clean verify

echo "[3/3] Done."