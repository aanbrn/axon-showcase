# Proposal

## Why

The update-check reports (`dependencyUpdates` + the web UI `npmOutdated`) list stable catalog updates and in-range npm
updates; taking them keeps the pinned set current. Re-resolved on 2026-10-12 rather than read off the tracker issue,
both halves had moved: the catalog (`zstd-jni` at `1.5.7-23`, not the `1.5.7-22` the issue named, and `spotbugs` newly
surfaced) and npm (five rows published after the refresh). The Gradle wrapper has a patch release (`9.8.0` → `9.8.1`).
One report row is the known spurious `log4j-core 2.17.1` (the Log4Shell floor published by `spotbugs-annotations`,
ADR-0007) and is deliberately skipped.

## What Changes

- **`gradle/libs.versions.toml`** — ten stable catalog bumps: `jetty` `12.1.13`→`12.1.14` (both Jetty BOMs), `netty-bom`
  `4.2.18.Final`→`4.2.19.Final`, `grpc` `1.84.0`→`1.84.2`, `elasticsearch-client-java` `9.5.4`→`9.5.5`, `httpclient5`
  `5.6.4`→`5.6.5`, `opentelemetry-bom` `1.66.0`→`1.67.0`, `zstd-jni` `1.5.7-21`→`1.5.7-23`, `kotlin` `2.4.20`→`2.4.21`,
  `spotbugs` `4.10.4`→`4.10.5`, `spotless-plugin` `8.10.3`→`8.10.4`.
- **The Spotless bump's reflow** — `8.10.3`→`8.10.4` bundles newer formatters, so `spotlessApply` reflows 24 tracked
  sources and build files (trailing commas in Kotlin DSL, rejoined Java expressions). Accounted for here rather than
  reported as "no source change", per the Formatting convention.
- **`showcase-web-ui/package-lock.json`** — an `npm update` reconciliation: the report's 13 direct rows (`vite`
  `8.3.2`→`8.3.4`, `vitest` and `@vitest/*` `5.0.3`→`5.0.4`, the `typescript-eslint` set `8.71.0`→`8.71.1`, `prettier`
  `3.9.9`→`3.9.10`, `@playwright/test` `1.63.0`→`1.64.0`, `@tanstack/react-query`, `react-hook-form`, `zod`,
  `@types/node`, `@vitejs/plugin-react`, `@vitest/coverage-v8`), plus the graph they re-resolve — 69 packages moved in
  all, including `rolldown` `1.2.11`→`1.2.13` with its platform bindings and the one **cross-major** move, `obug`
  `2.2.1`→`3.0.0` (a `vitest` transitive).
- **The Gradle wrapper** — `9.8.0` → `9.8.1` (`distributionUrl` plus the regenerated `gradle-wrapper.jar`), and the
  three documents that name the toolchain version (`AGENTS.md`, `README.md`, `openspec/config.yaml`).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A pure dependency bump whose coordinates do not change a specified behavior.

## Impact

- Files: `gradle/libs.versions.toml`, `gradle/wrapper/*`, `showcase-web-ui/package-lock.json`, the reflowed sources, and
  the doc pins the two version bumps move — `AGENTS.md`, `README.md`, `openspec/config.yaml` (Gradle 9.8.1) and
  `docs/adr/0012-prefer-typed-opensearch-java-client.md`, `docs/ideas.md` (`elasticsearch-client-java` 9.5.5).
- Verified by the **full `check`** (integration tests on Testcontainers) plus `:showcase-web-ui:build`, since the bump
  touches a runtime-path library and a bundle-path package (`vite`).
- **Out of scope** — the other update-check findings each need environment-specific verification and are separate units:
  the Helm chart bump (`prometheus-community-stack` `91.9.0`→`92.3.0`, a live install + smoke), the buildpack pins
  (builder `0.4.653`→`0.4.659` with `paketo-nginx`/`paketo-procfile`, an image build + run), and the Snyk CLI pin
  (`v1.1307.4`→`1.1308.0`, the credentialed weekly run).
