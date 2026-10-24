# Proposal

## Why

`ShowcaseProjector` applies each event with a non-idempotent write and acknowledges the batch whatever the per-item
outcome. Under at-least-once delivery a replayed `ShowcaseScheduledEvent` is logged as a version conflict, a late older
event can overwrite newer state, and a transient OpenSearch item rejection (`429`, `es_rejected_execution_exception`) is
acknowledged and never retried — the read model can diverge from the event log with no repair path. The behaviour spec
codifies "logged and processing continues", so the current guarantee covers delivery, not convergence. This change makes
projection writes idempotent, dispatches an aggregate's events causally, and retries transient item failures before
acknowledging.

## What Changes

- `showcase-projection-model/.../ShowcaseEntity.java`: add the aggregate sequence of the last applied event, mapped as a
  long field, so a write can be compared with the state it would overwrite.
- `showcase-projection-service/.../OpenSearchProjectionWriter.java` (new): map each event to its bulk operation —
  carrying the create/update aggregate sequence and leaving the delete unversioned — apply the guard script that merges
  only a newer sequence, execute the request with an immediate refresh, retry a transient call failure, and classify
  each item's outcome.
- `showcase-projection-service/.../ShowcaseProjector.java`: treat a create conflict or a missing-document delete caused
  by redelivery as convergence rather than an error.
- `showcase-projection-service/.../ShowcaseBatchWriter.java` (new): schedule each round with at most one event per
  aggregate, so an aggregate's successor is not dispatched while its predecessor is unresolved; a retryable item
  therefore cannot be overtaken by a later event of the same aggregate.
- `showcase-projection-service/.../ShowcaseBatchWriter.java`: re-send the unresolved items of the round, bounded by the
  existing `showcase.projector.retry` settings; terminal item failures keep today's logged-and-continue outcome until
  the parked dead-letter step.
- `helm/chart/src/main/helm/values.yaml`: set `projectionService.updateStrategy.type` to `Recreate`, so an upgrade never
  runs the previous projector concurrently with the guarded one.
- The change's delta specs (`…/specs/showcase/read-side/projection-service/spec.md` and `…/projection-model/spec.md`);
  the design's Amendments table is the single list of the clauses they amend.
- `docs/adr/0022-*.md`: record the projection failure policy — idempotent versioned writes now, dead-letter and rebuild
  next, fail-stop rejected.
- `docs/ideas.md`: park the dead-letter/re-drive, rebuild/runbook, and projector-operability units the analysis
  surfaced.
- `showcase-projection-service/.../ShowcaseProjector.java` and the bundled Grafana dashboard: rename the ingest-age
  timer from `projectionLag` to `eventAge`, matching what it measures, and follow the name in the dashboard's queries.
- Tests: unit coverage of the event-to-operation mapping, the status classification and the dispatch rounds; component
  coverage of a retryable item whose aggregate's successor waits and of a batch completing only after the drain;
  integration coverage of a duplicate create, an identical-record redelivery, an older-sequence write, and the seeded
  `sequence` of a created document.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/read-side/projection-service`: duplicate creates converge instead of being logged as errors, replayed older
  events no longer regress the document, and transient item failures are retried before the batch is acknowledged.
- `showcase/read-side/projection-model`: the document's field mapping gains the `sequence` long field.

## Impact

- `showcase-projection-model`: one new document field and its Jackson round-trip coverage; the query service and web UI
  read the entity but need no change (the field is not exposed in their responses).
- `showcase-projection-service`: the projector's write path and its tests, plus the removal of its dead
  `BPE_DEFAULT_DB_HOSTS` image default (the service has no datasource; the rest of its dead-configuration sweep stays
  parked).
- OpenSearch index: the `showcases` mapping gains a long field, and the upgrade rebuilds the index and resets the
  projection consumer group so every document is written by the guarded code from sequence 0 — conditional on the
  retained history covering each showcase's creation event, which the upgrade verifies before deleting anything.
- Helm chart: `projectionService.updateStrategy.type` becomes `Recreate` (the values default), so no old and new
  projector overlap; the dashboard's ingest-age queries follow the renamed timer.
- No API change and no runtime dependency change; the projector's component-test suite gains `reactor-test` for
  `StepVerifier`. No new infrastructure.
