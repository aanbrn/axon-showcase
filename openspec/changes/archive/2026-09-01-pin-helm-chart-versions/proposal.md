## Why

Three Helm chart coordinates in the version catalog are floating major-line pins (`77.x.x`, `1.x.x`, `2.x.x`), so
`helmInstallToLocal` deploys "latest 77.x / latest 1.x / latest 2.x" on every run — not a reproducible, reviewable
version. The infra charts (postgres, kafka, opensearch) are already pinned concretely and gate-verified; the remaining
charts should be too, as the foundation for a later automated "your pin is stale" check.

## What Changes

- Pin `prometheus-community-stack` from `77.x.x` to a concrete version (`77.14.0`) in `gradle/libs.versions.toml`.
- Pin `grafana-tempo` from `1.x.x` to a concrete version (`1.24.4`).
- Pin `bitnami-common` from `2.x.x` to a concrete version (`2.41.0`).
- Document in `AGENTS.md` the convention that **all Helm chart coordinates in the catalog are concrete pins — never a
  floating `x.x`** — so the project cannot reintroduce floating chart pins. This is documented only, not a build gate,
  for now.
- Add `--version` flags to the manual `helm install` commands in `AGENTS.md` matching the catalog pins, so the
  documented manual path no longer contradicts the verified pins.
- The concrete bitnami chart pins (`16.7.27`, `31.5.0`, `2.0.10`) and the `verifyInfraImageVersions` gate are
  unchanged.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `showcase/quality/infra-image-versions`: the "Infrastructure image references are single-sourced" requirement gains
  the rule that every Helm chart coordinate in the catalog (not only the infra image charts) is a concrete version —
  the floating `77.x.x`/`1.x.x`/`2.x.x` observability and common-subchart pins become concrete.

## Impact

- `gradle/libs.versions.toml` — three coordinates become concrete.
- `AGENTS.md` — chart-pin convention note + `--version` flags on the manual `helm install` commands.
- `openspec/specs/showcase/quality/infra-image-versions/spec.md` — requirement/scenario wording.
- No build-gate changes; `verifyInfraImageVersions` and its cache behavior are untouched.