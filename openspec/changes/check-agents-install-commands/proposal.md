# Proposal

## Why

`AGENTS.md`'s Kubernetes Deployment section quotes each Helm chart's version by hand in its manual
`helm install … --version` block, so a chart bump can silently leave it stale — the `bump-kps-chart-91-8` bump had to
correct `kps … --version 91.4.1` by hand, and it went unnoticed until review. The `infra-image-versions` capability
already states "No surface SHALL hard-code an independent version", but only the infra image tags are gated; the manual
install block is not.

## What Changes

- `build-logic` — a new `verifyInstallCommands` task and a pure rules object that parse the `AGENTS.md` install block
  and check each chart's `--version` against the catalog pin (the same `chartRef` → `pinnedVersion` pairs `helmUpdates`
  uses).
- `build.gradle.kts` — wire the task into `check`.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` — delta: the single-sourced requirement gains the
  AGENTS.md install-command verification.
- `AGENTS.md` — `verifyInstallCommands` added to the `check`-note enumeration and the `verifyInfraImageVersions` prose.
- `README.md` — `verifyInstallCommands` added to the verification-command block.
- `docs/ideas.md` — the parked idea is removed (implemented by this change).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the "Infrastructure image references are single-sourced" requirement gains a
  scenario verifying the `AGENTS.md` manual install commands against the catalog.

## Impact

- `build-logic` (a new task + rules + tests), `build.gradle.kts` (`check` wiring), `AGENTS.md` and `README.md` (the
  check enumerated), the `infra-image-versions` spec delta, and the `docs/ideas.md` removal.
- No application, source, or runtime behavior change.
