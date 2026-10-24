# Design

## Context

See `proposal.md` for the motivation. The constraints that shape the approach:

- The projector receives `EventMessage`s that the Kafka converter rebuilds as `GenericDomainEventMessage`s, carrying the
  aggregate type, id and sequence from the record headers (`DefaultKafkaMessageConverter.buildMessage`, axon-kafka
  4.12.1). The aggregate sequence is therefore already available on the consumption side; no event payload changes.
- The publisher keys each record by Axon's sequencing policy — the aggregate identifier — so all events of a showcase
  share a partition (`DefaultKafkaMessageConverter.recordKey`, `SequentialPerAggregatePolicy`).
- Each event writes only the fields it carries (a partial document); the projection is not a full-state snapshot per
  event. The mapping converter omits null-valued properties unless `storeNullValue` is set
  (`MappingElasticsearchConverter.writeProperties`), which is what makes the partial write work.
- Reactor Kafka forces `enable.auto.commit=false` and commits acknowledged offsets on its default five-second interval,
  so acknowledging after the write bounds a replay to the uncommitted suffix of a partition.
- The entity declares no `@Version` property, and OpenSearch maintains an internal `_version` that the projector never
  sets explicitly.
- The projector's batch processing is private and its acknowledgement sits inside the `start()` stream, so no component
  test can drive a batch today.

## Goals / Non-Goals

**Goals:**

- A redelivered event leaves a document already at or beyond that event's sequence unchanged.
- A create or update older than the document's state never regresses it.
- A retryable per-item OpenSearch failure is retried before the batch's offsets are acknowledged, without letting a
  later event of the same aggregate overtake it.

**Non-Goals:**

- Durable handling of terminal item failures (dead-letter queue, gap-aware repair) — parked as its own unit.
- Read-model rebuild as a general recovery facility and its runbook — parked as its own unit.
- Projector operability state, buffer-unit correction, and configuration hygiene (beyond the dead `BPE_DEFAULT_DB_HOSTS`
  image default removed here) — parked.
- Fencing an outstanding server-side write across a partition reassignment — not established by this change (see Risks).
- Claiming exactly-once effects.

## Decisions

### The aggregate sequence is the version, taken from the event message

`DomainEventMessage.getSequenceNumber()` is monotonic per showcase and identical across redeliveries, which is exactly
what a guard needs.

- Rejected: the event timestamp — not monotonic across producers.
- Rejected: the Kafka offset — per partition, not per aggregate, and changes across topics.
- Rejected: a new payload field — a command-API change for information the transport already carries.
- A `ShowcaseEvent` delivered as a plain `EventMessage` — the converter's fallback when the aggregate headers are absent
  — carries no sequence: it is counted as a failure and skipped, because it cannot be ordered against the document.

### A stored `sequence` field guards updates through an inline script

The document stores `sequence` (the aggregate sequence of the last applied event). An update is a scripted update that
merges the partial document only when the incoming sequence is greater:

```text
if (ctx._source.sequence == null || params.sequence > ctx._source.sequence) {
  ctx._source.putAll(params.doc);
  ctx._source.sequence = params.sequence;
}
```

- Rejected: **external versioning** (`version` + `versionType=external`). Existing documents carry internal `_version`
  values that a new external version can be lower than, so an upgrade would return a per-item version conflict — logged
  as an error — until the aggregate sequence passes the stored `_version`, and only a rebuild clears a permanently ahead
  document, which is a recovery facility this change does not own.
- Rejected: **`if_seq_no`/`if_primary_term`**. OpenSearch-internal sequence numbers require a read per event, adding a
  round trip and a race between the read and the write.
- The delete is unguarded: a delete has no partial document to merge, Removed is terminal for its aggregate, and an
  external-version guard is rejected above; the replay model below assumes no outstanding write lands after it.

### An aggregate's next event is dispatched only after its predecessor is resolved

The batch is grouped by showcase id and drained in rounds:

- Each round sends one bulk with at most one event per aggregate — the next undispatched event of each.
- A successful item makes that aggregate's next event eligible for the following round.
- A retryable item stays pending and keeps that aggregate's successor undispatched.
- A terminal item is logged, skipped, and the aggregate advances; the resulting partial state is the documented residual
  the parked repair unit owns.
- The batch completes — and its records are acknowledged — only after every item of the batch has a resolved outcome:
  success, convergence, or an explicitly logged terminal/exhausted skip. Dispatching a round's request is not an
  outcome; its response must be received and classified.

The alternative — one bulk carrying an aggregate's consecutive events — is rejected: OpenSearch applies bulk items
independently, so a retryable item at sequence `k` with `k+1` in the same bulk would leave `k`'s fields permanently
suppressed by the guard after the retry.

This relaxes the previous total partition order to per-aggregate order: a partition input `A1, A2, B1` may be written
`A1, B1` then `A2`. The ordering requirement is amended to say so explicitly.

### Retryable item failures re-send the unresolved items

- A retryable outcome is identified by the item's HTTP status: `429` (rejected under load) and `503` (unavailable).
- Within a round, a retryable item is re-sent with the existing `showcase.projector.retry` budget — the configured
  minimum backoff doubling per re-send, capped at 2^10 of it so a long budget does not grow the delay unboundedly — each
  send itself inside the retained call-level retry; the unresolved items are the failed ones, never their undispatched
  successors.
- When the budget is exhausted, the item keeps the logged-and-acknowledged best-effort outcome; the owner confirmed that
  boundary, and ADR-0022 records it.
- Terminal item statuses — mapping, parsing, missing document, version conflict — are not retried.

### Redelivery converges within the ordered-replay model

- A `create` whose document exists is a redelivery of the same `ShowcaseScheduledEvent` — only that event creates — so
  the conflict is recognized as convergence: no error log, no retry.
- A `delete` of a missing document is the delete's intended end state: no warning, no retry.
- An ordered replay of a deleted aggregate's full suffix (create through remove) recreates the document transiently and
  deletes it again; the guarantee is convergence to the same final state, not that every intermediate write is a no-op.
- The delete is unguarded, and the guarantee assumes no outstanding write from a revoked consumer lands after it.

### The write path is three components, not one

- `ShowcaseProjector` owns the Kafka lifecycle, record conversion and the batch acknowledgement; it takes the batch
  writer, not OpenSearch.
- `ShowcaseBatchWriter` owns the causal scheduling — per-aggregate queues, rounds, the retry budget and the completion.
- `OpenSearchProjectionWriter` owns the OpenSearch mechanics — mapping, the guard script, the refresh policy, the
  call-level retry and the item classification — and implements the writer's `RoundExecutor` contract.

Each entry point is a production contract (`write` for the writer, `execute` for the adapter), so the tests drive those
rather than a widened private method. Rejected: keeping the mapping and classification as package-private methods on the
projector for their tests, which is what the first draft did.

### The projection stays a partial-document projection

An event that is skipped leaves the fields it carried missing until a later event carries them. That residual gap is the
repair unit's subject; this change does not claim to reconstruct it, and the spec's missing-document scenario stays as
it is.

### Legacy documents and mixed-version writers are outside the guarantee

- A document written before this change has no `sequence`. The first post-upgrade event would seed it, but a replayed
  older event could then regress a legacy FINISHED document to STARTED.
- A rolling update would also let the previous projector (which ignores the guard) write concurrently with the new one.

The migration therefore stops the projector before touching the index, rebuilds the index and resets the consumer group,
and deploys with `Recreate`; when the retained history is complete (the precondition below), every document is written
by the guarded code from sequence 0. A deployment that skips the rebuild, or whose retained history is incomplete, is
outside the guarantee.

## Amendments

Each amended clause of a `MODIFIED` requirement, beside its replacement, because the delta replaces the whole
requirement. This table is the single list of amended clauses; the proposal points at it.

| Requirement                                                               | Amended clause                                                                                                                                     | Replacement                                                                                                                                                                                                         | Why                                                                                                                               |
| ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `projection-service` / Kafka event consumption                            | — (new scenario)                                                                                                                                   | a `ShowcaseEvent` without the aggregate headers is counted as a failure and not projected                                                                                                                           | it carries no sequence to order it by                                                                                             |
| `projection-service` / Projection to the showcases index                  | "applying one write operation per event"                                                                                                           | "…and storing the aggregate sequence of the last applied event on the document"                                                                                                                                     | the guard needs the last-applied sequence                                                                                         |
| `projection-service` / Scheduled, Started, Finished scenarios             | field list without a version                                                                                                                       | field list plus "and its aggregate sequence"                                                                                                                                                                        | seed and maintain the guard value                                                                                                 |
| `projection-service` / Duplicate create is surfaced                       | "the item failure is logged (version conflict) and does not prevent further records from being processed"                                          | "the conflict is recognized as a redelivery of an already-applied event, the existing document is left unchanged, and processing continues"                                                                         | a replay is expected under at-least-once; logging it as an error is the defect                                                    |
| `projection-service` / Delete of a missing document is surfaced           | "the not-found outcome is logged as a warning and does not prevent further records from being processed"                                           | "the not-found outcome is recognized as the delete's intended end state and does not prevent further records from being processed"                                                                                  | deleting an absent document reaches the desired state                                                                             |
| `projection-service` / Older replayed event does not regress the document | — (new scenario)                                                                                                                                   | a lower-sequence create/update is a no-op and preserves the document                                                                                                                                                | the anti-regression guarantee the change adds                                                                                     |
| `projection-service` / Batch processing and ordering — body               | "preserving per-partition order … acknowledge record offsets only after the batch's OpenSearch writes complete"                                    | "preserving per-aggregate order — independent aggregates may interleave across a batch's dispatch rounds … only after every item of the batch has a resolved outcome"                                               | the drain writes one event per aggregate per round; dispatching a request is not an outcome                                       |
| `projection-service` / Batch processing and ordering — acknowledgement    | "a batch's OpenSearch bulk write completes successfully"                                                                                           | "every item of a batch has a resolved outcome — success, convergence, or a logged skip — across all of the batch's dispatch rounds"                                                                                 | the drain makes acknowledgement follow every item's response, not one bulk's completion                                           |
| `projection-service` / Batch processing and ordering — failed writes      | "an OpenSearch write fails with a transient error … retried with exponential backoff up to a maximum attempt count before the stream is restarted" | "a bulk item fails with a retryable outcome … the unresolved items of that round are re-sent with backoff up to the configured maximum attempt count, and when that budget is exhausted the item failure is logged" | OpenSearch reports load rejections as per-item errors inside a 200 response; an exhausted item budget does not restart the stream |
| `projection-service` / Batch processing and ordering — new scenario       | — (new scenario)                                                                                                                                   | "A later event is not dispatched while its predecessor is unresolved"                                                                                                                                               | the invariant that makes a retry safe for a partial aggregate                                                                     |
| `projection-service` / Batch processing and ordering — stream restart     | "the OpenSearch write exhausts its retries"                                                                                                        | "a bulk call fails with a transient error after its retries"                                                                                                                                                        | only the call-level failure restarts the stream                                                                                   |
| `projection-service` / At-least-once delivery                             | "the resulting duplicate write is logged as an error but the stream continues"                                                                     | "the redelivered write converges on the document's current state and the stream continues"                                                                                                                          | duplicates are expected; the document may already be advanced                                                                     |
| `projection-model` / Field mapping                                        | field list without a version                                                                                                                       | field list plus "`sequence` as `long`"                                                                                                                                                                              | the document carries the guard value                                                                                              |

## Risks / Trade-offs

- **The inline script is a runtime string** — a typo fails at write time, not compile time. Mitigated by an integration
  test that applies an older sequence after a newer one against real OpenSearch and asserts the document is unchanged;
  the same test asserts the false branch leaves the document's fields and `sequence` untouched.
- **A single hot aggregate costs one round trip per event**, because a round carries at most one event per aggregate;
  many aggregates still fill a round. Accepted for a showcase workload.
- **An outstanding write from a revoked consumer is not fenced** — the guard prevents a regression, but a late create
  after the delete's replay could recreate a removed document. Named here; the repair unit owns recovery.
- **Terminal item failures still lose that event's fields** — the documented best-effort boundary the owner accepted.
- **Behavior change for existing tests** — the integration cases that assert an error or warning on a duplicate now
  assert convergence, and must also assert that no error was logged and that a field the event does not carry is
  preserved.
- **The `showcaseProjector.batch.size` metric keeps its current meaning** — the batch's incoming event count, recorded
  once per batch; it does not become a per-bulk figure.
- **The ingest-age timer is renamed** from `showcaseProjector.projectionLag` to `showcaseProjector.eventAge`, because it
  measures event age at ingest rather than projection latency; the bundled dashboard's queries move with it, and an
  external alert on the old series needs the same rename.

## Migration Plan

The upgrade procedure is ADR-0022's `Upgrade procedure`; this design fixes the constraints it must satisfy:

- Replay coverage is proved by event identity — every showcase id in the current index must have its creation event in
  the topic's retained range — and the abort runs before anything is deleted; a count or timestamp comparison is not a
  proof, since the design rejects timestamps as a version for the same non-monotonicity.
- The old projector is stopped first, before the checkpoint blocks writes and before any destructive step; a clone or
  snapshot needed later would otherwise reject the still-running projector's writes.
- A recoverable checkpoint (a snapshot, or a clone after blocking writes) and the group's committed offsets are recorded
  before the index is deleted; without either, the upgrade aborts.
- The consumer group's offsets are reset while the group is inactive, because that CLI rejects a reset on an active
  group.
- The read model is rebuilt from sequence 0, so no document keeps a pre-change state.
- The rollback repeats the forward procedure's stop-first order before it restores the checkpoint: a new projector still
  running would write concurrently with the restoration.
- The rollback clears the write block a clone checkpoint carries, or the restored index accepts no writes from the old
  projector.
- The verification reads six signals separately: the upgrade's completion, the mapping, a replayed document's
  `sequence`, the absence of missing-document failures for a retained creation, the replay's completeness — the index's
  id set equals the retained ids whose latest event is not a removal, which a scheduled-only showcase needs because it
  produces no later missing-document error — and a projector log with no ERROR line: a loss (a terminal item, an
  exhausted retry, an unorderable event message) is logged at ERROR, so the check is the level, not one message.

## Claims

| Claim                                                                                                                                                                                              | Source                                                                                                                                                                                                        |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The Kafka converter rebuilds a `GenericDomainEventMessage` carrying the aggregate sequence from the record headers, and falls back to a plain `EventMessage` when the aggregate headers are absent | axon-kafka 4.12.1 sources, `DefaultKafkaMessageConverter.buildMessage` / `buildDomainEventMessage` / `buildEventMessage` (Gradle cache)                                                                       |
| The record key is the aggregate identifier, so an aggregate's events share a partition                                                                                                             | axon-kafka 4.12.1 sources, `DefaultKafkaMessageConverter.recordKey` (`SequentialPerAggregatePolicy` default)                                                                                                  |
| Reactor Kafka forces `enable.auto.commit=false`                                                                                                                                                    | reactor-kafka 1.3.25 sources, `ImmutableReceiverOptions` line 113                                                                                                                                             |
| Reactor Kafka commits acknowledged offsets on a 5s default interval                                                                                                                                | reactor-kafka 1.3.25 sources, `ImmutableReceiverOptions` line 102 (`commitInterval = Duration.ofMillis(5000)`)                                                                                                |
| Each event writes a partial document, because the converter omits null-valued properties                                                                                                           | spring-data-elasticsearch 5.5.13 sources, `MappingElasticsearchConverter.writeProperties` (null branch, `storeNullValue`)                                                                                     |
| A bulk item exposes an HTTP status and an error cause                                                                                                                                              | opensearch-java 3.9.0 sources, `BulkResponseItem.status()` / `error()`                                                                                                                                        |
| Before the change, the projector's per-batch processing is private and `start()` acknowledges after it                                                                                             | `git show HEAD:showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java` (`processMessages`/`processEvents` private; the `concatMap` acknowledgement in `start()`)                |
| A Removed event is terminal for its aggregate                                                                                                                                                      | `ShowcaseAggregate` (`on(ShowcaseRemovedEvent)` → `markDeleted()`)                                                                                                                                            |
| A delete bulk operation carries no document to merge (it does expose `version`/`versionType`)                                                                                                      | opensearch-java 3.9.0 sources, `DeleteOperation` and `BulkOperationBase` (its builder)                                                                                                                        |
| Kafka's consumer-group CLI rejects an offset reset on an active group                                                                                                                              | Not established from the repository; the ADR names the `kafka-consumer-groups` command the procedure uses, and the inactive-group rule is that command's, to be confirmed at the upgrade                      |
| The batch-size metric records the incoming batch's event count once per batch                                                                                                                      | `git show HEAD:showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java` (`batchSizeDistribution.record(events.size())`; its description "Size of processed batches (in events)") |
| Only `ShowcaseScheduledEvent` creates the document                                                                                                                                                 | `OpenSearchProjectionWriter.toBulkOperation`, the `switch` arms                                                                                                                                               |
| The entity declares no `@Version` property (the added `sequence` is the guard's value)                                                                                                             | `git show HEAD:showcase-projection-model/src/main/java/showcase/projection/ShowcaseEntity.java`                                                                                                               |
| `ShowcaseEntityMappingCT` asserts nine mapped properties before the change                                                                                                                         | `git show HEAD:showcase-projection-model/src/componentTest/java/showcase/projection/ShowcaseEntityMappingCT.java` (its `noUnexpectedFields` case)                                                             |
| The event store carries a per-event timestamp in a `VARCHAR` column                                                                                                                                | `showcase-command-service/src/main/resources/db/migration/postgresql/V0_1_0_1__axon-event-store.sql` (`domainevententry."timestamp"`)                                                                         |
| The default retry budget is 3 attempts with a 100 ms minimum backoff                                                                                                                               | `ShowcaseProjectorProperties` (`new Retry(3, Duration.ofMillis(100))`) and `showcase-projection-service/src/main/resources/application.yml` (`max-attempts: 3`, `min-backoff: PT0.1S`)                        |
| The test publisher increments the aggregate sequence except `publishEventWithSequence`, which carries an explicit one                                                                              | `showcase-test/src/main/java/showcase/test/KafkaTestPublisher.java` (`publishEvent`, `publishEventWithSequence`)                                                                                              |
| The query service's MapStruct mapper ignores the new source property                                                                                                                               | `ShowcaseMapper` (`unmappedTargetPolicy = IGNORE`; source policy is the default)                                                                                                                              |
| The index-initialization Job is a `pre-install,pre-upgrade` hook                                                                                                                                   | `helm/chart/src/main/helm/templates/query-service/index-initialization-job.yaml` (its annotations)                                                                                                            |
| An existing index receives the mapping through `putMapping`                                                                                                                                        | `ShowcaseQueryApplication.opensearchIndexInitializer` (the `indexOperations.exists()` branch)                                                                                                                 |
| The projection service's update strategy is configurable and defaulted to RollingUpdate before the change                                                                                          | `git show HEAD:helm/chart/src/main/helm/values.yaml` (`projectionService.updateStrategy.type`) and its Deployment template                                                                                    |

Dismissed candidates from `scripts/claim-scan.py`, by kind — one reason per kind, covering every kind the scan emits:

- `absolute` (`only`, `never`, `none`): rhetorical quantifiers whose scope the enclosing sentence states.
- `count`: task numbering, the ledger rows' own numeric fields, and the protocol values tabulated above
  (`429`/`503`/`200`) and the 9 → 10 mapping count.
- `cross-ref`: compound nouns (`dead-letter/re-drive`, `rebuild/runbook`, `create/update/delete`), and one upstream
  issue reference (`spring-data-elasticsearch#3334`) the main spec already cites.
- `path`: exact paths and commands that resolve; abbreviated artifact paths (`<module>/.../<File>.java`) name a file
  under that module; and the planned `docs/adr/0022-*.md`, which this change creates.
