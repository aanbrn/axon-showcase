# Proposal

## Why

The four JVM service images are built with Spring Boot's default Paketo builder, an **unpinned**
`paketobuildpacks/builder-noble-java-tiny:latest`. On 2026-10-23 that release retagged `latest` to an arm64-only
manifest, and Spring Boot derives the build platform from the pulled builder image, so `bootBuildImage` pulled the arm64
run image on the amd64 CI runner and failed with `exec /cnb/lifecycle/creator: exec format error`; the nightly `E2E` run
went red, and the deployment smoke and release image builds carry the same exposure. The web-UI image already pins its
builder in the catalog; the JVM path does not.

## What Changes

- Add `paketo-builder-noble-java-tiny = "0.0.200"` and `paketo-health-checker = "2.14.0"` to the version catalog.
- Set `bootBuildImage`'s builder from the catalog pin in `spring-boot-conventions.gradle.kts`, and pin the
  health-checker buildpack there as an image reference instead of letting it resolve as `latest` from the registry.
- Add both coordinates to `buildpackChecks` in `build.gradle.kts`, so `buildpackUpdates` reports their newer versions
  and `verifyDocumentedPins` requires them stated in the `deployment-operations` pack.
- Widen `DocumentedPinRules` (and its tests) to the two pin forms the existing catalog does not cover: a second builder
  (`builder-<name>:<version>`) and a buildpack referenced as an image (`docker://<repository>/<name>:<version>`).
- State the two new pins, and why the JVM builder is pinned rather than floating, in the `deployment-operations` pack.
- Reword the README's buildpack-update sentence to each pinned builder, since the catalog now pins two.
- Reference upstream `paketo-buildpacks/builder-noble-java-tiny#344`, which already reports the retag and names its
  cause, instead of filing a duplicate.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/infra-image-versions`: the documented-pin requirement states which pin forms `check` verifies and
  which pins the pack must state — one builder and one buildpack spelling today, two builders and an image-reference
  buildpack form after this change.
- `showcase/quality/merge-governance`: the buildpack update report covers the pinned Paketo builder in the singular; it
  must cover each pinned builder, since the catalog pins two.

## Impact

- **Build**: `gradle/libs.versions.toml`, `build-logic/src/main/kotlin/spring-boot-conventions.gradle.kts`,
  `build.gradle.kts`, `build-logic/src/main/kotlin/DocumentedPinRules.kt` and its tests.
- **CI**: the image-building workflows (`e2e.yml`, `deployment-smoke.yml`, `release.yml`) gain a reproducible builder;
  no workflow file changes.
- **Docs**: `.opencode/skills/deployment-operations/SKILL.md`, `README.md`, plus a parked out-of-scope docs defect in
  `docs/ideas.md`.
- **Specs**: the two deltas above.
- **Runtime**: the base image is pinned with the builder — builder `0.0.200` names `ubuntu-noble-run-tiny:0.0.150`, and
  the old floating `latest` (builder `0.0.198`) named run image `0.0.148` — so base-OS patches now arrive with a builder
  bump; the entrypoint and environment are unchanged.
