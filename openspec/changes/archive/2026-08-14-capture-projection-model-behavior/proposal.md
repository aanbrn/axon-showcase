## Why

The `showcase-projection-model` module defines the `showcases` read-model document — its OpenSearch mapping, index sort
configuration, and serialization contract — but this contract is shared by three consumers (projection-service writes
it, query-service reads it, query-client deserializes it) and is owned by no spec. The projection-service spec covers
the lifecycle (which event populates which field) and the query-service spec covers query behavior, yet the index schema
that makes both work is only pinned by the `ShowcaseEntityMappingCT`/`ShowcaseEntityJacksonCT` tests.

## What Changes

- Add one new capability spec under the `read-side` group:
    - `showcase/read-side/projection-model`: the shared read-model document contract — the `showcases` OpenSearch index
      name, the `showcaseId` descending sort setting, the per-field mapping (keyword/text/date_nanos types, nanosecond
      date format, `duration` deliberately unmapped), and the Jackson serialization contract (round-trip fidelity,
      nanosecond precision, null preservation).
- The spec documents current behavior derived from the `ShowcaseEntity` implementation and its component tests. No
  behavioral or code changes.

## Capabilities

### New Capabilities

- `showcase/read-side/projection-model`: The read-model document contract — the `showcases` index and its sort setting,
  the OpenSearch field mapping (types and date formats, `duration` intentionally unmapped), and the Jackson
  serialization behavior shared by the projection and query sides.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-projection-model-behavior/specs/showcase/read-side/projection-model/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
