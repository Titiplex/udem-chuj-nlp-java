# Architecture

## Main layers

1. Input parsing
2. Internal token and alignment model
3. Rule evaluation engine
4. Extractors and context population
5. Feature, template, and routing resolution
6. CoNLL-U rendering

## Design idea

The Java core should stay as stable as possible while linguistic behavior evolves mostly through YAML rule packs and lexical resources.

## Core principle

Rules are data, not hardcoded linguistic logic.

## Recommended internal boundaries

- `parser`: reads DOCX or pre-tokenized inputs
- `model`: aligned tokens, gloss pieces, annotation lines
- `engine`: applies rules in a deterministic order
- `extractors`: derive structured information from glosses or text
- `lexicon`: wraps lexical resource loading and lookup
- `export`: serializes final annotation to CoNLL-U

## Engine expectations

A robust rule engine should make the following order explicit and testable:

1. load rule
2. evaluate matching constraints
3. populate extractor context
4. resolve routing/template expressions
5. apply assignments
6. emit final token annotation

If this order is implicit, debugging YAML becomes much harder.
