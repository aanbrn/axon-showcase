# Surface the projector's consumer backlog

## Why

The projector reports its lifecycle state, the time of its last resolved batch, and its in-flight batches, and the
dashboard renders that progress as a time: the age of an event at ingest and the idle time since the last resolved
batch. Both stop being evidence when consumption stops — an event that is never ingested produces no sample, and an idle
age cannot separate a stall from a quiet system (ADR-0023). Neither states the size of the outstanding work. A producer
that outruns the projector, or a consumer that stopped committing, therefore shows as a healthy running projector, and
no panel states how many records are outstanding.

## What Changes

- `showcase-projection-service`: a lifecycle-managed sampler creates its own Kafka `AdminClient` and, on an interval,
  reads the topic's end offsets and the consumer group's committed offsets, publishing the distance per partition as
  metrics. The sampler runs outside the receiver's poll loop and outside the health and scrape paths, so a stalled
  consumer does not stop the observation.
- A metric carries the age of the last successful sample, and a counter counts failed samples, so a stale number is
  distinguishable from a fresh one.
- A partition the sample cannot resolve — no committed offset, a committed offset past the end, or one the topic no
  longer reports — is reported as unresolved rather than as zero backlog; a failed end-offset read fails the whole
  sample instead, which the failure counter reports.
- `ShowcaseProjectorHealthIndicator` gains the backlog summary and the observation time as details; its status
  calculation, its readiness membership, and liveness are unchanged, and it omits the summary when no partition
  resolved.
- The bundled Grafana dashboard's Projection Service section gains a backlog panel and a sample-age panel.
- `read-side/projection-service` gains the requirement "The projector's consumer backlog is observable", and the
  existing "Projector operating state is observable" points at it while keeping its lifecycle, readiness, and liveness
  rules unchanged.
- ADR-0023 gains a dated amendment: the observation exists now, and the `UP`-while-stalled boundary is retained, with
  the reason.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/read-side/projection-service` — adds "The projector's consumer backlog is observable" (the per-partition
  distance, the freshness of the last successful sample, the failed-sample count, and the outcome that an unresolvable
  partition or an unavailable sample is reported as such rather than as zero), and points "Projector operating state is
  observable" at it while keeping that requirement's lifecycle-state, readiness, and liveness rules unchanged.

## Impact

- **Code**: `ShowcaseProjectorProperties` gains a nested `Backlog`; a new `ShowcaseProjectorBacklogSampler` and its
  metrics; `ShowcaseProjectorHealthIndicator` gains details; `application.yml` documents the new properties.
- **Build**: `showcase-projection-service/build.gradle.kts` adds the Axon Kafka starter to the `integrationTest` suite,
  which a custom suite must re-declare to compile against a main-source type (`gradle-build`'s custom-suite rule).
- **Tests**: unit tests for the offset arithmetic and the metrics publication (beside
  `ShowcaseProjectorStateMonitorPrometheusTests`, which pins the exposition names the panels query), an integration test
  against a Kafka container, and the two new property cases in `ShowcaseProjectorPropertiesCT`.
- **Dashboard and docs**: the bundled dashboard JSON's Projection Service section; `README.md`'s observability bullets
  and its panel count; `docs/adr/0023-projector-health-readiness.md`;
  `.opencode/skills/deployment-operations/SKILL.md`'s reading of the two signals; `docs/ideas.md`'s parked entry, which
  this change implements.
- **Dependencies**: no new artifact. `kafka-clients` is already on the classpath through `reactor-kafka`, and
  `AdminClient` comes from it.
- **Behavior**: no API, no readiness or liveness outcome, and no projection semantics change. The new work is one
  scheduled admin read per interval.
- **Deployment**: no chart template, ServiceMonitor, or values change. The projection service's existing ServiceMonitor
  scrapes `/actuator/prometheus`, so the new metrics reach Prometheus with no wiring change; only the bundled dashboard
  JSON gains panels.
- **Verification**: the module's unit, component, and integration suites; the repository's `check`, which parses the
  dashboard JSON; `openspec validate --all`; and the local stack's Prometheus and Grafana, from which the four series
  and the provisioned panels are read back against the controls that produced them.
