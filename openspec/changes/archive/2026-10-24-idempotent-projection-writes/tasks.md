# Tasks

## 1. Versioned writes

- [x] 1.1 Add the nullable `Long sequence` field to `ShowcaseEntity` in `showcase-projection-model`, mapped with
      `FieldType.Long`; verify with `./gradlew :showcase-projection-model:compileJava` and
      `./gradlew :showcase-query-service:compileJava` (the mapper ignores the new source property). Verified: entity
      compiles in both modules.
- [x] 1.2 Extend `ShowcaseEntityJacksonCT` so the round-trip case includes a set `sequence`, and
      `ShowcaseEntityMappingCT` so it asserts `sequence` maps as `long` and its property count grows from 9 to 10;
      verify with `./gradlew :showcase-projection-model:componentTest`. Verified:
      :showcase-projection-model:componentTest green.
- [x] 1.3 Read the aggregate sequence from the consumed `DomainEventMessage` and carry it into the create and update
      bulk operations; verify with a unit test on the OpenSearch adapter asserting the create's document carries the
      sequence and the request refreshes, with the guard's behaviour covered by the integration case and the delete
      built without a version. Verified: `OpenSearchProjectionWriterCT` asserts the create sequence and the refresh; the
      older-started integration case exercises the guard.
- [x] 1.4 Replace the update's partial `document` with the guard script that merges only when the incoming sequence is
      greater, treating a missing `sequence` as `-1`; verify with an integration test that writes a newer sequence and
      then an older one against the container's OpenSearch and asserts the document is unchanged. Verified: the
      integration case writes a newer sequence then an older value and the document is unchanged.
- [x] 1.5 Add a fixed-sequence publish path to `KafkaTestPublisher` (the current methods always increment per aggregate)
      so an older-sequence write can be produced; verify with a unit test in `KafkaTestPublisherTests` asserting the
      captured message's sequence equals the explicit value, as its existing cases capture the message and not the
      record headers. Verified: `KafkaTestPublisherTests` asserts the explicit sequence.
- [x] 1.6 Assert the guard's false branch leaves the document untouched; verify in the same integration test that the
      stored document's fields and `sequence` are unchanged after the older write, and record the item's observed result
      rather than pinning one. Verified: the older-write integration case asserts the stored fields and `sequence` are
      unchanged.
- [x] 1.7 Assert the partial write preserves a field the event does not carry; verify that the started-event integration
      case still reads the `title` written by the scheduled event after the update. Verified: the started integration
      case asserts `title` survives the update.
- [x] 1.8 Confirm the seeded `sequence` of a created document and the mapped `params.doc` serialization; verify by
      reading the stored document's `sequence` after a create and after an update. Verified:
      `showcaseScheduledEvent_singleEvent_storesTheAggregateSequence`.

## 2. Causal dispatch and convergence

- [x] 2.1 Make the per-batch scheduling its own component (`ShowcaseBatchWriter`) whose `write` is the production entry
      point, so the component tier drives it with a recording executor; verify with
      `./gradlew :showcase-projection-service:test`. Verified: the unit-tier `ShowcaseBatchWriterTests` drives `write`
      with a recording `RoundExecutor`; `:showcase-projection-service:test` green.
- [x] 2.2 Drain the batch in rounds: each round carries at most one event per aggregate, and the batch completes only
      after every item has a resolved outcome; verify with the component case over two events of one aggregate and one
      of another, asserting the first round holds one event per aggregate, the second holds the successor, and the batch
      completes after the second. Verified: `ShowcaseBatchWriterTests.write_drainsOneEventPerAggregatePerRound`.
- [x] 2.3 Keep the successor undispatched while a retryable item is unresolved; verify with the component test that
      returns a retryable status for the predecessor and then success, asserting the successor appears only in a later
      round and that the emitted operations are the create plus the successor's guarded update (the merged document
      state itself is asserted by the integration cases of tasks 2.4 and 2.5). Verified: the retry component case
      asserts the successor only in a later round.
- [x] 2.4 Treat a create's version conflict as convergence: no error log, no retry; verify with the existing
      duplicate-scheduled integration case, updated to assert the document is unchanged and no `ERROR` line was
      captured, and with an integration case asserting a started update preserves the fields of the create (the merged
      state). Verified: `showcaseScheduledEvent_sameEventTwice_convergesWithoutError` and the started case.
- [x] 2.5 Verify the acceptance case in two halves, because only a stub can induce a deterministic per-item rejection:
      at the component tier, a started item rejected once with a retryable status while a finished event for the same
      showcase follows in the same batch, asserting the finished event is written only in a later round after the
      started one succeeds; at the integration tier, the existing started-then-finished case asserting the final
      document carries `startedAt`, `duration`, `status` = FINISHED, and `finishedAt` (real OpenSearch cannot be made to
      reject one item on demand). Verified: component ordering case plus the finished integration case.
- [x] 2.6 Treat a delete's not-found outcome as convergence: no warning log, no retry; verify with the existing
      duplicate-removed integration case, updated to assert no `WARN` line was captured. Verified:
      `showcaseRemovedEvent_sameEventTwice_convergesWithoutWarning`.
- [x] 2.7 Replay the identical record instead of a newly built one; extend `KafkaTestPublisher` (or the integration
      case) so the same headers and body are published twice, and verify the document is unchanged and no error is
      logged. Verified: `showcaseScheduledEvent_identicalRecordTwice_convergesWithoutError`.
- [x] 2.8 Assert processing continues after a converged redelivery; verify that a subsequent event in the same
      integration case applies to the document. Verified: the duplicate-scheduled case publishes a started event after
      the redelivery.

## 3. Retryable item failures

- [x] 3.1 Classify bulk item outcomes by HTTP status: `429` and `503` retryable, every other status terminal; verify
      with the adapter's unit cases covering applied, retryable, converged, and terminal (including an error-bearing
      delete 404). Verified: `OpenSearchProjectionWriterCT.execute_*`.
- [x] 3.2 Re-send the unresolved items of the round with the existing `showcase.projector.retry` backoff budget when any
      item reports a retryable status; verify with the component test asserting the re-send count for a retryable item.
      Verified: `write_retryableItemHoldsTheSuccessorAndIsResentAlone`.
- [x] 3.3 Keep the logged-and-acknowledged outcome once the budget is exhausted; verify with a component test asserting
      the logged outcome and that the batch completes (no stream restart). Verified:
      `write_exhaustedBudgetSkipsTheItemAndReleasesTheSuccessor`.
- [x] 3.4 Do not retry a terminal item status, including in a bulk that also carries a retryable one; verify with a
      component test asserting one execution for the terminal item and a re-send for the retryable one. Verified:
      `write_terminalItemIsNotResent`.
- [x] 3.5 Keep the call-level retry for a thrown transient failure; verify with the adapter's unit cases asserting the
      call is retried on a transient failure and not on a non-transient one. Verified:
      `OpenSearchProjectionWriterCT.execute_retriesTransientCallFailures` and `execute_doesNotRetryOtherFailures`.
- [x] 3.6 Assert the batch completes only after the drain and fails when a write fails, so the acknowledgement
      downstream cannot run early; add `libs.reactor.test` to the projection service's `componentTest` suite, verify
      with a component test using `StepVerifier` that a stubbed failing call leaves the batch Mono in error, and record
      that an integration test can observe only the 5s-bounded committed offset, not the ordering. Verified:
      `write_doesNotCompleteWhileTheFinalRoundIsPending` and `write_failedWriteLeavesTheBatchInError`.

## 4. Records, documentation, and migration

- [x] 4.1 Write ADR-0022 recording the projection failure policy — idempotent versioned writes and causal dispatch now,
      dead-letter and gap-aware repair next, fail-stop rejected — including the owner-confirmed best-effort boundary
      after an exhausted retry budget; verify the file exists with Status, Context, Decision, and Consequences.
      Verified: `docs/adr/0022-projection-failure-policy.md`.
- [x] 4.2 Park the deferred units in `docs/ideas.md` under a dated section: dead-letter queue with re-drive, read-model
      rebuild with runbook, and projector operability with the buffer-unit correction; the repair entry names gap-aware
      repair (a superseded partial event cannot be restored by replaying it through the guard); verify each carries the
      trailing status tag. Verified: `docs/ideas.md` 2026-10-23 entries.
- [x] 4.3 Set `projectionService.updateStrategy.type: Recreate` in `helm/chart/src/main/helm/values.yaml`; verify with
      `./gradlew :helm:chart:helmLintMainChartFull` and a rendered `strategy:` in the packaged chart's
      projection-service Deployment. Verified: the packaged chart renders `strategy: Recreate` for the
      projection-service Deployment.
- [x] 4.4 Document the upgrade procedure (scale the projection Deployment to zero, delete the index, reset the
      `showcase-projector` group's offsets with the `kafka-consumer-groups` CLI while it is inactive, deploy) in the
      ADR's Consequences and the change's report; verify the steps name the stop-first order, the CLI command, the group
      id, and the chart hook. Verified: the ADR's `Upgrade procedure` carries the stop-first order in both the forward
      steps and the rollback, the `kafka-consumer-groups` command and the group id.
- [x] 4.5 Re-read both delta specs against the implemented behavior and confirm every scenario has a test that exercises
      it (convergence, causal ordering, retry, missing document, the `sequence` mapping); record any mismatch as a task,
      not as a passing check. Verified: every delta scenario maps to a test (see the review task).
- [x] 4.6 Run the pre-write derivation pass (the `review-gate` skill's first stage), whose output is the claims table in
      `design.md`; seed it with `python3 scripts/claim-scan.py` after every change-dir file is in the diff
      (intent-to-add included, so the untracked deltas are scanned), re-derive it after the implementation's last edit,
      and verify by re-reading each source. Verified: the claims table was re-read after the implementation and again
      after the review's last edit (two rows corrected: the update-strategy row's pre-change source and the test
      publisher's sequence claim).
- [x] 4.7 Write the upgrade's replay-completeness precondition into the ADR and the change's report: prove coverage by
      event identity — every showcase id in the current index has its `ShowcaseScheduledEvent` in the topic's retained
      range — record a checkpoint and the group's offsets, and abort before deleting the index when either is missing;
      verify the text names the identity check, the checkpoint and the abort. Verified: the ADR's `Upgrade procedure`
      steps 1–2 carry the identity check, the checkpoint and the abort.

- [x] 4.8 Remove the projection service's dead `BPE_DEFAULT_DB_HOSTS` entry from its `bootBuildImage` environment map
      (the service has no datasource) and record the removal in the proposal's Impact; verify the variable appears in no
      projection-service file. Verified: `showcase-projection-service/build.gradle.kts` keeps only the server-port,
      Kafka and OpenSearch defaults.

- [x] 4.9 Apply the consultant-requested migration hardening: give the ADR's rollback the stop-first order before it
      restores the checkpoint, and add the post-replay completeness check (the index's id set equals the retained ids
      whose latest event is not a removal, plus no ERROR line in the replay's projector log), sweeping the design's
      migration constraints and the workflow follow-up; verify the ADR names both (the rehearsal's own record is task
      4.10). Verified: ADR-0022 steps 7–8 and `design.md`'s Migration Plan carry both.

- [x] 4.10 Rehearse the populated legacy upgrade and its rollback on the local cluster and fix what it finds: build the
      pre-change images from `origin/main` (kept as `:pre-rehearsal`), populate 14 showcases (6 scheduled-only, 3
      started, 3 finished, 2 removed, so 12 expected survivors) through the gateway, run ADR-0022's procedure, then roll
      back. The rehearsal found two defects: the `Recreate` upgrade was rejected because the live Deployment kept
      `strategy.rollingUpdate` (the template now renders `rollingUpdate: null`), and the restored clone carried
      `index.blocks.write` (ADR-0022's rollback now clears it). Verified: the replay left 12/12 survivor ids equal to
      the expected set, the mapping and a document carry `sequence`, the projector log has no ERROR, and the rollback
      restored 12 sequence-less documents with the pre-change projection image resuming at the recorded offsets. (The
      bitnami Kafka pod needs `JMX_PORT= KAFKA_OPTS=` to run the consumer-group CLI.)

## 5. Review and verification

- [x] 5.1 Run `./gradlew :showcase-projection-service:check :showcase-projection-model:check` (Docker present, the
      integration tier included, `ShowcaseEntityMappingCT` and `ShowcaseEntityJacksonCT` among them) and
      `./gradlew spotlessApply spotlessCheck` after the last edit; verify both complete successfully. Verified:
      :showcase-projection-service:check and :showcase-projection-model:check green with Docker; spotless green; chart
      lint green.
- [x] 5.2 Run the deep-review routing decision and record its outcome and signals on this task; verify with
      `python3 scripts/review-routing.py --change idempotent-projection-writes`. Verified: `REQUIRED`, signs
      `infrastructure-or-deployment` (`helm/chart/…/values.yaml`), `public-contract` (`ShowcaseEntity` and its component
      tests), `domain-surface` (`ShowcaseProjector`, `ShowcaseBatchWriter`). The `review-thorough` pass then ran: it
      found the `payloadType` tag dropped from the ingest-age timer (restored), the item-level backoff fixed rather than
      exponential (aligned), and the ignored-payload branch untested (covered).
- [x] 5.3 Run `lesson-capture` when the skip condition does not hold and record the applied net delta of the artifact
      the lesson landed in (`AGENTS.md`, a subject pack, or a skill), or the skip and its reason, on this task; verify
      the recorded figure with `git diff --numstat <that path>` after `spotlessApply`. Verified: three durable proposals
      applied — `check-and-test-authoring` gains the fixture-reaches-the-state mode, `review-gate`'s behavior-sweep
      bullet widens to implementation-added behavior, and `jvm-development`'s wiring bullet covers a rewrite of the same
      class; `AGENTS.md` is unchanged (net 0); `git diff --numstat` shows `check-and-test-authoring` +14/−0,
      `review-gate` +11/−6, `jvm-development` +9/−6.

## Workflow follow-up

- The deployment smoke ran against the branch
  (`gh workflow run deployment-smoke.yml --ref idempotent-projection-writes`, run 40064082017) and passed, covering the
  fresh-cluster install of the chart.
- On an upgrade install, read the signals separately: the upgrade's completion (`helm upgrade --wait` fails when the
  hook fails, and the Job is deleted on success so no log survives), the mapping's presence with
  `GET showcases/_mapping`, a replayed document's `sequence` with `GET showcases/_search?size=1`, the absence of
  missing-document failures in the projector's log for a showcase whose creation event was retained, the replay's
  completeness (the index's id set against the retained ids whose latest event is not a removal), and a projector log
  with no ERROR line (each loss form logs its own message, so the check is the level). Record which halves ran.
- The populated legacy upgrade and rollback ran on the local cluster (ADR-0022's steps, the six signals, 12/12 ids); the
  snapshot-repository path and a multi-node rebuild remain parked in `docs/ideas.md`.
