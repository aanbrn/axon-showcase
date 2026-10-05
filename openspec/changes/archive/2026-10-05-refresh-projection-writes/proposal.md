# Proposal

## Why

The projector writes to OpenSearch with no refresh policy, so a projected write becomes searchable only at the index's
scheduled refresh (default 1 s). The web UI reconciles the read model by refetching the list until an event's effect is
visible (a 200 ms debounce plus up to 5 × 500 ms settles), because that refresh window is unpredictable — and each extra
refetch is another gateway plus query-service round trip. Forcing a refresh on the projector's bulk makes a projected
write searchable as soon as the write completes, so the UI's first refetch can see it.

## What Changes

- `showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java` — set `Refresh.True` on the
  `BulkRequest` the projector executes, and extract the request's construction into a package-private method so the
  policy is unit-testable.
- `showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorTests.java` (new) — assert the
  projector's bulk request carries `Refresh.True`.
- No spec delta: no capability spec covers projection visibility timing (the `read-side/projection-service` spec
  describes document contents, batching, ordering, acknowledgement, and at-least-once delivery, not when a write becomes
  searchable), so `.openspec.yaml` sets `skip_specs: true`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The change is a projection-write mechanism (when a write becomes searchable) with no spec'd scenario outcome; the
`read-side/projection-service` requirement that offsets are acknowledged only after the writes complete still holds.

## Impact

- **Code:** the projection service's write path only — the bulk request gains a refresh. No gateway, query-service, web
  UI, API, or contract change.
- **Build/tests:** a new unit test on the bulk request; the existing projector integration suite stays green.
- **Deployment:** an OpenSearch refresh per projector batch. Refresh is the most expensive index operation, so the
  write-throughput cost must be measured — the change owes a load-test re-measure against the committed baseline, and
  its acceptance depends on the result (a regression falls back to the bounded alternative, a shorter index
  `refresh_interval`).
