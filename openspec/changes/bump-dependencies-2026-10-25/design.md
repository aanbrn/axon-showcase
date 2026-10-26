# Design

## Context

See `proposal.md` — Why. Both halves were **re-resolved on 2026-10-25** with the repo's own checks rather than read off
the tracker issue: `./gradlew dependencyUpdates` (the catalog and the wrapper line) and `:showcase-web-ui:npmOutdated`
(npm). Every version was then confirmed against its registry on the same day — Maven Central's `maven-metadata.xml` per
coordinate, `services.gradle.org/versions/current`, and the npm registry's `latest` endpoint — because the tracker issue
refreshes weekly and the report resolves against a live index.

The catalog report names fourteen rows, one more than issue #13 carries:
`com.github.jknack:handlebars [4.5.5 -> 4.5.6]`. All fourteen are taken: a candidate hold on `reactor-blockhound` was
tried and deliberately withdrawn (Decisions). The issue body's last refresh was 2026-10-24, so the report is this unit's
authority for the set. No row is filtered: each is a minor or patch on its taken line —
`config/dependency-updates/major-disabled.txt` lists `org.springdoc:springdoc-openapi-starter-webflux-ui`, which
suppresses its `3.x` major alone, so the taken `2.10.0` is exactly the minor the report carries — and
`config/dependency-updates/hold-back.txt` names none of the coordinates. Nothing is held back: the report comes back
clean, and a candidate hold on `reactor-blockhound` was withdrawn deliberately (Decisions).

What each bump can reach, derived from the modules' resolved dependency reports — the runtime path from the four
services' `integrationTestRuntimeClasspath` trees, and the `handlebars` absence from every main-build JVM subproject's
`dependencies` report:

- **Every service**: the Jackson 2 and 3 BOMs, the Micrometer BOMs and Reactor's BOM are imported by `:platform`, so
  `jackson-databind`/`jackson-core`, the Micrometer registries and the tracing bridge, and `reactor-core` resolve in all
  four; `lz4-java` and `zstd-jni` arrive as `kafka-clients`' codecs, and the root build's `dependencySubstitution` /
  `eachDependency` rule rewrites the unmaintained `org.lz4:lz4-java` line onto the `at.yawk.lz4` fork the platform
  constrains.
- **The gateway and the query side**: `grpc-bom` supplies `grpc-netty-shaded` and `grpc-stub`; neither the command nor
  the projection service resolves a gRPC artifact.
- **The gateway only**: `springdoc-openapi-starter-webflux-ui` and the `swagger-ui` webjar.
- **The projection service's integration tier, both clients' component tier, and the gateway's component and e2e
  tiers**: `reactor-blockhound`, the five suites that call `BlockHound.install()`. The query service declares the
  dependency and passes the BlockHound JVM arguments without a suite installing it, so its tier runs with BlockHound
  inert.
- **Every non-test compile**: `errorprone` and `nullaway` gate the annotation-processor path.
- **No main-build subproject**: `handlebars` is a `:platform` constraint only. Sweeping `dependencies` across the
  sixteen main-build JVM subprojects, the `platform` BOM included — the four services, the two APIs, the two clients,
  the three extensions, `showcase-test`, the projection model, the query proto and `load-tests`; `build-logic` is an
  included build and `helm` packages the chart — finds no resolution of the coordinate, so the constraint and its
  catalog entry are its only references. Its removal is not decided here: the platform's rationale for the constraint is
  not recorded, and removing a constraint is a separate decision from bumping it.

## Goals / Non-Goals

**Goals:**

- Take the stable catalog rows, the in-range npm rows, and the wrapper patch release the reports resolve now.
- Verify at the tiers each half's effect reaches: the integration tier for the runtime-path coordinates, the four
  BlockHound tiers `check` runs (the gateway's e2e harness is the separate opt-in tier the owner asked to run, task
  2.7), the frontend `check` and bundle `build` for the npm set, and the whole `check` for the wrapper.

**Non-Goals:**

- **`@hookform/resolvers` `6.0.0`** — a manifest major is a migration with its own decision, not an update-check row;
  the 2026-10-22 npm unit recorded the same stop, and the report keeps listing it.
- **Removing the dead `handlebars` constraint** — see Context; it is parked for its own unit.
- **Any other major**: none is in either set (`reactor-bom`'s move is inside the `2025.0` train, a minor by the report's
  calendar-version rule).

## Decisions

**Take the whole routine set in one unit.** The catalog, npm and wrapper halves share the trigger (the weekly reports),
the timing and the review, and the repo's precedent for this unit type is the same trio. Splitting them would pay three
full verifications for one week's updates.

**Retire the `UnrecognisedJavadocTag` suppressions instead of bumping the version they name.** The suppressions silenced
a false positive Error Prone emitted when Lombok copied a field Javadoc carrying an inline tag. At `2.51.0` the warning
is gone, and the `jvm-development` pack's rule retired on exactly that condition ("an ErrorProne bump past the fix
lands"). The differential, run on the five files with their suppressions removed:

| Error Prone | `UnrecognisedJavadocTag` lines |
| ----------- | ------------------------------ |
| `2.50.0`    | 18                             |
| `2.51.0`    | 0                              |

Both runs compile the same tree; the positive control is a deliberate `@Nullable` dereference in the same module, which
fails the compile with `[NullAway] dereferenced expression 'probe' is @Nullable` — so Error Prone was processing the
sources in both runs, and the warning's absence is not the check being off. The suppression is a warning-level
suppression, which no gate fails on, so its removal restores the warning as a signal rather than changing any gate.

**Verify the wrapper bump by the jar's provenance, not by a four-file diff.** Only `gradle-wrapper.properties` changed:
the jar and both scripts regenerate byte-identical. The jar was checked against the 9.8.2 distribution's own
(`gradle-wrapper-main-9.8.2.jar`'s `gradle-wrapper.jar` member) by SHA-256, which is the fact the "all four files
change" expectation would have proxied.

**Diagnose the CI stall instead of pinning around it.** The bumped tree wedged the gateway's two `@WebFluxTest` slices
on CI twice in three runs (`1dd4f1ce`, `5e6973d3`: 38 + 7 `TimeoutException`s at `.exchange()`, 45 dumps showing an idle
JVM with no thread in the request path), passed on a re-run of the same commit, and is green locally in six runs — one
initial plus five consecutive — and three more under CPU saturation. A CI bisect put `reactor-blockhound`
`1.0.18.RELEASE` under suspicion:

| Catalogue state                              | CI runs | Result                                                |
| -------------------------------------------- | ------- | ----------------------------------------------------- |
| `reactor-bom` 2025.0.8 + `blockhound` 1.0.18 | 3       | 2 failed, 1 passed (re-run)                           |
| `reactor-bom` 2025.0.7 + `blockhound` 1.0.17 | 1       | passed                                                |
| `reactor-bom` 2025.0.8 + `blockhound` 1.0.17 | 2       | passed (`ee0f6f42`, control run `51dad975` at 12m29s) |

The hold was built on that table and then **withdrawn by an explicit owner decision**: each cell is one to three runs,
so the table is a precaution's justification, not an isolation. The unit states less than it first appeared to. The
stall's **frequency is not established** — three failures in six attempts on the delivered configuration, then six
consecutive green runs of the same tree once the diagnostic's dump changed, which touches no test behaviour — and the 45
failure-time dumps **could not describe the request path at all**: the gateway enables virtual threads, Reactor's
`boundedElastic` therefore runs on them (measured in the failing slice as `loomBoundedElastic-1`, `virtual=true`), and
`Thread.getAllStackTraces()` omits virtual threads, so the "idle JVM" they showed is an artefact of the instrument.
`ThreadDumpWatchdog` therefore shells out to `jcmd Thread.dump_to_file`, which does include them (3 s, then every 7;
both the plain and the template hook, so `@ParameterizedTest` cases count too) and marks a degraded snapshot with its
reason instead of passing it off as complete. Its positive control on the runner passed: a temporary probe parked a
virtual thread, and the CI log carried one `(virtual threads included)` snapshot with that thread's
`VirtualThread.parkNanos` stack and no `INCOMPLETE` header (commit `2b7ea466`, probe reverted afterwards). `blockhound`
1.0.18 with `reactor-core` 3.8.7 remains untried, and so does every discriminator for `reactor-core` 3.8.8 itself.

**Leave the dated records alone.** `docs/audits/2026-10-19.md` and `docs/audits/2026-10-24.md` name the wrapper
`9.8.1-all` as the state they verified, and `AGENTS.md`'s Snyk sentence names the Jackson `3.2.3` fix a past scan
needed; those are records of a run, not current values, so rewriting them would falsify what they describe.

**Use `npm update` rather than pinning the two rows by hand.** It reconciles the graph the two in-range releases
re-resolve, and the removals matter as much as the moves here: the `micromatch` chain left the lockfile, which retired
ADR-0014's "braces still resolves" consequence.

## Risks / Trade-offs

- **A runtime-path bump breaks a cross-library contract only at runtime** → the full `check` with integration tests is
  the mitigation; a `springdoc` minor is the one with a documented history of breaking at startup, and the gateway's
  component/integration tiers exercise it.
- **`npm update` moves packages beyond the two rows** → the lockfile diff is described by what moved, and the frontend
  `check` plus the bundle `build` cover the moved toolchain.
- **The ESLint boundaries plugins moved together** → the frontend `check` runs the lint gate, so a changed rule fails it
  rather than passing silently.
- **Removing five suppressions can surface the warning again on a later Error Prone** → it would surface as a warning,
  not a failure; that is the trade the retirement accepts, and a re-introduction is a fresh fix, not a silent
  regression.

## Migration Plan

None: version bumps, taking effect on the next build.

## Claims table

| Claim                                                                                                                        | Source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ---------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The fourteen catalog rows, at the versions named                                                                             | `./gradlew dependencyUpdates`, re-resolved 2026-10-25: `lz4-java [1.12.0 -> 1.12.1]`, `jackson-bom [2.22.3 -> 2.22.4]`, `jackson-bom [3.2.3 -> 3.2.4]`, `handlebars [4.5.5 -> 4.5.6]`, `zstd-jni [1.5.7-23 -> 1.5.7-24]`, `error_prone_core [2.50.0 -> 2.51.0]`, `nullaway [0.14.2 -> 0.14.3]`, `grpc-bom [1.85.0 -> 1.85.1]`, `micrometer-bom [1.17.1 -> 1.17.2]`, `micrometer-tracing-bom [1.7.1 -> 1.7.2]`, `reactor-bom [2025.0.7 -> 2025.0.8]`, `blockhound [1.0.17.RELEASE -> 1.0.18.RELEASE]`, `springdoc-openapi-starter-webflux-ui [2.9.1 -> 2.10.0]`, `swagger-ui [5.33.2 -> 5.33.3 -> 5.34.0]` |
| The stall is diagnosed rather than pinned around                                                                             | the CI bisect above (one to three runs per cell) plus the owner's decision; `ThreadDumpWatchdog` snapshots every thread while a test runs, so the next failing CI run names what the request waited for                                                                                                                                                                                                                                                                                                                                                                                                   |
| The next occurrence is diagnosable rather than re-runnable                                                                   | `ThreadDumpWatchdog` snapshots every thread while a test runs (3 s, then every 7), where `ThreadDumpOnFailure` snapshots only after the timeout; a passing run emits nothing                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Each is the registry's latest release on the taking line                                                                     | Maven Central `maven-metadata.xml` per coordinate and `https://services.gradle.org/versions/current` (`9.8.2`), both read 2026-10-25; `micrometer`/`micrometer-tracing`/`reactor-bom` are at their latest stable line (the only newer versions are `-M`/`-RC`), `springdoc` `2.10.0` is the last `2.x` (the `3.x` line is the suppressed major)                                                                                                                                                                                                                                                           |
| `handlebars` is in the set though issue #13 omits it                                                                         | the report's update section (`handlebars [4.5.5 -> 4.5.6]`) against issue #13's body, last updated 2026-10-24                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| No taken row is a suppressed major, and nothing taken is deferred                                                            | the report's update section is exactly the fourteen rows, every one a minor or patch on its taken line; `org.springdoc:springdoc-openapi-starter-webflux-ui` appears in `config/dependency-updates/major-disabled.txt`, which suppresses its `3.x` major alone, so the taken `2.10.0` is the reported minor; no other row's coordinate is in that file or in `hold-back.txt`, and the update section comes back empty after the bump                                                                                                                                                                      |
| The two in-range npm rows                                                                                                    | `:showcase-web-ui:npmOutdated`, re-run 2026-10-25: `react-hook-form 7.90.0 7.90.0 7.91.0`, `eslint-plugin-boundaries 7.2.1 7.3.0 7.3.0`, plus the deferred `@hookform/resolvers 5.9.2 5.9.2 6.0.0`                                                                                                                                                                                                                                                                                                                                                                                                        |
| The lockfile's moved set                                                                                                     | parsed package-by-package from `git show 3456bcb1:showcase-web-ui/package-lock.json` (the unit's refreshed base; `origin/main` never touched the lockfile, so the original `837aaa52` copy is identical) against the working tree: 13 moved, 5 removed, 0 added, plus two `dev`→`devOptional` flips on unmoved entries                                                                                                                                                                                                                                                                                    |
| The five removed packages are the `micromatch` chain                                                                         | the removed entries are `braces`, `fill-range`, `is-number`, `micromatch`, `to-regex-range`, and the new `eslint-plugin-boundaries`/`@boundaries/elements` entries no longer declare `micromatch`                                                                                                                                                                                                                                                                                                                                                                                                         |
| `package.json` is unchanged                                                                                                  | `git status` names only `package-lock.json` under `showcase-web-ui/`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| The wrapper is complete at `9.8.2`                                                                                           | `./gradlew --version` reports `Gradle 9.8.2`; `distributionUrl` ends `-all.zip`; the jar's SHA-256 equals the 9.8.2 distribution's `gradle-wrapper-main-9.8.2.jar` member (`3b8a2577…`)                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Error Prone `2.51.0` fixed the false positive                                                                                | the A/B in Decisions: 18 `UnrecognisedJavadocTag` lines at `2.50.0`, 0 at `2.51.0`, same five files and suppressions removed in both runs                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| The probe's compile actually ran Error Prone                                                                                 | a deliberate `@Nullable` dereference in the gateway module fails `compileJava` with `[NullAway] dereferenced expression 'probe' is @Nullable`                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| The runtime-path reach                                                                                                       | the four services' resolved `integrationTestRuntimeClasspath` trees, grepped per coordinate — the runtime-path bullets the Context states                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| The test-tooling reach                                                                                                       | the six `implementation(libs.reactor.blockhound)` declarations — projection service, query service (declared, with no suite installing it), query client, command client, and the gateway's two suites — the five `BlockHound.install()` call sites, and `code-check-conventions.gradle.kts`'s `errorprone`/`nullaway` wiring                                                                                                                                                                                                                                                                             |
| `handlebars` resolves in no main-build subproject                                                                            | `./gradlew :platform:dependencies` and each other main-build subproject's `dependencies` report (twelve in one invocation, 15,732 lines), plus the four services' `integrationTestRuntimeClasspath` trees, grepped for `handlebars` and `jknack`: zero hits                                                                                                                                                                                                                                                                                                                                               |
| `.snyk` needs no sweep                                                                                                       | its ignore entries name `hibernate-validator`, `jakarta.validation-api` and `org.springframework*` coordinates only                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| No npm ignore goes stale                                                                                                     | `npm audit` reported `found 0 vulnerabilities` on the update run, and `npmAudit` passes — the list is empty either way                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| The doc sites are current-value mentions except the two records, and the examples were de-versioned rather than re-versioned | `AGENTS.md`/`openspec/config.yaml`'s Gradle version, `README.md`'s wrapper pin, the `docs/ideas.md` NullAway parenthetical and ADR-0014's `braces` consequence are current values; the `reactor-bom` examples in `README.md`, the `gradle-build` pack and the `dependency-management` delta were de-versioned against the rule's own shape; `docs/audits/*` and the Snyk `3.2.3` sentence record past runs                                                                                                                                                                                                |

Dismissals over the scan's candidate shapes, by kind:

- **`path` and `cross-ref`** — each names an artifact this change edits or rests on, or a generated line inside the
  lockfile (a resolved tarball URL or integrity hash) that no sentence claims anything about; the artifact was verified
  to exist when the claim resting on it was written, and every such claim is already a row above.
- **`count`** — each figure the artifacts state is one of the rows above, apart from two shapes: a task number or a line
  position, which is an identifier, and a pre-existing repository fact on a line this change edits (`AGENTS.md`'s module
  and JVM-module counts), which this change neither states nor moves.
- **`absolute`** — each quantifier is bounded by a set the Context or the row it sits in names: the main-build JVM
  subprojects' resolved dependency reports, the catalog's entries, the report's update section, the lockfile diff, or,
  for the parked idea's reference count, a repository-wide grep.
