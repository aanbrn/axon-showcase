## Why

The `showcase-identifier-extension` module provides the foundational identity layer for the entire axon-showcase system —
KSUID generation via Axon's `IdentifierFactory` SPI and Bean Validation of KSUID-formatted strings across commands,
queries, and REST endpoints. Its behavior is currently undocumented in the spec catalog, making it difficult to reason
about changes that touch identity, validation, or pagination cursor semantics.

## What Changes

- Adds a new spec capturing the `showcase-identifier-extension` module's behavioral contract: KSUID generation via Axon
  SPI, `@KSUID` Bean Validation constraint semantics, null-handling rules, and the module's role as a shared dependency.

## Capabilities

### New Capabilities

- `showcase/identifier-extension`: KSUID identifier generation and validation — covers the Axon `IdentifierFactory` SPI
  integration, the `@KSUID` constraint annotation and `KsuidValidator` behavior, null-safety conventions, and the
  module's dependency exposure rules (`api` vs `implementation`).

### Modified Capabilities

_(none — this is a pure documentation capture with no behavioral changes)_

## Impact

- **Specs only**: no code, build, or deployment changes.
- Adds `openspec/specs/showcase/identifier-extension/spec.md` to the spec catalog.
- Future changes touching identity generation or `@KSUID` validation can reference this spec as a baseline.
