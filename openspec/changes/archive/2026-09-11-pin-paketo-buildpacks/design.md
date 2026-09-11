## Context

See proposal.md — Why. The web-UI image is built by a generic `dockerBuildImage` task
(`frontend-conventions.gradle.kts`) that runs `pack` with `builder = "paketobuildpacks/builder-jammy-base"` (floating
`:latest`) and an explicit, unversioned `buildpacks` list. A freshly-pulled builder now bundles two
`paketo-buildpacks/procfile` versions (5.13.7 and 5.14.0), so the unversioned reference cannot resolve. Reproduced
locally with `pack build … --pull-policy always` (same error and run-image digest as CI); the four JVM service images
are unaffected (they don't reference `procfile`).

## Goals / Non-Goals

**Goals:**

- Make the web-UI image build resolve deterministically and stop the intermittent `e2e`/`helmInstallToLocal` failures.

**Non-Goals:**

- Pinning the builder image itself, or adding automated tracking of buildpack/builder versions (a separate follow-up).
- Changing what the image does (still `build/dist` served by nginx, via the same buildpack set).

## Decisions

**D1: Pin explicit versions on the two buildpacks the task passes, rather than dropping it or pinning the builder.** The
resolution error's remedy is an explicit version. _Alternatives considered:_ drop the explicit `procfile` (rejected —
the repo stages a `Procfile` precisely so the procfile buildpack sets the start command); pin the builder to a concrete
tag instead (rejected for now — broader blast radius and it needs a bump cadence; kept as the follow-up).

**D2: Pin the current latest — `paketo-buildpacks/nginx@1.2.0` and `paketo-buildpacks/procfile@5.14.0`.** Both are
published registry versions and were verified to build locally. _Alternative considered:_ `procfile@5.13.7` (the version
the builder's `web-servers`/nginx group references — lower compatibility risk since it is the builder-tested
combination, but older; the security difference is negligible because `procfile` is a build-time glue buildpack with no
runtime network surface, while the HTTP-facing `nginx` is at its latest in both).

**D3: Single-source the versions in the Gradle version catalog (`paketo-nginx`, `paketo-procfile`) rather than as inline
literals in the convention.** `frontend-conventions.gradle.kts` already reads `libs`
(`val libs = the<LibrariesForLibs>()`) for the Node runtime, and the catalog already holds non-Gradle-resolved
tool/runtime versions (`helm`, `checkstyle`, `protoc`, `node`) and the infra image tags — so the buildpack versions
belong there for consistency. They are `[versions]`-only entries (no `[libraries]` module: a buildpack id is not a
`group:name` coordinate); neither update check consumes a bare version entry, so the gain is single-sourcing, not
automation.

## Risks / Trade-offs

- [A pinned buildpack version goes stale silently — neither `dependencyUpdates` nor `helmUpdates` covers Paketo
  buildpacks or the builder] → flagged in the `AGENTS.md` note; pinning/tracking the builder is out of scope here.
- [Pinning to a version the builder does not bundle could be an untested combination] → both pins are published registry
  versions and built locally (nginx 1.2.0 already matches the builder; `procfile` 5.14.0 is the current release).
