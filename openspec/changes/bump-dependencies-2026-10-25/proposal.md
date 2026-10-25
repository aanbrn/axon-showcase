# Proposal

## Why

The weekly update checks list stable catalog updates, two in-range npm updates, and a Gradle wrapper patch release.
Re-resolved on 2026-10-25 with the repo's own checks rather than read off issue #13: `./gradlew dependencyUpdates` names
fourteen catalog rows and `Gradle: [9.8.1 -> 9.8.2]`, of which thirteen are taken and one is held back on CI evidence
(below), and `:showcase-web-ui:npmOutdated` names two in-range rows plus one deferred major. The catalog report carries
one row the issue body does not — `com.github.jknack:handlebars [4.5.5 -> 4.5.6]` — because the issue was last refreshed
on 2026-10-24, so the report is the authority for this unit's set.

## What Changes

- **`gradle/libs.versions.toml`** — thirteen keys: `lz4-java` `1.12.0`→`1.12.1`, `jackson2-bom` `2.22.3`→`2.22.4`,
  `jackson3-bom` `3.2.3`→`3.2.4`, `handlebars` `4.5.5`→`4.5.6`, `zstd-jni` `1.5.7-23`→`1.5.7-24`, `errorprone`
  `2.50.0`→`2.51.0`, `nullaway` `0.14.2`→`0.14.3`, `grpc` `1.85.0`→`1.85.1`, `micrometer-bom` `1.17.1`→`1.17.2`,
  `micrometer-tracing-bom` `1.7.1`→`1.7.2`, `reactor-bom` `2025.0.7`→`2025.0.8`, `springdoc-openapi-starter`
  `2.9.1`→`2.10.0`, `swagger-ui` `5.33.2`→`5.34.0`.
- **`reactor-blockhound` held at `1.0.17.RELEASE`** — the one catalog row this unit does not take. Taking
  `1.0.18.RELEASE` made the gateway's two `@WebFluxTest` slices wedge on CI: 38 `ShowcaseRestControllerCT` cases and 7
  `TelemetryControllerCT` cases timed out at `.exchange()` in two of three runs of the bumped tree, with 45 thread dumps
  whose content shows an idle JVM and no thread in the request path; the same tree passed on a re-run and every local
  run, six of them plus three under CPU saturation. A CI bisect isolated the coordinate: `reactor-core` 3.8.8 with
  `blockhound` held at `1.0.17.RELEASE` was green where the bumped pair failed twice. The mechanism is unproven, so this
  is a recorded deferral, not a fix: `design.md` carries the probe, `docs/ideas.md` the diagnosis and its next step, and
  the upstream report is cited there.
- **`gradle/wrapper/gradle-wrapper.properties`** — `9.8.1`→`9.8.2`. The wrapper jar and the two scripts regenerate
  byte-identical, so only the properties file changes; the jar still matches the one the 9.8.2 distribution ships.
- **`showcase-web-ui/package-lock.json`** — `react-hook-form` `7.90.0`→`7.91.0` and `eslint-plugin-boundaries`
  `7.2.1`→`7.3.0` in range, plus the graph they re-resolve: 13 packages moved, 5 removed and none added. The removals
  are the `micromatch` chain (`braces`, `fill-range`, `is-number`, `micromatch`, `to-regex-range`), which the two
  boundaries packages dropped at their new versions; two entries flip from `dev` to `devOptional` (`fast-deep-equal`,
  `require-from-string`), and `@napi-rs/wasm-runtime`'s bump narrows its two `@emnapi` peer ranges to
  `^1.7.1 || 2.0.0-beta.2`. `package.json` is unchanged: every moved release already satisfies its caret range.
- **Five Error Prone suppressions removed** — `UnrecognisedJavadocTag` no longer fires at `2.51.0` on the
  Lombok-processed classes that carried `@SuppressWarnings("UnrecognisedJavadocTag")` (`ShowcaseApiProperties`,
  `ShowcaseEntity`, and the three telemetry classes), so the suppression and its upstream reference are retired rather
  than bumped. The differential is in `design.md`.
- **`openspec/changes/bump-dependencies-2026-10-25/specs/showcase/quality/dependency-management/spec.md`** — a
  `MODIFIED` delta de-versioning the calendar-versioning examples (see Modified Capabilities below), as the
  `reactor-bom` examples in `README.md` and the `gradle-build` pack were de-versioned rather than re-versioned.
- **Docs that name a bumped version as a current value** — `AGENTS.md` and `openspec/config.yaml` (Gradle `9.8.2`),
  `README.md`'s wrapper pin, the NullAway parenthetical in `docs/ideas.md`, and ADR-0014's `braces` consequence, which
  no longer resolves at all.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

`showcase/quality/dependency-management`: the calendar-versioning requirement's two scenarios illustrate their rule with
the `reactor-bom` service release this change moves, so they now read as a past pinned pair. `MODIFIED` de-versions
those examples (the `2025.0` train, "a later `2025.0.x`") rather than swapping in the new numbers, which would leave the
next service-release bump with the same stale example. The bump itself changes no behavior, so the delta edits no
normative clause.

## Impact

- **Runtime path, so the integration tier verifies it**: the moved BOMs, Kafka codecs (`lz4-java`, `zstd-jni`), Jackson,
  Reactor and Micrometer reach every service; `grpc` reaches the gateway and the query side; `springdoc`/`swagger-ui`
  reach the gateway only. `design.md` states the reach derived from the resolved classpaths.
- **Test-tooling path**: `reactor-blockhound` stays at `1.0.17.RELEASE` (above), and it is installed by the projection
  service's integration suite, both clients' component suites, and the gateway's component and e2e suites — `check` runs
  four of the five (all but the gateway e2e suite, which is the opt-in task); `errorprone`/`nullaway` gate every
  non-test compile.
- **Bundle path**: `react-hook-form` and the ESLint stack are web-UI sources, so the frontend `check` and the production
  bundle `build` run.
- **Build path**: the wrapper bump makes the whole `check` run under `9.8.2`.
- **Observable**: the catalog report's update section lists only the held `reactor-blockhound` row, its wrapper line
  reads `UP-TO-DATE`, and the npm report lists only the deferred `@hookform/resolvers` major.
- **No suppression moves**: `.snyk`'s ignores are all Spring and Validation coordinates, and `npmAudit` passing shows no
  `npm-audit-ignores.json` entry went stale.
