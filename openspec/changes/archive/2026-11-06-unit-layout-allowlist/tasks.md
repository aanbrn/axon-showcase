# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/unit-layout-allowlist/unit.md`.

## 1. The rule

- [x] 1.1 The delta at `specs/showcase/quality/commit-hygiene/spec.md` adds the layout requirement and replaces the
      finding store's file-set sentences with a pointer to it, keeping all sixteen of the store requirement's scenarios.
      Verify: `openspec validate --all` passes, `openspec validate unit-layout-allowlist --strict` reports no warning,
      and the store block carries sixteen `#### Scenario:` headings, the same set as the main spec's. Done: the delta
      carries the added requirement with seven scenarios and the modified store block with all sixteen, in order;
      `openspec validate --all` 27 and `--strict` valid.

## 2. The check

- [x] 2.1 `scripts/commit-hygiene.py` gains `layout_stray_paths`, which reads every tracked path under `docs/units/`,
      admits `unit.md`, `rounds.tsv`, `review-handover.md` and `process-defects.md` at the first level and
      `findings/round-<N>.md` at the second, and returns each offending path with its unit; a `--unit-layout` mode
      prints them naming the unit, the path and that the layout does not admit it. Load `check-and-test-authoring`
      before writing the check. Verify: the mode on a scratch unit holding a root-level file names the unit, the path
      and that the layout does not admit it. Done: `layout_stray_paths` admits the four first-level filenames and the
      direct `findings/round-<N>.md`, returns every other tracked path with its unit, and `--unit-layout` prints each as
      `Unit layout: <path> is not a path the unit layout admits`.
- [x] 2.2 `build.gradle.kts` registers `verifyUnitLayout` in the shape its sibling `verifyFindingInstances` uses —
      `inputs.file("scripts/commit-hygiene.py")`, `inputs.dir("docs/units")`, `outputs.upToDateWhen { false }`, and
      `commandLine(pythonExecutable, "scripts/commit-hygiene.py", "--unit-layout")`, never a bare command name — and the
      `check` block's `dependsOn` list names it; the mode joins `MODE_FLAGS`, the parser's mode group, the dispatch, and
      the coverage test's mapping. Verify: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` runs the task, and
      the coverage test accepts the new mode. Done: `verifyUnitLayout` sits beside its sibling with the same four inputs
      and `pythonExecutable`, `check` depends on it, and the mode joins `MODE_FLAGS`, the parser group, the dispatch and
      `MODE_TEST_COVERAGE`.
- [x] 2.3 `scripts/test-commit-hygiene.py` gains `UnitLayoutTests` with these arms: every admitted kind passes — a unit
      holding all five; a root-level file the layout does not admit fails naming the unit, the path and that the layout
      does not admit it; a third level under `findings/`, such as `findings/consultations/advisor-1.md`, fails; a path
      at another location shaped like an admitted name, such as `other/round-1.md`, fails, so admission is a position
      test as well as a name test; an admitted filename in the wrong place, such as `unit.md` under `findings/`, fails;
      a unit carrying only `rounds.tsv` and its instances files passes; an untracked file passes. Verify: each failing
      arm returns 0 with the predicate widened to admit everything, which proves it reaches the predicate rather than a
      neighbouring check; and the all-five acceptance arm holds with `unit.md` included, so a predicate that rejected an
      admitted name cannot pass. Done: nine arms — all five kinds, a root stray, a third level under `findings/`,
      `other/round-1.md`, `findings/unit.md`, rounds-only, an untracked file, a file outside any unit's directory, and
      the count. Controls: with the predicate widened to admit everything the five failing arms return 0; with `unit.md`
      dropped from the admitted set the all-five arm fails; with the outside-a-unit skip removed that arm fails.
- [x] 2.4 `--finding-instances` gives up its store-only stray clause, and the two arms that covered it move to 2.3, so
      one predicate owns the file set. Verify: the store mode's remaining arms pass, the layout mode covers the store's
      subtree through its nested arm, and no test asserts the removed clause. Done: `--finding-instances` no longer
      appends a stray-file failure and its three store arms are removed; the store's own violations and their arms are
      untouched, and the layout's third-level and wrong-place arms cover what the store clause caught.
- [x] 2.5 `.opencode/skills/review-gate/SKILL.md`: the writer step's bullet points at the layout requirement instead of
      the store's, and names the layout as what admits the kinds. Verify: the bullet names the layout requirement and
      carries no copy of its text. Done: the writer step's bullet names the layout requirement and the build's rejection
      of a path it does not admit; the skill carries no copy of the requirement's text.

## 3. The record

- [x] 3.1 `docs/ideas.md`: this work's entry leaves the `## 2026-11-06` section, and that section's other entries stay.
      Verify: `./gradlew verifyIdeasSections` passes and the section still names the IDEA-integration entry and the
      parked defect record. Done: the entry left `## 2026-11-06` at the proposal stage; the section keeps the
      IDEA-integration entry and the parked field-6 correction, and `verifyIdeasSections` passes.

## 4. The gates

- [x] 4.1 The claims table in `design.md` is re-derived after the last edit, each row run against the base or the final
      tree as the row states, every row reproduces, and the `### Dismissals` block names a reason for each candidate
      kind the scan emits. Verify: each source command re-run after the final change, and
      `python3 scripts/claim-scan.py --base d420246c` shows every candidate covered by a row or a dismissal. Done: the
      table was re-derived after the last edit and `claim-scan.py --base d420246c` shows every candidate covered by a
      row or a dismissal.
- [x] 4.2 `./gradlew spotlessApply` runs after the final edit to a Spotless-owned file, and `openspec validate --all`
      passes. Verify: both commands, in that order, on the final tree. Done: `spotlessApply` after the last edit, then
      `openspec validate --all` at 27 passed.
- [x] 4.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes, and `python3 scripts/test-commit-hygiene.py`
      passes under the declared Python floor. Verify: both green on the final tree. Done:
      `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 8 s with `verifyUnitLayout` executing, and
      `python3 scripts/test-commit-hygiene.py` exits 0.
- [x] 4.4 The deep-review routing decision runs once from the completed implementation —
      `python3 scripts/review-routing.py --unit unit-layout-allowlist --change unit-layout-allowlist` — and its outcome
      and the signals it matched are recorded on this task. Verify: the outcome is recorded with its signals, and a
      `REQUIRED` outcome is run before completion. Done: `review-routing.py` returns `REQUIRED` on the completed
      implementation, its signal `requirement-block-added`; the deep review runs before completion.
- [x] 4.5 `openspec/specs/showcase/quality/commit-hygiene/spec.md`: refresh the `## Purpose`, which now names the
      store's file set as the guard's reach; the layout requirement owns the kinds instead. A delta cannot carry a
      Purpose, so the edit rides the archive commit. Verify: the Purpose names the unit layout, and the requirement's
      text is untouched by the edit.
      Done: the Purpose now names the unit layout's kinds, and the requirement's text is untouched by the edit.