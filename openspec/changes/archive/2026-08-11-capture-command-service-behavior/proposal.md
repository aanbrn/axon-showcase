## Why

The `showcase-command-service` write side (Axon aggregate, saga, interceptors, Kafka publisher, PostgreSQL event store)
exists and is tested but has never been captured in an OpenSpec spec. Without a spec, its externally observable behavior
is undocumented and changes to it are not reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the command service's current behavior as requirement
  statements (MUST clauses), derived from the existing implementation and its component/integration tests.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/command-service`: Current behavior of the write side — the four commands supported on the Axon distributed
  command bus, the aggregate's state machine transitions and emitted events, validation rules, error translation,
  saga/deadline behavior, Kafka event publishing to `axon-showcase-events`, and the PostgreSQL event/saga/scheduler/title
  reservation stores.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-command-service-behavior/specs/showcase/command-service/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.