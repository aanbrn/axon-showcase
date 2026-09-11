# Proposal: Pin and track the Paketo builder

## Why

The web-UI image is built by `pack` against the floating `paketobuildpacks/builder-jammy-base:latest`.
`pin-paketo-buildpacks` (#147) fixed the immediate `multiple versions … must specify an explicit version` failure by
pinning the two buildpacks, but the **builder** itself (its lifecycle, run-image reference, and bundled buildpacks)
still floats: the same class of upstream drift can still break the build, and the builder toolchain is not reproducible.
The builder publishes a version almost daily (`latest` = `0.4.639` today) and no update check covers Paketo versions, so
any pin would otherwise go stale silently.

## What Changes

- Pin the builder to a concrete version tag, catalog-owned as `paketo-builder-jammy-base` (`0.4.639`), in
  `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`.
- Add a `buildpackUpdates` Gradle task that reports newer versions for the pinned Paketo builder and buildpacks from the
  registry, and a weekly `buildpack-updates` workflow that opens/updates a "Buildpack updates" issue (mirroring
  `helmUpdates`/`helm-updates.yml`).
- Record the pin and the new check in `AGENTS.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/merge-governance` — add a requirement for the buildpack update report (scheduled + manual + not a
  merge gate), alongside the existing dependency-updates and helm-updates requirements.

## Impact

- **Build**: `gradle/libs.versions.toml`, `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`,
  `build.gradle.kts`, `build-logic/src/main/kotlin/BuildpackUpdatesTask.kt` (new).
- **CI**: `.github/workflows/buildpack-updates.yml` (new).
- **Docs**: `AGENTS.md`.
- **Spec**: `showcase/quality/merge-governance` delta.
- **Scope note**: the run image (`paketobuildpacks/run-jammy-base:latest`) is deliberately left floating, so base-OS
  security patches keep flowing; pinning it end-to-end is out of scope.
