# Tasks

## 1. Widen the lesson-capture definition

- [x] 1.1 In `.opencode/agent/lesson-capture.md`, widen the frontmatter `description` to the unit: "Captures lessons
      learned from a unit into AGENTS.md and reports the rules the unit retires. Use after a unit's implementation (and
      its quick review) finishes…" — verify the frontmatter still parses (an OpenCode reload loads the definition).
- [x] 1.2 Widen the trigger sentence and the inputs list (lines ~10–18): "After a unit's implementation is done…", "The
      unit's diff and/or commit history", and keep "the change dir (proposal/design/tasks/delta spec)" as-is — a change
      dir is a real artifact only a change has. Verify by reading the list back.
- [x] 1.3 Widen the retirement clause (lines ~36–39): "Also report what the unit retires", "a unit is the moment a rule
      can become unnecessary", "the rules this unit makes obsolete or redundant", "the unit's new enforcement subsumes
      it", "why the unit makes it so". Verify the clause still reads as one argument and its dashes pair.
- [x] 1.4 Widen the marker convention (line ~32) to a greppable `captured: <unit>` marker — the unit name — and widen
      the promotion gate's source clause (line ~57) to "a file the unit did not author". Verify no other `change`-scoped
      wording remains in the file (`grep -n "change" .opencode/agent/lesson-capture.md`) and that each survivor (e.g.
      `the change dir`) is deliberate.

## 2. Widen the review subject in the review-quick definition where it is meant

- [x] 2.1 In `.opencode/agent/review-quick.md`, widen the review subject where the unit is meant: the frontmatter
      `description` ("a unit's proposal or implementation"), the opening sentence ("Given a unit — its proposal…"), and
      verify the non-OpenSpec clause ("or, for a non-OpenSpec change (a docs refresh, a standalone fix), a diff against
      the repository with no change dir") is kept — it is the widening's own rationale.
- [x] 2.2 Widen the durability clause's subject to the unit: the clause that reads "for a change whose diff adds
      `AGENTS.md` rules (a capture)" becomes "for a unit whose diff adds …", matching the delta and the unit-scoped
      capture. Keep "a future change would act on" (it is about a future change, not the reviewed unit). Verify no "for
      a change whose diff" remains: `grep -n "for a change whose diff" .opencode/agent/review-quick.md` (no match).

## 3. Spec deltas

- [x] 3.1 Write the delta at
      `openspec/changes/scope-capture-trigger-to-the-unit/specs/showcase/quality/agent-skills/spec.md`: two `MODIFIED`
      requirements. (a) `Per-change quality-gate and analysis subagents are available` carrying all six existing
      scenarios in order with the byte-identical header, widened in the review scenario (the review subject and the
      durability clause), the capture scenario (its WHEN, the lessons' source, the retirement clause, the source clause,
      and the marker form), the retirement scenario's body, and the requirement description's `per-change` adjective.
      (b) `Report-producing subagents follow a shared report contract` carrying all four existing scenarios in order
      with the byte-identical header, its bearer list's `per-change` adjective widened to `per-unit`. Verify with
      `openspec validate --changes`. Note in the report that the first requirement's _header_ keeps `Per-change` — a
      `MODIFIED` block cannot retitle it and a `REMOVED`+`ADDED` pair is disproportionate for an adjective.
- [x] 3.2 Write the delta at
      `openspec/changes/scope-capture-trigger-to-the-unit/specs/showcase/quality/commit-hygiene/spec.md`: one `MODIFIED`
      requirement carrying all four existing scenarios in order, with only the `captured: <change>` pointer widened to
      `captured: <unit>`. Verify with `openspec validate --changes`.
- [x] 3.3 Read both capabilities' `## Purpose` and decide whether this change falsifies their text; record the decision,
      and if a refresh is owed apply it in the archive commit (a delta cannot carry a `## Purpose`). Decision:
      `agent-skills`'s Purpose DOES move — it names "the per-change review and lesson-capture agents" (main spec `:7`),
      the same subject this change widens, so the archive commit refreshes it to `per-unit`; `commit-hygiene`'s Purpose
      describes the mechanical guards and enumerates no capture scope, so it needs no refresh. Record the
      `commit-hygiene` no-refresh reason too.

## 4. Docs the change owns

- [x] 4.1 Update `README.md`'s self-learning prose and agent-table row: line ~14 ("each unit's lessons are written
      back"), the `lesson-capture` row ("after every unit", "the rules a unit obsoletes"), the `Code review` bullet
      ("every unit is auto-reviewed" — the review-gate subject the delta widens), the `Lesson capture` bullet ("after
      each unit", "the rules the unit makes obsolete"), and the `Every change closes the loop` bullet ("runs after a
      unit's implementation"). Verify the README's shape is preserved (table cell widths and the surrounding bullet list
      read coherently after the edit).
- [x] 4.2 Remove the parked idea "Widen the capture trigger's scope across its other artifacts — parked; no change yet."
      from `docs/ideas.md` (the entry beginning at line ~35), since this change implements it. Verify with
      `grep -n "capture trigger's scope" docs/ideas.md` (no match) and confirm no other entry was disturbed.
- [x] 4.3 Widen `.opencode/agent/experience-analyzer.md`'s first `per-change` instance (line ~13): the layer it sits
      above is now per-unit, so "the top layer above the per-unit `review-quick`/`lesson-capture` agents". Keep the
      second instance (line ~30, "per-change captures") — it names the aggregates a retrospective reads, which are
      per-change in practice; record the keep reason in the change's report.

## 5. Audit the surrounding artifacts (no edit expected — confirm, do not assume)

- [x] 5.1 Audit `AGENTS.md` for residues of the convention: grep it for the change-scoped capture vocabulary — the
      phrases `the change retires`, `the change's lessons`, `change did not author`, `change's new enforcement`,
      `change makes it so`, and `rules the change` — and read each hit, plus the capture bullet and the auto-review
      bullet in full. Expected: the capture bullet's `after a unit's implementation`, `the unit makes obsolete`, and
      `captured: <unit>` are already correct, and the auto-review bullet's "a change's proposal (planning artifacts)" is
      the OpenSpec-vocabulary half of its two-part sentence. Edit only an instance genuinely narrower than the rule, and
      record any kept instance with its reason.
- [x] 5.2 Sweep every artifact for the convention rather than a single phrase. First grep the marker form over
      `openspec/specs/`, `.opencode/`, `README.md`, `AGENTS.md`, `docs/`, and `scripts/` for both spellings; then grep
      the change-scoped capture phrasing over the same set — the phrases `change's implementation`, `after a change`,
      `after each change`, `after every change`, `the change's lessons`, and `the change retires`; then grep the
      `per-change` adjective over the same set. Confirm each hit is either edited by tasks 1–4, is a deliberate keep
      recorded in 2.2/4.3/5.1, is the marker-form fixture in `scripts/test-commit-hygiene.py` (mechanical, not a
      spelling assertion — below), or lives in `openspec/changes/archive/` or a dated record under `docs/` (the
      historical record, left as recorded).
- [x] 5.3 Confirm the mechanical consumers need no change: read `scripts/commit-hygiene.py` and
      `scripts/git-hooks/pre-commit` and verify the guard matches the bare `captured:` token and checks placement only,
      with no `<change>`/`<unit>` spelling assertion; the `captured: <change>` string in
      `scripts/test-commit-hygiene.py` is a fixture proving a backticked mention is not a marker, not an assertion on
      the form. Record the verification in the change's report.
- [x] 5.4 Widen the `per-change` adjective in `AGENTS.md`'s experience-analyzer bullet (line ~1018): it names the
      `review-quick`/`lesson-capture` layer directly above this change, so "above the per-unit `review-quick`/
      `lesson-capture` agents". The adjective in the agents-auditor's accretion-class prose — `AGENTS.md` `:1018`'s
      sibling `.opencode/agent/agents-auditor.md:101` and main spec `:246` — names "the per-change workflow" as one
      example of a meta-rule subject, the generic class the literal `per-change` matches, so it is kept; the
      `experience-analyzer.md` aggregate is kept per task 4.3. Record both keeps and the widening in the report.
- [x] 5.5 Confirm the surviving change-scoped mentions are deliberate, and name each in the report: the requirement
      header `Per-change quality-gate and analysis subagents are available` (a `MODIFIED` block cannot retitle it); the
      scenario label `A change that obsoletes a rule yields a retirement candidate` (same constraint);
      `.opencode/agent/review-thorough.md:~3` ("a change's implementation against its delta specs" — a thorough review
      is inherently change-scoped); `experience-analyzer.md:~30` ("per-change captures" — the aggregate a retrospective
      reads); `agents-auditor.md:101` and main spec `:246` ("the per-change workflow" — a meta-rule subject class); and
      `AGENTS.md`'s auto-review bullet's "a change's proposal". No edit for any of these; the reason is recorded in
      `design.md`.

## 6. Verification

- [x] 6.1 Run `openspec validate --changes` and confirm every `MODIFIED` block carries every scenario its main-spec
      requirement has, in the main spec's order, with the byte-identical header.
- [x] 6.2 Run `./gradlew spotlessApply` after the final edit to any Spotless-owned file, then confirm `spotlessCheck`
      passes and the PR gate (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) is green.
- [x] 6.3 Smoke-test the two edited definitions after an OpenCode reload, with both controls. The positive control is a
      **non-change unit** (e.g. a data/docs refresh with no change dir) the capture and the review must accept — they
      must engage without requiring a proposal or delta spec, which is the widening's own subject. The negative control
      is the change-scoped work they must still perform: the review's durability challenge on a unit whose diff adds
      `AGENTS.md` rules, and the capture's retirement analysis on a unit that obsoletes a rule. Verify each seed's
      premise against the repository before reading the run and record which branch each run exercised, as
      `2026-09-24-widen-lesson-capture-to-retirements` did. A running session serves the pre-edit definition until
      OpenCode reloads, so a mid-session run is not evidence of the edit; the reload is the check's precondition, not a
      task deferred across the merge.
- [x] 6.4 Run `review-quick` over the implementation diff and fix its findings, re-running until clean; then request the
      user's manual review pass before committing. Confirm the multi-artifact set is complete against the archived
      analogous changes (`make-lesson-capture-consolidate`, `widen-lesson-capture-to-retirements`,
      `name-review-finding-classes`): both definitions' frontmatter `description`s, no new trigger command (the capture
      has none), the README's table row and prose, the `## Purpose` decision (task 3.3), and the proposal's New/Modified
      Capabilities subsections — all addressed above.
