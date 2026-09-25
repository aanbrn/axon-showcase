# Proposal: Bump the catalog and the npm patch dependencies

## Why

The (now-fixed) `dependency-updates` tracker carries 27 stable catalog updates — plus the spurious `log4j-core` row it
also lists (ADR-0007) — and two web UI npm rows: the in-range `vitest` patch and the deferred `typescript` major. 26 of
the 27 catalog updates are taken; the 27th, `opensearch-java` `3.9.0`→`3.10.0`, is held back — 3.10.0 changes
`Hit.matchedQueries()`'s return type from `List` to a new `MatchedQueries`, binary-incompatible with
`spring-data-opensearch` 2.x (the query-service ITs fail with `NoSuchMethodError` in `DocumentAdapters.from`), and
supporting it needs the Spring-Boot-4-blocked 3.x line (ADR-0004). The rest are minor/patch within their current majors
(the JVM `major-disabled` list already excludes the deferred JVM majors). Taking them clears 26 catalog rows and the npm
`vitest` row from the tracker's next run — the held-back `opensearch-java` row, the spurious `log4j-core` row and the
deferred `typescript` major remain.

## What Changes

- `gradle/libs.versions.toml` — bump the 19 `[versions]` entries: `lz4-java` `1.11.2`→`1.11.3`,
  `elasticsearch-client-java` `9.5.1`→`9.5.4`, `spotless-plugin` `8.10.0`→`8.10.2`, `caffeine` `3.2.4`→`3.3.0`,
  `handlebars` `4.5.4`→`4.5.5`, `zstd-jni` `1.5.7-16`→`1.5.7-19`, `protobuf` `4.36.0`→`4.36.2`, `nullaway`
  `0.13.8`→`0.14.1`, `opentelemetry-bom` `1.65.0`→`1.66.0`, `swagger` `2.2.54`→`2.2.55`, `httpcore5` `5.4.3`→`5.4.4`,
  `jetty` `12.1.12`→`12.1.13`, `kotlin` `2.4.10`→`2.4.20`, `mockito` `5.23.0`→`5.24.0`, `spring-data-opensearch`
  `2.0.7`→`2.0.8`, `lombok` `1.18.46`→`1.18.48`, `springdoc-openapi-starter` `2.9.0`→`2.9.1`, `swagger-ui`
  `5.32.14`→`5.32.15`, and `wiremock-spring-boot` `4.2.2`→`4.4.2`.
- `showcase-web-ui/package-lock.json` — `vitest` resolves `5.0.1`→`5.0.2` (the manifest's `^5.0.1` already admits it, so
  `package.json` is unchanged). The reconciled graph also moves vitest's required `why-is-node-running` `2.3.0`→`3.2.2`
  (dropping its now-unneeded `siginfo`/`stackback`) and prunes three stale `extraneous` entries (`ajv`, `fast-uri`,
  `json-schema-traverse`); no unrelated package moves.
- `AGENTS.md` / `docs/ideas.md` — refresh the two claims this bump falsifies: the Caffeine gotcha's verification anchor
  (`3.2.4`→`3.3.0`, re-verified) and the NANOS idea's `spring-data-opensearch` 2.0.7 line reference (now 2.0.8, which
  declares `spring-data-elasticsearch` 5.5.13 directly).

**Not taken:** the held-back `opensearch-java` `3.10.0` (see Why), the spurious `log4j-core` row (a
`spotbugs-annotations` build-environment floor, not an update) and the deferred `typescript` 7 (its own parked idea).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`; a version bump changes no behavior, and the
`dependency-management` requirements that enumerate suppressed majors are unaffected by minor/patch bumps).

## Impact

- **Build**: the version catalog and the web UI's lockfile. No build logic or application code change.
- **Tests**: the standard gates run against the bumped set.
- **Deployment / CI**: the 26 catalog rows clear on the tracker's next run; the held-back `opensearch-java` row, the
  spurious `log4j-core` row and the deferred `typescript` major remain. No `major-disabled` entry covers a same-major
  hold-back, so the tracker will re-list `opensearch-java` `3.10.0` weekly until a future bump pass or a hold-back
  mechanism addresses it (see `design.md`).
