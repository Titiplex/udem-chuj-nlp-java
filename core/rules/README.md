# Rule packs

This directory contains YAML rule packs.

Suggested convention:

- `default.yaml`: stable baseline for routine use
- `experimental.yaml`: in-progress rules or alternative strategies

Recommended workflow:

1. modify one rule at a time
2. add or update a minimal fixture
3. run `mvn clean verify`
4. run a manual end-to-end example
