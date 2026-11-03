# Process defects — generalize-process-defects-2026-10-28

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### generalize-process-defects-2026-10-28#1 — A recorded design decision was proposed for reversal without the design being read

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal stage.
- **Stage** — proposal (the P1 disposition, before the independent consultation).
- **Expected** — "Interrogate the premise" requires the archived changes to be read for an existing decision about the
  mechanism a design would move: a recorded decision is the design to adopt or to reverse deliberately, and a change's
  own design states which it is.
- **Observed** — the pass presented `.opencode/commands/sync-branch.md`'s step 3 and its decision point as an accidental
  contradiction, and proposed making step 3 run the refresh. The archived design records the hand-off as a deliberate
  addition beside the authorization, so the proposal reversed a decision instead of repairing wording. The independent
  consultation found the design; the analysis had not read it, and no claims-table row covered it.
- **Evidence** — `openspec/changes/archive/2026-10-27-add-sync-branch-command/design.md` D2 says the command "adds only
  what the rule does not say: that an explicit invocation authorizes the refresh, that a call from a diagnosis is
  read-only, **that an open pull request is handed to the owner**, and what to report", and D3 records
  `skip_specs: true` for the same behaviour.
- **Consequence** — the proposal was reclassified from a wording repair to a deliberate behaviour change (direction A),
  routed to a change of its own with a `MODIFIED` delta on the `agent-skills` requirement, and the owner approved that
  route. No artifact shipped on the wrong classification.
- **Knowledge boundary** — observed for P1. Not checked whether another proposal in this pass rests on an unread
  archived decision; only P1's premise was re-read against the archive.

- **Decision** — no-change: 'Interrogate the premise' already requires reading the archived design a proposal would
  reverse.

### generalize-process-defects-2026-10-28#2 — A merged record's asserted timeline was relied on and was false

- **Recorded** — 2026-10-28, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the proposal stage (the
  P5 discussion).
- **Stage** — proposal (the P5 disposition).
- **Expected** — a process-defect record is cited as evidence for the fact it asserts; where an analysis rests a
  proposal's materiality on a record's `Consequence`, that fact is verified against a source rather than repeated from
  the record's sentence.
- **Observed** — `retry-chart-dependency-fetch#2` states "The check that found it was the owner's, run after the
  manual-pass request", and the pass repeated that cost when it weighed P5. The owner doubted the timing; the local
  OpenCode session store places the check inside the review loop, and round 1's report did not name the omission.
- **Evidence** — the record's `Consequence` sentence, against the session store's timestamps: round 1's invocation at
  `2026-10-27T23:38:00Z`, its report at `23:44:31Z` (nine findings, none of them the omission), the owner's request at
  `23:45:25Z`, `help --task` at `23:45:25Z`, the description added by `23:46:05Z`, round 2's invocation at `23:48:45Z`,
  and the manual-pass request at `2026-10-28T00:05:37Z`.
- **Consequence** — P5's materiality was understated in the first presentation until the owner raised it. The record's
  heading, `Stage`, `Evidence`, `Consequence` and `Knowledge boundary` were corrected in this unit beside its `Decision`
  line, and the P5 outcome stayed `no-change` on the occurrence count, not on the corrected cost. Nothing else rested on
  the false sentence.
- **Knowledge boundary** — observed for this record. Not checked whether other merged records' asserted timelines are
  wrong; the pass took record text and PR bodies as evidence elsewhere without a session-store measurement.

- **Decision** — no-change: one occurrence, corrected in its own unit; below the promotion gate.
