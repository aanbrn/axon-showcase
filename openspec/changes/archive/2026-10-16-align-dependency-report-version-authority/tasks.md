# Tasks

## 1. The rule

- [x] 1.1 Add a clause to `DependencyUpdateRules.shouldReject`: reject the candidate when the catalog declares that
      coordinate and the declared version is not older than the candidate **while the plugin's reported current differs
      from it** (the shared `Versions` comparator), so a row whose candidate the project already takes — or is behind —
      reports nothing to update. Keep the existing decisions (non-stable, non-catalog-owned, major-blocked, held-back)
      and their order of evaluation.
- [x] 1.2 Change the input from `Set<String>` to the catalog's `coordinate -> version` map, so the rule reads the
      declared version; keep the "not catalog-owned" decision driven by membership in that map.
- [x] 1.4 Take the per-call performance wins the IDE and the change's own cost surface: hoist `isNonStable`'s keyword
      list and version regex out of the method (it is called twice per candidate), build the `group:module` coordinate
      once per `shouldReject` call instead of twice, and make `catalogVersions`' chain lazy after IDEA's
      `ConvertCallChainIntoSequence` warning. All behaviour-identical, and none of them shortens the resolver walk that
      dominates the run.
- [x] 1.3 Update the KDoc to state the decision: the catalog is the version authority, so a row whose candidate the
      declared version is not older than, while the plugin's reported current differs from it, reports nothing to
      update, whatever "current" the plugin derived from a constraint floor.

## 2. The catalog parse

- [x] 2.1 Add a `catalogVersions(lines)` function to `DependencyUpdateRules` — the shape `disabledEntries` uses for the
      suppression files — turning `= { group = "…", name = "…", version.ref = "…" }` entries into
      `coordinate -> version` pairs by resolving the referenced `[versions]` key.
- [x] 2.2 In `dependency-versions-conventions.gradle.kts`, read `gradle/libs.versions.toml` and pass that function's
      result to `shouldReject`, replacing the coordinate-set parse. Leave `checkBuildEnvironmentConstraints = true`.

## 3. Tests

- [x] 3.1 Test `catalogVersions` over the catalog's real shape: a known entry yields its coordinate and its resolved
      version, an entry whose `[versions]` key is absent is skipped, and a library entry missing a field yields no pair
      rather than a wrong version.
- [x] 3.2 Extend `DependencyUpdateRulesTests` with the rule's cases: the `log4j-core`-shaped input (`current 2.17.1`,
      `candidate 2.26.0`, catalog `2.26.1`) is rejected, as is an exact match (`2.26.1` → `2.26.1`); a candidate newer
      than the catalog (`2.27.0`) is not rejected; a coordinate absent from the catalog stays rejected; the
      major-disabled and held-back cases keep their outcomes.
- [x] 3.3 Show the rule's case failing without the clause: run it with clause 1.1 temporarily reverted, confirm it is
      accepted, restore, and confirm it is rejected.

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 4.2 Run `./gradlew :build-logic:test`, then `./gradlew dependencyUpdates` to completion and read the **partials**
      (`build/dependencyUpdates/partials/*.json`) for `log4j-core`'s status: it must be `latestVersion: "none"` with no
      "rejected by component selection rules" list that still holds an acceptable candidate — not a `report.txt` left
      over from an earlier run (this change's first attempt read a stale aggregate twice). Confirm the aggregate report
      is newer than the run and no longer lists an update row for it, and that no other row or section changed. Outcome:
      the update section holds 4 rows (caffeine, jcache, grpc-bom, swagger-ui) against 5 before, the `log4j-core` row
      gone and `log4j-core:2.17.1` moved to the informational "Failed to determine the latest version" section (which
      the weekly issue does not extract); the partials read `latestVersion: "none"` for the coordinate in 20 projects
      and `2.26.1` in the other 16; and the "using the latest release version" section holds 145 entries, 83
      catalog-owned.
- [x] 4.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessApply`/`spotlessCheck`,
      `openspec validate --all`, and the manual 120-character check over the changed set.
- [x] 4.4 Record the run's wall-clock cost against the recorded baseline: rejecting the candidate set makes each
      resolution walk more versions, so the task is expected to take substantially longer, and the figure belongs in the
      change's report. Outcome: 2061 s (34m21s) on the run that resolved the candidate set, against ~72 s for a fresh
      pre-change run; 8 s on the repeat whose lookups were cached. `dependencyUpdates` is not part of `check`, so only
      the weekly workflow pays the cold figure.

## 5. Docs the change owns

- [x] 5.1 `AGENTS.md` and `README.md` — corrected for the row's attribution (Gradle's own ban, not a
      `spotbugs-annotations` constraint) and for the row now being filtered, pointing at ADR-0021. Outcome: `AGENTS.md`
      keeps the rule's shape and its trap (a rule that also rejects an up-to-date coordinate's own version empties the
      up-to-date section); `README.md` states the `log4j-core` update row is no longer reported.
- [x] 5.2 `docs/adr/` — add ADR-0021 recording the decision (the catalog is the report's version authority; the accepted
      false-negative surface; the measured cost), and set ADR-0007's Status to Superseded by ADR-0021 without altering
      its Context or its recorded experiments.
- [x] 5.3 `openspec/config.yaml` — sweep its `context:` block and rules for a statement of this behaviour. Outcome:
      nothing to update (its SpotBugs mention is about the include/exclude filters).
- [x] 5.5 Park the gap this work surfaced — `build-logic`'s own plugin pins are outside every update check, since the
      report runs in the main build only — in `docs/ideas.md` under a new dated section, evidenced by `helm-plugin`
      sitting at `3.1.2` for the eighteen days after `3.2.0` shipped while two weekly reports listed no helm row.
- [x] 5.4 Record the capability's `## Purpose` outcome: **no refresh owed** — the Purpose already frames the capability
      as scoping and filtering the report, and the new clause narrows which catalog-owned coordinates are reported
      without moving that scope.

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass's output — the claims table in `design.md` — and re-derive each row
      against its source before the first review round.
- [x] 6.2 Append each `review-quick` round's row to `docs/review-rounds.tsv` when that round's report arrives (the rows
      from this change's earlier, abandoned attempt stand as its own record; the series is continuous per unit, so this
      attempt's rounds are rows 7-10).
- [x] 6.3 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta. Outcome: three durable items
      applied — an artifact is evidence only of the run that wrote it (the stale-aggregate lesson, on the citation
      gotcha), a filter's removal test proves nothing about the classifications it moves (on the "assert what the
      subject emits" sub-bullet, the 83-entry regression), and the redundant tool-guard value dropped from the
      build-environment paragraph.
- [x] 6.4 Request the user's manual review pass, and archive only after it, the CI run, and the change's own PR.
