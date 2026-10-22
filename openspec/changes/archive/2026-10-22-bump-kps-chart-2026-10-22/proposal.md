# Proposal

## Why

The Helm update report names one row: `prometheus-community-stack: 92.9.0 -> 92.11.0`. It was re-resolved with the
check's own run (`./gradlew helmUpdates`, which refreshes the chart repositories before searching) rather than a
hand-run `helm search repo`, whose **local** index read a stale version on a previous bump. The row is still current.

The chart is the observability stack the deployment installs, and `infra-image-versions` specifies that an observability
chart bump is verified with a live install and smoke test before it merges — the release installs, the monitoring pods
are ready, and the Prometheus targets and Grafana datasources are wired. `verifyInfraImageVersions` does not cover this
chart (it gates the bitnami infra charts' `image.tag` drift), so the live smoke is the verification.

## What Changes

- **`gradle/libs.versions.toml`** — `prometheus-community-stack` `92.9.0` → `92.11.0`, a concrete pin as the catalog's
  rule requires.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the manual `helm install kps … --version` command moves to
  `92.11.0`. `verifyInstallCommands`, part of `check`, asserts it against the catalog pin, so leaving it would fail the
  build.
- **Left as recorded:** `docs/audits/2026-10-19.md`'s observation and `AGENTS.md`'s incident narration both cite
  `92.9.0`; they are dated records of what ran then, not live statements of the pin.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): the `infra-image-versions` requirement already states exactly what this change does — a concrete
pin, surfaced as actionable, verified by a live install and smoke before merge — so the bump complies with the scenario
rather than changing it.

## Impact

- **The local observability release and the documented install path.** The live upgrade is the verification; the
  documented command is gated by `verifyInstallCommands`.
- **No application change:** the chart is third-party, and the application chart's own dependency (the Bitnami `common`
  subchart) is untouched.
