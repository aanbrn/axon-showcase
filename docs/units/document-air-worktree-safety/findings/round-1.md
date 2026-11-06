- [contradiction] `proposal.md` — the Why says a unit owns exactly one sibling tree created by `new-worktree.sh`, while
  the Workspace rule gives the primary checkout to the sequential unit and the sibling only to a new unit that finds it
  occupied.
- [claim-unreproducible] `proposal.md` — "nothing in the repository can see Air's tree" contradicts the guard sentence
  beside it, since the guard resolves the claim in the acting worktree's git directory.
- [over-claim] `design.md` — ADR-0024 is rendered as "the write right is not session-bound", while its Decision
  establishes that the v2 surface offers no session-bound control, leaves v1 unverified, and records the property as not
  ensured.
- [claim-missing] `design.md` — the Context's PR #853 assignment and its "idea note" reference have no claims row or
  dismissal.
- [missing-task] `tasks.md` — D3's constraint that the guidance never claims a global worktree off switch is verified by
  no task.
- [structured-writing] `proposal.md` — the before-start set and the seven-step recovery are each packed into one
  sentence, and the tasks repeat the shapes.
