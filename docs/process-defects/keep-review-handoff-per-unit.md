# Process defects — keep-review-handoff-per-unit

### keep-review-handoff-per-unit#1 — the proposal's new files were left unstaged until the owner asked

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's question in the turn.
- **Stage** — proposal.
- **Expected** — `AGENTS.md`'s change lifecycle, step 2: the unit's proposal stays uncommitted and its new files are
  staged, so the owner's review pass shows them in the Changes panel of the unit's tree.
- **Observed** — the proposal, its delta, its tasks, and the unit record were written and left untracked; the index was
  empty. The owner asked why the new files were not in the index, and they were staged after that.
- **Evidence** — the owner's question; before the fix `git status --porcelain --untracked-files=all` listed the five
  paths as untracked and `git diff --cached --stat` was empty, while after it the same command reports five files and
  `--review-ready` passes.
- **Consequence** — a review pass at that moment would have shown no changes. Corrected in the same turn; nothing was
  committed or lost.
- **Knowledge boundary** — observed: this unit's staging state before and after the correction, and that the two units
  before it in this session were staged only at the manual-pass boundary, so their proposal stages were unstaged too.
  Not checked: whether the same omission occurred in units from earlier sessions.

### keep-review-handoff-per-unit#2 — two edits anchored on text the formatter had already changed

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — implementation.
- **Expected** — an edit's anchor matches the file's current text, taken from a read that follows the last formatter
  run.
- **Observed** — two edits used anchors from a read made before `spotlessApply` reflowed the files: the `docs/ideas.md`
  entry and the `docs/contracts/process-bindings.md` table row. Both anchors matched nothing, and the edits no-opped;
  re-reading each file and re-anchoring applied them.
- **Evidence** — the two failed edits reported their old text as not found, naming the closest current line; the
  re-reads showed the reflowed text, and the re-anchored edits applied.
- **Consequence** — two extra tool calls. The failures no-opped rather than misapplying, so no file carried a wrong
  edit.
- **Knowledge boundary** — observed: the two failures and their recovery. Not checked: whether other pending anchors in
  this session predate a formatter run.

### keep-review-handoff-per-unit#3 — the proposal stage ran no quick review

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), during the review gate.
- **Stage** — proposal.
- **Expected** — `showcase/quality/agent-skills` requires the bounded quick review after a unit's proposal and after its
  implementation, so the planning artifacts get their own screen before the code is written.
- **Observed** — the proposal stage produced no review. The implementation screen ran first and was recorded as round 1;
  the proposal round was then run late and recorded as round 2, and the unit's hand-over says so.
- **Evidence** — `docs/units/keep-review-handoff-per-unit/rounds.tsv` row 1 belongs to the implementation screen; the
  hand-over's round-2 section reads "Stage: proposal, run after the implementation screen because the stage was
  skipped".
- **Consequence** — the plan reached implementation unscreened. The late round found seven issues, among them the
  missing claims table and an unticked pre-write task, which a proposal screen would have seen before the work; the
  instances are `docs/units/keep-review-handoff-per-unit/findings/round-2.md`.
- **Knowledge boundary** — observed: that no proposal-stage invocation preceded the implementation, and the late round's
  findings. Not checked: whether the same findings would have arrived at the proposal stage.

### keep-review-handoff-per-unit#4 — three rounds were recorded after their invocation instead of before it

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the round-3 report.
- **Stage** — review.
- **Expected** — the preflight requires the invocation's hand-over recorded **before** the caller waits on it, because a
  stalled invocation cannot be cancelled and reports nothing; the repository's rule predates this change, which gives it
  a per-unit path.
- **Observed** — rounds 1, 2, and 3 were each recorded after their screen returned, and each section says so in a "Late
  record" line. Round 1's note also promised that later rounds would record theirs before waiting.
- **Evidence** — the hand-over's three sections and their late-record notes; the rounds' order in `rounds.tsv` against
  the sections' written order.
- **Consequence** — a stall in any of those three rounds would have had no record to recover from. No stall occurred,
  and the practice changed from round 4: the section is written before the invocation is made.
- **Knowledge boundary** — observed: the record timing and the notes. Not checked: whether the same lateness occurred in
  units before this one, whose hand-overs were written in change directories.

### keep-review-handoff-per-unit#5 — the clean closing round's instances file was never written

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), when the advisor's consultation read
  the store.
- **Expected** — every recorded round carries its instances file, and a round that raised nothing carries `- none`; the
  delta and the review gate both state it.
- **Observed** — round 6's row was appended and no `findings/round-6.md` was written; the store held five files for six
  rounds until the consultation read the directory and reported the gap.
- **Evidence** — `ls docs/units/keep-review-handoff-per-unit/findings/` listed rounds 1–5 after round 6's row; the file
  exists now with `- none`.
- **Consequence** — the unit's store undercounted one round. `verifyFindingInstances` did not flag it, because this
  unit's rounds are dated on the store's boundary and are exempt.
- **Knowledge boundary** — observed: the missing file and its repair. Not checked: whether the preceding change's clean
  rounds lacked files too, whose rounds were boundary-dated for the same reason.
