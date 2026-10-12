# Design

## Context

See `proposal.md` — Why. The actionable sets were **re-resolved on 2026-10-12** with the repo's own checks rather than
read off the tracker issue: `./gradlew dependencyUpdates` (catalog), `:showcase-web-ui:npmOutdated` (npm),
`helmUpdates`, `buildpackUpdates`, and `toolingUpdates`. Both halves moved since the issue refreshed on 2026-10-10: in
the catalog, `zstd-jni` to `1.5.7-23` and `spotbugs` `4.10.5` newly surfaced; in npm, five rows (`vitest` and
`@vitest/coverage-v8`, `@tanstack/react-query`, `react-hook-form`, `zod`) published after the refresh. That is why the
re-resolve is the step, not the issue's text.

The catalog rows taken are the stable, same-major ones. The `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` row
is the known `gradle-versions-plugin` build-environment artifact (the floor of a Log4Shell guard published by
`spotbugs-annotations`, upstream `ben-manes/gradle-versions-plugin#755`; ADR-0007) and is skipped, as the repo does on
every run.

The Spotless plugin bump reflows sources. That is the plugin's bundled formatter changing version, not a config change —
the same class as the recorded `8.10.2`→`8.10.3` bump, which reflowed 13 Java test sources. `spotlessApply` is the
canonical output, and `spotlessCheck` passes against it; a whitespace-ignored diff is non-empty because the reflow adds
trailing commas and rejoins expressions (content-adjacent, not whitespace).

## Goals / Non-Goals

**Goals:**

- Take the stable catalog and in-range npm updates the current reports list, at the versions the reports resolve now.
- Bump the wrapper, and account for the formatter reflow the Spotless bump causes.

**Non-Goals:**

- The Helm, buildpack, and Snyk-tooling findings — each is a separate unit with environment-specific verification (a
  live Helm install, an image build and run, a credentialed scan), and none is verifiable in this change's gate.
- The `log4j-core` row: a known spurious artifact of the build environment, not a real update.

## Decisions

**Bump only the same-major, stable rows, and skip the spurious one.** A major bump goes through `major-disabled.txt` and
a coordinated migration (Axon, Spring, Flyway, the Bitnami charts); this change is the routine stable set. The
`log4j-core` row is excluded on the ADR-0007 reasoning rather than by adding a suppression (the report is expected to
carry it).

**Accept the Spotless reflow in this change rather than deferring the plugin bump.** The reflow is the formatter's own
output, gated by `spotlessCheck`, and the verification (a full `check`) is what a formatter change needs anyway. Holding
the bump back to keep the diff small would leave the pinned plugin a release behind for no safety gain.

**Verify with the full `check` (integration tests) plus `:showcase-web-ui:build`.** A same-major runtime-path bump can
be binary-incompatible with another pinned library while compilation and the Docker-free suites pass (`opensearch-java`
`3.9.0`→`3.10.0` was), so the integration tier is the tier that verifies this; the web UI's bundle is built because the
npm set includes bundle-path packages (`vite`).

## Risks / Trade-offs

- A runtime-path bump breaks a cross-library contract only at runtime → the full `check` with integration tests is the
  mitigation, as the Test tiers convention records.
- The npm reconciliation moves packages beyond the report's 13 rows → the lockfile diff is described by what moved, and
  the frontend `check` is run.
- The wrapper bump's scripts are unchanged (a patch release) → verified by diffing the four wrapper files; only the
  properties and the jar changed.

## Migration Plan

None: a version bump, taking effect on the next build.
