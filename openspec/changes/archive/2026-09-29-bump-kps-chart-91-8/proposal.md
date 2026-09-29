# Proposal

## Why

The weekly `helm-updates` check (tracker #40, 2026-09-28) flagged a newer `kube-prometheus-stack` chart than the pinned
version. The observability charts carry no `*-image-tag` to cross-check against a test-surface pin, so a bump is
verified only by installing it.

## What Changes

- `gradle/libs.versions.toml` — `prometheus-community-stack` 91.4.1 → 91.8.1 (the resolved latest; the tracker named
  91.8.0, since superseded).
- `AGENTS.md` — the manual `helm install kps … --version` command in the Kubernetes Deployment section moves to
  `91.8.1`.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure Helm-chart-pin bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `gradle/libs.versions.toml` (the `prometheus-community-stack` pin).
- `AGENTS.md` — the manual install command's version refreshed.
- The observability release the `kps` Helm target installs; no application, source, or runtime behavior change.
