## Why

The `showcase-query-service` read side (REST protobuf transport, Axon query handlers searching OpenSearch, validation,
and error translation) exists and is tested but has never been captured in an OpenSpec spec. Without a spec, its
externally observable behavior is undocumented and changes to it are not reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the query service's current behavior as requirement
  statements (MUST clauses), derived from the existing implementation and its integration tests.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/query-service`: Current behavior of the read side — the REST endpoints `/query` and `/streaming-query` over a
  protobuf transport, dispatch on the Axon streaming query bus, the two supported queries (`FetchShowcaseListQuery`,
  `FetchShowcaseByIdQuery`), OpenSearch search behavior (filtering, sorting, pagination), query validation, and error
  mapping to structured problem details.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-query-service-behavior/specs/showcase/query-service/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
