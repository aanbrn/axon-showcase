# Process defects — move-process-defect-records

### move-process-defect-records#1 — the delta carried twelve requirements where two were intended

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — proposal and design.
- **Expected** — a `MODIFIED` delta block holds exactly the requirements the change alters, copied verbatim from the
  main spec.
- **Observed** — the delta was assembled by copying a line range from the main spec, and the range spanned ten
  requirements the change does not touch (from the routing requirement to the capture one). The file held twelve
  `### Requirement:` headers instead of two.
- **Evidence** — `rg -c '^### Requirement:'` over the delta printed 12; after the rebuild it prints 2, and the file is
  296 lines rather than 723 (`wc -l` on the delta before and after the rebuild).
- **Consequence** — caught by the technical-advisor consultation before implementation; the delta was rebuilt from the
  two requirements' exact spans and no unrelated requirement was modified.
- **Knowledge boundary** — observed: the wrong count and the rebuild. Not checked: whether an earlier change's delta
  carried the same over-copy, which `openspec validate` would not flag because it accepts unchanged blocks.

- **Decision** — no-change: the programmatic-edit rule already requires verifying at the granularity edited (the
  requirement-header count); the advisor declined a separate delta-bound clause as a restatement.

### move-process-defect-records#2 — a bulk fixture rename left two routing tests exercising a same-path move

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — implementation.
- **Expected** — after the directory is retired, the routing fixtures name a record path that still exists, and a rename
  fixture renames to a different directory.
- **Observed** — a textual replacement of `docs/process-defects/` with `docs/retrospectives/` in the test file turned a
  rename fixture into `git mv X X` and left a `mkdir` of the retired directory, so two tests errored.
- **Evidence** — `test-review-routing.py` reported two errors at the rename and the code-file-into-records tests; both
  pass after the move targets a different directory and the `mkdir` names one that exists.
- **Consequence** — two test runs failed before the fix; no committed state carried it.
- **Knowledge boundary** — observed: the two errors and their repair. Not checked: whether other textual replacements in
  this unit produced a semantically empty fixture that still passes.

- **Decision** — no-change: the programmatic-edit rule already requires scoping a file-wide substring replacement or
  asserting its intended match count.

### move-process-defect-records#3 — the proposal-stage quick review was skipped in every unit of the session

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's question.
- **Stage** — review, at the proposal stage.
- **Expected** — `showcase/quality/agent-skills` requires the bounded quick review after a unit's proposal and after its
  implementation, so the planning artifacts get their own screen before code is written.
- **Observed** — the first three units of this session went from the proposal straight into implementation with one
  quick round, run over the implementation; no proposal-stage invocation was made. The skip was recorded by the reviewer
  in `keep-review-handoff-per-unit` and that unit's proposal round then ran late, as its round 2. The flow was copied
  from the preceding unit each time, and the copy carried no proposal round.
- **Evidence** — `keep-review-handoff-per-unit`'s hand-over reads "Stage: proposal, run after the implementation screen
  because the stage was skipped"; its defect record `#3` states the same for that unit; the rounds files of the first
  three units begin with an implementation screen.
- **Consequence** — three plans reached implementation without their own screen. One unit's late proposal round found
  six issues, including a missing claims table, that an earlier screen would have surfaced before the work.
- **Knowledge boundary** — observed: the ordering in this session's three units and the reviewer's finding. Not checked:
  whether units before this session did the same, whose records do not state a stage for their first round.

- **Decision** — proposed: applied as the `rules.tasks` gate-stage item; the `agent-skills` requirement already owns the
  stage list.

### move-process-defect-records#4 — the plan's review-gate item was copied from the previous plan, not derived from the gate

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's question.
- **Stage** — proposal, while authoring `tasks.md`.
- **Expected** — `AGENTS.md`'s recurring-unit rule: a change's plan derives from the last analogous archived change as a
  floor **and is reconciled with what the rules mandate now**; the gate's stages are `review-gate` §1–§5's, and
  `showcase/quality/agent-skills` requires a quick review after the proposal and after the implementation.
- **Observed** — when the plans were authored, all four carried one aggregate item for the review gate, and the stage
  list was not reconciled: the proposal round appeared in none of them. Each plan's text was carried from the preceding
  unit's plan rather than rebuilt from the rule. This unit's plan was split into two items (4.5 and 4.6) at the review
  round that found the omission.
- **Evidence** — the three archived plans — `openspec/changes/archive/2026-11-03-split-the-review-ledger/tasks.md`,
  `…-persist-review-round-findings/tasks.md`, and `…-keep-review-handoff-per-unit/tasks.md` — each hold a single "review
  gate runs" item, and this unit's live plan now carries the two split items; 68 archived plans name a proposal review
  (`rg -l -i 'review.{0,25}proposal|proposal.{0,25}review' openspec/changes/archive/*/tasks.md | wc -l`), so the step
  exists in the corpus and its absence here is a regression.
- **Consequence** — four plans under-specified the gate. Three units shipped without the proposal round; this unit ran
  it only after the owner asked, and its record `#3` states the same for the earlier three.
- **Knowledge boundary** — observed: the four plans and the corpus count. Not checked: which earlier plan first
  collapsed the item, and whether units outside this session inherit the same shape.

- **Decision** — proposed: applied as the `rules.tasks` gate-stage item; the recurring-unit floor rule already required
  reconciling the copied plan with what the rules mandate now.

### move-process-defect-records#5 — one gate item carried five independently actionable steps

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's question.
- **Stage** — proposal.
- **Expected** — `AGENTS.md`'s Structured Writing Standard: one independently actionable statement per unit, and a
  procedure's steps as numbered or separate items.
- **Observed** — "The review gate runs: quick pass … routing decision … manual pass" is one item carrying five actions
  (proposal round, implementation round, routing, deep review, manual pass), so a missing stage had no missing line to
  show it. The same aggregate phrasing appears in the three archived plans.
- **Evidence** — the quoted item in each of the four `tasks.md` files; the standard's rule text.
- **Consequence** — the omission was invisible to a reader of the plan; it surfaced only when a reviewer compared the
  rounds against the gate's stages, on the third unit.
- **Knowledge boundary** — observed: the item text in the four plans. Not checked: whether other aggregate items in
  those plans hide further omissions.

- **Decision** — proposed: applied as the `rules.tasks` gate-stage item; the writing standard already required one
  actionable statement per item.

### move-process-defect-records#6 — no gate reads whether a plan carries the gate's stages

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's question.
- **Stage** — review, at the proposal boundary.
- **Expected** — a plan missing a stage of the process it will run is reported by something before the unit ships.
- **Observed** — `openspec validate --all` passes, the pre-commit guard passes, and the archive step passes with a plan
  whose only review-gate item names the loop and not the proposal round; the deficiency was reported only by a reviewer
  reading the rounds, and only on the third unit.
- **Evidence** — the three archived plans passed every gate named above at their merges; `openspec validate` checks
  artifact presence and shape, and `commit-hygiene.py`'s checks read records, tasks ticks, and staged sets, none of them
  a plan's stage list.
- **Consequence** — the omission could repeat indefinitely without a gate signal; it is caught only by a reader who
  holds the gate's stage list beside the plan.
- **Knowledge boundary** — observed: the gates' passes over the deficient plans. Not checked: whether a check over a
  plan's stage list is feasible without pinning wording, which this unit's own tasks do not attempt.

- **Decision** — proposed: applied as the `rules.tasks` gate-stage item, an authoring-system correction; no check over a
  plan's stage list is proposed, its feasibility unchecked.

### move-process-defect-records#7 — the pull request was merged before its archive commit

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — ship and merge.
- **Expected** — the archive — the change directory moved, the main spec synced, and the unit record set to `closed` —
  is an additional commit in the same pull request, after the `build` check is green and before the merge.
- **Observed** — PR #834 was merged with its implementation commit alone. The live change directory
  `openspec/changes/move-process-defect-records/` remained on `main` and the unit record read `State: active`. The
  archive was then applied on a follow-up branch and pull request.
- **Evidence** — after the merge, `test -d openspec/changes/move-process-defect-records` succeeded and
  `rg '^State:' docs/units/move-process-defect-records/unit.md` printed `active`; the follow-up branch
  `archive-move-process-defect-records` carries `openspec archive`'s move and the `closed` state.
- **Consequence** — `main` carried a completed change read as in flight until the follow-up merged. Nothing else was
  affected; the implementation commit is unchanged.
- **Knowledge boundary** — observed: the post-merge state and the repair. Not checked: whether the merge sequence (a
  watch and a merge in one command) omitted the archive step by construction or by my ordering.

- **Decision** — no-change: "A change merged without its archive is incomplete" already requires the archive commit in
  the same pull request before the merge.
