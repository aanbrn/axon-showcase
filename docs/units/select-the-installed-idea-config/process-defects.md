# Process defects — select-the-installed-idea-config

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### select-the-installed-idea-config#1 — A findings line carried two locations, so the instances check failed

- **Recorded** — 2026-11-08, the agent.
- **Stage** — proposal review.
- **Expected** — a round's instance line is `- [<class>] <location> — <claim>`, one location token.
- **Observed** — the round-1 entry for the structural defect was written with two paths in the location
  (`tasks.md:17-22, design.md:28-32`), which the check reads as one token and rejects.
- **Evidence** — `--finding-instances` reported the line as outside the instance shape, quoting it.
- **Consequence** — the check failed once; the entry was rewritten with a single location and the second path moved into
  the claim.
- **Knowledge boundary** — observed the check's rejection and the fix. Did not check whether other units' instances
  files carry the same shape; the check would report them on their own rounds.

### select-the-installed-idea-config#2 — A findings file was overwritten while its row was appended

- **Recorded** — 2026-11-08, the agent.
- **Stage** — proposal review.
- **Expected** — the advisor's boundary correction is recorded in the hand-over and the quick round that follows it gets
  its own findings file and row.
- **Observed** — the decision's finding was written into `findings/round-7.md`, which held the clean round-7 `- none`,
  and a second round-7 row was appended, so the clean round lost its record and the ledger carried two rows for round 7.
- **Evidence** — `rounds.tsv` listed round 7 twice, and `findings/round-7.md` held a finding rather than `- none`.
- **Consequence** — the clean round's record was restored and the duplicate row removed in the same step; no check had
  read the wrong state.
- **Knowledge boundary** — observed the overwrite and the duplicate row while reviewing the step's output. Did not check
  whether the same pattern occurred in an earlier round of this unit.
