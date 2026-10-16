# Design

## Context

See `proposal.md` — Why. What constrains the design, established by running the tool rather than by reading the files:

- **The included build cannot run the main report.** `build-logic`'s convention plugins live in its own
  `src/main/kotlin` as precompiled script plugins, which exist for _consumers_: applying
  `dependency-versions-conventions` inside `build-logic` fails configuration with
  `Plugin [id: 'dependency-versions-conventions'] was not found in any of the following sources: … Included Builds (No included builds contain this plugin)`,
  even though the plugin descriptors are present in the built jar. This is the finding that re-derived the plan: the
  first shape — run `dependencyUpdates` in the included build — cannot work.
- **The pins are catalog libraries, and only they are uncovered.** `build-logic/build.gradle.kts` consumes twenty-one
  catalog libraries; nine of them (`jackson2-bom`, `jackson2-databind`, `commons-lang3`, `assertj-bom`, `assertj-core`,
  `log4j-api`, `log4j-core`, `log4j-toSlf4j`, `spotbugs`) also resolve in the main build and appear in its report today.
  The twelve `*-plugin` pins do not, and exactly one of those reaches the report: `spotless-plugin`, through the root's
  `alias(libs.plugins.spotless)`.
- **The plugin portal serves every pin.** Each coordinate's
  `https://plugins.gradle.org/m2/<group>/<path>/maven-metadata.xml` returns `200`, including the dotted group
  `gradle.plugin.io.gatling.gradle`. The portal is already a declared repository in the main build (the release
  _artifact_ URL redirects, so only the metadata URL is the lookup).
- **The portal's latest is not always stable.** Its `<release>`/`<latest>` names a pre-release for at least three of the
  twelve — `kotlin-gradle-plugin` at `2.5.0-Beta1`, `spring-boot-gradle-plugin` at `4.2.0-M2`, `helm-plugin` at
  `3.2.1-beta1` — while the report the check complements carries `revision = "release"` and filters with `isNonStable`,
  and the delta's scenario speaks of _stable_ updates. The check must apply the same filter.
- **The suppression lists apply to these coordinates.** `config/dependency-updates/major-disabled.txt` lists
  `org.springframework` as a group prefix, which covers `spring-boot-gradle-plugin` — one of the twelve, whose next
  stable release is the Spring Boot 4 major the project defers (ADR-0004, `showcase/quality/dependency-management`); the
  hold-back list applies the same way.
- **The sibling checks' shape.** `HelmUpdatesTask`, `BuildpackUpdatesTask`, and `ToolingUpdatesTask` live in
  `build-logic/src/main/kotlin`, are registered in the root `build.gradle.kts` with a `reportFile` under
  `build/<name>-updates/report.txt`, and keep their lookup and parse helpers beside or inside the task (`BuildpackJson`,
  `ToolingJson`), with `BuildpackUpdatesTaskTests` pinning a verbatim provider response body. Helm's lookup shells out
  to `helm search repo`; the others read HTTP surfaces.

## Goals / Non-Goals

**Goals:**

- Every `*-plugin` pin `build-logic` declares gets a latest-version lookup and an actionable row in the weekly issue
  when it lags behind a newer **stable** release the project does not defer.
- A pin added to `build-logic`'s dependencies fails the check until it is listed, rather than going uncovered the way
  the helm pin did.
- The check's rows mean what the report's rows mean: the same stability rule, the same two suppression files.
- Keep one place where updates are read, and keep the merge gate out of it.

**Non-Goals:**

- Running a second `dependencyUpdates` (impossible in that build) or declaring the pins again in the main build (a
  shadow edge free to drift from what `build-logic` resolves).
- Covering the nine catalog libraries `build-logic` shares with the main build — they already have a report.
- Covering `[versions]`-only entries (`node`, buildpack ids, image tags), which keep their checkers or their deliberate
  hand-audit status.
- Making the check a merge gate.

## Decisions

**D1 — A repo-owned declared-list check, scoped to the pins.** The check declares the twelve plugin aliases, looks up
each one's latest stable release, and writes a report; the workflow surfaces its actionable rows in the same issue. The
included build cannot run the main report (Context), and declaring the pins in a main-build configuration would
duplicate a list that must track `build-logic`'s own file — the drift ADR-0015 exists to prevent. The scope is the pins
specifically: the other nine libraries `build-logic` consumes are already the main report's rows.

**D2 — The lookup names its failure.** The plugin portal alone, which serves its own published plugins and proxies Maven
Central, so it carries all twelve; a coordinate whose latest cannot be read is reported as an unread lookup, never as
current — the trap `buildpackUpdates`/`helmUpdates` were bitten by, and the reason their bullet exists. A coordinate
with no newer release reports nothing.

**D3 — The declared list is asserted against both files.** Every `-plugin` alias `build-logic/build.gradle.kts` consumes
must be declared, and every declared alias must resolve in the catalog — which owns both its coordinate and its pinned
version. A pin the build consumes but the list omits, and a declared pin the build no longer consumes (or repeats), each
fail the check naming it, which is the property that makes the coverage durable rather than a list that quietly falls
behind.

**D4 — The check reuses the report's deciding rule.** Stability and suppression come from the report's own rules, not
from a re-derived match: `DependencyUpdateRules.shouldReject` is called per candidate, with the catalog's pin as
`currentVersion`, the catalog map, `disabledEntries` over `major-disabled.txt`, and `holdBackEntries` over
`hold-back.txt` — the same composition the convention's `rejectVersionIf` makes. So a milestone
(`kotlin-gradle-plugin`'s `2.5.0-Beta1`), a deferred major (`org.springframework` covering `spring-boot-gradle-plugin`),
and a held-back line are filtered exactly as the report filters them. This is reachable precisely because the task is
registered in the _main_ build, where those helpers and files live; inside the included build they would not be on the
classpath at all.

**D5 — The same `dependency-updates` job and the same issue.** The owning requirement states "the same
`dependency-updates` job", the owner notification is per-issue, and a second issue would split where updates are read.
The check's actionable rows appear as their own section.

**D6 — No ADR change.** ADR-0015's Decision — versions single-sourced in the catalog, each entry's checker identified, a
hand-audit remainder for the rest — is what this implements; the remainder shrinks by the twelve pins and stays true for
the `[versions]`-only entries.

**D7 — The reporting contract is specified in `dependency-management`.** That capability's Purpose owns how the build's
dependency update reports are scoped and filtered, and its requirements are written per report (one for the Gradle
report, one for the web UI's), so a third report needs its own requirements there rather than a widened clause — its
semantics are not the Gradle report's (its scope is the included build's pins, not the catalog-owned coordinates the
main report resolves). `merge-governance`'s delta carries the workflow side, which is the part that was re-derived.

## Risks / Trade-offs

- **A lookup that fails silently reads as current** → D2 makes an unread lookup reportable, and the verification's
  controls check both directions plus the failure path.
- **The portal's latest is a pre-release, or a deferred major** → D4 filters with the report's own rules, so the rows
  match; the verification deliberately pins a coordinate behind and confirms which rows appear.
- **The list drifts from `build-logic`'s dependencies** → D3's assertion fails the check instead.
- **A coordinate covered by both the report and this check** → the issue carries both sections, so it is visible rather
  than silently deduplicated.
- **Twelve lookups per run** → a fraction of a second each, against the resolution walk a second report would have paid.

## Migration Plan

None: the change adds a check and its docs; reverting removes them.

## Open Questions

None.

## Claims table

| Claim                                                      | Source                                                                                                                                                                                                                                                                                                      |
| ---------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The included build cannot run the main report              | a probe: applying `dependency-versions-conventions` in a copy of `build-logic` fails with `Plugin [id: 'dependency-versions-conventions'] was not found … Included Builds (No included builds contain this plugin)`, while the same copy without that line configures cleanly                               |
| The pins resolve on the plugin portal                      | `curl -s -o /dev/null -w '%{http_code}' https://plugins.gradle.org/m2/<group>/<path>/maven-metadata.xml` → `200` for the twelve coordinates' metadata                                                                                                                                                       |
| The pins are catalog libraries build-logic consumes        | `build-logic/build.gradle.kts`'s `implementation(libs.<x>.plugin)` lines; `build-logic/settings.gradle.kts`'s `from(files("../gradle/libs.versions.toml"))`                                                                                                                                                 |
| The nine shared libraries already have rows                | a fresh `./gradlew dependencyUpdates` run: `jackson-bom`, `jackson-databind`, `commons-lang3`, `assertj-bom`, `assertj-core`, `log4j-api`, `log4j-core`, `log4j-to-slf4j` and `spotbugs` each appear                                                                                                        |
| Exactly one of the twelve reaches the report today         | grepping a report run's rows for each coordinate: `com.diffplug.spotless:com.diffplug.spotless.gradle.plugin` is a hit; the other eleven, `spotbugs-gradle-plugin` included, are not                                                                                                                        |
| The portal's latest is a pre-release for some pins         | the metadata bodies: `kotlin-gradle-plugin` → `2.5.0-Beta1`, `spring-boot-gradle-plugin` → `4.2.0-M2`, `helm-plugin` → `3.2.1-beta1`                                                                                                                                                                        |
| `spring-boot-gradle-plugin` is covered by a deferred major | `config/dependency-updates/major-disabled.txt` lists `org.springframework`, whose header states the prefix matches the group and its sub-groups                                                                                                                                                             |
| The gap's cost                                             | `helm-plugin` at `3.1.2` while `3.2.0` was published (2026-09-27), until the 2026-10-15 bump: three scheduled runs fall in that window (2026-09-28, 2026-10-04, 2026-10-10, per `gh run list --workflow dependency-updates.yml`), and the report those runs execute cannot see this pin (the coverage rows) |
| The stability and suppression rules are reusable here      | `DependencyUpdateRules` (`isNonStable`, `disabledEntries`, `holdBackEntries`, `shouldReject`) lives in the same module as the task, and `config/dependency-updates/` is at the main root, where the registered task runs                                                                                    |
| The sibling checks' shape to follow                        | the three `*UpdatesTask` classes in `build-logic/src/main/kotlin`, registered in the root `build.gradle.kts` with a `reportFile`; their helpers live beside or inside them (`BuildpackJson`, `ToolingJson`), and `BuildpackUpdatesTaskTests` pins a verbatim provider body                                  |
| ADR-0015 frames this as a duty                             | its Decision's "single-sourcing, not update tracking … a hand-audit remainder"; its Consequences' "checking which (if any) update checker covers it"                                                                                                                                                        |
| The check's composition matches the convention's           | `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`'s `rejectVersionIf` block passes `catalogVersions`, `disabledEntries`, `holdBackEntries` and `currentVersion` to `shouldReject` — the same four inputs 1.4/D4 use                                                                  |
| The capability's requirements are written per report       | `openspec/specs/showcase/quality/dependency-management/spec.md`: the requirement bodies name `dependencyUpdates` and the web UI's `npmOutdated` respectively, and its Purpose enumerates those two reports                                                                                                  |
