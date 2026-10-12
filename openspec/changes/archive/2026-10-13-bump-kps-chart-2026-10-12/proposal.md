# Proposal

## Why

The `helmUpdates` check's current report — the "Helm updates" tracker issue #40, refreshed 2026-10-10 — names one row:
`prometheus-community-stack: 91.9.0 -> 92.3.0`. It is a chart **major** among the observability charts, which are
deliberately actionable — unlike the Bitnami infra majors, the observability charts carry no `*-image-tag` whose
preconfigured value a major would silently diverge from, so nothing suppresses them and the upgrade is a normal install.
Re-resolved before bumping, per the stale-target rule: the local Helm index was stale (still listing `91.9.0`), and
after `helm repo update prometheus-community` the latest is `92.3.0`, matching the report.

## What Changes

- **`gradle/libs.versions.toml`** — `prometheus-community-stack` `91.9.0`→`92.3.0`.
- **`AGENTS.md`** — the manual `helm install kps … --version` command in the Kubernetes Deployment section moves with
  it, because `verifyInstallCommands` (part of `check`) asserts those commands against the catalog's chart pins — the
  two are one edit, not two.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A chart version bump; no specified behavior changes. `showcase/quality/infra-image-versions`
already owns this exact case — it names `prometheus-community-stack` among the concrete pins, carries the scenario "An
observability chart major bump is actionable and smoke-tested", and holds the requirement that the `AGENTS.md` manual
install commands match the catalog. This change satisfies that wording (bump the pin, run the smoke, move the documented
command) without altering it, so no delta is owed — not that no capability covers it.

## Impact

- Files: `gradle/libs.versions.toml`, `AGENTS.md`.
- **Verified by a live install + smoke**, which is the only thing that proves a bumped chart deploys: `kps` installed
  from the new chart into `monitoring`, then pods ready, Prometheus targets up, and Grafana's datasources wired.
  `verifyInfraImageVersions` gates only the Bitnami infra charts' image-tag drift, not this.
