# Release process

## Before release

Check the following:

- all tests pass
- README is up to date
- examples still run
- changelog has been updated
- default rule pack is consistent with current engine behavior

## Local release build

```bash
mvn clean package
bash scripts/release.sh v0.1.0
```

## GitHub release

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Release contents

A release should include:

- executable jar
- baseline rules
- lexical resources
- runnable examples
- project README
- changelog

## Versioning advice

The engine version and the rule-pack version do not have to evolve at the same speed.

A practical model is:

- engine: `0.x.y`
- rules: `YYYY.MM`
