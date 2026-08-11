## Why

The `showcase-projection-service` read-model side (Kafka consumption, per-event OpenSearch projection, batch/retry
behavior) exists and is tested but has never been captured in an OpenSpec spec. Without a spec, its externally
observable behavior is undocumented and changes to it are not reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the projection service's current behavior as requirement
  statements (MUST clauses), derived from the existing implementation and its integration tests.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/projection-service`: Current behavior of the read-model side — Kafka subscription to `axon-showcase-events`
  with consumer group `showcase-projector`, deserialization of Axon event messages, the per-event OpenSearch writes to
  the `showcases` index (create/update/delete), batch and retry semantics, per-partition ordering, at-least-once
  delivery via offset acknowledgements, and failure handling for missing documents and duplicates.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-projection-service-behavior/specs/showcase/projection-service/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.