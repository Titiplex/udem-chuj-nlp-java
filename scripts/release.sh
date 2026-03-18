#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-dev}"
DIST_DIR="dist/chuj-nlp-${VERSION}"

rm -rf dist
mkdir -p "$DIST_DIR"

cp target/*.jar "$DIST_DIR"/

if [ -d rules ]; then
  cp -r rules "$DIST_DIR"/
fi

if [ -d lexicons ]; then
  cp -r lexicons "$DIST_DIR"/
fi

if [ -d examples ]; then
  cp -r examples "$DIST_DIR"/
fi

cp README.md "$DIST_DIR"/
[ -f CHANGELOG.md ] && cp CHANGELOG.md "$DIST_DIR"/

cat > "$DIST_DIR/README-release.md" <<EOF
# Release bundle ${VERSION}

Contents:
- executable jar
- default rules
- lexicons
- examples
- project README
- changelog

Typical usage:
java -cp <jar> org.titiplex.Main input.docx rules/default.yaml output.conllu
EOF

tar -czf "dist/chuj-nlp-${VERSION}.tar.gz" -C dist "chuj-nlp-${VERSION}"

echo "Release bundle created:"
echo " - $DIST_DIR"
echo " - dist/chuj-nlp-${VERSION}.tar.gz"