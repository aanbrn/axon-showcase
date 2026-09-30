# Tasks

## 1. Colon-safe parsing and the hold-back rule

- [x] 1.1 Add `config/dependency-updates/hold-back.properties` with a header comment documenting the format (a
      `group:module` coordinate held at a `major.minor` version line, one entry per line, `#` comments) and the shipped
      entry `org.opensearch.client:opensearch-java=3.9`, whose comment names the coordinate and points at the
      `showcase/quality/dependency-management` rationale. Verify with
      `grep -n '^org.opensearch.client:opensearch-java=3.9$' config/dependency-updates/hold-back.properties` and by
      reading the file. — verified: the grep returns the single shipped entry line.
- [x] 1.2 Extract `DependencyUpdateRules` (an `internal object` in `build-logic/src/main/kotlin/`, KDoc on the object
      and every function): a line parser for both suppression files (`#` and blank lines ignored, the coordinate read as
      the text before the first `=` so a `group:module` key keeps its colon), the moved predicates (`isNonStable`,
      `isCalendarVersioned`, `isMajorBump`, `versionTrain`, `leadingInteger`, `matchesDisabled`), the new
      `isHeldBack(candidateVersion, heldLine)` (a candidate is held back when it shares the held line's major version
      and its `major.minor` train is newer than the held line, compared with `Versions.isNewer`), and a
      `shouldReject(...)` composing the whole decision. Verify `./gradlew -p build-logic compileKotlin` succeeds. —
      verified: `compileKotlin` succeeded with the object in place.
- [x] 1.3 Add `build-logic/src/test/kotlin/DependencyUpdateRulesTests.kt` with `@DisplayName` on the class and every
      test, covering: the parse of both file shapes (a `group:module` key survives with its colon; a bare group line
      stays a group prefix; comments and blank lines are ignored), exact-coordinate matching (the shipped
      `org.opensearch.client:spring-data-opensearch` entry matches that module and not `opensearch-java` — the drift the
      parser fix closes), the hold-back boundaries for a coordinate held at `3.9` (`3.10.0` held back, `3.9.1` not held
      back, `4.0.0` not held back, an unlisted coordinate unaffected), and the moved predicates' existing expectations
      (catalog-ownership/major behavior, calendar trains). Verify `./gradlew -p build-logic test` passes, and confirm
      the hold-back and exact-coordinate cases fail with the rule disabled and pass with it enabled (a test green on
      both sides verifies nothing). Read the failing output, not only the final test result. — verified: 16 tests pass
      (re-derived with `grep -c '@Test'`); control A (a Properties-like first-colon split in the parser) failed the two
      parse tests and the composition test; control B (`isHeldBack` forced false) failed the held-back, sibling,
      composed-decision, and composition tests; both controls were reverted and the suite re-ran green.
- [x] 1.4 Wire `dependency-versions-conventions.gradle.kts`: read both suppression files with `DependencyUpdateRules`'s
      parser (replacing `Properties.load`) and delegate `rejectVersionIf` to `shouldReject`. Verify
      `./gradlew -p build-logic test` and `./gradlew :help` (every project's convention plugins load) both succeed. —
      verified: both commands succeeded.
- [x] 1.5 End-to-end control: run `./gradlew dependencyUpdates` and read `build/dependencyUpdates/report.txt`. Capture
      its "later release versions" section twice — with the hold-back entry present (the `opensearch-java` row is
      absent) and with the entry commented out (the row returns) — and diff the two sections: the only difference is the
      `opensearch-java` row, so no other catalog-owned row moved. That also bounds the parser fix's blast radius: no
      `opensearch-java`/`opensearch-rest-client` major exists to surface, and the unit test in 1.3 pins their matching.
      Record both section reads as the task's evidence. — verified: with the entry present the "later release versions"
      section has 8 rows and no `opensearch-java`; commented out it has 9, the extra being
      `org.opensearch.client:opensearch-java [3.9.0 -> 3.10.0]`; the diff of the two sections is exactly that row.
      `opensearch-rest-client [3.8.0 -> 3.9.0]` is reported in both, showing the parser fix did not over-suppress.
- [x] 1.6 Refresh the mechanism's docs and remove the implemented idea: `AGENTS.md` (the `dependencyUpdates` command
      comment and the "Major-blocking entries …" paragraph), `README.md` (the suppression paragraph),
      `.opencode/commands/dependency-updates.md`, and `docs/ideas.md` (delete the "Hold back a same-major JVM
      coordinate" entry). In the same `docs/ideas.md` pass, refocus the deferred rework/rename: park it as a new entry
      in a new `## 2026-10-01` section at the top of the file (sections are newest-first), with the trailing
      `— parked; no change yet.` disposition; the entry SHALL say that a line-parsed list is not Java properties, that
      it covers the two `config/dependency-updates/*.properties` files plus the helm list (reworked the same way — a
      line parse owned by `HelmUpdateRules`, beside the tested `sameMajor`), that it is not a new capability (no chart
      hold-back), and that it is not docs-only (the loader paths are hard-coded in
      `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` and `build.gradle.kts`), so the entry,
      the paths, and the doc/spec references move together. Verify with
      `grep -rn 'hold-back.properties' AGENTS.md README.md .opencode/commands/dependency-updates.md` (anchored on the
      path, not the bare `hold-back` token, which already matches `hold-back-nginx-buildpack` in `AGENTS.md`), that the
      implemented idea is gone from `docs/ideas.md`, and that the parked rework/rename entry is present there. —
      verified: the path grep returns the three doc sites (`AGENTS.md` ×2, `README.md`, the command file); the hold-back
      idea is gone and the `## 2026-10-01` rework/rename entry is present.

## 2. Architecture-auditor sweep widening

- [x] 2.1 Add the dependency hold-back list to the swept suppression surfaces in
      `.opencode/agent/architecture-auditor.md` (beside the `major-disabled.properties` entry). Verify by reading the
      bullet: it names both config files. — verified: the bullet names `major-disabled.properties` and
      `hold-back.properties`.
- [x] 2.2 Add the same surface to the trigger command's step-1 read-list in `.opencode/commands/audit-architecture.md`
      (it enumerates the surfaces verbatim, so it would otherwise contradict the widened definition). Verify by reading
      the step: it names both config files. — verified: the step names both files.
- [x] 2.3 Add the same surface to the architecture-auditor bullet in `AGENTS.md` ("It sweeps the surfaces a rationale
      must exist for (…)"). Verify with `grep -n 'held-back coordinates' AGENTS.md` returning that bullet (the bullet
      keeps the terse surface name rather than the file path, which the definition and command carry). — verified: the
      grep returns the architecture-auditor bullet.
- [x] 2.4 Verify the two deltas validate: `openspec validate --all` accepts
      `openspec/changes/hold-back-minor-dependency-updates/specs/showcase/quality/agent-skills/spec.md` (its `MODIFIED`
      block carries all three existing scenarios) and the `dependency-management` `ADDED` requirement. — verified:
      `openspec validate --all` reports 25 passed, 0 failed.

## 3. Close-out

- [x] 3.1 Run `./gradlew spotlessApply`, then `./gradlew -p build-logic test` and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm both green. Re-run `spotlessApply` after any
      last edit to a Spotless-owned file (a task tick included) and re-check `spotlessCheck`. — verified: build-logic
      test green; `check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL in 1m50s.
- [ ] 3.2 Refresh the `showcase/quality/dependency-management` `## Purpose` (its current text describes major-only
      suppression) to cover the hold-back list — a delta cannot carry a Purpose, so this edit lands in the archive
      commit per `AGENTS.md`. Record the deferral in the change report.
- [x] 3.3 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task. — applied: one merge into the
      existing config-read-path gotcha (a format the repository defines is parsed and unit-tested by the repo, not read
      through a library format's assumptions; the `java.util.Properties` first-`:` mis-parse is recorded as the
      incident), with the mechanism's duplicate wording in the Build & Test note reclaimed as a pointer; of five
      candidates, four were already covered by existing bullets (the review-loop count rule, the test-display-name and
      control-anchor clauses, the enumerated-set rule) and were not re-added. Net `AGENTS.md` delta **+3 lines** (the
      merge's own contribution: the 2-line lead unchanged, +4 in the general-rule passage, −1 reclaimed from the Build &
      Test note); `verifyCapturedMarkers` and `spotlessCheck` pass with the new `captured:` marker.
