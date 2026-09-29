# Proposal

## Why

The weekly `tooling-updates` check (tracker #308, 2026-09-29) flagged a newer Snyk CLI than the version the
dependency-security workflow pins.

## What Changes

- `.github/workflows/dependency-security.yml` — the `snyk/actions/setup` step's `snyk-version` moves `v1.1307.3` →
  `v1.1307.4`.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure workflow-pin bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `.github/workflows/dependency-security.yml` (the Snyk CLI pin).
- No source, application, or runtime behavior change.
