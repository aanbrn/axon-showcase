# Proposal: Pin the Paketo buildpacks for the web-UI image

## Why

The nightly `e2e` workflow failed at `:showcase-web-ui:dockerBuildImage` with
`unable to resolve version: multiple versions of 'paketo-buildpacks/procfile' - must specify an explicit version`. The
build passes **unversioned** buildpacks against the floating `paketobuildpacks/builder-jammy-base:latest`, and the
current builder bundles two `procfile` versions (5.13.7 and 5.14.0), so the reference is ambiguous. It is intermittent
(an upstream transitional builder publish), and it also breaks `helmInstallToLocal`/releases, which build the same
image.

## What Changes

- Add the two buildpack versions to the Gradle version catalog (`paketo-nginx`, `paketo-procfile`) and pin the web-UI
  image build to them, in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`: `paketo-buildpacks/nginx@1.2.0`
  and `paketo-buildpacks/procfile@5.14.0` (both verified to build locally).
- Add a short note to `AGENTS.md` recording the pinned buildpacks and why (the floating-builder ambiguity).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. The `showcase/deployment/web-ui` spec requires the image to serve the built bundle over HTTP with nginx; pinning
  the buildpack versions does not change that behavior. This is a build-tooling fix, so no spec delta.

## Impact

- **Code**: `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` (the `dockerBuildImage` task's `buildpacks`).
- **Build**: `gradle/libs.versions.toml` (the two `[versions]` entries).
- **Docs**: `AGENTS.md`.
- **Scope note**: pinning the builder itself (and tracking buildpack/builder updates, which neither `dependencyUpdates`
  nor `helmUpdates` cover) is out of scope for this fix — a separate follow-up.
