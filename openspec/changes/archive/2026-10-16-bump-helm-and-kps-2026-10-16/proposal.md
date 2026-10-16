# Proposal

## Why

The `helmUpdates` check reports two rows — `helm: 4.3.0 -> 4.3.1` and `prometheus-community-stack: 92.3.0 -> 92.9.0` —
from this change's own run of it (the "Helm updates" tracker issue #40 that the weekly workflow maintains still carries
the previous unit's rows, as the workflow updates it on its next pass). Both were re-resolved with the tools' own
lookups before bumping, per the stale-target rule: the Helm CLI's latest release is `v4.3.1`, and the local index was
stale (listing `92.3.0`) until `helm repo update prometheus-community` reported `92.9.0`, matching it. The chart is one
of the observability charts, which are deliberately actionable — unlike the Bitnami infra majors, they carry no
`*-image-tag` whose preconfigured value a bump could silently diverge from — and the CLI is the Gradle Helm plugin's
client version, consumed by `helm-conventions` as `libs.versions.helm`. One live install exercises both: the plugin
downloads the new client to install the new chart.

## What Changes

- **`gradle/libs.versions.toml`** — `helm` `4.3.0`→`4.3.1` and `prometheus-community-stack` `92.3.0`→`92.9.0`.
- **`AGENTS.md`** — the manual `helm install kps prometheus-community/kube-prometheus-stack --version` command in the
  Kubernetes Deployment section moves with the chart, because `verifyInstallCommands` (part of `check`) asserts those
  commands against the catalog's chart pins — the two are one edit, not two.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. Two version bumps; no specified behavior changes. `showcase/quality/infra-image-versions`
already owns this exact case — it names `prometheus-community-stack` among the Helm chart coordinates that must be a
concrete version, carries the scenario "An observability chart major bump is actionable and smoke-tested", and holds the
requirement that the `AGENTS.md` manual install commands match the catalog. This change satisfies that wording (bump the
pin, run the live install + smoke, move the documented command) without altering it, so no delta is owed — not that no
capability covers it. The Helm CLI pin is the plugin's client version under the catalog single-sourcing rule (ADR-0015),
which this change likewise satisfies rather than changes.

## Impact

- Files: `gradle/libs.versions.toml`, `AGENTS.md`.
- **Verified by a live install + a smoke**, the only thing that proves a bumped observability chart deploys and that the
  new CLI client works: `kps` upgraded from the new chart in `monitoring`, then pods ready, Prometheus targets up, and
  Grafana's datasources wired. `verifyInfraImageVersions` gates only the Bitnami infra charts' image-tag drift, not
  this.
