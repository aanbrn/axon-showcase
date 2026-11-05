# Tasks — surface the projector's consumer backlog

The unit's record is `docs/units/surface-projector-backlog/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and every scanned
      candidate is accounted for. Verified: `python3 scripts/claim-scan.py` over the staged set yields `path`,
      `absolute`, `count`, and `cross-ref` candidates only; `design.md`'s dismissal line states one reason per kind, and
      the claims table's eleven rows carry the change's substantive claims.

## 1. The sampler, its metrics, and the indicator

- [x] 1.1 `ShowcaseProjectorProperties`: add a nested `Backlog` holding the sample `interval` (default 15 seconds,
      bounded below at 1 second) and the admin request `timeout` (default 5 seconds, bounded below at 100 milliseconds
      so a sample's three calls stay well inside one interval), bound as `showcase.projector.backlog.*` and documented
      in `application.yml` as unmeasured starting defaults, the timeout bounding **each** admin call rather than a whole
      sample (decision 2). Verify: `ShowcaseProjectorPropertiesCT` gains an env-var binding case
      (`PROJECTOR_BACKLOG_INTERVAL`, `PROJECTOR_BACKLOG_TIMEOUT`) and a rejection case for each in the shape its
      existing cases use, **and** its two enumerating cases — the documented defaults and the yml placeholders — cover
      the two new properties, since those assert every property the class carries. Done: `Backlog` binds
      `showcase.projector.backlog.*` with PT15S/PT5S defaults bounded at 1s/100ms; `ShowcaseProjectorPropertiesCT`
      gained both env-var binding cases, both rejection cases, and the two enumerating cases now assert the new
      properties.

- [x] 1.2 `ShowcaseProjectorBacklogSampler`: a `SmartLifecycle` component that builds an `AdminClient` from the
      configured consumer properties filtered to `AdminClientConfig.configNames()`, samples on its own virtual thread at
      the configured interval, and closes the client on stop. Each sample describes the topic, reads the end offsets and
      the group's committed offsets, and visits the **union** of both partition sets. Verify: no `group.id` or
      `partition.assignment.strategy` reaches the client, the sampler starts and stops with the context, and a partition
      present in only one of the two sets is visited. Done: `adminConfig()` filters the consumer properties by
      `AdminClientConfig.configNames()`, and `ShowcaseProjectorBacklogSamplerTests` asserts the connection keys survive
      while the consumer-only ones do not; an `AdminClientFactory` seam lets a case verify `close(Duration)` on stop; a
      second case proves `isRunning()` follows start and stop and that a stopped sampler attempts no further sample;
      `resolve` visits the union of both partition sets.

- [x] 1.3 The same class: the offset arithmetic lives in a package-private method that takes the two maps and returns
      each partition's distance or its unresolved reason, and the gauge-writing path lives in a second package-private
      method that takes the resolved partitions and a registry, so both are callable with no broker. Register
      `showcaseProjector.consumerLag` per partition (tags `topic`, `partition`, base unit `records`), publish `NaN` for
      an unresolved partition, and register `showcaseProjector.consumerLag.unresolved`,
      `showcaseProjector.consumerLag.failed` (a counter), and `showcaseProjector.consumerLag.observed` (epoch second of
      the last successful sample, base unit `seconds`, `0` before one); a failed sample leaves the last values and the
      observation time unchanged. Verify: task 2.2 drives the writing method and asserts each outcome. Done: `resolve`
      is the package-private arithmetic, `publish` the package-private writing path; the gauges are
      `showcaseProjector.consumerLag` (per `topic`/`partition`, base unit `records`), `.unresolved`, `.failed`, and
      `.observed` (base unit `seconds`), and a failed sample leaves the snapshot untouched.

- [x] 1.4 A Prometheus exposition test beside `ShowcaseProjectorStateMonitorPrometheusTests`, pinning the names the two
      dashboard panels query (`showcaseProjector_consumerLag_records`, `..._unresolved`, `..._failed_total`,
      `..._observed_seconds`). Verify: it fails if a Micrometer name or a base unit changes. Done:
      `ShowcaseProjectorBacklogSamplerPrometheusTests` pins `showcaseProjector_consumerLag_records`,
      `_unresolved_partitions`, `_observed_seconds`, and `_failed_total`.

- [x] 1.5 `ShowcaseProjectorHealthIndicator`: add `consumerLag` (the maximum resolved partition distance, omitted when
      none resolved), `consumerLagUnresolvedPartitions`, and `consumerLagObservedEpochSecond` as details, leaving the
      status calculation, the readiness group, and liveness untouched. Verify: a test asserts all three details, the
      omission, the case where one partition resolved and others did not (so a partial answer is visible), and that the
      status still follows the lifecycle state alone. Done: the indicator carries `consumerLag`,
      `consumerLagUnresolvedPartitions`, and `consumerLagObservedEpochSecond`; `consumerLag` is omitted before a
      resolved partition, and `ShowcaseProjectorHealthIndicatorTests` asserts a partial sample and that the status still
      follows the lifecycle state.

## 2. Verification

- [x] 2.1 Unit tests for the arithmetic (`ShowcaseProjectorBacklogSamplerTests`): a normal distance, a partition with no
      committed offset, a committed offset past the end, a partition absent from the topic, and an empty pair of maps
      each resolve as specified. Verify: each case names its own outcome and fails against a mutation of the arithmetic
      it covers. Done: `ShowcaseProjectorBacklogSamplerTests` covers the normal distance, the vanished partition, the
      uncommitted partition, the offset past the end, and the empty pair — each asserted against `resolve`'s own output.

- [x] 2.2 Unit tests for the publication, driving task 1.3's writing method with a `PrometheusMeterRegistry` and no
      broker: a resolved partition publishes its distance, an unresolved one publishes `NaN`, the `unresolved` gauge
      counts them, a failed sample does not move `observed` and increments the failure counter, and a successful one
      moves `observed`. Verify: each of the four outcomes is asserted from a scrape. Done: the same class drives
      `publish` over a `PrometheusMeterRegistry` and asserts the distance, the `NaN` for an unresolved partition, the
      unresolved count, and the observation time — including that an empty sample leaves no distance. The failed-sample
      counter's control lives at the integration tier (task 2.3), where a real client fails.

- [x] 2.3 An integration test against a Kafka container (`ShowcaseProjectorBacklogIT`): a consumed topic reports zero; a
      topic with records and a stopped projector reports the produced count; restarting the projector drains it to zero;
      a group that never committed reports unresolved partitions and no distance; a sampler pointed at a closed port
      reports a failed sample without publishing a zero. Verify: each case is asserted against a real broker, and the
      drain is awaited on the observed value rather than a fixed pause. Done: `ShowcaseProjectorBacklogIT` runs against
      a Kafka container with a fresh topic and group per case — committed zero, unconsumed backlog, drain to zero, a
      never-committed group, and an unreachable broker, where the last one awaits
      `showcaseProjector_consumerLag_failed_total` rising before it asserts that no distance was published.

- [x] 2.4 `./gradlew :showcase-projection-service:check` (Docker available, coverage gate on) and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verify: both pass. Done:
      `./gradlew :showcase-projection-service:check` passes with integration tests and the coverage gate.

- [x] 2.5 The deployed signal: with the local stack installed, read the new series back from Prometheus and the panel
      back from Grafana, exercising a positive backlog, a fresh zero, an unresolved partition, and a stale observation —
      each reading checked against the control that produced it, not against the panel's appearance. A screenshot alone
      does not establish query correctness. Verify: each of the four readings is recorded with the query that produced
      it.

      Done, against the expressions the panels carry:

      - **fresh zero** — partitions 0 and 1 read `0` at a sample age of `20.9 s`.
      - **unresolved** — partition 2 reads `NaN`, with `max by (pod) (…_unresolved_partitions) = 1`, a real partition the
        group never committed to.
      - **stale** — scaling the Deployment to zero grew the sample age from `34.9 s` to `73.5 s` before Prometheus marked
        the series stale.
      - **positive backlog** — the owner-approved reversible control pointed the readiness probe at
        `/actuator/health/liveness` and `OS_URIS` at a dead endpoint, so the writer failed while Kafka stayed reachable.
        With five showcases created, `max by (partition, topic, pod) (showcaseProjector_consumerLag_records{…})` read `2`
        on partition 0 and `1` on partition 1 (partition 2 `NaN`) across four sampling cycles, with
        `…_failed_total = 0`. Restoring `OS_URIS` and the readiness path drained both partitions to `0`, returned the pod to
        ready, and left the live Deployment matching the chart's manifest.

- [x] 2.6 `openspec validate --all` and `./gradlew spotlessApply` after the last edit to a Spotless-owned file. Verify:
      the validation totals pass and the tree carries no formatter change. Done: `openspec validate --all` reports 27
      passed, and `spotlessApply` left no change.

- [x] 2.7 `showcase-projection-service/build.gradle.kts`: the `integrationTest` suite re-declares
      `libs.axon.extensions.kafka.springBootStarter`, because a custom suite does not inherit the module's
      `implementation` edges on its compile classpath (`gradle-build`'s custom-suite rule). Verify:
      `./gradlew :showcase-projection-service:compileIntegrationTestJava` passes with that declaration and fails without
      it. Done: the edge is declared and `compileIntegrationTestJava` passes; removing it fails that compile, which is
      how the missing type surfaced.

## 3. The dashboard and the durable records

- [x] 3.1 The bundled dashboard's Projection Service section: a backlog panel (per-partition series,
      `max by (partition, topic, pod)` so replicas neither multiply a distance nor let one replica's stale value stand
      for another's fresh one) and a sample panel carrying the sample age (guarded against the pre-first-sample zero the
      way the sibling panel does, `time() - (showcaseProjector_consumerLag_observed_seconds{…} > 0)`) beside the
      unresolved-partition count, each with a description naming what it does not prove. Verify:
      `./gradlew verifyDashboardJson` passes, ids are unique, both panels carry a description and a non-blank
      expression, and the sample panel shows both values with the same pod identity. Done: panels 90
      (`Consumer backlog`, `max by (partition, topic, pod)`) and 91 (`Backlog sample`, guarded age beside the unresolved
      count) are provisioned; `verifyDashboardJson` passes and both carry a description.

- [x] 3.2 `README.md`: the dashboard's panel count, the observability bullet's statement of what the projection service
      exposes, and the Cool Story bullet that names its state and progress. Verify: the count matches the dashboard
      JSON, and no passage claims a stall verdict. Done: the README states 46 panels across 8 sections, the
      observability bullet names the backlog and the sampler, and the Cool Story bullet names the outstanding backlog.

- [x] 3.3 `docs/adr/0023-projector-health-readiness.md`: a dated `## Amendment` recording that the backlog is
      observable, that the `UP`-while-stalled boundary is retained because a diagnostic number is not a binary health
      policy and no measured threshold exists, and that the Consequences sentence naming the parked idea —
      "Consumer-lag-based stall detection is parked in `docs/ideas.md` (2026-10-24)" — is superseded, with the surviving
      content in the amendment itself. The Status stays `Accepted` and the Decision text is unchanged. Verify: the
      amendment quotes the sentence it supersedes, and that no live pointer to the removed entry survives — the quoted
      sentence is the amendment's own record of what was superseded, not a reference a reader would follow. Done:
      Verified: the ADR carries the 2026-11-05 amendment, quotes the parked entry as superseded, and leaves the Decision
      and `Accepted` status unchanged.

- [x] 3.4 `.opencode/skills/deployment-operations/SKILL.md`: one bullet on reading the signals — a zero backlog with a
      fresh sample, a stale sample, an unresolved partition, and the fact that zero does not prove projection
      completeness. Verify: the bullet states the readings and the limitation, and names no threshold. Done: the pack's
      Deployment, Helm & Kubernetes section carries the bullet on reading the three signals and states that a zero
      distance does not prove completeness.

- [x] 3.5 `docs/ideas.md`: remove the parked entry this change implements ("Detect a stalled projector from consumer
      lag"), keeping the surviving half of its reasoning out of the removal — an implemented idea leaves the file, and
      its durable content is the ADR amendment. Verify: the entry is gone and the file's dated section still carries its
      entries. Done: `docs/ideas.md` no longer carries the entry, and the ADR amendment holds its surviving content.

- [x] 3.6 The delta `specs/showcase/read-side/projection-service/spec.md` is written and valid. Verify:
      `openspec validate surface-projector-backlog --strict` passes. Done:
      `openspec validate surface-projector-backlog --strict` passes with eleven scenarios.

## 4. The review gate and the records

- [x] 4.1 The proposal stage's quick review: `review-quick` over the planning artifacts until a clean round, with each
      round's row and findings under `docs/units/surface-projector-backlog/`. Verify: `--expect-clean` and
      `--session-continuity` pass. Verified: rounds 1–4 in `rounds.tsv`, the closing round 4 with no finding class;
      `--expect-clean`, `--session-continuity` and `--review-ready` pass.

- [x] 4.2 The owner's manual pass over the proposal. Verify: the approval is recorded before the implementation starts.
      Done: the owner's instruction "Как исправишь все замечания советника и дойдешь с ним до согласия, так начинай
      реализацию" authorized implementation from the advisor's agreement, so this unit took that single boundary instead
      of a separate proposal pass; the advisor's third round stated the agreement explicitly ("Implement the reconciled
      proposal; I explicitly agree with its technical direction"), and the manual pass over the finished unit remains
      4.7.

- [x] 4.3 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass. Done:
      rounds 5–11 in `rounds.tsv`, the closing round 11 with no finding class; `--expect-clean`, `--session-continuity`
      and `--review-ready` pass. Round 5's first invocation stopped at its classification step and is recorded as a
      `replaced` row.

- [x] 4.4 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded.
      Done: `python3 scripts/review-routing.py --unit surface-projector-backlog --change surface-projector-backlog`
      reports REQUIRED on four signals — the dashboard JSON (infrastructure-or-deployment), the three production classes
      (domain-surface), and the added requirement block — so the deep review runs.

- [x] 4.5 The deep review, when 4.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Done: the routing made the deep review required, and `review-thorough`
      reported one nitpick — the ADR amendment's opening read as though stall detection shipped, against the retained
      decision and the change's own Non-Goals. Reworded to name the observation as what lands and the detection as
      superseded out of scope; the pass re-checked the sampling loop, the `NaN` publication, the lifecycle, the
      admin-config filter, the delta, the tests, the panels and the durable records as sound.

- [x] 4.6 The technical advisor's consultation on the implementation, when the owner authorizes it. Verify: the
      authorization and the findings' disposition are recorded. Partly done: the first consultation reported two
      confirmed problems and a hypothesis — the deployed verification marked complete beyond its evidence, the sample
      panel's "healthy zero" wording, and a field-override matcher keyed on a templated legend. The panel wording now
      requires resolved zero distances and zero unresolved partitions, the override matches query B by frame reference,
      and the deployed task is reopened pending the positive control the advisor asked me to propose. Done: the owner
      authorized the pass ("Отревьювь с советником до согласия") and the stack ("Локальный стенд в полном твоем
      распоряжении"), and the consultation closed in explicit agreement ("I explicitly agree that the implementation is
      ready for that pass"). Round 1 found two confirmed problems and a hypothesis — the deployed task marked complete
      beyond its evidence, the sample panel's healthy-zero wording, and an override keyed on a templated legend; the
      first two were corrected in the tree and the third re-keyed to query B, and the open question was settled by the
      reversible control recorded on task 2.5. Round 2 reported no confirmed problems, hypotheses, or open questions.

- [x] 4.7 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Done: the owner reviewed the finished unit and instructed delivery in one message
      ("Одобряю. Отгружай."); nothing was committed before it.

- [x] 4.8 `docs/units/surface-projector-backlog/process-defects.md`: one record per defect observed, or the statement
      that none was. Verify: the file exists before 4.7 is requested. Done: a record per defect observed — the
      integration test whose cases shared a topic and group, and the build-file edit made without its pack loaded.
