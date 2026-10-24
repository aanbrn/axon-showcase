# ADR-0022: Projection failure policy — idempotent versioned writes and causal dispatch

Date: 2026-10-23

Status: Accepted

## Context

`ShowcaseProjector` applies each consumed event with a non-idempotent bulk write and acknowledges the batch whatever the
per-item outcome. Under at-least-once delivery that produced three defects: a replayed `ShowcaseScheduledEvent` logged a
version conflict, a later event could overwrite the state an earlier one had written, and an OpenSearch load rejection
returned as a per-item error inside a successful response was logged and acknowledged, so the update was never retried.

An analysis of the hand-written service (2026-10-23) confirmed the divergence and compared the recovery policies. The
owner chose a staged plan rather than one large change, and within the first stage chose the causal-dispatch mechanism
over a narrowed best-effort claim. The owner also confirmed the remaining boundary: a retryable item whose budget is
exhausted is logged and acknowledged, not stalled.

## Decision

1. **The aggregate sequence is the version.** Each create and update carries the event message's sequence
   (`DomainEventMessage.getSequenceNumber()`), and the document stores it in a `sequence` long field.
2. **Updates merge only when newer.** An inline OpenSearch script merges a partial document only when the incoming
   sequence is greater than the stored one; an equal or lower sequence is a no-op. External versioning is rejected
   because existing documents carry internal `_version` values a new external version can be lower than, and `if_seq_no`
   is rejected for its extra read per event.
3. **Redelivery converges.** A create conflict and a delete not-found are recognized as an already-applied event's
   outcome, not logged as errors. The delete stays unguarded: it merges no document, a removed event is terminal for its
   aggregate, and an ordered replay of the aggregate's suffix reconciles the final state.
4. **An aggregate's successor waits.** A batch is drained in rounds with at most one event per aggregate per round; a
   retryable item keeps its aggregate's successor undispatched. This relaxes total partition order to per-aggregate
   order, which the behaviour spec states.
5. **Only retryable statuses retry.** A bulk item's HTTP status classifies it: `429` and `503` are retried with the
   existing `showcase.projector.retry` budget; every other failure is terminal and logged. A batch's records are
   acknowledged only after every item has a resolved outcome — success, convergence, or a logged skip.
6. **The upgrade rebuilds the read model.** Because documents written before this change carry no sequence, and a
   rolling update would mix writers, the upgrade verifies the retained Kafka history covers every showcase's creation
   event, stops the projector, deletes the index, resets the projection consumer group's offsets, and deploys with
   `Recreate`. A document without a sequence, or a deployment that skips the rebuild, is outside the guarantee.

Alternatives considered and rejected:

- _Fail-stop: stall the partition on any item failure._ Strongest per-record guarantee, but it couples availability to a
  human's response for data that is replayable; the parked repair work can add it if the owner prefers it.
- _A dead-letter queue in this change._ It cannot recover a partially applied aggregate without gap-aware repair (a
  superseded partial event is not replayable through the guard), so the queue alone would move the loss, not fix it.
- _Replacing every write with an upsert._ It can overwrite a later state with an older event, and partial events would
  create incomplete documents.
- _External versioning._ See decision 2.

## Consequences

### Upgrade procedure

1. Prove replay coverage by event identity, not by timestamps: page the current index's ids with a point-in-time or
   `search_after` (`GET showcases/_pit`, then `_search` with `size=10000` and `search_after`) until the listing is
   exhausted — a single `size=10000` page truncates silently above the `index.max_result_window` — and collect the
   `ShowcaseScheduledEvent` creation events from the topic's retained range. Every indexed showcase id must have its
   creation event. IF any indexed showcase lacks its creation event, THEN abort before deleting anything — the index
   stays, and the authoritative reconstruction belongs to the deferred rebuild unit.
2. Scale the projection Deployment to zero and wait for its pod to terminate, so no writer is active for the rest of the
   procedure.
3. Record a recoverable checkpoint and the offsets: an OpenSearch snapshot when the deployment has a snapshot
   repository, otherwise a clone (`PUT showcases/_settings {"index.blocks.write": true}`, then
   `POST showcases/_clone/showcases-checkpoint`); if neither is possible, abort. Record the `showcase-projector` group's
   committed offsets. Both the clone and the write block happen only after step 2, so no event is rejected during the
   window.
4. Validate the reset's plan without executing it (`kafka-consumer-groups --reset-offsets --to-earliest --dry-run`) and
   confirm the command targets the configured group and topic. Run the CLI inside the Kafka pod with the container's JMX
   variables cleared (`env JMX_PORT= KAFKA_OPTS= kafka-consumer-groups.sh …`); otherwise it tries to bind the broker's
   JMX port and fails.
5. Delete the `showcases` index and reset the `showcase-projector` group's offsets with the `kafka-consumer-groups` CLI
   (`--reset-offsets --to-earliest --execute`) while the group is inactive; that CLI rejects a reset on an active group.
   The same cleared-JMX invocation applies.
6. Deploy with `projectionService.updateStrategy.type: Recreate` (the chart emits `rollingUpdate: null` beside it,
   because a server-side apply that only changes the type keeps the existing release's `rollingUpdate` block and the API
   rejects the pair); the index-initialization Job (a `pre-install, pre-upgrade` hook) recreates the index with the
   `sequence` mapping and the projector replays from the retained start.
7. Verify separately:
   - the upgrade completes (`helm upgrade --wait` fails when the hook fails, and the Job is deleted on success), the
     mapping carries the field (`GET showcases/_mapping`), and a replayed document shows its `sequence`
     (`GET showcases/_search?size=1`);
   - the projector logs no missing-document failure for a retained creation;
   - the replay is complete: once the `showcase-projector` group's lag reaches zero, page the index's ids with a
     point-in-time or `search_after` (as in step 1), build the expected surviving set — the ids whose retained range
     carries a `ShowcaseScheduledEvent` and whose latest retained event is **not** a `ShowcaseRemovedEvent` (a removed
     showcase must be absent, since that event deletes the document) — and require the sets to be equal. A missing id is
     a lost document, and a scheduled-only showcase produces no later missing-document error to reveal it;
   - the replay's projector log carries no ERROR line. A loss is logged at ERROR — for example `Bulk item failed` for a
     terminal bulk item, `skipped after a terminal write outcome` for the batch entry it leaves unresolved,
     `skipped after exhausting the retry budget` for an exhausted retry, or
     `Skipped event message without an aggregate sequence` for a showcase event that cannot be ordered — so the check is
     the ERROR level itself, not one message, and the log is the only per-item record of the loss.
8. Roll back in the same stop-first order the forward procedure uses: scale the projection Deployment to zero and wait
   for its pod to terminate, so the new projector cannot write while the checkpoint is restored; then restore the
   checkpoint (delete `showcases` and clone `showcases-checkpoint` back, or restore the snapshot) and clear the restored
   index's inherited write block (`PUT showcases/_settings {"index.blocks.write": false}`) — a clone carries step 3's
   block, and without the clear the old projector cannot write — then reset the group's offsets to the recorded values,
   revert the code, and deploy. A document written by the new code carries the extra `sequence` field, which the
   previous version ignores.

- The parked units own the remaining gap: a dead-letter queue with re-drive, gap-aware repair of a superseded partial
  event, a read-model rebuild with its runbook, and the projector's operability state and buffer-unit correction
  (`docs/ideas.md`, 2026-10-23).
- A terminal item failure still loses the fields that event carried; that is the best-effort boundary the owner
  accepted, recorded here rather than implied by the code.
- The upgrade procedure is destructive and conditional on the retention check; a deployment whose retained history is
  incomplete keeps the old index and remains outside the guarantee.
- The `showcaseProjector.batch.size` metric keeps its meaning (the incoming batch's event count, recorded once per
  batch), because a batch now spans several bulk requests.
