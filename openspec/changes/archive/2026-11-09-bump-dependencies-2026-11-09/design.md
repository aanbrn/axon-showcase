# Design

## Context

See `proposal.md` — Why. The set was **re-resolved on 2026-11-09** with the repository's own checks, in one invocation:
`./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates`, run in this unit's workspace at the
branch's base revision. Each taken version was then confirmed at its registry the same day — Maven Central's
`maven-metadata.xml` per catalog coordinate and per plugin pin, and `npm outdated`'s own rows — because the tracker
issue refreshes weekly and the report resolves against a live index.

The catalog report names eleven coordinates, of which ten are taken: `at.yawk.lz4:lz4-java` `1.12.1 → 1.14.0`,
`co.elastic.clients:elasticsearch-java` `9.5.5 → 9.5.6`, `com.diffplug.spotless`'s plugin marker `8.10.4 → 8.11.0`,
`com.fasterxml.jackson:jackson-bom` `2.22.4 → 2.23.0`, `com.github.luben:zstd-jni` `1.5.7-25 → 1.5.7-26`,
`com.google.errorprone:error_prone_core` `2.51.0 → 2.52.0`, `com.google.guava:guava` `33.7.2-jre → 33.8.0-jre`,
`io.netty:netty-bom` `4.2.19.Final → 4.2.20.Final`, `io.opentelemetry:opentelemetry-bom` `1.67.0 → 1.68.0` and
`tools.jackson:jackson-bom` `3.2.4 → 3.3.0`. The account's `[versions]` keys are `lz4-java`,
`elasticsearch-client-java`, `spotless-plugin`, `jackson2-bom`, `zstd-jni`, `errorprone`, `guava`, `netty-bom`,
`opentelemetry-bom` and `jackson3-bom`. The eleventh row, `com.github.kagkarlsson:db-scheduler-spring-boot-starter`
`16.12.0 → 17.0.0`, is a major and is a Non-Goal.

The npm report names eight in-range direct rows, which are taken from their current to their wanted version:
`@playwright/test` `1.64.1 → 1.65.0`, `@reduxjs/toolkit` `2.13.0 → 2.14.0`, `@tanstack/react-query` `5.105.0 → 5.107.1`,
`@vitejs/plugin-react` `6.1.2 → 6.2.1`, `@vitest/coverage-v8` `5.0.5 → 5.1.0`, `eslint-plugin-boundaries`
`7.4.0 → 7.4.2`, `vite` `8.3.5 → 8.3.6` and `vitest` `5.0.5 → 5.1.0`. Two further packages have a manifest major as
their only update: `@hookform/resolvers` (`5.9.2 → 7.0.3`) and `jsdom` (`30.1.3 → 31.0.0`). The report filters
`typescript`'s major through `config/web-ui-updates/major-disabled.txt`, so its `7.0.2` latest does not appear.

The `buildLogicUpdates` report names four rows, which are three distinct pins because the `helm-plugin` and
`helm-releases-plugin` rows share one version ref: `helm-plugin` `3.3.1`, `protobuf-plugin` `0.10.1`, and
`spotless-plugin` `8.11.0`, the same catalog ref the catalog report lists. Of these, `protobuf-plugin` is the one pin
taken as a `build-logic` pin, `spotless-plugin` is counted among the ten catalog rows, and the `helm-plugin` row is
excluded because the `adopt-plugin-retried-fetches` unit owns `3.3.0 → 3.3.1`; it merged as `0deca079` while this unit
was in flight, so taking it here would duplicate a merged change.

The wrapper line reads `Gradle: [9.8.2 -> 9.9.0]`, which is a `/gradle-update` follow-up rather than an update-check
row, as in the preceding bump units.

What each taken bump can reach:

- **Every service's runtime**: `lz4-java` and `zstd-jni` are the Kafka codecs — `kafka-clients` names the maintained
  `at.yawk.lz4` fork directly, and the `org.lz4:lz4-java` substitution ADR-0020 records is protective for a future
  consumer of the vulnerable coordinate — while `netty-bom` versions the reactor-netty stack the four services carry;
  `opentelemetry-bom` versions the OTLP exporter every service declares; `guava` is a platform constraint and a
  command-service dependency, and it reaches the `checkstyle` and Error Prone `annotationProcessor` tool classpaths the
  `dependency-security` requirement names.
- **The query and projection services' and the query client's test graph**: `elasticsearch-java` is declared by the
  query service, the projection service, and the query client's component-test suite.
- **Every classpath through the platform**: `jackson2-bom` and `jackson3-bom` are platform imports (Spring Boot's
  Jackson 2 default and the Axon/Elasticsearch Jackson 3 line).
- **Every non-test compile**: `errorprone`, which runs with `nullaway`; a new check or a changed analysis can fail a
  compile the bump did not touch.
- **The formatter**: `spotless-plugin` ships the palantir/ktfmt versions, so a bump can reflow sources tree-wide.
- **The generated gRPC sources**: `protobuf-plugin` is the `showcase-query-proto` generator.

## Goals / Non-Goals

**Goals:**

- Take the ten stable catalog rows, the eight in-range npm rows, and the one `build-logic` pin (`protobuf-plugin`) the
  reports resolve now.
- Verify at the tiers each half's effect reaches: the integration tier for the runtime-path coordinates, the frontend
  `check` and the production bundle `build` for the npm set, and the build gates for `errorprone`, `spotless-plugin` and
  `protobuf-plugin`.
- Re-run the three reports and confirm every taken row is gone.

**Non-Goals:**

- **`db-scheduler` `17.0.0`** — a major is a migration with its own decision, not an update-check row.
- **`@hookform/resolvers`'s and `jsdom`'s majors** — the preceding npm and dependency units recorded the same stop for
  them, both are manifest majors, and the report keeps listing them; their current latest versions move between runs, so
  this Non-Goal names no version.
- **The Gradle wrapper `9.9.0`** — `/gradle-update` owns the wrapper bump, through `scripts/update-gradle-wrapper.py`.
- **`helm-plugin` / `helm-releases-plugin` `3.3.1`** — the `adopt-plugin-retried-fetches` unit, merged as `0deca079`,
  takes it.

## Decisions

- **The three reports are re-run from the repository, not read off issue #13.** The issue is a weekly snapshot, and the
  re-run names three coordinates the issue does not (`jackson-bom` `2.23.0`, `tools.jackson:jackson-bom` `3.3.0`,
  `zstd-jni` `1.5.7-26`) and one pin it omits (`helm-plugin` `3.3.1`, which the issue's build-logic block never lists).
- **`spotlessApply` runs after the plugin bump and its reflow is part of the diff.** The formatter's version ships with
  the plugin, so a reflowed file is a real effect of the change and is recorded rather than treated as noise.
- **A release-compatibility preflight precedes the bump, and the unit stays whole unless that preflight finds
  migration-sized work.** The 2026-11-09 consultation confirmed the grouping but could evidence no compatibility across
  LZ4 `1.13`/`1.14`, Error Prone `2.52.0` with the pinned NullAway, and Spotless `8.11.0`; a coordinate whose release
  needs migration-sized source remediation, substantial non-formatting remediation, or a weakened check is split out at
  that point. The preflight (`tasks.md` 2.3) found none: LZ4's changes are upstream-validation tightening that no
  repository code calls, Spotless's only behaviour change is a `google-java-format` default this build does not
  configure, and Error Prone's new default check compiles every module.
- **The npm graph is reconciled with `npm update`, not by editing ranges.** `npm update` moves the graph its caret
  ranges already admit; `package.json` stays byte-identical, so the lockfile carries only resolved versions.
- **`skip_specs`**: no capability spec describes the pinned versions (see `proposal.md` — Modified Capabilities).

## Verification plan

| Half                        | Tier the change runs                                                                                                                                                                                                                                              |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Catalog runtime coordinates | Full `./gradlew check` (integration tests on Testcontainers, coverage gate enabled): resolves the bumped coordinates and boots the services with them, while no integration configuration selects a compression type, so the LZ4/ZSTD codec path is not exercised |
| npm set                     | `:showcase-web-ui:check`, `:showcase-web-ui:build`, `:showcase-web-ui:npmAudit`                                                                                                                                                                                   |
| `build-logic` pin           | The same `check`, plus `compileJava` on `:showcase-query-proto` for the protobuf generation                                                                                                                                                                       |
| Gateway e2e classpath       | `./gradlew compileE2eTestJava --refresh-dependencies`, since `check` includes no `e2eTest` tier                                                                                                                                                                   |
| `spotless-plugin`           | `spotlessApply` then `spotlessCheck`, with the reflowed set accounted for                                                                                                                                                                                         |
| Report clearance            | The three report tasks re-run, naming no taken coordinate                                                                                                                                                                                                         |

## Claims table

Seeded with `scripts/claim-scan.py --base origin/main`; each candidate is resolved into a row or the dismissal below.

| Claim                                                                | Source                                                                                                                                                                                                                              |
| -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The catalog report names eleven rows, of which ten are taken         | `$TMPDIR/opencode/bump-2026-11-09/catalog-report-first.txt`, its `The following dependencies have later release versions:` block                                                                                                    |
| The npm report names eight in-range rows and two manifest majors     | `$TMPDIR/opencode/bump-2026-11-09/npm-report-first.txt`, its table                                                                                                                                                                  |
| `buildLogicUpdates` names four rows, which are three pins            | `$TMPDIR/opencode/bump-2026-11-09/build-logic-report-first.txt`                                                                                                                                                                     |
| The wrapper is `9.8.2` and `9.9.0` is a newer stable                 | `/Users/afanasyev/Development/axon-showcase-bump-dependencies-2026-11-09/build/dependencyUpdates/report.txt`, `Gradle CURRENT updates`                                                                                              |
| Each taken version is its coordinate's newest stable release         | `curl -s https://repo1.maven.org/maven2/<path>/maven-metadata.xml` per coordinate; the plugin marker's Central metadata for `spotless-plugin`                                                                                       |
| `helm-plugin` `3.3.1` is owned by another unit                       | `openspec/changes/archive/2026-11-09-adopt-plugin-retried-fetches/proposal.md`, its `What Changes` list, and the merged commit `0deca079`                                                                                           |
| The re-run names three coordinates and one pin issue #13 omits       | `gh issue view 13 --json body`, its catalog and build-logic blocks, against `$TMPDIR/opencode/bump-2026-11-09/catalog-report-first.txt`                                                                                             |
| No suppression file names a taken coordinate                         | `grep` over `config/dependency-updates/major-disabled.txt`, `hold-back.txt`, and `config/web-ui-updates/major-disabled.txt`                                                                                                         |
| The npm graph moved as `package-lock.json` records it                | `git show <base>:showcase-web-ui/package-lock.json` against the working file, compared with a script                                                                                                                                |
| The taken rows are gone                                              | The three report tasks' re-run, whose catalog section names no taken coordinate                                                                                                                                                     |
| The full gate is green                                               | The `./gradlew check` run's own output                                                                                                                                                                                              |
| The `e2eTest` classpath compiles                                     | `./gradlew compileE2eTestJava --refresh-dependencies`, which `check` does not run                                                                                                                                                   |
| No integration configuration selects a compression type              | `git grep -nE 'compression[.-]type' -- '*integrationTest*' '*application*.yml'` (13 and 14 files) returns no match, while the control `-- 'openspec/changes/archive/2026-09-22-address-snyk-2026-09-21-findings/design.md'` matches |
| The frontend gates are green and the bundle emits                    | `:showcase-web-ui:check` and `:showcase-web-ui:build` output, and `showcase-web-ui/build/dist/assets`                                                                                                                               |
| No live document names a taken version as the current one            | `git grep -nE '<old version>' -- . ':!openspec/changes' ':!showcase-web-ui/package-lock.json' ':!gradle/libs.versions.toml'`                                                                                                        |
| The two sites the sweep retargets are the ones it names              | `docs/adr/0012-prefer-typed-opensearch-java-client.md` and `docs/ideas.md`, whose `9.5.5` mentions the sweep retargets to `9.5.6`                                                                                                   |
| The consultation's grouping verdict and its no-compatibility finding | `docs/units/bump-dependencies-2026-11-09/review-handover.md`, its `advisor-scope-1` section (`Result`, `Its findings`, `What changed in the plan`)                                                                                  |

**Dismissed candidates.** The `package-lock.json` lines the scan flags are its integrity hashes and package URLs, which
are npm's own serialization rather than claims. The `path` candidates are file names, commands, and package names cited
as the subject of a row, not assertions about the repository. The `absolute` and `count` candidates are prose
quantifiers and version digits (`Jackson 2`, `Jackson 3`) rather than counts of a set. The `cross-ref` candidates name a
product family and a formatter (`Axon/Elasticsearch`, `palantir/ktfmt`) and this repository's tracker issue, none of
which is an `owner/repo` reference to resolve.
