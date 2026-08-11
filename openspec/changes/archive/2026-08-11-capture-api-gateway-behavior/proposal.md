## Why

The `showcase-api-gateway` REST entry point (command and query endpoints under `/showcases`, asynchronous write handling
with idempotency keys, in-memory cache fallback on query failures, and structured error mapping) exists and is tested but
has never been captured in an OpenSpec spec. Without a spec, its externally observable behavior is undocumented and
changes to it are not reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the API gateway's current behavior as requirement statements
  (MUST clauses), derived from the existing implementation and its component/integration tests.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/api-gateway`: Current behavior of the REST entry point — the schedule/start/finish/remove write endpoints
  with idempotency-key handling and asynchronous acceptance on timeout, the list and by-ID read endpoints, in-memory
  cache fallback on transient query failures, validation of request parameters, and mapping of command/query errors to
  structured problem details.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-api-gateway-behavior/specs/showcase/api-gateway/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
