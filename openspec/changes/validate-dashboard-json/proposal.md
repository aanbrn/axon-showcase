# Proposal

## Why

The chart's bundled Grafana dashboard (`helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`) is
emitted by a ConfigMap template as an opaque string, so `helm lint` (even strict, with the full/minimal configurations)
and `helm template` never parse it: a malformed dashboard JSON passes every gate and surfaces only when Grafana's
sidecar fails to provision the dashboard in a live deployment. This was latent while the Web UI section was added — no
merge-gate check would have caught a broken file.

## What Changes

- Add a `verifyDashboardJson` Gradle task (a `build-logic` task class, registered in the root build) that parses every
  `helm/chart/src/main/helm/files/grafana-dashboards/*.json`, fails `check` naming the offending file and the parse
  error otherwise, and add it to the root `check` task's members.
- Add a focused unit test for the task's rule (the class holding the parse, alongside the existing `build-logic` task
  tests).
- Refresh the gate enumerations the new member joins — `AGENTS.md`'s `check` note and `README.md`'s verification-command
  block (the only two places the `verify*` members are listed); remove the implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/code-quality`: adds a build-check requirement — the standard `check` task parses the chart's bundled
  Grafana dashboard JSON, failing on a malformed file — since this capability owns the repository's non-source build
  gates (workflow lint, module-dependency verification, commit hygiene) and their `check` membership.

## Impact

- **Files**: `build-logic/src/main/kotlin/` (new task class + its rule), `build-logic/src/test/kotlin/` (task test), the
  root `build.gradle.kts` (task registration + `check` membership), and the docs (`AGENTS.md`, `README.md`,
  `docs/ideas.md`), plus the change dir and the `code-quality` delta.- **Build / tests / services**: adds one Gradle
  task to `check` (pure local JSON parsing, no network, no Docker) and its build-logic test; no Java, dependency,
  service, or chart change.
- **Verification**: proven by a known-bad (a truncated/invalid JSON fails the task naming the file) and a known-good
  (the committed dashboard passes), and by the task running inside `check`.
