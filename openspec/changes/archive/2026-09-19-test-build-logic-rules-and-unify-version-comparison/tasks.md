# Tasks — test the build-logic rule sets and unify the version comparison

## 1. The shared version comparator

- [x] 1.1 Add `build-logic/src/main/kotlin/Versions.kt` with `internal object Versions`: `isNewer(candidate, current)`
      (numeric segments, zero-padded, equal spellings equal), `compare(a, b)` (numeric, then the longer spelling wins —
      the selection tiebreak of design D1), `numericParts(version)` (a leading `v` is stripped, a non-numeric prefix
      yields an empty list), `leadingInteger(version)`, and `highest(versions)` (the maximum by `compare`). Done —
      `Versions.kt` holds all five, each with a KDoc naming the decision it encodes.
- [x] 1.2 Point `build-logic/src/main/kotlin/ToolingUpdatesTask.kt` at `Versions` and delete `ToolingVersions`; keep
      `ToolingJson` as the home of the provider patterns and the token. Move that class's comparison cases out of
      `ToolingUpdatesTaskTests.kt` (they land in `VersionsTests.kt`, task 3.1), leave its provider-pattern guards in
      place, and retitle its `@DisplayName` to what it still covers. Done — the task calls `Versions.isNewer`, the
      object is gone, and the class now reads `@DisplayName("Tooling provider patterns")` with its two JSON guards.
- [x] 1.3 Point `build-logic/src/main/kotlin/BuildpackUpdatesTask.kt` at `Versions`: `Versions.highest` for the reported
      tag and `Versions.isNewer` for the decision; move the Docker Hub `name` pattern to a named `internal` constant
      that tolerates a space after the colon (design D5). Delete `compareVersions` and `numericParts`. Done — the
      pattern is `BuildpackJson.TAG_NAME` (`"name":\s*"([0-9][^"]*)"`) and the task keeps only its HTTP query.
- [x] 1.4 Point `build-logic/src/main/kotlin/HelmUpdatesTask.kt` at `Versions` and delete its `isNewer`, `numericParts`
      and `leadingInteger`. Done — both comparisons call `Versions.isNewer` and the three privates are gone.

## 2. The pure rule objects

- [x] 2.1 Add `build-logic/src/main/kotlin/InfraImageVersionRules.kt` mirroring `ModuleDependencyRules`: the top-level
      `image:`/`tag:` parse, the segment count, the floating-reference rejection (fewer than two numeric segments) and
      the truncation match, each returning its message. Reduce `VerifyInfraImageVersionsTask` to the adapter: run Helm
      per check, call the rules, throw on a message, walk the values files, write the result file. Done — the rules
      object returns `floatingReferenceReason`/`mismatchReason` messages and the task's `verify()` is now four lines of
      calls around its Helm query.
- [x] 2.2 Add `build-logic/src/main/kotlin/HelmUpdateRules.kt`: the same-major filter over a version list, so
      `latestSameMajorVersion` only parses and the suppression stays at the call site as the set membership it is. Done
      — `check()` keeps its set-membership branch (`check.name in majorDisabled`) and `latestSameMajorVersion` ends in
      `HelmUpdateRules.sameMajor`.
- [x] 2.3 Verify no behavior moved: `git diff` the extracted code against its original for each of the three extractions
      and confirm the only semantic difference is the one design D1 intends. Done — `:build-logic:test` green, the three
      live reports byte-identical before and after, and an audit of the removed logic lines (non-blank, non-comment)
      found the ones absent from the new sources to be exactly the call-site redirects, the comparator relocation with
      its rename, the hardened pattern, the rules' `check.`-prefixed derivation and the D1 tiebreak change — no dropped
      logic.

## 3. The tests

- [x] 3.1 `build-logic/src/test/kotlin/VersionsTests.kt`: ordering (`1.2.1` newer than `1.2`), equivalence (`1.2` equals
      `1.2.0` in both directions, so neither is newer), the `v` prefix, a non-numeric prefix, a major-only version,
      `leadingInteger`, and `highest` preferring the longer spelling among numerically equal tags while still returning
      the highest number. Carry over the cases migrated from `ToolingUpdatesTaskTests.kt`, including the one whose
      answer changes (`isNewer("1.2.0", "1.2")` was true and is now false). Done — 8 cases, including the flipped
      `1.2.0`/`1.2` pair and `highest` on `["5", "5.15", "5.15.0", "latest"]`.
- [x] 3.2 `build-logic/src/test/kotlin/InfraImageVersionRulesTests.kt`: the top-level `image:`/`tag:` parse (present,
      absent, a `tag:` outside the `image:` block, an `image:` key that is not at the top level), `truncateToSegments`
      (`17.6` matching a chart `17.6.0`, `3.9.0` requiring an exact match), the floating-reference rejection for a
      bare-major tag, and the mismatch message naming both versions. Done — 7 cases.
- [x] 3.3 `build-logic/src/test/kotlin/HelmUpdateRulesTests.kt`: the same-major filter picks the first candidate in the
      pinned major and ignores another major. Done — the filter case, with the same-major selection decision it carries.
- [x] 3.4 `build-logic/src/test/kotlin/BuildpackUpdatesTaskTests.kt`: the Docker Hub pattern against a real trimmed
      response body (and against the same body with a space after the colon, which the hardened pattern also matches),
      and the alias selection returning `5.15.0` rather than `5.15` when both are present. Done — 2 cases, the fixture a
      verbatim slice of a live `paketobuildpacks/procfile` response around its `5.15.0` tag.

## 4. Docs

- [x] 4.1 Refresh the update-check gotcha in `AGENTS.md`: the alias rule now names the two operations (the comparison
      that decides whether there is an update treats equal spellings as equal, while the reported tag prefers the longer
      spelling), and the three comparators are one shared, tested `Versions` object. Done — the sentence now names
      `Versions.isNewer` and `Versions.highest` and why the two operations differ.

## 5. Verification

- [x] 5.1 `./gradlew :build-logic:test` green, with the four new classes listed in the output. Done — `VersionsTests`
      (8), `InfraImageVersionRulesTests` (7), `BuildpackUpdatesTaskTests` (2), `HelmUpdateRulesTests` (1), plus the two
      retained `ToolingUpdatesTaskTests` guards, `ModuleDependencyRulesTests` (6) and `ComposeServicesTests` (2): 28
      tests, 0 failures.
- [x] 5.2 `./gradlew spotlessApply` then `./gradlew spotlessCheck` green (Kotlin and markdown are both formatter-owned,
      so the manual 120 check applies only to the YAML — `.openspec.yaml` — which is clean). Done — and one pre-existing
      over-long string in `VerifyInfraImageVersionsTask.kt` was split by concatenation, leaving zero lines over 120 in
      the touched Kotlin.
- [x] 5.3 Capture the three live reports before and after the refactor
      (`./gradlew toolingUpdates helmUpdates buildpackUpdates`) and diff them — they must be identical, which is the
      evidence that the unification preserves behavior on the current pins. Done — all three byte-identical
      (`snyk-cli: v1.1307.2 -> 1.1307.3`, `prometheus-community-stack: 91.4.0 -> 91.4.1`, and
      `paketo-builder-jammy-base: 0.4.642 -> 0.4.643` with `paketo-nginx: 1.2.0 -> 1.2.1`).
- [x] 5.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, and `openspec validate --all` with the change
      validating as `skip_specs`. Done — `check` green (1m44s, including `:build-logic:test`, `workflowLint` and
      `verifyModuleDependencies`), `openspec validate --all` 23/23 with the change's zero deltas accepted.
- [x] 5.5 Confirm the alias pairing behind design D1's equivalence rule: `5.15` and `5.15.0` in the Paketo repository
      resolve to the same image digest (Docker Hub tags API), recorded in the change's report as the evidence the
      trade-off rests on. Done — `5.15`, `5.15.0` and even the bare `5` all carry
      `sha256:a1711924cc3596dcf1bbb4567e32425e7acffd6b58554bc2a6f524becbd2a24c`, so the spellings are one release.
