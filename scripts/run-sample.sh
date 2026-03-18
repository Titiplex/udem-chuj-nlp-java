#!/usr/bin/env bash
set -euo pipefail

JAR="target/chuj-nlp-core-0.1.0.jar"
INPUT="examples/minimal/input.docx"
RULES="examples/minimal/rules.yaml"
OUTPUT="build/out/minimal.conllu"

mkdir -p build/out

mvn -q -DskipTests package

if [ ! -f "$JAR" ]; then
  echo "Jar not found: $JAR"
  exit 1
fi

java -cp "$JAR" org.titiplex.Main "$INPUT" "$RULES" "$OUTPUT"

echo "Generated: $OUTPUT"