# Proposal

## Why

The weekly `helm-updates` check (tracker #40, the 2026-10-04 run) flagged a newer `kube-prometheus-stack` chart than the
pinned version. The observability charts carry no `*-image-tag` to cross-check against a test-surface pin, so a bump is
verified only by installing it.

## What Changes

- `gradle/libs.versions.toml` — `prometheus-community-stack` 91.8.1 → 91.9.0 (the resolved latest; the tracker named
  91.9.0, confirmed with `helm search repo`).
- `AGENTS.md` — the manual `helm install kps … --version` command in the Kubernetes Deployment section moves to `91.9.0`
  (the `verifyInstallCommands` gate cross-checks it against the catalog pin).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure Helm-chart-pin bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `gradle/libs.versions.toml` (the `prometheus-community-stack` pin).
- `AGENTS.md` — the manual install command's version refreshed.
- The observability release the `kps` Helm target installs; no application, source, or runtime behavior change.
