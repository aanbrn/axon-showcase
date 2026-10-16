# Design

## Context

See `proposal.md` — Why. The mechanism this design rests on, established by reading the build's own graphs, the pinned
artifacts, and the plugin's per-project output rather than from memory:

- **The floor is Gradle's own ban**: the pinned Gradle 9.8.1 distribution's `lib/gradle-logging-9.8.1.jar` contains
  `org/gradle/internal/logging/util/Log4jBannedVersion.class` with
  `LOG4J2_CORE_VULNERABLE_VERSION_RANGE = [2.0, 2.17.1)` and `LOG4J2_CORE_REQUIRED_VERSION = 2.17.1`, applied to every
  classpath Gradle builds — hence the row naming twenty projects
  (`root project, :helm, :helm:chart, :load-tests, :platform and 15 others`), several of which declare no SpotBugs.
  ADR-0007 attributed the floor to `com.github.spotbugs:spotbugs-annotations`, which held for the `4.10.3` pinned then
  (one of the versions publishing that guard), while the pinned `4.10.5` module metadata contains no `log4j` entry at
  all — so its attribution does not explain the row today.
- **`rejectVersionIf` does reach the row.** With a rule rejecting `log4j-core`'s candidates, the task's per-project
  partials record `latestVersion: "none"` with "Versions rejected by component selection rules" — no update row is
  reported for it. A reading that the row survived came from a stale aggregate report the run had not regenerated; the
  partials are a run's authoritative output.
- **The candidate set is walked in full**: the rule must reject every candidate at or below the declared version for a
  coordinate whose reported current differs from it, since any survivor becomes the row; the resolver therefore walks
  more versions than before, which is the cost this change accepts (measured below).
- `DependencyUpdateRules.shouldReject` already receives `catalogOwned`, a `Set<String>` of coordinates parsed from
  `gradle/libs.versions.toml`, with no access to the versions the catalog declares.

## Goals / Non-Goals

**Goals:**

- Remove the row without weakening what the task reads: `checkBuildEnvironmentConstraints` stays `true`.
- Keep every existing rejection decision (non-stable, non-catalog-owned, major-blocked, held-back) exactly as it is.
- Have the rule verified against a run's own output, since a cached report misled this change's first attempt twice.

**Non-Goals:**

- Removing or editing Gradle's own log4j ban, or any other module's resolution metadata. Suppressing a Log4Shell guard
  to silence a report row is the wrong trade, and Gradle's ban is not ours to change.
- Special-casing `log4j-core` or any other coordinate by name: the rule is stated over the catalog, so it also covers
  the next constraint — Gradle's or another's — that floors a coordinate the project already takes.
- Widening the report's scope (a new module, a new ecosystem).

## Decisions

**D1 — The catalog's declared version decides, and only where the plugin's current disagrees with it.** `shouldReject`
gains the catalog's `coordinate -> version` map and rejects a candidate no newer than the declared version **when the
plugin's reported current also differs from it** (the shared `Versions.isNewer` comparator) — the signature of a current
read from a constraint floor. Rationale: the `dependency-management` spec already makes the catalog the project's
version authority, so such a row reports nothing to update; and the second half of the condition is load-bearing rather
than decorative, because the plugin treats a coordinate whose every candidate is rejected as unresolved — without it,
the rule emptied the report's up-to-date section of all 83 catalog-owned entries (a review round caught it, and the
corrected version restores them while still removing the row). Alternatives rejected: requiring strict equality (the
resolver reports the newest surviving candidate, so rejecting only the declared version leaves the next one lower as the
row — the partials show the walk offering the declared version and lower); naming the coordinate in a suppression list
(brittle, silent about the next occurrence).

**D2 — Keep `checkBuildEnvironmentConstraints = true`.** Disabling it removes the row but also the visibility of any
build-environment constraint that is not Gradle's own (a tool raising its minimum) — ADR-0007 rejected it on that
ground, and this design keeps the judgement while narrowing its rationale, since the constraint behind this row is
Gradle's unchangeable one.

**D3 — A rule, not a suppression file.** A `hold-back.txt` entry (`log4j-core=2.17`) would also suppress the row, but
the file means "the last version line the project can take" — false here — and `isHeldBack` would suppress genuine
same-major candidates above `2.17` as well. The major-disabled file cannot express it (its rule needs a major bump; this
is same-major). Both were weighed and rejected.

**D4 — Supersede ADR-0007's Decision rather than edit it.** Its Context and experiments remain the evidence for the
reporting path, and a reversed Decision should read as reversed, so its Status becomes Superseded by ADR-0021. ADR-0021
carries what changed: ADR-0007's compared alternatives never weighed a rule over the catalog (the machinery —
`DependencyUpdateRules` — arrived with `hold-back-minor-dependency-updates`, #464), the attribution is Gradle's ban, and
the rule's cost is recorded so the next reader does not have to re-measure it.

**D5 — The catalog parse moves out of the convention script.** Resolving a `version.ref` through `[versions]` is the
change's riskiest step, and a convention plugin cannot be unit-tested (the repo verifies a plugin's effect through a
consuming module), so the resolve step becomes a `catalogVersions(lines)` function on `DependencyUpdateRules` — the
shape `disabledEntries` uses — with its own tests; the convention reads the file and passes the result.

**D6 — Verify against a run's partials, not its aggregate report.** A `dependencyUpdates` run writes per-project
partials as each resolution completes, and only then the aggregate report; a run that is interrupted or whose report is
not regenerated leaves a stale aggregate behind, which this change's first attempt read as the run's result twice. The
verification reads `build/dependencyUpdates/partials/*.json` for the coordinate's status and confirms the aggregate is
newer than the run.

## Risks / Trade-offs

- **A catalog-owned coordinate whose resolved version another surface governs below the catalog's stops being reported**
  → stated in the spec requirement, here, and in ADR-0021. Narrow in this repo: a coordinate enters the catalog to be
  governed by it, and the platform imports its constraints, so a competing surface must out-rank the platform.
- **The weekly update workflow gets slower** (tens of minutes against a couple) because the rejected candidates are
  walked → recorded here and in ADR-0021; accepted, since the row's cost is a re-derivation every week and the workflow
  is observational.
- **The rule hides a real update by accident** (the catalog read wrongly) → the resolve step is unit-tested over the
  catalog's real shape, the verification reads a run's partials, and the up-to-date section's size is checked against
  the pre-guard count, so both what the rule removes and what it must leave alone are observed rather than assumed.

## Migration Plan

None: the change filters a report, ships in the same commit as its tests, and reverts by reverting that commit.

## Open Questions

None.

## Claims table

| Claim                                                                                                                  | Source                                                                                                                                                                                                                                                                                         |
| ---------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The floor is Gradle's own log4j ban                                                                                    | `unzip -p <dist>/lib/gradle-logging-9.8.1.jar org/gradle/internal/logging/util/Log4jBannedVersion.class \| strings` → `LOG4J2_CORE_VULNERABLE_VERSION_RANGE`, `[2.0, 2.17.1)`, `LOG4J2_CORE_REQUIRED_VERSION`, `2.17.1`                                                                        |
| The pinned `spotbugs-annotations` publishes no such constraint, so ADR-0007's attribution does not explain the row now | `grep -c log4j ~/.gradle/caches/modules-2/files-2.1/com.github.spotbugs/spotbugs-annotations/4.10.5/*/spotbugs-annotations-4.10.5.module` → 0                                                                                                                                                  |
| `rejectVersionIf` reaches the row, and rejecting its candidates removes it                                             | a run's own partials (`build/dependencyUpdates/partials/*.json`) record `latestVersion: "none"` with "Versions rejected by component selection rules: … 3.0.0-beta3 …" for `log4j-core`; the aggregate `report.txt` is written afterwards and was stale in the run this was first misread from |
| The fix removes the row and nothing else                                                                               | the same report's update section holds 4 rows after the change (caffeine, jcache, grpc-bom, swagger-ui) against 5 before, `log4j-core:2.17.1` having moved to the informational "Failed to determine the latest version" section                                                               |
| The up-to-date catalog-owned entries are unaffected                                                                    | the "using the latest release version" section holds 145 entries, 83 of them catalog-owned (the unguarded rule: 62 entries, 0 catalog-owned)                                                                                                                                                   |
| `spotbugs-annotations` `4.10.3` published the constraint ADR-0007 saw                                                  | Maven Central's `spotbugs-annotations-4.10.3.module` carries a `dependencyConstraints` entry on `log4j-core` at `strictly [2.17.1, 3[` with the Log4Shell reason; `4.10.4` and `4.10.5` carry none                                                                                             |
| The change's cost                                                                                                      | 2061 s (34m21s) on the run that resolved the candidate set, 8 s on a repeat with the lookups cached                                                                                                                                                                                            |
| `2.17.1` resolves nowhere on the classpaths checked                                                                    | `./gradlew :showcase-command-service:dependencies --configuration {compile,runtime}Classpath` → no `log4j-core:2.17.1` entry                                                                                                                                                                   |
| The rule's input today is a coordinate set, with no versions                                                           | `DependencyUpdateRules.shouldReject`'s `catalogOwned: Set<String>` parameter, replaced by the `coordinate -> version` map `catalogVersions` returns                                                                                                                                            |
