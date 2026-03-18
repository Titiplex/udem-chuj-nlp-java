# Testing

## Categories

### Unit tests

Use unit tests for:

- extractor behavior
- utility methods
- parser helpers
- rendering helpers
- YAML normalization helpers

### Integration tests

Use integration tests for:

- YAML loading
- engine and rule interaction
- lexicon-backed behavior
- end-to-end CoNLL-U generation

### Regression tests

Regression tests freeze previously fixed bugs.

Every bug fix should add at least one regression fixture.

## Standard commands

Run the full suite:

```bash
mvn clean verify
```

Run the sample pipeline:

```bash
bash scripts/run-sample.sh
```

## Rule testing advice

Every important rule should have:

- a positive case
- a negative case
- a boundary case

## Snapshot strategy

For stable end-to-end tests, store short expected `.conllu` outputs and compare them exactly.

That is usually more valuable than testing tiny implementation details in isolation.
