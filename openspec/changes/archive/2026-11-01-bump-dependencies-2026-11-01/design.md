# Design

## Context

See `proposal.md` — Why. The set was **re-resolved on 2026-11-01** with the repository's own checks, in one invocation:
`./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates`. Each version was then confirmed at its
registry on the same day — Maven Central's `maven-metadata.xml` per catalog coordinate, the plugin portal's metadata per
plugin pin, and `npm outdated`'s own rows — because the tracker issue refreshes weekly and the report resolves against a
live index.

The catalog report names five coordinates: `ch.qos.logback:logback-classic` and `ch.qos.logback:logback-core`
(`1.6.5 → 1.6.6`, one catalog key), `com.github.jknack:handlebars` (`4.5.6 → 4.5.8`), `com.github.luben:zstd-jni`
(`1.5.7-24 → 1.5.7-25`) and `org.webjars:swagger-ui` (`5.34.0 → 5.34.1`). Its wrapper line reads
`Gradle: [9.8.2: UP-TO-DATE]`, so this unit does not touch the wrapper. No row is filtered:
`config/dependency-updates/major-disabled.txt` names none of these coordinates and
`config/dependency-updates/hold-back.txt` names only `opensearch-java`; every row is a minor or patch on its taken line.

The npm report names eight in-range direct rows — `@tanstack/react-query` `5.105.0`, `@types/node` `26.6.6`, the three
`typescript-eslint` packages `8.72.1`, `eslint` `10.14.0`, `eslint-plugin-boundaries` `7.4.0`, `react-redux` `9.3.1` —
and one package whose only update is a manifest major (`@hookform/resolvers`, which the report read as `7.0.0` at the
first run and `7.0.1` at the re-run, so the major alone is what the row means).

The `buildLogicUpdates` report names five rows, which are four pins: `dependencyVersions-plugin` `0.67.0`,
`errorprone-plugin` `5.2.0`, `helm-plugin` `3.3.0` (the catalog's `helm-releases-plugin` shares this version ref) and
`lombok-plugin` `9.8.3`.

What each bump can reach:

- **Every service**: `logback` is the logging implementation, and `zstd-jni` arrives as `kafka-clients`' codec.
- **The gateway only**: the `swagger-ui` webjar, resolved through `springdoc-openapi-starter-webflux-ui` and versioned
  by the platform's constraint.
- **No module's classpath**: `handlebars`, whose only reference is the platform's constraint.
- **Every non-test compile**: `errorprone-plugin` (with `nullaway`) and `lombok-plugin`.
- **The chart tasks and the release path**: `helm-plugin` 3.3.0 removes its configuration-cache obstacles (so its tasks
  are re-measured with the cache enabled — Decisions) and changes the `tags` API from `Set` to `SetProperty`, which the
  root build's seven `release { }` blocks call as `tags.add(...)`/`tags.addAll(...)` (`build.gradle.kts`); the
  `helmInstallToCi` dry run is where those calls are configured.
- **The update report itself**: `dependencyVersions-plugin` 0.67.0 changes the report's project-URL handling and line
  endings.

## Goals / Non-Goals

**Goals:**

- Take the stable catalog rows, the in-range npm rows, and the plugin pins the three reports resolve now.
- Verify at the tiers each half's effect reaches: the integration tier for the runtime-path coordinates, the frontend
  `check` and bundle `build` for the npm set, and the build gates for the plugin pins.
- Re-measure the two claims the `gradle-build` pack makes about `helm-plugin` 3.2.0 against 3.3.0 and update them.

**Non-Goals:**

- **`@hookform/resolvers`'s major** — a manifest major is a migration with its own decision, not an update-check row;
  the 2026-10-22 and 2026-10-25 npm units recorded the same stop, and the report keeps listing it.
- **Removing the parked dead `handlebars` `:platform` constraint** — `docs/ideas.md` (2026-10-25) parks it for its own
  unit; a constraint's rationale is the owner's to state.
- **Any other major**: none is in either set.

## Decisions

- **`helm-plugin` `3.3.0` is taken and the two pack claims are re-measured rather than assumed.** The pack states that
  `helm-plugin` 3.2.0 still calls the deprecated `Project.getProperties()` (a hard error in Gradle 10, tracked upstream
  as `build-extensions-oss/gradle-helm-plugin#145`) and that the plugin's tasks could not store a configuration-cache
  entry before 3.2.0. The bump is the moment to re-read both: a `--warning-mode all` run shows whether the deprecation
  survives at 3.3.0, and each workflow-invoked task is executed with the cache enabled, requiring a stored entry and
  then a reused one, because a plugin task type that cannot store fails only when executed.
- **The npm graph is reconciled with `npm update`, not by editing ranges.** `npm update` moves the graph its caret
  ranges already admit; `package.json` stays byte-identical, so the lockfile carries only resolved versions.
- **`skip_specs`**: no capability spec describes the pinned versions (see `proposal.md` — Modified Capabilities).

## Verification plan

| Half                        | Tier the change runs                                                               |
| --------------------------- | ---------------------------------------------------------------------------------- |
| Catalog runtime coordinates | Full `./gradlew check`, integration tests on Testcontainers, coverage gate enabled |
| npm set                     | `:showcase-web-ui:check`, `:showcase-web-ui:build`, `:showcase-web-ui:npmAudit`    |
| Plugin pins                 | The same `check`, plus the `helm-plugin` cache and deprecation probes (above)      |
| Report clearance            | The three report tasks re-run, naming no bumped coordinate                         |

## Claims table

Seeded with `scripts/claim-scan.py --base origin/main`; each candidate is resolved into a row or the dismissal below.

| Claim                                                                   | Source                                                                                                                                                             |
| ----------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The catalog report named five rows at the first run                     | `/tmp/updates-2026-11-01.log`, its `The following dependencies have later release versions:` block (the 3.3 re-run overwrote `build/dependencyUpdates/report.txt`) |
| The npm report named eight in-range rows and one major at the first run | `/tmp/updates-2026-11-01.log`, its `npm outdated` table (the re-run overwrote `showcase-web-ui/build/npm-outdated.txt`)                                            |
| `buildLogicUpdates` names five rows, which are four pins                | The same log's `Build-logic pin updates:` block                                                                                                                    |
| The wrapper is `9.8.2` and up to date                                   | The first run's `Gradle CURRENT updates:` section                                                                                                                  |
| Each taken version is its coordinate's latest release                   | Maven Central `maven-metadata.xml` per catalog coordinate; the plugin portal's metadata per plugin pin                                                             |
| 72 packages moved, one added, eleven removed, root block unchanged      | `git show HEAD:showcase-web-ui/package-lock.json` against the working file, compared with a Python script                                                          |
| `helm-plugin` `3.3.0` still emits `Project.getProperties`, twice        | `/tmp/deprecation-trace2.log`, from `./gradlew help --no-configuration-cache --warning-mode all -Dorg.gradle.deprecation.trace=true`                               |
| A reused cache entry emits no such warning                              | `/tmp/deprecation-trace.log`, the same command over a reused entry                                                                                                 |
| The chart packaging stores, then reuses, a cache entry                  | `/tmp/helm-cache-1.log` (stored), `/tmp/helm-cache-2.log` (re-stored after a task created a watched `Chart.yaml`), `/tmp/helm-cache-3.log` (reused)                |
| `helmInstallToCi`'s graph stores a cache entry                          | `/tmp/helm-install-dry.log`, `./gradlew helmInstallToCi --dry-run`                                                                                                 |
| The workflow's `awk` still extracts the 0.67.0 report's rows            | A temporary `swagger-ui = "5.34.0"` pin, the workflow's own `awk` program over the report, then reverted                                                           |
| The bumped rows are gone                                                | The three report tasks' re-run (`/tmp/updates-final.log`): no catalog section, `UP-TO-DATE` wrapper, no build-logic rows                                           |
| The full gate is green                                                  | `/tmp/full-check2.log`: `BUILD SUCCESSFUL in 5m 45s`, the integration and component tiers executed                                                                 |
| The frontend gates are green and the bundle emits                       | `/tmp/webui-check.log` (25 Vitest files, 118 tests) and `showcase-web-ui/build/dist/assets`                                                                        |
| No live document names a bumped version as the current one              | `git grep -nE '<old version>' -- . ':!openspec/changes' ':!showcase-web-ui/package-lock.json' ':!gradle/libs.versions.toml'`                                       |

**Dismissed candidates.** The `package-lock.json` lines the scan flags are its integrity hashes and package URLs, which
are npm's own serialization rather than claims; the `absolute` and `count` candidates the scan reports inside
`proposal.md`, `design.md` and `tasks.md` are ordinary prose and task numbering.
