# Design

## Context

See `proposal.md` — Why. Both halves were **re-resolved on 2026-10-16** with the repo's own checks rather than read off
the tracker issue: `./gradlew dependencyUpdates` (the catalog) and `:showcase-web-ui:npmOutdated` (npm). The wrapper's
line reads `9.8.1: UP-TO-DATE`, so no wrapper bump; the `log4j-core` row the report used to carry is now filtered by the
report's own rule and does not appear.

What each bump can reach:

- **The reach, derived once from the resolved classpaths**
  (`./gradlew :<module>:dependencies --configuration integrationTestRuntimeClasspath`): the `caffeine` key feeds two
  libraries and reaches two services — **the command service** (`caffeine` and `jcache`, the latter through
  `CaffeineConfiguration`'s aggregate/saga/association caches) and **the gateway** (`caffeine`). The `grpc` key reaches
  **the gateway**, **the query service** and the modules that declare its artifacts — **the platform** (the BOM import),
  **the query client** and **the query proto**; the query service reaches them through `grpc-spring-boot-starter` rather
  than by declaring an `io.grpc` artifact itself. `swagger-ui` reaches **the gateway** as a webjar, on the classpath
  through the platform. All three are runtime path, and the integration tier is what verifies them — which is why task
  2.1 runs it for the command, gateway and query services.
- The npm set spans the bundle (`react`/`react-dom`), the test tooling (`vitest`, `@vitest/coverage-v8`, `jsdom`, and
  `@playwright/test`), the lint gate (`typescript-eslint` and its two packages), and application data/state (`zod`,
  `@tanstack/react-query`, `@testing-library/user-event`). The frontend `check` exercises all of them except
  `@playwright/test`, whose suite is the opt-in `e2eTest` (the type-check reads `src`, the lint runs `eslint src`, and
  no e2e ran in this unit) — so the Playwright bump rests on the four direct rows around it resolving and the lockfile
  diff, not on the `check`. The bundle `build` covers the two that ship.
- `npm update` reconciles the whole graph rather than the twelve rows: 50 packages moved, including `rolldown` and its
  platform bindings, `postcss`, and the browser-data packages (`caniuse-lite`, `node-releases`,
  `baseline-browser-mapping`), plus one **cross-major** transitive, `@asamuzakjp/dom-selector` `9.2.4`→`10.0.3` (a
  `jsdom` dependency).

The version-bump staleness sweep found nothing stale. Two mentions of a bumped version are deliberate records rather
than current values:

- `AGENTS.md`'s Caffeine bullet says "(verified against Caffeine `3.3.0`)" — the version an observation was made
  against, like ADR-0007's tool version, so rewriting it would falsify the claim it describes.
- `showcase-web-ui/package.json` declares caret ranges (`"react": "^19.3.0"`), not pinned values; the lockfile is where
  the resolution lives, and the ranges still admit the new versions.

## Goals / Non-Goals

**Goals:**

- Take the stable, same-major catalog rows and the in-range npm rows the reports resolve now.
- Verify at the tiers each half's effect reaches: the integration tier for the runtime-path bumps, the frontend `check`
  and bundle `build` for the npm set.

**Non-Goals:**

- The Gradle wrapper: already current, so no wrapper bump and no toolchain-version doc sweep.
- Any major bump: none is in either set (the one cross-major move is a `jsdom` transitive, not a manifest major), and a
  manifest major would go through `major-disabled.txt` and a coordinated migration.

## Decisions

**Take both halves in one unit, as the routine set.** They share the trigger (the weekly reports), the timing, and the
review, and the repo's precedent for this unit type is exactly this pair. Splitting them would pay two full
verifications for one week's updates.

**Verify with the full `check` (integration tests), the frontend `check`, and the bundle `build`.** `caffeine` and
`grpc` are runtime path, so the Test-tiers convention puts them behind the integration tier; `react`/`react-dom` are in
the bundle, so `build` is run rather than `check` alone.

**Run `npmAudit` as the npm-specific check.** An `npm update` can introduce an advisory and, unlike the JVM's `.snyk`
policy, an entry in `npm-audit-ignores.json` that matches no finding fails that task — so its pass is the evidence that
no ignore went stale with the update.

**Leave `package.json`'s ranges and the Caffeine observation mention alone.** The manifest's carets are the declared
constraint; the lockfile carries the resolution the update changed. And a doc naming the version an observation was made
against is a record, not a current value.

## Risks / Trade-offs

- **A runtime-path bump breaks a cross-library contract only at runtime** → the full `check` with integration tests is
  the mitigation, as the Test-tiers convention records.
- **The npm reconciliation moves packages beyond the twelve rows, including a cross-major transitive** → the lockfile
  diff is described by what moved, and the frontend `check` plus the bundle `build` cover it.
- **The eslint update can change lint findings** → the frontend `check` runs lint; a new finding fails it rather than
  passing silently.

## Migration Plan

None: version bumps, taking effect on the next build.

## Claims table

| Claim                                                     | Source                                                                                                                                                                                                                                                                                                                                   |
| --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The catalog rows, at the versions named                   | `./gradlew dependencyUpdates`'s update section, re-resolved for this change (`caffeine [3.3.0 -> 3.3.1]`, `grpc-bom [1.84.2 -> 1.84.3 -> 1.85.0]`, `swagger-ui [5.33.1 -> 5.33.2]`)                                                                                                                                                      |
| The `log4j-core` row is gone                              | the catalog report's update section, re-run for this change: `DependencyUpdateRules.staleCurrentVersion` filters the row the report used to carry                                                                                                                                                                                        |
| The npm rows, and the set that moved                      | `:showcase-web-ui:npmOutdated` (twelve direct rows) and the lockfile diff parsed package-by-package: 50 moved, one cross-major                                                                                                                                                                                                           |
| The wrapper is current                                    | the same report's `Gradle CURRENT updates` line reads `9.8.1: UP-TO-DATE`, so no wrapper bump is owed                                                                                                                                                                                                                                    |
| Nothing filtered was taken, and nothing taken is deferred | the report's update section is empty after the bump; none of the three coordinates is in `config/dependency-updates/major-disabled.txt` or `hold-back.txt`; the one cross-major move is a `jsdom` transitive the manifest admits, not a manifest major, and `config/web-ui-updates/major-disabled.txt` still defers `typescript`'s major |
| The runtime-path reach                                    | the resolved `integrationTestRuntimeClasspath` of the command service, the gateway and the query service, queried per module — the list the Context states                                                                                                                                                                               |
| The bundle-path reach                                     | `showcase-web-ui/package.json`'s `react`/`react-dom` dependencies and the built bundle                                                                                                                                                                                                                                                   |
| No `.snyk` ignore moves                                   | `.snyk`'s seven coordinates are all `org.springframework*`; none is a library this bump moves                                                                                                                                                                                                                                            |
| No npm ignore goes stale                                  | `./gradlew :showcase-web-ui:npmAudit` passes, which it does not when an entry in `npm-audit-ignores.json` matches no finding                                                                                                                                                                                                             |
| The two doc mentions are records, not current values      | `AGENTS.md`'s Caffeine bullet reads "(verified against Caffeine `3.3.0`)"; `showcase-web-ui/package.json` carries caret ranges                                                                                                                                                                                                           |
