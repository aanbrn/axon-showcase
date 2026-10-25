# Proposal

## Why

The projector has a defined failure policy since `idempotent-projection-writes` (ADR-0022), but its operating condition
is invisible: `isRunning()` reports only that a subscription object exists while the stream retries a failure forever,
so a projector that is repeatedly recovering is indistinguishable from one that is consuming, and the actuator reports
it healthy. Its backpressure buffer is documented in events but applied after batching, so the effective bound is the
configured value times the batch size. The service's `application.yml` also carries settings no classpath reads, which
suggests knobs that do not exist. This change makes the condition observable, makes the buffer's documented unit true,
and removes the confirmed-dead configuration.

## What Changes

- `showcase-projection-service/.../ShowcaseProjectorStateMonitor.java` (new): holds the projector's operating state —
  stopped, running, retrying — the time of the last resolved batch, and the in-flight batch count, and registers them as
  Micrometer gauges under the `showcaseProjector` prefix.
- `showcase-projection-service/.../ShowcaseProjectorHealthIndicator.java` (new): reports the projector's state through
  the actuator, so a retrying or stopped projector is not reported healthy; a running one reports its last progress.
- `showcase-projection-service/.../ShowcaseProjector.java`: drive the monitor — `RUNNING` only once the consumer holds
  partitions (its assignment listener), retrying on a stream failure, stopped on stop, and the last progress after each
  resolved batch; assemble the consumption pipeline in a testable method instead of inline in `start()`.
- `showcase-projection-service/.../ShowcaseProjector.java`: apply the backpressure buffer before the batch window with
  `limitRate(maxSize)`, so the configured capacity bounds events per partition; the committed control measures the
  overflow at `buffer + 2 × maxSize + 1` (the window and the write prefetch included).
- `showcase-projection-service/src/main/resources/application.yml`: add the projector indicator to the readiness group
  (liveness keeps its default) and remove the configuration blocks the design proves unread (`resilience4j.*`,
  `spring.kafka.*`), keeping the ones proven live with their reasons.
- The projection Deployment's `startupProbe` moves to `/actuator/health/liveness`, so a projector that has not yet
  subscribed cannot fail the startup probe and be restarted; Prometheus retained the target while readiness was DOWN, so
  the projection Service remains unchanged.
- `docs/adr/0022` (Consequences narrowed) and a new ADR for the probe posture;
  `.opencode/skills/jvm-development/SKILL.md` gains a `## Naming` section recording the owner-approved naming convention
  (its provenance is the owner's approval, not a captured lesson).
- The change's four delta specs (`…/specs/showcase/read-side/projection-service/spec.md`,
  `…/specs/showcase/deployment/helm-chart/spec.md`, `…/specs/showcase/quality/commit-hygiene/spec.md` and
  `…/specs/showcase/quality/agent-skills/spec.md`); `design.md`'s Amendments lists the clauses they amend.
- The review-record restoration: `scripts/commit-hygiene.py` gains the `caller-restarted` reason, its tests gain the
  acceptance/rejection controls, and the ledger's rows and acknowledged-gap comments are corrected.
- Tests: unit coverage of the monitor's transitions and the indicator's statuses, a unit control that the buffer's
  overflow point matches the documented unit, and an integration test that the readiness group contains the projector
  indicator while liveness does not. The assignment and retry wiring has no committed control; the live rehearsal in
  task 2.7 is the evidence, and an assertion in the existing Kafka-backed IT is the smallest future improvement.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/read-side/projection-service`: the projector's operating state is observable through the actuator and its
  progress is recorded; the consumption buffer's unit and effective bound are stated.
- `showcase/deployment/helm-chart`: the projection service's startup probe targets the liveness endpoint.
- `showcase/quality/commit-hygiene`: the session-continuity requirement gains the `caller-restarted` violation record
  and the misfiled-deep-pass acknowledgement.
- `showcase/quality/agent-skills`: a replacement reviewer session needs an evidenced recovery case; an independent
  screen is not one.

## Impact

- `showcase-projection-service`: the projector's lifecycle wiring and its tests, one new health indicator, one new gauge
  family, and the service's configuration file.
- Deployment: `projectionService` pods become not-ready while the projector is retrying or stopped (the readiness group
  gains the indicator), so a rollout surfaces a projector awaiting initial assignment or retrying a pipeline failure;
  liveness — and the startup probe now pointed at it — never restarts a pod for it; and the Service is unchanged.
- Observability: new `showcaseProjector.state`, `showcaseProjector.processing`, and `showcaseProjector.last.progress`
  gauges; a Grafana panel is not part of this change.
- No API or document-mapping change; no new infrastructure; no dependency change.
- Prerequisite execution: the local cluster is restored to merged `main` through ADR-0022's upgrade procedure before the
  behaviour work, because the rehearsal left it on the pre-change release with sequence-less documents.
