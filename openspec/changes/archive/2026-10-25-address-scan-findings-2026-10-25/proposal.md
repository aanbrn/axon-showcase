# Proposal

## Why

The scheduled `Dependency Security` run (`40125920555`, 2026-10-24) is red in both its jobs. The `snyk` job reports two
High advisories in `com.tdunning:t-digest@3.3`, reached in `showcase-projection-service` through
`spring-data-opensearch` → `opensearch-rest-high-level-client` → `org.opensearch:opensearch`; the coordinate has no
patched release. Its `.snyk` ignores were removed on 2026-10-16 as dead, on the finding that only `load-tests` carried
them — but the projection service's `componentTest` suite re-declared `spring-data-opensearch` without the
high-level-client exclusion that the `showcase-projection-model` dependency beside it already applies, so the coordinate
still resolves. The `web-ui-audit` job fails because `GHSA-vfj7-8cjw-p6xm` was withdrawn upstream on 2026-10-23, leaving
its suppression entry matching no finding.

## What Changes

- **`showcase-projection-service/build.gradle.kts`** — the `componentTest` suite's redundant `spring-data-opensearch`
  declaration is removed: the suite already receives that dependency through `project(":showcase-projection-model")`'s
  `api` edge, which carries the `opensearch-rest-high-level-client` exclusion, so `t-digest` no longer resolves on the
  suite's classpath without a second declaration site.
- **`showcase-web-ui/npm-audit-ignores.json`** — the withdrawn `GHSA-vfj7-8cjw-p6xm` entry is removed, leaving an empty
  suppression list.
- **`docs/adr/0014-web-ui-npm-dependency-checks.md`** — its `braces` consequence records the advisory's withdrawal and
  the entry's removal instead of the retirement condition that no longer applies.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. Both fixes restore an outcome the `showcase/quality/dependency-security` spec already states — a scan that reports
no vulnerable paths, with only fixless advisories suppressed — and neither changes a specified mechanism or scenario
(the dependency wiring is a build mechanism whose alternatives yield the same outcome, and the npm entry's removal is
what the staleness scenario requires).

## Impact

- **Dependencies**: `com.tdunning:t-digest` and the `org.opensearch:opensearch` tree leave the projection service's
  `componentTest` classpath (compile and runtime); no production classpath carried either.
- **Build/tests**: the projection service's `componentTest` suite compiles and runs without the legacy high-level REST
  client. The re-resolved classpaths are the projection service's `componentTest` compile and runtime plus its
  `test`/`componentTest` runtime, and the build's `integrationTest`/`e2eTest` compile classpaths.
- **Scans**: the next credentialed `dependencySecurityCheck` run and `:showcase-web-ui:npmAudit` go green.
