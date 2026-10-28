# Process defects — run-pr-refresh-on-explicit-invocation

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### run-pr-refresh-on-explicit-invocation#1 — A replacement was declared applied without re-reading the artifact

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal round.
- **Stage** — review (rounds 3 and 4 of the proposal loop).
- **Expected** — a fix is verified against the finding's own claim before the hand-over reports it fixed: a scripted
  replacement asserts its anchor and the changed line is re-read, since an unmatched `str.replace` returns its input
  unchanged.
- **Observed** — round 3's fix to the claims table's row 12 used an unasserted replacement, which did not match the
  formatted table row; the round-4 hand-over, and the round-3 record's own text, declared the relabel applied. The
  reviewer re-read the row and reported it unfixed.
- **Evidence** — `design.md` row 12 still read "the docs-refresh convention" after round 4's report; the round-4
  hand-over's item 1 said it now names the "A recurring unit type" bullet; the reviewer's round-4 finding 1 quotes the
  unchanged row.
- **Consequence** — one extra review round for a defect the hand-over had declared fixed. Repaired with an asserted
  replacement and a re-read of the formatted row. Nothing shipped with the stale label.
- **Knowledge boundary** — observed for this change's design table. Did not check whether other unasserted replacements
  in this change no-opped silently; every replacement after round 4 asserts its anchor.

### run-pr-refresh-on-explicit-invocation#2 — A spec delta was designed on a premise the requirement's own antecedent contradicts

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal round.
- **Stage** — proposal (the delta and the design's D5).
- **Expected** — before designing a spec delta, read the requirement it modifies for its subject: a requirement's
  obligation is stated by its antecedent, and a command outside that class is not bound by it.
- **Observed** — the delta added an exception to the mutating-unit lifecycle requirement on the premise that the
  requirement governs `/sync-branch`'s lifecycle text. The requirement binds "a project-authored command that starts a
  mutating unit that ships", and `/sync-branch` refreshes an existing branch and starts none; the archived design's D3
  clause was read as coverage. The reviewer's finding exposed the antecedent, the advisor withdrew the premise, and the
  delta was removed.
- **Evidence** — `openspec/specs/showcase/quality/agent-skills/spec.md:1331` (the antecedent);
  `git grep -n "sync-branch" 5a2895b5 -- openspec/specs/` returns no hits; the archived
  `2026-10-27-add-sync-branch-command/design.md` D3; the reviewer's round-2 finding 2 and round-3 finding 1.
- **Consequence** — the delta artifact, its task group, and the proposal's capability entry were written and then
  removed; the change now carries `skip_specs: true`. The reduced scope is the owner's decision at the manual pass,
  because the earlier plan was approved with the delta.
- **Knowledge boundary** — observed for this requirement. Did not check whether other changes' deltas rest on a
  requirement whose antecedent their subject does not meet.

### run-pr-refresh-on-explicit-invocation#3 — A correction was swept only at the sites the finding named

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal round.
- **Stage** — review (rounds 4 and 5 of the proposal loop).
- **Expected** — a corrected statement is swept across the artifacts that carry it, not only the line a finding names,
  since a site list restated in several sections is one statement with several copies.
- **Observed** — the boundary's site list ("decision point, lifecycle section, and completion criteria") was corrected
  in Goals and D4 after round 4's finding, while the Risks section kept the third site; round 5 reported the residue.
- **Evidence** — `design.md`'s Goals and D4 named two sites after round 4; its Risks section still named three; round
  5's finding 1 quotes the Risks sentence.
- **Consequence** — one extra review round. Repaired by correcting the Risks copy and sweeping the statement's other
  occurrences; the surviving "completion criteria" occurrence is the report fields, not the boundary.
- **Knowledge boundary** — observed for this statement. Did not check whether other corrected statements in the change
  have a third copy; the change's artifacts were grepped for this one's tokens.
