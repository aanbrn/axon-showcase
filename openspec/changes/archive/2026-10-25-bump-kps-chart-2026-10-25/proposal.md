# Proposal

## Why

The Helm update report names one row, `prometheus-community-stack: 92.11.0 -> 92.13.1`, re-resolved locally with
`./gradlew helmUpdates` (which refreshes the chart repositories before searching); issue #40's 2026-10-24 comment, added
by that date's `helm-updates` run, records the same row. The chart is the observability stack the deployment installs,
and `showcase/quality/infra-image-versions` makes an observability chart's update actionable and requires it to be
verified by a live install + smoke test before merge. Leaving the pin where it is keeps the deployed stack two chart
minor releases behind the upstream release line.

## What Changes

- **`gradle/libs.versions.toml`** — the `prometheus-community-stack` coordinate moves `92.11.0` → `92.13.1`, a concrete
  pin as the catalog's rule requires.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the manual `helm install kps … --version` command in the
  Kubernetes Deployment block moves to `92.13.1`; `verifyInstallCommands` (part of `check`) asserts it against the
  catalog pin, so leaving it would fail the build.
- **No other chart coordinate moves:** `grafana-tempo` `1.24.4` and the Bitnami infra charts are untouched, because the
  check reports no row for them.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): the `infra-image-versions` requirement already states exactly what this change does — a concrete
pin, surfaced as actionable rather than suppressed, verified by a live install and smoke before merge — so the bump
complies with that scenario instead of changing it.

## Impact

- **The local observability release and the documented install path.** The live upgrade is the verification; the
  documented command is gated by `verifyInstallCommands`.
- **No application change:** the chart is third-party, and the application chart's own dependency (the Bitnami `common`
  subchart) is untouched.
- **No test or build-logic change:** `verifyInfraImageVersions` gates only the Bitnami infra charts' `image.tag` drift,
  and the observability charts carry no `*-image-tag`.
