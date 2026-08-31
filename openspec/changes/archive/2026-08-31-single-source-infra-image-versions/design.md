# Design: single-source infrastructure image versions

## Context

The app's infra (PostgreSQL, Kafka, OpenSearch) is referenced in three surfaces: the Bitnami Helm charts
(`build.gradle.kts` releases + `helm/values/*/values-local.yaml`), `docker-compose.yml`, and Testcontainers (IT + e2e).
Today the versions flow from independent catalog sources: broad-major `*-image` literals (`17`, `3.9.2`, `3`) for
compose/Tests and range `bitnami-*` chart versions (`16.x.x`, `31.x.x`, `2.x.x`) for the Helm charts. They already
diverge (Kafka `3.9.2` vs `3.9.0`), and nothing keeps them in sync. See `proposal.md` for motivation.

## Goals / Non-Goals

- **Goal**: the test surfaces (compose/Tests) pin concrete official image tags (`*-image-tag`); the deployment pins
  Bitnami **chart versions** (`bitnami-*`), and each chart's preconfigured default `image.tag` is the deployed Bitnami
  tag.
- **Goal**: `check` fails on drift between the chart-preconfigured Bitnami image tag and the official image tag used in
  tests.
- **Non-Goal**: making compose/Tests and Helm use identical image *builds* — official vs Bitnami images legitimately
  differ; only their app *versions* must agree.
- **Deferred (follow-up)**: the available chart major-track updates — `bitnami/postgresql` 18.x (PostgreSQL 18.6) and
  `bitnami/kafka` 32.x (Kafka 4.0) — are deliberately not part of this change. They are a coordinated chart-version +
  image-tag bump that should land after this model is in place, so the drift gate validates them.

## Decisions

### Test-surface tags become concrete `*-image-tag` coordinates

Replace the broad-major `*-image` literals (`17`, `3.9.2`, `3`) with concrete `*-image-tag` coordinates
(`17.6`, `3.9.0`, `3.2.0`) in the catalog, and point docker-compose and the Testcontainers IT/e2e suites at the new
accessors. A broad major is not a concrete image tag, so the test surfaces were never pinned to a real version.

- *Alternatives considered*: keeping the broad-major `*-image` literals — rejected: a major like `17` is not a
  concrete tag, so compose/Tests could pull different patch versions over time.

### Chart versions are the single authority for the deployed Bitnami image tag

Pin the Bitnami **chart versions** in the catalog (`bitnami-postgresql = "16.7.27"` etc.) and let each chart's
preconfigured default `image.tag` be the deployed tag. The charts ship the tag in their default values, so the chart
version is the complete, inspectable source of truth — no separate Bitnami tag coordinate and no `image.tag` override
in build logic or values files.

- *Alternatives considered*: (1) adding a `*-bitnami-tag` catalog coordinate alongside the chart version — rejected:
  manual maintenance of revision-suffixed tags (`17.6.0-debian-12-r4`) that drift as charts move; (2) deriving the
  Bitnami tag from a chart version in build logic — rejected: still requires resolving the chart, so the tag would be
  re-implemented outside the chart itself.

### The verify task resolves each chart's preconfigured `image.tag` via the Helm CLI

A root task `verifyInfraImageVersions` (wired into `check`) runs `helm show values bitnami/<chart> --version <pinned>`
for each component, reads the chart's preconfigured default `image.tag`, and extracts its leading numeric app-version
segment. It compares that against the `*-image-tag`'s leading numeric segment after stripping trailing `.0`
zero-padding from **both** sides: the official tag and the chart app version may differ only by trailing zero segments
(e.g. postgres official `17.6` vs chart app version `17.6.0`, or `17` vs `17.0.0`) and still count as equal, while a
genuine patch difference (e.g. `17.0.1` vs `17`) fails. It uses the plugin-managed Helm client and the chart repository
the plugin registers, so no CI setup step is needed; `helmUpdateRepositories` refreshes the repo index (TTL-cached).

- *Alternatives considered*: (1) keeping a pure-catalog comparison — rejected: the chart is the only authority for the
  Bitnami tag, so a catalog check has nothing to compare; (2) parsing a vendored snapshot of chart values —
  rejected: a vendored snapshot would itself drift from the real chart.

### Chart versions are pinned to concrete versions, not ranges

The catalog coordinates move from ranges (`16.x.x`) to concrete versions (`16.7.27`). A range would let the chart
resolve to a different version (and thus a different preconfigured image tag) than the one `check` verifies against.

- *Alternatives considered*: keeping `x.x.x` ranges and letting `helm show values` resolve them — rejected: the verify
  could resolve a newer chart than the one installed, defeating the drift gate.

### The verify task also rejects `image.tag` overrides in the repo's values files

The verify task additionally scans each infra release's values files (`helm/values/*/values*.yaml`) and fails if any of
them pins `image.tag`. This makes the "SHALL NOT override `image.tag`" rule enforceable: the repo cannot reintroduce a
second, drifting source for the deployed Bitnami tag even by accident (the chart version remains the single authority).

- *Alternatives considered*: (1) relying on the chart-default comparison alone — rejected: the task resolved only the
  chart's shipped default, so an `image.tag` pin added to a values file later would silently diverge the deployed tag
  from tests; (2) comparing the release's effective rendered values — heavier and couples the task to a full render,
  while a targeted values-file scan covers the same ground.

### The gate derives its checks from the actual Helm releases

The verify task's checks are not a separately maintained list: each check is derived from the `helm.releases` container
at execution time, reading each infra release's chart reference, chart version, and values directories. The only
mapping kept in the build script is the chart-ref → component/image-tag lookup (`bitnami/postgresql` → postgres +
`postgres-image-tag`, etc.). This makes the gate follow the real configuration: renaming a release (and its values
directory) retargets its check automatically, moving/removing a release drops its check, and no hardcoded path or chart
version can silently drift from the release definition.

- *Alternatives considered*: (1) keeping the checks hardcoded in `build.gradle.kts` — rejected: the gate would scan
  stale values directories and chart versions after a rename, and a removed release would leave a stale check;
  (2) deriving at configuration time — rejected: the task is registered before the `helm.releases` container is
  populated, so the derivation must be a lazily-evaluated provider.

## Risks / Trade-offs

- [Verify task needs the Helm CLI + network at `check` time] → Mitigation: the plugin downloads the client and
  registers the chart repo automatically; the repo index is TTL-cached (1h) so `check` does not re-fetch on every run.
- [A chart bump changes the preconfigured image tag] → Mitigation: the drift gate fails the build, forcing a
  coordinated `bitnami-*` + `*-image-tag` update.
- [Deployment environments can still override `image.tag` at release time] → Mitigation: deliberate external overrides
  (e.g. `--set` flags or values files outside this repo) are out of scope for an in-repo gate; the task enforces that
  the repo itself does not introduce such an override, and the single-source contract holds for the Helm releases
  configured here.
- [Kafka 3.9.0 fails to start under Testcontainers (KAFKA-18281)] → Mitigation: the `KafkaContainer` usages override
  `KAFKA_LISTENERS` to empty-host listeners (`PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094`), which the
  buggy validation accepts; keep the override when bumping the Kafka tag.

## Migration Plan

1. Replace the `*-image` literals with `*-image-tag` coordinates and pin the `bitnami-*` chart versions; point the
   compose/Tests accessors at the new tags.
2. Add `verifyInfraImageVersions` (wired into `check`): derive its checks from the `helm.releases` container, resolve
   each chart's preconfigured `image.tag` via the Helm CLI, compare against `*-image-tag`, and reject `image.tag` pins
   in the repo's values files.
3. Update `AGENTS.md`; run `check` and `openspec validate --all`.

Rollback: revert the catalog + build-logic + task changes; the old majors restore previous behavior.

## Open Questions

None.