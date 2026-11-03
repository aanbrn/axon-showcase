# Process defects — discover-paused-units

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### discover-paused-units#1 — the proposal was written before the consultation on the widened scope

- **Recorded** — 2026-11-03, the main agent.
- **Stage** — proposal.
- **Expected** — the owner's widened requirement (discovery across worktrees, with a choice when several units are
  paused) goes to the technical advisor before the change's artifacts are created; a consultation is not an approval to
  start writing them.
- **Observed** — the proposal, design, tasks, and both deltas were written first, and only a later owner message
  ("Shouldn't we have discussed this with the advisor first?") brought the consultation forward.
- **Evidence** — the artifacts existed in the working tree when the follow-up consultation was opened, and the advisor's
  first response to it said the earlier recommendation was not a permission to write them.
- **Consequence** — the artifacts were revised twice around gaps the earlier consultation found: the paused-record
  lifecycle, the no-change-dir scope, the empty-search behavior, and the resolver's contract. No artifact was lost.
- **Knowledge boundary** — observed in this session. The owner's intent behind the widened requirement is read from the
  requirement itself and from the question that followed it; it was not stated separately.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-11-03 entry, which states the spec-amendment
  vehicle for the ordering this record reports.

### discover-paused-units#2 — a scripted replacement reported success while it changed nothing

- **Recorded** — 2026-11-03, the main agent.
- **Stage** — proposal.
- **Expected** — a scripted edit of the claims table, run after a merge moved the base revision, rewrites the rows it
  names and fails loudly when an anchor does not match.
- **Observed** — a `str.replace` sequence without an assertion printed `ok` while three rows kept their old values, so
  the table still recorded five checkpoints where the refreshed base holds six.
- **Evidence** — the round that followed reported the three rows as unreproducible at the new base, and the same command
  run with an assertion then failed on the reflowed text.
- **Consequence** — one extra review round; the rows were re-derived and reproduce.
- **Knowledge boundary** — observed in this session. Whether the same pattern recurs elsewhere in the unit was not
  checked beyond the review rounds' reports.

- **Decision** — no-change: the edit-verification rule already requires the anchor's occurrence count before a scripted
  replacement, since `str.replace` returns its input unchanged.
