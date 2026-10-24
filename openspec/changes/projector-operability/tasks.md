# Tasks

## 1. Baseline and state monitor

- [x] 1.1 Restore the local cluster to merged `main` through ADR-0022's `Upgrade procedure` before the operability
      rehearsal; verify a replayed document carries `sequence`, the projector consumes, and a created showcase appears
      in the index.
  - Historical verification, before task 2.7: release revision 8 used `Recreate`; the index was rebuilt through the
    procedure, and 12 documents carried `sequence` with no projector ERROR.
  - This records the baseline verification at that time, not the final local document count. Task 2.7 records the later
    destruction and reduced fixture.

## 2. Observability wiring

- [x] 2.1 Drive the monitor from `ShowcaseProjector`: `RUNNING` **only** from the receiver's partition-assignment
      listener (`ReceiverOptions.addAssignListener`), `RETRYING` from the stream's `doBeforeRetry`, `STOPPED` on stop,
      and the progress timestamp after each resolved batch's write — `start()` itself must not set `RUNNING`; verify
      with unit tests over a resolved and a failed batch and with task 2.7's live controls. Broker-client retries that
      emit no pipeline error do not change the state. Verified: `ShowcaseProjectorTests` covers the resolved and failed
      batch paths; the assignment listener is the only `running()` call site, and 2.7 exercises the assignment boundary
      against the cluster.
- [x] 2.5 Measure the scrape path while readiness is **actually** DOWN; verify by stopping OpenSearch and inducing a
      stream failure, then querying Prometheus for the target and a fresh `showcaseProjector_state` sample. Verified:
      Prometheus kept `up{job="axon-showcase-projection-service"} = 1` and scraped a fresh state sample while readiness
      was DOWN, so no Service change is needed and `publishNotReadyAddresses` stays out of the chart.
- [x] 2.6 Add the projection Service's `publishNotReadyAddresses` scenario to the helm-chart delta **only if** task 2.5
      measured the target disappearing; verify every rendered Service either way and that the delta's requirements match
      the rendered chart. Verified: 2.5 found the target persisting, so the flag and its chart scenario are not added;
      the helm delta keeps only the startup-probe requirement and the rendered chart is unchanged.
- [x] 2.7 Verify the lifecycle boundary against the cluster.
  - Assignment control passed: offsets at the end, state running, progress `0`, readiness UP, and liveness UP.
  - Failure control passed: OpenSearch was stopped and a showcase event was published so a non-empty bulk failed with
    `DataAccessResourceFailureException`. The projector reported `retrying` (state 2), readiness DOWN with the failure
    detail, and liveness UP, with the same pod UID and `restartCount = 0`.
  - Recovery verification: readiness returned to UP after OpenSearch was restored.
  - Destructive aftermath: stopping OpenSearch with local persistence disabled erased the projection index. The
    subsequent restore replayed one document; the earlier 12-document fixture was not restored.
  - Resulting local state: a reduced disposable rehearsal fixture containing one document, not a complete restoration of
    the baseline corpus. Readiness recovery does not establish read-model completeness.
  - The earlier 75-second broker outage left the state running; this accepted boundary is recorded in the design.

## 3. Buffer contract

- [x] 3.1 Probe the pinned Reactor's demand model before choosing the buffer mechanism: with a blocked writer, a
      timer-expired partial batch, and an above-capacity burst, measure where records accumulate with the buffer before
      and after the batch window; record the measurement that selects events-bounded (a bounded window demand) or
      batches-bounded (the property corrected to batches with the effective event bound), and update the design's buffer
      decision and the delta's clause and scenario to the measured mechanism. Verified: the committed `projectPartition`
      control asserts overflow on record `bufferMaxSize + 2 × maxSize + 1` under a blocked writer; the effective
      per-partition capacity includes the configured buffer, one batch window, and one write prefetch.

## 4. Dead configuration

- [x] 4.1 Remove `resilience4j.*` and `spring.kafka.*` from the service's `application.yml` after reproducing each as
      unread (no Resilience4j jar and no spring-kafka on the runtime classpath), keep
      `spring.webflux.problemdetails.enabled`, `spring.data.elasticsearch.repositories.enabled: false` and the
      `management.*.export.enabled` defaults with the reasons the design records, and verify the service starts and
      consumes with the trimmed configuration. Verified: `application.yml` no longer carries `resilience4j.*` or
      `spring.kafka.*`; the service boots and the ITs consume in the same run.

## 5. Records and verification

- [x] 5.1 Narrow ADR-0022's Consequences sentence that names this unit as parked (its Decision is unchanged) and record
      the readiness/liveness and startup-probe posture in a new ADR; verify the ADR-0022 sentence lists only the units
      that remain and the new ADR names both probes, the not-ready-addresses choice, and the reason. Verified: ADR-0022
      lists only the three remaining units; ADR-0023 records both probes, the milestone and the not-ready-addresses
      condition.
- [x] 5.2 Surface the new state signal in the README's observability path; verify the text mentions the projector's
      state without duplicating the values in `application.yml`, and that the Grafana panel it points at remains the
      separate parked unit the design names. Verified: the README names the projector state and the readiness detail,
      with no duplicated values.
- [x] 5.3 Remove the operability entry from `docs/ideas.md` (this change implements it) and leave the file's remaining
      entries as they are; verify the entry is gone and the rest of the file is unchanged. Verified: the operability
      entry is gone from `docs/ideas.md` and the other entries are untouched.
- [x] 5.4 Confirm the two follow-ons the design names are parked — a dashboard panel for the projector's state and
      consumer-lag-based stall detection (no Kafka admin client in the service) — in `docs/ideas.md`'s 2026-10-24
      section, correcting their wording if the implementation changes the metric names; verify each carries its status
      tag and its named blocker. Verified: the dashboard-panel and lag-detection entries exist in the 2026-10-24 section
      with their tags and blockers.
- [x] 5.5 Run `./gradlew :showcase-projection-service:check` (Docker present) and `./gradlew check` after the last edit;
      verify `spotlessApply`/`spotlessCheck` and `openspec validate projector-operability` are green. Verified: the
      module `check` (unit, component, integration with Docker, coverage, Checkstyle, SpotBugs) and the root `check` are
      green; `spotlessCheck` and `openspec validate` pass.
- [ ] 5.6 Run the review gate: `review-quick` until clean, the routing decision (`REQUIRED`:
      `infrastructure-or-deployment`, `domain-surface`, `requirement-block-added`), the deep review, and the owner's
      manual pass before any commit; the rounds are recorded in `docs/review-rounds.tsv`.
- [x] 5.7 Record the lesson-capture decision after the review gate with the applied net delta or the skip and its
      reason; the unit's compliance findings and the authoring-surface edit rule out the documented skip condition. The
      `jvm-development` `## Naming` section is an owner-approved convention, not a capture: it extends no existing rule
      and its decision difference is that a class added beside an existing subject takes that subject's prefix.
      Verified: lesson capture applied — 3 additions, 0 retirements; `AGENTS.md` unchanged (net 0);
      `deployment-operations` +8/−0, `check-and-test-authoring` +10/−0, `review-gate` +6/−4 (`git diff --numstat` after
      `spotlessApply`). The ledger recurrence is covered by the stage-2 checkpoint (#671) and the `check` gate; a
      pre-commit ledger guard is parked as its own unit.
- [x] 5.8 Reconcile completed measurements with every live statement of their decision, and rewrite verification notes
      from their evidence with explicit historical scope and subsequent destructive effects, per the review-gate
      procedure; verify no artifact states a decision as open after its measurement and no task record contradicts a
      committed control. Verified: rounds 12–15 reconciled — the delta's buffer clause, the scrape decision, the
      proposal conditionals, the task records and the duplicate scenario were corrected; the design's claims row is
      anchored to `HEAD` and the capture is consolidated.

#### review-record-restoration

| Round | Authoritative record                                                                                                                                                                                                                                                                                                                                                                                             |
| ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 11    | Deep pass 1, session `ses_e928f0350ffeaLt64wstmEXof6`; routing `REQUIRED`; findings (a)–(d) — see the evidence paragraph below.                                                                                                                                                                                                                                                                                  |
| 14    | Deep pass 2, session `ses_e91dce152ffeMz8POwBK847hWH`; routing `REQUIRED`. Findings: (a) `wording` — a duplicated delta bullet — FIXED_AND_VERIFIED; (b) `wording` — a stale, duplicated proposal bullet — FIXED_AND_VERIFIED.                                                                                                                                                                                   |
| 16    | Quick round, session `ses_e919ebf00fferreW4hkEQKO7JZ`; findings: (a) `contradiction` — the authoring bullet contradicted the overflow control it cites — FIXED_AND_VERIFIED (the bullet now states the demand-ignoring fixture measures the operator's occupancy, equal to the bound only because the requirement counts the downstream demand); (b) `wording` — a garbled design sentence — FIXED_AND_VERIFIED. |

The two deep passes ran in separate sessions: the second did not continue the first, and no evidenced recovery case
exists for that restart — the deep-review continuity was lost, and the record states the loss rather than claiming a
permitted replacement. Neither pass has a quick-ledger data row. Rounds 9, 12, 13 and 15 are quick rounds whose
`expired` reason was false (the caller started fresh sessions); their reason is corrected to `caller-restarted`, which
records the violation and does not authorize another restart. The next quick round continues round 16's session.

Finding evidence: round 11 (a) `ShowcaseProjectorTests.project_burstBeyondTheBuffer_overflowsInEvents` —
FIXED_AND_VERIFIED by the demand-respecting control and the exact overflow assertion; (b) the buffer decision in
`design.md` and the `ShowcaseProjectorProperties` Javadoc — FIXED_AND_VERIFIED by the per-partition scope text; (c) the
state writes in `ShowcaseProjectorStateMonitor` — ACCEPTED by the owner as latent: it accepts the unqualified state
writes (the generation guard is deferred to the code path that adds an in-process restart), because `stop()` runs only
at context shutdown, there is no in-process stop/start, and no actuator lifecycle endpoint is exposed; (d) the claims
rows in `design.md` — FIXED_AND_VERIFIED by the re-anchored citations. Round 14 (a) the duplicated bullet in the
projection-service delta — FIXED_AND_VERIFIED by its removal; (b) the stale proposal bullet — FIXED_AND_VERIFIED by its
removal. Round 16 (a) the authoring bullet in `check-and-test-authoring` — FIXED_AND_VERIFIED by the rewrite and the
committed controls; (b) the garbled sentence in `design.md` — FIXED_AND_VERIFIED by the rewrite.

## Workflow follow-up

- Run the deployment smoke against the branch before the archive commit: the readiness group changes what a successful
  rollout means, so a fresh-cluster install must reach ready with the projector consuming.
- If the smoke's `--wait` fails while the projector is starting, read the projector's readiness detail first and record
  which half failed rather than loosening the group.
