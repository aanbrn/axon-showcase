# Tasks

## 1. The helm line parse

- [x] 1.1 `build-logic/src/main/kotlin/HelmUpdateRules.kt` — add the line-list parse (blank lines and `#` comments
      skipped; an entry read as the text before the first `=`, an optional `=value` dropped), KDoc on the object and the
      function, mirroring `DependencyUpdateRules`' shape without sharing its `=value` half. Verify
      `./gradlew -p build-logic compileKotlin` succeeds. — verified: `disabledEntries`/`disabledEntriesOf` compile.
- [x] 1.2 `build-logic/src/test/kotlin/HelmUpdateRulesTests.kt` — cover the parse: comments and blank lines skipped, a
      chart name kept whole, an `=value` suffix dropped, and a full-line `#` comment that contains an `=` skipped whole.
      `@DisplayName` on the class and each test; the existing `sameMajor` case stays. Verify
      `./gradlew -p build-logic test` passes, and run a scratch control that returns every trimmed line without the
      comment/blank/`=` handling, to confirm the comment and `=value` cases fail against it (a control must perturb the
      surface the test reads). Read the failing output, not only the final result. — verified: the suite is green; the
      naive control reddened exactly the four parse cases (`60 tests completed, 4 failed`), then reverted green.
- [x] 1.3 Wire the helm task to the parsed list — verify `./gradlew :help` and `./gradlew helmUpdates` succeed, and that
      the report suppresses the same charts as before. — verified, **with a design correction the implementation
      forced**: the parse cannot live in the root build script at all, because `HelmUpdateRules` is not on its
      buildscript classpath (probed: `ClassNotFoundException`), whatever its visibility. So `HelmUpdatesTask` now takes
      the file as a `majorDisabledFile` `@InputFile` and parses it in its action (where the rule object is in scope),
      and `build.gradle.kts` only points the task at the renamed path. `./gradlew helmUpdates` succeeds. Its rows are a
      live `helm search repo` lookup and vary run to run — one run showed `bitnami-kafka: 31.5.0 -> 32.4.3` (a
      same-major minor, the documented `latestSameMajorVersion` fallback), another showed only
      `prometheus-community-stack` — so the tick's evidence is the run succeeding plus the parse being pinned by 1.2's
      tests, not any particular row.

## 2. Rename the three lists and sweep the references

- [x] 2.1 Rename the files (`git mv`) to drop the extension their format no longer uses:
      `config/dependency-updates/major-disabled.properties` → `major-disabled.txt`,
      `config/dependency-updates/hold-back.properties` → `hold-back.txt`,
      `config/helm-updates/major-disabled.properties` → `major-disabled.txt`. — verified: all three renamed; the helm
      list's three chart names intact.
- [x] 2.2 Update every **live** reference to the old paths — sweep with
      `grep -rn 'major-disabled\.properties\|hold-back\.properties'` and confirm the only remaining hits are the dated
      `docs/audits/` reports and `openspec/changes/archive/` (historical records, left as written). The live sites: the
      loader paths in `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` and `build.gradle.kts`;
      `AGENTS.md` (the `dependencyUpdates` note, the "Major-blocking entries" paragraph, and the architecture-auditor
      bullet); `README.md`; `.opencode/commands/dependency-updates.md` (**two** prose sites, the major-disabled and the
      hold-back sentence); `.opencode/agent/architecture-auditor.md`; `.opencode/commands/audit-architecture.md`; and
      `docs/adr/0007-dependency-updates-build-environment-constraint-noise.md` and
      `docs/adr/0011-defer-axon-framework-5-migration.md`. Verify by reading each edited site and by the grep above. —
      verified: the sweep leaves only `docs/audits/`, `openspec/changes/archive/`, the change dir's own artifacts (which
      describe the rename), and the main `agent-skills` spec — the last by design, since the delta syncs it at archive.
- [x] 2.3 The `agent-skills` delta:
      `openspec/changes/rework-the-helm-suppression-list/specs/showcase/quality/agent-skills/spec.md` carries the
      renamed paths in its `MODIFIED` "Unexplained design intent is surfaced for clarification" requirement, with all
      three existing scenarios verbatim and in order. Verify `openspec validate --changes` accepts it (the `MODIFIED`
      header matches the main spec's, and no scenario is dropped). — verified: `openspec validate --changes` passes.
      (The main spec was restored after an accidental direct edit — before archive, the delta is the only vehicle for a
      spec change.)
- [x] 2.4 `docs/ideas.md` — remove the implemented "Rework the suppression lists and rename them off `.properties`"
      idea. Verify the entry is gone. — verified: the block is removed, and the surviving npm idea's reference to the
      JVM list was retargeted to `.txt`. **Near-miss, restored:** the first removal sliced from the idea's first line to
      the next section boundary, which also deleted the following bullet in the same section — the unrelated
      `ubuntu-latest` runner-migration idea, still live (12 jobs still on that label; `actions/runner-images#14748`
      still open). It was restored on the owner's prompt. The unit of removal is one bullet, not a section, and this
      task's own verification ("the entry is gone") cannot see a collateral deletion — re-read the file after a
      programmatic cut.

## 3. Close-out

- [x] 3.1 `./gradlew spotlessApply`, then `./gradlew -p build-logic test` and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green. (No Docker/graded run — the change is
      build-logic configuration and docs.) — verified: `check` BUILD SUCCESSFUL (2m36s); `spotlessCheck` green.
- [x] 3.2 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task. — applied: two appends to
      existing bullets, each with a `captured: rework-the-helm-suppression-list` marker — the probe-first bullet gains
      that a `build-logic` object is unreachable from a root script's _configuration_ path (`includeBuild` keeps its
      classes off the root buildscript classpath, probed as `ClassNotFoundException`), so the rule lives in the
      `build-logic` task that applies it (an `@InputFile`, `@Optional` when absent); the durable-artifact bullet gains
      that a task touching a live surface must not pin a value the surface emitted (the `helmUpdates` rows vary run to
      run). Capture net **+9 `AGENTS.md` lines** (read from `git diff --numstat`; the two appends are +4 and +5); four
      other candidates were already covered (the main-spec-at-archive rule, the rename sweep, the derived-claim and
      read-the-edit rules) and were not re-added. `verifyCapturedMarkers` and `spotlessCheck` green.
