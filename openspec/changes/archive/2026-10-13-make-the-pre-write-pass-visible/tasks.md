# Tasks

## 0. The pre-write derivation pass (this change carries its own record)

- [x] 0.1 Ran the pre-write derivation pass over this change's planning artifacts before the first review round,
      deriving each of the four claim classes `AGENTS.md`'s review-loop bullet names from its source: the artifact set
      was derived from the analogue's (`make-the-capture-visible`) artifact-bearing tasks rather than listed; every
      mention of the four classes was grepped and single-sourced to the bullet; and the config rule's shape was derived
      from `ci.yml`'s probe. The pass also caught that the Context's round figures could not be sourced, which is why
      they were removed rather than restated.

## 1. Inject the pre-write pass into every change's tasks

- [x] 1.1 Added a `tasks` rule to `openspec/config.yaml`, appended to the existing set: every change carries the
      pre-write derivation pass — the four claim classes `AGENTS.md`'s review-loop bullet names — as a visible, tickable
      task, pointing at the bullet rather than restating its list. Written as a single string (no unquoted `: `).
- [x] 1.2 Verified the CLI consumes the rule:
      `openspec instructions tasks --change make-the-pre-write-pass-visible     --json` carries the new rule and the
      unchanged `lesson-capture` rule (the control — a malformed rule drops the whole set).
- [x] 1.3 Ran the CI configuration probe locally, both arms. Clean: `openspec new change openspec-config-probe` output
      no `must be an array of strings` / `could not parse` warning. Known-bad: an injected unquoted `: ` made
      `openspec new change` emit `could not parse … (Implicit keys need to be on a single line)` — the probe's grep
      would fail; the rule was restored and the probe directory removed (verified clean again).

## 2. Add the pull-request checklist record

- [x] 2.1 Added a checklist line to `.github/PULL_REQUEST_TEMPLATE.md`'s "Test plan" recording the pre-write derivation
      pass — the change's `tasks.md` task, or, for a unit with no change dir, by ticking the line.
- [x] 2.2 Read the line back: it names the pass and is a record, not a gate; the template's two required sections
      (Summary, Test plan) remain.

## 3. State the record on the main agent's obligation

- [x] 3.1 In `AGENTS.md`'s review-loop bullet, stated the record beside the pass it describes: every unit carries the
      pass as a task — a task in a change's `tasks.md`, or a line in the pull-request checklist for a unit with no
      change dir — so a missing box is a skip, not a pass that found nothing. The bullet's own enumeration of the spec
      clauses (the `review-quick` pointer) names the pre-write pass record, as each analogous change widened it.

## 4. Capture the behavior in the spec

- [x] 4.1 Wrote the delta at
      `openspec/changes/make-the-pre-write-pass-visible/specs/showcase/quality/agent-skills/spec.md`: one `MODIFIED`
      requirement carrying the main spec's `Per-change quality-gate and analysis subagents are available` block verbatim
      (all eight existing scenarios, in order, byte-identical header), with the record `AND` clause added to the
      quick-review scenario and a new `A skipped pre-write pass is a missing record` scenario.
- [x] 4.2 Confirmed the requirement header matches byte-for-byte, every existing scenario is present in order, and the
      new scenario uses four hashtags. Decision: no `## Purpose` refresh owed — the capability's scope (the per-unit
      review and capture agents) is unchanged.

## 5. Docs the change owns

- [x] 5.1 Updated `README.md`'s self-learning bullet to note the visible record — the unit carries the pass, so a
      skipped one shows as a missing box.
- [x] 5.2 Annotated `docs/retrospectives/2026-10-13.md`'s S1 row as **Applied** (`make-the-pre-write-pass-visible`); the
      table has no Disposition column, so the record is an annotation on the row.

## 6. Confirm the review definition needs no edit (do not assume)

- [x] 6.1 Read `.opencode/agent/review-quick.md` and confirmed no edit is owed: the pass is the main agent's, run before
      the review, and the subagent cannot observe its own skip.

## 7. Verification

- [x] 7.1 Ran the implementation `review-quick` loop: round 1 (= continuous round 12) was clean, 0 findings; the swept
      artifact set matches the analogous archived changes (`make-the-capture-visible`'s union, with the probe sites
      exercised by task 1.3's two arms rather than edited).
- [x] 7.2 Ran the per-unit `lesson-capture`; its verdict is **net 0** — every candidate is covered on `main` (the pass's
      carriage is the statement this unit added to the review-loop bullet; the "evidence scaffolding outran its sources"
      lesson is the pass's own class 1; the config-rule/probe items are the existing probe captures), and it retires
      nothing. Applied net `AGENTS.md` delta: **0 lines**. One docs nit applied: the retrospective's S1 prose paragraph
      now reads **Applied**.
- [x] 7.3 Re-ran `openspec validate --changes` after the last artifact edit: valid, the `MODIFIED` block carrying the
      main spec's eight scenarios plus the new one.
- [x] 7.4 Ran `./gradlew spotlessApply` after the last edit to a Spotless-owned file; `spotlessCheck` passes.
- [x] 7.5 The manual 120-character check over `openspec/config.yaml` and the change dir returns nothing.
- [ ] 7.6 Request the user's manual review pass, once 7.2's applied capture and 7.4's formatter output are what the
      reviewer sees.
