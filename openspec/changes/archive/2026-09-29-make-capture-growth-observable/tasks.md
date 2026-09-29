# Tasks

## 1. Make the capture state its delta

- [x] 1.1 Update `.opencode/agent/lesson-capture.md`: the report contract's verdict line gains the proposed net
      `AGENTS.md` delta in lines (additions minus retirements), and the growth paragraph states that the applying agent
      records the applied net delta after `spotlessApply`. Check the definition's frontmatter `description`, the README
      agent table's `lesson-capture` row, and the README's Lesson-capture bullet, and record which (if any) needs an
      edit — the subagent's purpose and inputs are unchanged, so likely none. Verify by reading the report contract, the
      growth paragraph, the frontmatter, and the README sites. Done: none needs an edit — the README summarizes the
      capture and does not enumerate its verdict fields, and the frontmatter `description` describes the subagent's
      purpose and inputs, which are unchanged.
- [x] 1.2 Update `AGENTS.md`'s capture bullet growth sentence: the applying agent records the applied net delta on the
      capture record (the change's `tasks.md` task, or the pull-request checklist line), so the merge-time read sees it.
      Verify by reading the bullet.
- [x] 1.3 Update `.github/PULL_REQUEST_TEMPLATE.md`'s capture checklist line so a unit with no change dir can record the
      applied delta beside it (the line currently records only that the capture ran). Verify by reading the line.
- [x] 1.4 Update `openspec/config.yaml`'s injected `tasks` rule to state that the capture task records the applied
      delta. Verify with `openspec instructions tasks --change make-capture-growth-observable --json` (the rule is
      consumed).

## 2. Capture the behavior in the spec

- [x] 2.1 Write the delta at
      `openspec/changes/make-capture-growth-observable/specs/showcase/quality/agent-skills/spec.md`: one `MODIFIED`
      requirement carrying the main spec's `Per-change quality-gate and analysis subagents are available` block verbatim
      (all seven existing scenarios, in order, byte-identical header), with the proposed-and-recorded-delta clause added
      to the capture scenario. Verify with `openspec validate --changes`.

## 3. Docs the change owns

- [x] 3.1 Update `docs/retrospectives/2026-09-28.md`'s S4 disposition cell to name this change, as the S1 and S2 rows
      do, leaving the suggestion's own paragraph as the window's record (a dated retrospective is a record, not a live
      claim). Verify `README.md`'s self-learning bullet and the `agent-skills` capability's `## Purpose` need no edit
      for this change and record the decision (the README sites the definition touches are checked in 1.1); also check
      `docs/ideas.md` and `docs/adr/`. Done: the README self-learning bullet and the `agent-skills` `## Purpose` need no
      edit; `docs/adr/` has no related entry, and `docs/ideas.md`'s related parked entry (the audit-count trend) stays
      valid — it concerns trending the audit reports' counts, distinct from the per-capture delta this change records.

## 4. Verification

- [x] 4.1 Run the implementation `review-quick` loop over the diff against this change's planning artifacts; fix its
      findings and re-run until it reports nothing new.
- [x] 4.2 Run the per-unit `lesson-capture` over this change and apply its durable proposals. Done: the capture returned
      `nothing durable` (net 0), 0 durable proposals, so nothing was applied; its run carried the edited verdict line's
      `nothing durable` (net 0) branch.
- [x] 4.3 Smoke-test the edited `lesson-capture` definition's branches: an addition (states the proposed net delta), a
      retirement (states a negative net delta, exercising the subtraction), and `nothing durable` (states
      `nothing     durable` (net 0)). The `nothing durable` branch was exercised by 4.2's run. The addition and
      retirement branches were not — this unit yielded no durable proposals and a synthetic seed is not grounded against
      the repository — so they remain the unit's residual, named in the report; a run echoing the edited text is not
      evidence of definition pickup (the registration gotcha says a self-echo proves nothing), so no reload claim is
      made here.
- [x] 4.4 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file and confirm `spotlessCheck` passes.
      Done: the applied net `AGENTS.md` delta is +1 line (11 insertions − 10 deletions against `origin/main`, the
      deletions being reflow), recorded here; the manual 120-character check over every changed file a formatter does
      not own is clean.
- [x] 4.5 Request the user's manual review pass — the step before committing.
