# Rule format

This document describes the YAML rule system used by the project.

## Goals

The YAML format should let a linguist or developer:

- match a token or gloss configuration
- call one or more extractors
- assign POS or features
- dispatch through routing logic
- consult lexical resources
- keep linguistic behavior outside the Java core

## Recommended structure

A rule file should usually contain these top-level sections:

- `def`: inventory declarations and shared definitions
- `lexicons`: lexical resources to load
- `extractors`: reusable extraction logic
- `rules`: ordered rule list

## Suggested rule shape

```yaml
- name: finite_transitive_rule
  priority: 100
  when:
    gloss_contains: ["A", "B"]
  extract:
    - agreement
  set:
    upos: VERB
    feats_template:
      Person[subj]: "${ctx.person_subj}"
      Number[subj]: "${ctx.number_subj}"
```

## Authoring principles

- One linguistic effect per rule whenever possible
- Prefer explicit names over clever compressed YAML
- Use extractor context instead of repeating regex logic in many rules
- Add a regression test for every non-trivial rule

## Recommended development pattern

For each new rule:

1. write a minimal positive example
2. write a minimal negative example
3. verify the expected CoNLL-U output
4. document unusual assumptions in a nearby comment

## Things worth validating automatically

- missing extractor references
- duplicate rule names
- malformed templates
- unknown feature names
- lexicon file missing from disk
- impossible routing branches
