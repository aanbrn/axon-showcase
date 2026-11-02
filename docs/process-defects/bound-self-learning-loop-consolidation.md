# Process defects — bound-self-learning-loop-consolidation

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### bound-self-learning-loop-consolidation#1 — A second unit was started before its shape was agreed

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — proposal.
- **Expected** — for a governance unit whose scope has already been corrected twice by the owner, settle the shape with
  the advisor before any repository edit, as the preceding units of this kind did.
- **Observed** — after the owner named the self-learning loop as the subject, the agent renamed the branch and edited
  the declaration, wrote a new ADR and two amendments, and drafted a change directory before consulting. The advisor
  then reversed the evidence role, the adoption claims, and the pause amendment.
- **Evidence** — two owner corrections in sequence: first that the subject is the self-learning loop rather than lesson
  capture, then "Надо было это еще пообсуждать с советником"; the advisor's report naming two confirmed problems (the
  evidence role crossing the destination exception; the declaration recording adoption ahead of the consumers); the
  reverts that followed.
- **Consequence** — several edits were discarded and the unit re-scoped to a decision-only record. No work shipped and
  nothing was lost, but one measurement-backed candidate was mixed into a unit that never adopted it.
- **Knowledge boundary** — observed: the edit sequence and the reverts. Assumed: that the advisor would have caught both
  problems before the edits, which the later report supports but does not prove. Not checked: whether the same
  edit-before-agreeing happened in earlier units.
