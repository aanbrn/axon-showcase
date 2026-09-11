## 1. Pin the builder

- [x] 1.1 Add `paketo-builder-jammy-base = "0.4.639"` to `gradle/libs.versions.toml` `[versions]`
- [x] 1.2 In `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`, set the builder from the catalog:
      `"paketobuildpacks/builder-jammy-base:" + libs.versions.paketo.builder.jammy.base.get()`

## 2. Buildpack update check

- [x] 2.1 Add `build-logic/src/main/kotlin/BuildpackUpdatesTask.kt` — for each configured check, query the Docker Hub
      tags API for its **repository**, take the highest numeric tag, and write the newer-than-pinned entries to a report
      file (mirroring `HelmUpdatesTask`). The check model carries the Docker Hub `repository` separately from the
      display name, since a buildpack's CNB id (`paketo-buildpacks/nginx`) is not its Docker Hub repository
      (`paketobuildpacks/nginx`)
- [x] 2.2 Register a `buildpackUpdates` task in the root `build.gradle.kts`, supplying the pinned builder and the two
      pinned buildpacks from the catalog with their Docker Hub repositories (mirroring the `helmUpdates` registration)
- [x] 2.3 Add `.github/workflows/buildpack-updates.yml` — weekly schedule + `workflow_dispatch`, `issues: write`,
      opening or updating a "Buildpack updates" issue (mirroring `helm-updates.yml`)
- [x] 2.4 Add the delta requirement to
      `openspec/changes/pin-and-track-paketo-builder/specs/showcase/quality/merge-governance/spec.md`

## 3. Docs

- [x] 3.1 `AGENTS.md`: (a) update the web-UI image paragraph — the builder is now **pinned** (catalog-owned
      `paketo-builder-jammy-base`), so the "the builder is floating (`...builder-jammy-base:latest`)" and "neither
      `dependencyUpdates` nor `helmUpdates` tracks buildpack/builder versions" claims must be revised to name
      `buildpackUpdates`; (b) add the `buildpack-updates.yml` workflow to the Continuous Integration section, alongside
      the dependency-updates and helm-updates paragraphs; (c) revise the build-tool-versions convention — its "bare
      `[versions]` entries are not resolved as dependencies … they go stale silently and must be audited by hand"
      sentence becomes materially false for the Paketo pins once `buildpackUpdates` exists; (d) add `buildpackUpdates`
      to the Snyk-CLI gotcha's list of update checks, for consistency

## 4. Verify

- [x] 4.1 `./gradlew buildpackUpdates` writes `build/buildpack-updates/report.txt`; with the current pins it reports no
      updates
- [x] 4.2 `./gradlew :showcase-web-ui:dockerBuildImage` succeeds with the pinned builder
- [x] 4.3 `./gradlew spotlessCheck`, `./gradlew workflowLint`, `openspec validate --all` pass
