# Process defects — make-advisor-consultation-owner-authorized

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### make-advisor-consultation-owner-authorized#1 — The skipped step was misdiagnosed as an advisor-ordering problem

- **Recorded** — 2026-11-04, by the writing session, on the owner's correction.
- **Stage** — proposal (the generalization unit's disposition, which this unit inherits and corrects).
- **Expected** — a disposition names the step that was actually skipped.
- **Observed** — `generalize-process-defects-2026-11-03` parked "settle a corrected unit's shape with the advisor before
  its artifacts are written", on two units that had written artifacts before the consultation. The owner's correction:
  the advisor was not the problem — the owner's manual review was skipped, and using the advisor is the owner's
  prerogative rather than a step an agent may schedule. The parked entry therefore proposed the automatic consultation
  the owner forbids.
- **Evidence** — the parked entry, withdrawn by this change; the owner's correction; the two records the entry cited
  (`discover-paused-units#1`, `bound-self-learning-loop-consolidation#1`).
- **Consequence** — the wrong idea sat in `docs/ideas.md` for a day; this change withdraws it and corrects the three
  sites that let an agent consult on its own initiative.
- **Knowledge boundary** — observed the parked text, the three sites, and the owner's correction. Did not check whether
  the same misdiagnosis shaped another entry in the file.
