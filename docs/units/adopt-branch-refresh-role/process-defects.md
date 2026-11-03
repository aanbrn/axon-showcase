# Process defects — adopt-branch-refresh-role

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### adopt-branch-refresh-role#1 — A role's value was stated as a heading the repository does not have

- **Recorded** — 2026-11-03, by the unit's agent.
- **Stage** — implementation.
- **Expected** — a role's value names a location this repository actually has, in the form it has it: a heading is a
  heading, a bold bullet is a bullet with its enclosing section.
- **Observed** — the declaration, the ADR amendment, the design and the draft PR body all valued `branch-refresh` as the
  "Refreshing a branch" heading. `AGENTS.md` states it as `- **Refreshing a branch:**`, a bullet under "Workflow: Change
  lifecycle" → "Failure handling"; no such heading exists. The same round found that the entry-pointer/applied-rule
  split had been drawn from a first reading of the consumers rather than from their uses.
- **Evidence** — the quick review's round-1 report (`claim-unreproducible` on the value, `coherence` on the split); the
  corrected rows in `docs/contracts/process-bindings.md` and `docs/adr/0028-…`; the inventory's two reclassified rows.
- **Consequence** — one review round and a rewrite of the design's Context and D1. Nothing shipped. A target that copied
  the first value would look for a heading it could not find.
- **Knowledge boundary** — observed: the four sites and their correction. Assumed: that the fixture's heading-shaped
  statement of the rule is why the error read as plausible. Not checked: whether the other declared roles' values all
  name existing anchors in the form they have.

- **Decision** — no-change: `docs/contracts/process-bindings.md` already requires reading the referenced owner before
  relying on its contract, and the value named a bullet `AGENTS.md` states under its own heading — an
  instruction-following failure against that rule, corrected in the declaration.
