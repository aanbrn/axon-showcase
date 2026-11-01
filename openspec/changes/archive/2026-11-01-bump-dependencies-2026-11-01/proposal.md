# Proposal

## Why

The weekly dependency check (issue #13) reports stable catalog updates, eight in-range npm rows, and five `build-logic`
report rows (four pins, since `helm-releases-plugin` shares the `helm-plugin` version ref); the Gradle wrapper is
already current. Re-resolved on 2026-11-01 with the repository's own checks rather than read off the issue:
`./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates` names the same set, so taking it keeps the
pinned set current.

## What Changes

- **`gradle/libs.versions.toml`** — four catalog coordinates: `logback` `1.6.5`→`1.6.6` (both `logback-classic` and
  `logback-core` reference it), `handlebars` `4.5.6`→`4.5.8`, `zstd-jni` `1.5.7-24`→`1.5.7-25`, `swagger-ui`
  `5.34.0`→`5.34.1`.
- **`gradle/libs.versions.toml`** — four `build-logic` plugin pins: `dependencyVersions-plugin` `0.66.0`→`0.67.0`,
  `errorprone-plugin` `5.1.1`→`5.2.0`, `helm-plugin` `3.2.0`→`3.3.0` (`helm-releases-plugin` shares that version ref),
  `lombok-plugin` `9.8.2`→`9.8.3`.
- **`showcase-web-ui/package-lock.json`** — an `npm update` reconciliation across the eight in-range direct rows and the
  graph they re-resolve: 72 packages moved, one added (`fdir`, hoisted out of `tinyglobby`) and eleven removed (the
  `unrs-resolver` wasm fallback chain with the `tslib` it carried, and four nested dedupes). `package.json` is
  unchanged: every moved release already satisfies its caret range.
- **`docs/ideas.md`** — no entry is implemented here. The `@hookform/resolvers` major the report keeps listing is
  recorded as a Non-Goal, and the parked `handlebars` `:platform` constraint keeps its own unit.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a pure dependency bump, which no capability spec covers (`skip_specs`). The `dependency-management` spec states
the reports' scoping and suppression rules, not the pinned versions, and its illustrative examples name no coordinate
this change moves; `dependency-security`'s `zstd-jni` floor (`1.5.7-14` or newer) stays satisfied.

## Impact

- **Runtime path, so the integration tier verifies it**: `logback` is the logging implementation of every service,
  `zstd-jni` arrives as `kafka-clients`' codec, and `swagger-ui` reaches the gateway through
  `springdoc-openapi-starter-webflux-ui`. `handlebars` is the one coordinate that reaches no module's classpath: it is a
  `:platform` constraint only (the 2026-10-25 `docs/ideas.md` entry holds the verification).
- **Bundle path**: the npm graph moves `@tanstack/react-query`, `react-redux`, `eslint` and the `typescript-eslint` set,
  so the frontend `check` and the production bundle `build` run.
- **Build path**: `errorprone-plugin` and `lombok-plugin` gate every non-test compile (with `nullaway`), `helm-plugin`
  drives the chart tasks, the release path's `tags` API, and their configuration-cache behaviour, and
  `dependencyVersions-plugin` is the update report itself.
- **No suppression moves**: `.snyk`'s ignores name no moved coordinate, and the npm side is checked by `npmAudit`.
