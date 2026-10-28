# Process defects — add-flag-process-defect-command

### add-flag-process-defect-command#1 — A formatter run re-broke a fix that had already been verified

- **Recorded** — 2026-10-28, by the writing session (`ses_e7f86f892ffe5UDzeevFdhToUq`), during the review stage.
- **Stage** — review (the proposal round's fix loop, rounds 8 and 9).
- **Expected** — a review finding's fix is verified on the state that ships: after `spotlessApply`, and by reading the
  changed line back, since `AGENTS.md`'s formatting rule places the formatter run after the final write and the
  formatter does not repair an inline-code boundary either way.
- **Observed** — the fix restored the two spaces around an inline command span in `tasks.md`. The verification scan and
  the edit's own report were both taken before `spotlessApply` ran later in the same command, and `spotlessApply`
  re-joined the span to its neighbours, so the defective line survived into round 9 while the hand-over asserted a
  zero-hit scan.
- **Evidence** — round 9's report quotes `tasks.md:27`'s defective text as ``README.md`returns`1``, a state that
  preceded the second fix, in the working tree and in
  `git show :openspec/changes/add-flag-process-defect-command/tasks.md`; the quote survives in that report rather than
  in the file, which now carries the corrected sentence. The scan ``grep -rn -E '[A-Za-z0-9]`[A-Za-z0-9]'`` over the
  change directory returned that hit before the second fix and nothing after it, and the line re-read after
  `spotlessApply` shows the spaces.
- **Consequence** — one extra review round spent on a defect the previous round had recorded as fixed; the interim
  verdict was wrong about the artifact, not about the intent. The line was restructured so that no code span abuts
  another, and re-verified after the formatter.
- **Knowledge boundary** — observed for this unit's `tasks.md`. Not checked whether other artifacts of this unit carry
  an inline-code boundary the formatter joins; the tightened scan covers a backtick with an alphanumeric on both sides,
  not every spacing defect a reflow can produce.

### add-flag-process-defect-command#2 — The probe's "uncommitted" assertion was vacuously true

- **Recorded** — 2026-10-28, by the writing session (`ses_e7f86f892ffe5UDzeevFdhToUq`), during implementation (task
  3.2).
- **Stage** — implementation (the seed run's fixture).
- **Expected** — an assertion that the written record is uncommitted separates "the command wrote it and left it in the
  tree" from "something committed it", so the fixture it runs against must have the record's file tracked before the
  append.
- **Observed** — the first fixture created `docs/process-defects/probe-merged-unit.md` inside the probe copy without
  committing it, so the file was untracked and `git status` reported the same `??` before and after the append; the
  "uncommitted" check could not fail.
- **Evidence** — the first run's `git -C <copy> status --short` printed `?? docs/process-defects/probe-merged-unit.md`
  before the append and the identical line after it; the fixture was then committed with record #1 alone and the append
  re-run, after which the status printed ` M docs/process-defects/probe-merged-unit.md`.
- **Consequence** — one rework of the probe fixture within the session. The assertion now separates the states, because
  the copy's status shows a modification rather than an untracked file.
- **Knowledge boundary** — observed for this probe's fixture. Not checked whether other verification tasks in this
  change assert something that is vacuous in their fixture's initial state.

### add-flag-process-defect-command#3 — The shipped lifecycle prohibited the write its own rule requires

- **Recorded** — 2026-10-28, by the writing session (`ses_e7f86f892ffe5UDzeevFdhToUq`), during the review stage.
- **Stage** — review (between quick rounds 21 and 22, on an independent consultation's finding).
- **Expected** — a command that directs an append into the documentation unit's tree for a merged or archived episode
  carries no prohibition forbidding that write; the rule and its exception have to be readable together, without the
  agent inferring an unstated exception.
- **Observed** — quick round 19 asked for the prohibition to be unambiguous, and the rewrite made it "never write into a
  working tree other than the episode unit's". A merged or archived episode has no tree, so the prohibition contradicted
  the preceding bullet, which requires exactly that documentation-unit append; round 21's clean pass accepted the text.
- **Evidence** — the pre-correction bullet as round 19's report quotes it and task 3.7 records it, against the design's
  placement decision and the delta's "the file of the unit the episode belongs to"; the correction now reads "Write only
  into the tree assigned to the episode's unit, or — when that unit has none — into the documentation unit's own tree
  carrying the append".
- **Consequence** — corrected before the deep review's pass; recorded on task 3.7 at the time rather than here, and
  entered here when round 22 reported the omission.
- **Knowledge boundary** — observed for this command's lifecycle text. Not checked whether other project commands pair a
  prohibition with a permission the same way.
